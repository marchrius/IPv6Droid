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

/**
 * Created by pelzi on 13.09.13.
 */
public class Qualification {
    public final int MAX_ATTEMPTS = 3;
    public final int TIMEOUT = 4000;

    private Configuration config;

    private boolean cone;

    /**
     * Constructor.
     * @param config the Configuration object containing server IPs to use.
     */
    public Qualification(Configuration config) {
        this.config = config;
    }

    /**
     * Performs the qualification procedure as described by RFC 4380.
     */
    public void run() throws QualificationTimeoutException {
        cone = true;
        boolean connected = false;
        while (!connected) {
            try {
                int attempt = 0;
                do {
                    if (++attempt > MAX_ATTEMPTS)
                        throw new QualificationTimeoutException ("No response within configured retry number");
                    doStartAction();
                } while (!waitForRouterAdvertisement());
                connected = true;
            } catch (QualificationTimeoutException e) {
                if (cone)
                    cone = false;
                else
                    throw e;
            }
        }

        /*
           If the client has received an RA with the cone bit in the IPv6
   destination address set to 1, it is behind a cone NAT and is fully
   qualified.  If the RA is received with the cone bit set to 0, the
   client does not know whether the local NAT is restricted or
   symmetric.  The client selects the secondary IPv4 server address, and
   repeats the procedure, the cone bit remaining to the value zero.  If
   the client does not receive a response, it detects that the service
   is not usable.  If the client receives a response, it compares the
   mapped address and mapped port in this second response to the first
   received values.  If the values are different, the client detects a
   symmetric NAT: it cannot use the Teredo service.  If the values are
   the same, the client detects a port-restricted or restricted cone
   NAT: the client is qualified to use the service.  (Teredo operates
   the same way for restricted and port-restricted NAT.)

   If the client is qualified, it builds a Teredo IPv6 address using the
   Teredo IPv6 server prefix learned from the RA and the obfuscated
   values of the UDP port and IPv4 address learned from the origin
   indication.  The cone bit should be set to the value used to receive
   the RA, i.e., 1 if the client is behind a cone NAT, 0 otherwise.  The
   client can start using the Teredo service.

         */
    }

    private void checkRouterResponse() {
        /*
           If a response arrives, the client checks that the response contains
   an origin indication and a valid router advertisement as defined in
   [RFC2461], that the IPv6 destination address is equal to the link-
   local address used in the router solicitation, and that the router
   advertisement contains exactly one advertised Prefix Information
   option.  This prefix should be a valid Teredo IPv6 server prefix: the
   first 32 bits should contain the global Teredo IPv6 service prefix,
   and the next 32 bits should contain the server's IPv4 address.  If
   this is the case, the client learns the Teredo mapped address and
   Teredo mapped port from the origin indication.  The IPv6 source
   address of the Router Advertisement is a link-local server address of
   the Teredo server.  (Responses that are not valid advertisements are
   simply discarded.)

         */
    }

    private boolean waitForRouterAdvertisement() {
        /*
        In the starting state, the client waits for a router advertisement
   from the Teredo server.
         */
        ...
        // response arrived, needs checking
        checkRouterResponse();

    }

    private void doStartAction() {
        // @todo implement
        /*When the interface is initialized, the system first performs the
   "start action" by sending a Router Solicitation message, as defined
   in [RFC2461].  The client picks a link-local address and uses it as
   the IPv6 source of the message; the cone bit in the address is set to
   1 (see Section 4 for the address format); the IPv6 destination of the
   RS is the all-routers multicast address; the packet will be sent over
   UDP from the service port to the Teredo server's IPv4 address and
   Teredo UDP port.  The connectivity status moves then to "Starting". */
    }

    public boolean isConeNat() {
        return cone;
    }

    public boolean isCone() {
        return cone;
    }

    public void setCone(boolean cone) {
        this.cone = cone;
    }
}
