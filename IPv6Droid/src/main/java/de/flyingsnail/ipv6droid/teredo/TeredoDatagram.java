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

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

/**
 * Created by pelzi on 15.09.13.
 */
public class TeredoDatagram {
    private OriginIndication originIndication = null;
    private ByteBuffer payload = null;

    /**
     * Constructs a TeredoDatagram from a received UDP Datagram. Checks for optional
     * originIndication and a valid IPv6 payload datagram.
     * @param packet the received DatagramPacket.
     */
    public TeredoDatagram (DatagramPacket packet) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(packet.getData());
        // check for origin indication included in the packet; if so, return it and push forward
        // position of byteBuffer at the byte past the origin indication.
        originIndication = OriginIndication.fromByteBuffer (byteBuffer);
        payload = byteBuffer.slice();
        // @todo implement this: parse the received structure and build the corresponding
        // field objects
        checkValidPayload();
    }

    /**
     * Constructs a TeredoDatagram for transmission, with the given originIndiation and payload,
     * both optional.
     * @param originIndication the OriginIndication to include (may be null).
     * @param payload the payload to include (may be null).
     */
    public TeredoDatagram (OriginIndication originIndication, ByteBuffer payload) {
        this.originIndication = originIndication;
        this.payload = payload;
        payload.reset();
        checkValidPayload();
    }

    private void checkValidPayload() {
        // @todo implement this: analyse payload to be valid
    }

    /**
     * Construct the UDP v4 packet corresponding to this Teredo packet.
     * @return the DatagramPacket to send
     */
    public DatagramPacket getUdpPacket() {
        // @todo implement
    }
}

