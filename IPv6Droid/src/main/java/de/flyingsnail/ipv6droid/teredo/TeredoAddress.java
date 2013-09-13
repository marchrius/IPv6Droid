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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * A class that helps in constructing and analysing a Teredo IPv6 address.
 * Created by pelzi on 13.09.13.
 */
public class TeredoAddress {
    /**
     * the constant global Teredo prefix.
     */
    static Inet6Address PREFIX;

    {
        try {
            PREFIX = (Inet6Address)Inet6Address.getByName("2001:0000:");
        } catch (UnknownHostException e) {
            PREFIX = null;
        }
    }

    static final short FLAG = (short) 0x8000;

    Inet6Address teredoPrefix;

    /**
     * The IPv4 address of the Teredo server. It is part of the client's IPv6 address.
     */
    Inet4Address serverIP;
    /**
     * The IPv4 address of the Teredo client. It is used to construct a part of the client's IPv6
     * address.
     */
    InetSocketAddress clientIP;

    /**
     * Constructor.
     * @param teredoPrefix the Inet6Address of the Teredo prefix to use. In case of null, default will be used.
     * @param serverIP {@link #serverIP serverIP value}
     * @param clientIP {@link #clientIP clientIP value}
     */
    public TeredoAddress(Inet6Address teredoPrefix, Inet4Address serverIP, InetSocketAddress clientIP) {
        if (serverIP == null || clientIP == null)
            throw new IllegalArgumentException("Null argument");

        if (teredoPrefix == null)
            this.teredoPrefix = PREFIX;
        else
            this.teredoPrefix = teredoPrefix;
        this.serverIP = serverIP;
        this.clientIP = clientIP;
    }

    /**
     * Calculate the IPv6 address from the information in this object.
     * @return the Inet6Address representing this information.
     */
    public Inet6Address getAddress() {
        byte address[] = new byte[16];
        ByteBuffer buffer = ByteBuffer.wrap(address);
        buffer.put(teredoPrefix.getAddress(), 0, 4);
        buffer.put(serverIP.getAddress());
        buffer.putShort(FLAG);
        short obfuscatedPort = (short)(~clientIP.getPort());
        buffer.putShort(obfuscatedPort);
        byte[] clientAddress = clientIP.getAddress().getAddress();
        for (byte b: clientAddress) {
            buffer.put((byte)(~b));
        }

        try {
            return (Inet6Address)InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Conversion implementation broken - sorry", e);
        }
    }
}
