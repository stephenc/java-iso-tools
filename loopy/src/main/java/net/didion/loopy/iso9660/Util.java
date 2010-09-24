package net.didion.loopy.iso9660;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.TimeZone;

import net.didion.loopy.util.BigEndian;
import net.didion.loopy.util.LittleEndian;

abstract class Util implements Constants {

    static final int getType(byte[] buffer) {
        return Util.getUInt8(buffer, 1);
    }

    /**
     * See section 7.1.1.
     */
    static final int getUInt8(byte[] buffer, int bp) {
        return LittleEndian.getUInt8(buffer, bp - 1);
    }

    /**
     * See section 7.1.2.
     */
    static final int getInt8(byte[] buffer, int bp) {
        return LittleEndian.getInt8(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 16-bit value LSB first. See section 7.2.1.
     */
    static final int getUInt16LE(byte[] buffer, int bp) {
        return LittleEndian.getUInt16(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 16-bit value MSB first. See section 7.2.2.
     */
    static final int getUInt16BE(byte[] buffer, int bp) {
        return BigEndian.getUInt16(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 16-bit value in both byteorders. See section 7.2.3.
     */
    static final int getUInt16Both(byte[] buffer, int bp) {
        return LittleEndian.getUInt16(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 32-bit value LSB first. See section 7.3.1.
     */
    static final long getUInt32LE(byte[] buffer, int bp) {
        return LittleEndian.getUInt32(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 32-bit value MSB first. See section 7.3.2.
     */
    static final long getUInt32BE(byte[] buffer, int bp) {
        return BigEndian.getUInt32(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 32-bit value in both byteorders. See section 7.3.3.
     */
    static final long getUInt32Both(byte[] buffer, int bp) {
        return LittleEndian.getUInt32(buffer, bp - 1);
    }

    /**
     * Gets a string of a-characters. See section 7.4.1.
     */
    static final String getAChars(byte[] buffer, int bp, int length) {
        return new String(buffer, bp - 1, length).trim();
    }

    /**
     * Gets a string of d-characters. See section 7.4.1.
     */
    static final String getDChars(byte[] buffer, int bp, int length) {
        return new String(buffer, bp - 1, length).trim();
    }

    /**
     * Gets a string of a-characters. See section 7.4.1.
     */
    static final String getAChars(
            byte[] buffer, int bp, int length, String encoding)
            throws UnsupportedEncodingException {
        return new String(buffer, bp - 1, length, encoding).trim();
    }

    /**
     * Gets a string of d-characters. See section 7.4.1.
     */
    static final String getDChars(
            byte[] buffer, int bp, int length, String encoding)
            throws UnsupportedEncodingException {
        return new String(buffer, bp - 1, length, encoding).trim();
    }

    static long getStringDate(byte[] buffer, int bp) {
        int i = bp - 1;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, toInt(buffer, i, 4));
        cal.set(Calendar.MONTH, toInt(buffer, (i = i + 4), 2) - 1);
        cal.set(Calendar.DATE, toInt(buffer, (i = i + 2), 2));
        cal.set(Calendar.HOUR_OF_DAY, toInt(buffer, (i = i + 2), 2));
        cal.set(Calendar.MINUTE, toInt(buffer, (i = i + 2), 2));
        cal.set(Calendar.SECOND, toInt(buffer, (i = i + 2), 2));
        cal.set(Calendar.MILLISECOND, toInt(buffer, (i = i + 2), 2) * 10);
        cal.setTimeZone(TimeZone.getTimeZone(getGMTOffset(buffer[i + 2])));
        return cal.getTimeInMillis();
    }

    private static int toInt(byte[] buffer, int offset, int len) {
        try {
            return Integer.parseInt(new String(buffer, offset, len));
        } catch (Exception ex) {
            return 0;
        }
    }

    static long getIntDate(byte[] buffer, int bp) {
        int i = bp - 1;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1900 + buffer[i]);
        cal.set(Calendar.MONTH, buffer[i + 1] - 1);
        cal.set(Calendar.DATE, buffer[i + 2]);
        cal.set(Calendar.HOUR_OF_DAY, buffer[i + 3]);
        cal.set(Calendar.MINUTE, buffer[i + 4]);
        cal.set(Calendar.SECOND, buffer[i + 5]);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTimeZone(TimeZone.getTimeZone(getGMTOffset(buffer[i + 6])));
        return cal.getTimeInMillis();
    }

    private static String getGMTOffset(byte b) {
        if (0 == b) {
            return "GMT";
        }
        StringBuffer buf = new StringBuffer("GMT");
        buf.append((b < 0) ? '-' : '+');

        int offsetMinutes = Math.abs(b) * 15;
        int hours = offsetMinutes / 60;
        int minutes = offsetMinutes % 60;
        buf.append(hours).append(':').append((0 == minutes) ? "00" : String.valueOf(minutes));

        return buf.toString();
    }
}