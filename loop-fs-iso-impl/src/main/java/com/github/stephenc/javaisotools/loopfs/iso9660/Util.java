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

package com.github.stephenc.javaisotools.loopfs.iso9660;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.TimeZone;

import com.github.stephenc.javaisotools.loopfs.util.BigEndian;
import com.github.stephenc.javaisotools.loopfs.util.LittleEndian;

public final class Util {

    /**
     * Gets an unsigned 8-bit value LSB first. See section 7.1.1.
     */
    public static int getUInt8(byte[] block, int pos) {
        return LittleEndian.getUInt8(block, pos - 1);
    }

    /**
     * Gets a signed 8-bit value LSB first. See section 7.1.2.
     */
    public static int getInt8(byte[] block, int pos) {
        return LittleEndian.getInt8(block, pos - 1);
    }

    /**
     * Gets an unsigned 16-bit value LSB first. See section 7.2.1.
     */
    public static int getUInt16LE(byte[] block, int pos) {
        return LittleEndian.getUInt16(block, pos - 1);
    }

    /**
     * Gets an unsigned 16-bit value MSB first. See section 7.2.2.
     */
    public static int getUInt16BE(byte[] block, int pos) {
        return BigEndian.getUInt16(block, pos - 1);
    }

    /**
     * Gets an unsigned 16-bit value in both byteorders. See section 7.2.3.
     */
    public static int getUInt16Both(byte[] block, int pos) {
        return LittleEndian.getUInt16(block, pos - 1);
    }

    /**
     * Gets an unsigned 32-bit value LSB first. See section 7.3.1.
     */
    public static long getUInt32LE(byte[] block, int pos) {
        return LittleEndian.getUInt32(block, pos - 1);
    }

    /**
     * Gets an unsigned 32-bit value MSB first. See section 7.3.2.
     */
    public static long getUInt32BE(byte[] block, int pos) {
        return BigEndian.getUInt32(block, pos - 1);
    }

    /**
     * Gets an unsigned 32-bit value in both byteorders. See section 7.3.3.
     */
    public static long getUInt32Both(byte[] block, int pos) {
        return LittleEndian.getUInt32(block, pos - 1);
    }

    /**
     * Gets a string of a-characters. See section 7.4.1.
     */
    public static String getAChars(byte[] block, int pos, int length) {
        return new String(block, pos - 1, length).trim();
    }

    /**
     * Gets a string of d-characters. See section 7.4.1.
     */
    public static String getDChars(byte[] block, int pos, int length) {
        return new String(block, pos - 1, length).trim();
    }

    /**
     * Gets a string of a-characters. See section 7.4.1.
     */
    public static String getAChars(byte[] block, int pos, int length, String encoding) {
        try {
            return new String(block, pos - 1, length, encoding).trim();
        }
        catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets a string of d-characters. See section 7.4.1.
     */
    public static String getDChars(byte[] block, int pos, int length, String encoding) {
        try {
            return new String(block, pos - 1, length, encoding).trim();
        }
        catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static long getStringDate(byte[] block, int pos) {
        int i = pos - 1;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, toInt(block, i, 4));
        cal.set(Calendar.MONTH, toInt(block, (i = i + 4), 2) - 1);
        cal.set(Calendar.DATE, toInt(block, (i = i + 2), 2));
        cal.set(Calendar.HOUR_OF_DAY, toInt(block, (i = i + 2), 2));
        cal.set(Calendar.MINUTE, toInt(block, (i = i + 2), 2));
        cal.set(Calendar.SECOND, toInt(block, (i = i + 2), 2));
        cal.set(Calendar.MILLISECOND, toInt(block, (i = i + 2), 2) * 10);
        cal.setTimeZone(TimeZone.getTimeZone(getGMTpos(block[i + 2])));
        return cal.getTimeInMillis();
    }

    private static int toInt(byte[] block, int pos, int len) {
        try {
            return Integer.parseInt(new String(block, pos, len));
        }
        catch (Exception ex) {
            return 0;
        }
    }

    public static long getDateTime(byte[] sector, int pos) {
        int i = pos - 1;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1900 + sector[i]);
        cal.set(Calendar.MONTH, sector[i + 1] - 1);
        cal.set(Calendar.DATE, sector[i + 2]);
        cal.set(Calendar.HOUR_OF_DAY, sector[i + 3]);
        cal.set(Calendar.MINUTE, sector[i + 4]);
        cal.set(Calendar.SECOND, sector[i + 5]);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTimeZone(TimeZone.getTimeZone(getGMTpos(sector[i + 6])));
        return cal.getTimeInMillis();
    }

    private static String getGMTpos(byte b) {
        if (0 == b) {
            return "GMT";
        }

        StringBuffer buf = new StringBuffer("GMT");
        buf.append((b < 0) ? '-' : '+');

        int posMinutes = Math.abs(b) * 15;
        int hours = posMinutes / 60;
        int minutes = posMinutes % 60;
        buf.append(hours).append(':').append((0 == minutes) ? "00" : String.valueOf(minutes));

        return buf.toString();
    }

    private Util() {
    }
}