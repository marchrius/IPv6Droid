/*
 * Copyright (c) 2013 Dr. Andreas Feldner.
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Contact information and current version at http://www.flying-snail.de/IPv6Droid
 */

package de.flyingsnail.ipv6droid.android;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.net.ssl.SSLContext;

import de.flyingsnail.ipv6droid.R;
import de.flyingsnail.ipv6droid.ayiya.Ayiya;
import de.flyingsnail.ipv6droid.ayiya.ConnectionFailedException;
import de.flyingsnail.ipv6droid.ayiya.Tic;
import de.flyingsnail.ipv6droid.ayiya.TicConfiguration;
import de.flyingsnail.ipv6droid.ayiya.TicTunnel;
import de.flyingsnail.ipv6droid.ayiya.TunnelNotAcceptedException;

/**
 * This class does the actual work, i.e. logs in to TIC, reads available tunnels and starts
 * a copy thread for each direction.
 */
class VpnThread extends Thread {
    /**
     * The Action name for a status broadcast intent.
     */
    public static final String BC_STATUS = AyiyaVpnService.class.getName() + ".STATUS";

    /**
     * The extended data name for the status in a status broadcast intent.
     */
    public static final String EDATA_STATUS_REPORT = AyiyaVpnService.class.getName() + ".STATUS_REPORT";

    /**
     * The tag for logging.
     */
    private static final String TAG = VpnThread.class.getName();

    /**
     * Time that we must wait before contacting TIC again. This applies to cached tunnels even!
     */
    private static final int TIC_RECHECK_BLOCKED_MILLISECONDS = 5 * 60 * 1000; // 5 minutes

    /**
     * The service that created this thread.
     */
    private AyiyaVpnService ayiyaVpnService;
    /**
     * The configuration for the tic protocol.
     */
    private TicConfiguration ticConfig;
    /**
     * The configuration of the intended routing.
     */
    private RoutingConfiguration routingConfiguration;
    /**
     * Android thing to post stuff to the GUI thread.
     */
    private Handler handler;
    /**
     * The file descriptor representing the local tun socket.
     */
    private ParcelFileDescriptor vpnFD;

    /**
     * The thread that copies from PoP to local.
     */
    private Thread inThread = null;

    /**
     * The thread that copies from local to PoP.
     */
    private Thread outThread = null;

    /**
     * The cached TicTunnel containing the previosly working configuration.
     */
    private TicTunnel tunnelSpecification;

    /**
     * The specific SSLContext, required as SixXS choose a cert provider not shipped with
     * Android trust stores.
     */
    private SSLContext sslContext;

    /**
     * An instance of StatusReport that continously gets updated during the lifecycle of this
     * VpnThread.
     */
    private VpnStatusReport vpnStatus;

    /**
     * The constructor setting all required fields.
     * @param ayiyaVpnService the Service that created this thread
     * @param cachedTunnel
     * @param config the tic configuration
     * @param routingConfiguration the routing configuration
     * @param sessionName the name of this thread
     * @param sslContext
     */
    VpnThread(AyiyaVpnService ayiyaVpnService,
              TicTunnel cachedTunnel,
              TicConfiguration config,
              RoutingConfiguration routingConfiguration,
              String sessionName,
              SSLContext sslContext) {
        setName(sessionName);
        this.vpnStatus = new VpnStatusReport();
        this.ayiyaVpnService = ayiyaVpnService;
        this.ticConfig = (TicConfiguration)config.clone();
        this.routingConfiguration = (RoutingConfiguration)routingConfiguration.clone();
        this.tunnelSpecification = cachedTunnel;
        this.sslContext = sslContext;
    };


    @Override
    public void run() {
        try {
            handler = new Handler(ayiyaVpnService.getApplicationContext().getMainLooper());

            if (tunnelSpecification == null) {
                readTunnelFromTIC();
            } else {
                Log.i(TAG, "Using cached TicTunnel instead of contacting TIC");
            }
            vpnStatus.setActiveTunnel(tunnelSpecification);
            vpnStatus.setProgressPerCent(25);
            reportStatus();

            // build vpn device on local machine
            VpnService.Builder builder = ayiyaVpnService.createBuilder();
            configureBuilderFromTunnelSpecification(builder, tunnelSpecification);

            // Perpare the tunnel to PoP
            Ayiya ayiya = new Ayiya (tunnelSpecification);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // setup local tun and routing
                    vpnFD = builder.establish();
                    vpnStatus.setActivity("Configured local network");
                    vpnStatus.setProgressPerCent(50);

                    // Packets to be sent are queued in this input stream.
                    FileInputStream localIn = new FileInputStream(vpnFD.getFileDescriptor());

                    // Packets received need to be written to this output stream.
                    FileOutputStream localOut = new FileOutputStream(vpnFD.getFileDescriptor());

                    // setup tunnel to PoP
                    ayiya.connect();
                    vpnStatus.setProgressPerCent(75);
                    vpnStatus.setActivity("Pinging PoP");
                    reportStatus();

                    // Initialize the input and output streams from the ayiya socket
                    DatagramSocket popSocket = ayiya.getSocket();
                    ayiyaVpnService.protect(popSocket);
                    InputStream popIn = ayiya.getInputStream();
                    OutputStream popOut = ayiya.getOutputStream();

                    // start the copying threads
                    inThread = startStreamCopy (localIn, popOut);
                    inThread.setName("AYIYA from local to POP");
                    outThread = startStreamCopy (popIn, localOut);
                    outThread.setName("AYIYA from POP to local");

                    // now do a ping on IPv6 level. This should involve receiving one packet
                    if (tunnelSpecification.getIpv6Pop().isReachable(10000)) {
                        postToast(ayiyaVpnService.getApplicationContext(), R.id.vpnservice_tunnel_up, Toast.LENGTH_SHORT);
                        /* by laws of logic, a successful ping on IPv6 *must* already have set the flag
                           validPacketReceived in the Ayiya instance.
                         */
                    } else {
                        Log.e(TAG, "Warning: couldn't ping pop via ipv6!");
                    };

                    vpnStatus.setActivity("Transmitting");

                    // wait until interrupted
                    try {
                        while (!Thread.currentThread().isInterrupted() && inThread.isAlive() && outThread.isAlive()) {
                            if (ayiya.isValidPacketReceived()) {
                                // major status update, just once per session
                                vpnStatus.setTunnelProvedWorking(true);
                                vpnStatus.setStatus(VpnStatusReport.Status.Connected);
                                vpnStatus.setProgressPerCent(100);
                            }

                            reportStatus();

                            // wait for half the heartbeat interval or until inThread dies.
                            // Note: the inThread is reading from the network socket to the POP
                            // in case of network changes, this socket breaks immediately, so
                            // inThread crashes on external network changes even if no transfer
                            // is active.
                            inThread.join(tunnelSpecification.getHeartbeatInterval()*1000/2);
                            if (inThread.isAlive())
                                ayiya.beat();

                            // See if we're receiving packets
                            if (!ayiya.isValidPacketReceived() ||
                                    checkExpiry (ayiya.getLastPacketReceivedTime(), tunnelSpecification.getHeartbeatInterval())) {
                                Log.i(TAG, "Our tunnel is having trouble - we didn't receive packets since "
                                        + ayiya.getLastPacketReceivedTime()
                                );
                                if (new Date().getTime() - tunnelSpecification.getCreationDate().getTime()
                                        > TIC_RECHECK_BLOCKED_MILLISECONDS) {
                                    if (readTunnelFromTIC()) {
                                        // TIC had new data - signal an IO problem to rebuild tunnel
                                        throw new IOException("Packet receiving had timeout and TIC information changed");
                                    } else {
                                        throw new ConnectionFailedException("This TIC tunnel doesn't receive data", null);
                                    }
                                }
                            }
                            Log.i(TAG, "Sent heartbeat.");
                        }
                    } catch (InterruptedException ie) {
                        Log.i(TAG, "Tunnel thread received interrupt, closing tunnel");
                        throw ie;
                    }
                } catch (IOException e) {
                    Log.i (TAG, "Tunnel connection broke down, closing and reconnecting ayiya", e);
                    vpnStatus.setProgressPerCent(50);
                    vpnStatus.setStatus(VpnStatusReport.Status.Disturbed);
                    vpnStatus.setActivity("Reconnecting");
                    reportStatus();
                    Thread.sleep(5000l); // @todo we should check with ConnectivityManager
                } finally {
                    if (inThread != null && inThread.isAlive())
                        inThread.interrupt();
                    if (outThread != null && outThread.isAlive())
                        outThread.interrupt();
                    ayiya.close();
                    try {
                        vpnFD.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Cannot close local socket", e);
                    }
                    postToast(ayiyaVpnService.getApplicationContext(), R.id.vpnservice_tunnel_down, Toast.LENGTH_SHORT);
                }
            }
            // important status change
            vpnStatus.setProgressPerCent(0);
            vpnStatus.setStatus(VpnStatusReport.Status.Idle);
            vpnStatus.setActivity("Tearing down");
            reportStatus();
        } catch (ConnectionFailedException e) {
            Log.e(TAG, "This configuration will not work on this device", e);
            // @todo inform the human user
            postToast(ayiyaVpnService.getApplicationContext(), R.id.vpnservice_invalid_configuration, Toast.LENGTH_LONG);
        } catch (InterruptedException e) {
            // controlled behaviour, no logging or treatment required
        } catch (Throwable t) {
            Log.e(TAG, "Failed to run tunnel", t);
            // if this thread fails, the service per se is out of order
            postToast(ayiyaVpnService.getApplicationContext(), R.id.vpnservice_unexpected_problem, Toast.LENGTH_LONG);
        }
        // if this thread fails, the service per se is out of order
        ayiyaVpnService.stopSelf();
        vpnStatus = new VpnStatusReport(); // back at zero
        reportStatus();
    }

    /**
     * Read tunnel information via the TIC protocol. Return true if anything changed on the current
     * tunnel.
     * @return true if something changed
     * @throws ConnectionFailedException if some permanent problem exists with TIC and the current config
     * @throws IOException if some (hopefully transient) technical problem came up.
     */
    private boolean readTunnelFromTIC() throws ConnectionFailedException, IOException {
        boolean tunnelChanged = false;

        // Initialize new Tic object
        Tic tic = new Tic (ticConfig);
        try {
            // some status reporting...
            vpnStatus.setActivity("Query TIC");
            vpnStatus.setStatus(VpnStatusReport.Status.Connecting);
            reportStatus();

            tic.connect(sslContext);
            List<String> tunnelIds = tic.listTunnels();
            TicTunnel newTunnelSpecification = selectFirstSuitable(tunnelIds, tic);
            tunnelChanged = !newTunnelSpecification.equals(tunnelSpecification);
            tunnelSpecification = newTunnelSpecification;
            vpnStatus.setActivity("Selected tunnel");
        } finally {
            tic.close();
        }
        return tunnelChanged;
    }

    private static boolean checkExpiry (Date lastReceived, int heartbeatInterval) {
        Calendar oldestExpectedPacket = Calendar.getInstance();
        oldestExpectedPacket.add(Calendar.SECOND, -heartbeatInterval);
        return lastReceived.before(oldestExpectedPacket.getTime());
    }

    /**
     * Return the first tunnel from available tunnels that is suitable for this VpnThread.
     * @param tunnelIds the List of Strings containing tunnel IDs each
     * @param tic the connected Tic object
     * @return a TicTunnel specifying the tunnel to build up
     * @throws IOException in case of a communication problem
     * @throws ConnectionFailedException in case of a logical problem with the setup
     * @todo Extend to first try the last used/a configured tunnel.
     */
    private TicTunnel selectFirstSuitable(List<String> tunnelIds, Tic tic) throws IOException, ConnectionFailedException {
        for (String id: tunnelIds) {
            TicTunnel desc = null;
            try {
                desc = tic.describeTunnel(id);
            } catch (TunnelNotAcceptedException e) {
                continue;
            }
            if (desc.isValid() && desc.isEnabled() && "ayiya".equals(desc.getType())){
                Log.i(TAG, "Tunnel " + id + " is suitable");
                return desc;
            }
        }
        throw new ConnectionFailedException("No suitable tunnels found", null);
    }

    /**
     * Setup VpnService.Builder object (in effect, the local tun device)
     * @param builder the Builder to configure
     * @param tunnelSpecification the TicTunnel specification of the tunnel to set up.
     */
    private void configureBuilderFromTunnelSpecification(VpnService.Builder builder,
                                                   TicTunnel tunnelSpecification) {
        builder.setMtu(tunnelSpecification.getMtu());
        builder.setSession(tunnelSpecification.getPopName());
        try {
            if (routingConfiguration.isSetDefaultRoute())
                builder.addRoute(Inet6Address.getByName("::"), 0);
            else {
                String routeDefinition = routingConfiguration.getSpecificRoute();
                StringTokenizer tok = new StringTokenizer(routeDefinition, "/");
                Inet6Address address = (Inet6Address) Inet6Address.getByName(tok.nextToken());
                int prefixLen = 128;
                if (tok.hasMoreTokens())
                    prefixLen = Integer.parseInt(tok.nextToken());
                builder.addRoute(address, prefixLen);
            }
        } catch (UnknownHostException e) {
            Log.e(TAG, "Could not add requested IPv6 route to builder", e);
            // @todo notification to user required
        }
        builder.addAddress(tunnelSpecification.getIpv6Endpoint(), tunnelSpecification.getPrefixLength());
        // @todo add the configure intent?
        Log.i(TAG, "Builder is configured");
    }

    /**
     * Create and run a thread that copies from in to out until interrupted.
     * @param in The stream to copy from.
     * @param out The stream to copy to.
     * @return The thread that does so until interrupted.
     */
    private Thread startStreamCopy(final InputStream in, final OutputStream out) {
        Thread thread = new Thread (new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "Copy thread started");
                    // Allocate the buffer for a single packet.
                    byte[] packet = new byte[32767];

                    // @TODO there *must* be a suitable utility class for that...?
                    while (!Thread.currentThread().isInterrupted()) {
                        int len = in.read (packet);
                        if (len > 0) {
                            out.write(packet, 0, len);
                            Log.d(TAG, Thread.currentThread().getName() + " copied package of size: " + len);
                        } else {
                            Thread.sleep(100);
                        }
                    }
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Log.i(TAG, "Copy thread interrupted, will end gracefully");
                    } else {
                        Log.e(TAG, "Copy thread got exception", e);
                    }
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Copy thread could not gracefully close input", e);
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Copy thread could not gracefully close output", e);
                    }
                }
            }
        }

        );
        thread.start();
        return thread;
    }

    private void postToast (final Context ctx, final int resId, final int duration) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ctx, resId, duration).show();
            }
        });
    }


    void reportStatus(/*int progressPerCent,
                               VpnStatusReport.Status status,
                               String activity,
                               TicTunnel activeTunnel,
                               boolean tunnelProvedWorking*/) {
        Intent statusBroadcast = new Intent(BC_STATUS)
                .putExtra(EDATA_STATUS_REPORT, vpnStatus);
        // Broadcast locally
        LocalBroadcastManager.getInstance(ayiyaVpnService).sendBroadcast(statusBroadcast);
    }

}
