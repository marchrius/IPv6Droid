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
 * Helper class to parse a Router Advertisement as raw bytes inside a UDP v4 DatagramPacket.
 * Created by pelzi on 15.09.13.
 */
public class RouterAdvertisement {
    /**
     * Construct a RouterAdvertisment object from a received payload.
     * @param input the ByteBuffer representing the received payload.
     * @throws IllegalArgumentException in case that the payload is not in fact a valid router advertisement.
     */
    public RouterAdvertisement (ByteBuffer input) throws IllegalArgumentException {
        // @todo implement parsing and checking
    }
}
