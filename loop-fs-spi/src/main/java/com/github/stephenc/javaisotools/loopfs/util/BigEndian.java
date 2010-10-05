/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (c) 2006-2007. loopy project (http://loopy.sourceforge.net).
 *  
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.github.stephenc.javaisotools.loopfs.util;

/**
 * Big endian (MSB first) conversion methods.
 */
public class BigEndian {

    /**
     * Gets a 16-bit unsigned integer from the given byte array at the given offset.
     */
    public static int getUInt16(byte[] src, int offset) {
        final int v1 = src[offset] & 0xFF;
        final int v0 = src[offset + 1] & 0xFF;
        return ((v1 << 8) | v0);
    }

    /**
     * Gets a 32-bit unsigned integer from the given byte array at the given offset.
     */
    public static long getUInt32(byte[] src, int offset) {
        final long v3 = src[offset] & 0xFF;
        final long v2 = src[offset + 1] & 0xFF;
        final long v1 = src[offset + 2] & 0xFF;
        final long v0 = src[offset + 3] & 0xFF;
        return ((v3 << 24) | (v2 << 16) | (v1 << 8) | v0);
    }
}
