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
import java.util.HashMap;
import java.util.Random;

/**
 * Created by pelzi on 13.09.13.
 */
public class DiscoveryTable extends HashMap<Inet4Address, BigInteger> {
    private Random random = new Random();

    /**
     * Generate a new entry with a new random number for a given IPv4 address. Write this entry to
     * this map.
     * @param address an Inet4Address for which a new entry will be generated.
     * @return the random BigInteger that was generated.
     */
    public BigInteger createEntry (Inet4Address address) {
        BigInteger code = new BigInteger(128, random);
        this.put(address, code);
        return code;
    }
}
