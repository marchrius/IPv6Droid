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

import java.nio.ByteBuffer;

/**
 * Helper class to create RouterSolicitation UDPv6 packets at ByteBuffer level for inclusion as
 * payload in UDP v4 Datagrams. Not a nice design, but I don't know how to get the raw bytes of
 * a Datagram :-(
 * Created by pelzi on 15.09.13.
 */
public class RouterSolicitation {
    public RouterSolicitation () {
        // @todo implement
    }

    public ByteBuffer createPacketData() {
        // @todo implement
    }
}
