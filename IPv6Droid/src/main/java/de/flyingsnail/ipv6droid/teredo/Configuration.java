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

import java.net.Inet4Address;

/**
 * This encapsulates the configuration required for the Teredo client.
 * Created by pelzi on 13.09.13.
 */
public class Configuration {
    private Inet4Address serverIp;
    private Inet4Address secondaryServerIp;

    /**
     * Constructor.
     * @param serverIp the primary IPv4 address of the Teredo server to use.
     * @param secondaryServerIp the secondary IPv4 address of the Teredo server to use.
     *                          Required for NAT type detection.
     */
    public Configuration(Inet4Address serverIp, Inet4Address secondaryServerIp) {
        if (serverIp == null || secondaryServerIp == null)
            throw new IllegalArgumentException("Null arguments supplied");
        this.serverIp = serverIp;
        this.secondaryServerIp = secondaryServerIp;
    }

    /**
     * Yield the primary server IPv4 address.
     * @return the Inet4Address of the server.
     */
    public Inet4Address getServerIp() {
        return serverIp;
    }

    /**
     * Yield the secondary server IPv4 address.
     * @return the secondary Inet4Address of the server.
     */
    public Inet4Address getSecondaryServerIp() {
        return secondaryServerIp;
    }
}
