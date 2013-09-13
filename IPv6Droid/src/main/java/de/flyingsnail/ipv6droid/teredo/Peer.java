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

import java.math.BigInteger;
import java.net.Inet4Address;
import java.util.Date;

/**
 * This represents teredo information on an IPv6 peer. This corresponds to one entry in the
 * list of recent peers as described in RFC 4380 - except for the IPv6 address, wich is the
 * key part of the associative array implementing said list.
 * Created by pelzi on 13.09.13.
 */
public class Peer {
    private Inet4Address mappedAddress;
    private int mappedPort;
    private boolean isMappedAddressTrusted;
    private BigInteger nounce;
    private Date lastReception;
    private Date lestTransmission;
    private int numBubbles;

    public Peer(int mappedPort, Inet4Address mappedAddress, BigInteger nounce) {
        this.mappedPort = mappedPort;
        this.mappedAddress = mappedAddress;
        this.nounce = nounce;
    }

    public void setMappedAddress(Inet4Address mappedAddress) {
        this.mappedAddress = mappedAddress;
    }

    public void setMappedPort(int mappedPort) {
        this.mappedPort = mappedPort;
    }

    public void setMappedAddressTrusted(boolean mappedAddressTrusted) {
        isMappedAddressTrusted = mappedAddressTrusted;
    }

    public void setNounce(BigInteger nounce) {
        this.nounce = nounce;
    }

    public void setLastReception(Date lastReception) {
        this.lastReception = lastReception;
    }

    public void setLestTransmission(Date lestTransmission) {
        this.lestTransmission = lestTransmission;
    }

    public void setNumBubbles(int numBubbles) {
        this.numBubbles = numBubbles;
    }
}
