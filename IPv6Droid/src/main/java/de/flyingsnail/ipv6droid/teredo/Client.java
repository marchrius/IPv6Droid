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

package de.flyingsnail.ipv6droid.teredo;

import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Random;

/**
 * This represents the Teredo client.
 * Created by pelzi on 13.09.13.
 */
public class Client {
    private boolean coneNat;

    public enum ConnectivityStatus {
        Initial, Connected, Starting, Offline
    }

    /**
     * Are we connected?
     */
    ConnectivityStatus connectivityStatus;

    InetSocketAddress myExternalAddress;

    TeredoAddress myTeredoAddress;

    Inet6Address myLinkLocalAdress;

    Date lastInteraction;

    long refreshInterval;

    long randomizedRefreshInterval;

    PeerList recentPeers;

    Configuration config;

    /**
     * Constructor. Creates a new Teredo client. It is unconnected at first.
     * @param config
     */
    public Client(Configuration config) {
        if (config == null)
            throw new IllegalArgumentException("Null argument supplied");
        this.config = config;
        connectivityStatus = ConnectivityStatus.Initial;
        lastInteraction = new Date(0l);
        recentPeers = new PeerList();
        refreshInterval = 30000l;
        randomizedRefreshInterval = (int)(refreshInterval * (1 - 0.25 * new Random ().nextDouble()));
        myLinkLocalAdress = Inet6Address.getByAddress(,, LINKLOCAL)
    }

    /**
     * Connect, i.e. run the qualification procedure with the configured server.
     */
    public void connect() throws QualificationTimeoutException {
        Qualification qualificator = new Qualification(config);
        connectivityStatus = ConnectivityStatus.Starting;
        try {
            qualificator.run();
        } catch (QualificationTimeoutException e) {
            connectivityStatus = ConnectivityStatus.Offline;
            throw e;
        }
        connectivityStatus = ConnectivityStatus.Connected;
        myExternalAddress = qualificator.getMappedAddress();
        Inet6Address teredoPrefix = qualificator.getTeredoPrefix();
        myTeredoAddress = new TeredoAddress(teredoPrefix,
                config.getServerIp(),
                myExternalAddress);
        coneNat = qualificator.isConeNat();
    }

    /**
     * Remove old unused entries from the peer list.
     */
    public compactPeerList () {
        recentPeers.compact();
    }

    public void maintain() {
        // @todo needs to be implemented! Depending on the refreshInterval/randomizedRefreshinterval
    }
}
