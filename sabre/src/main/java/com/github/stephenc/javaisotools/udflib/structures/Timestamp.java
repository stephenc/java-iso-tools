/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (c) 2006. Björn Stickler <bjoern@stickler.de>.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.github.stephenc.javaisotools.udflib.structures;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;

import com.github.stephenc.javaisotools.udflib.tools.BinaryTools;

public class Timestamp {

    public int TypeAndTimezone;                // Uint16
    public int Year;                        // Int16
    public byte Month;                        // Uint8
    public byte Day;                        // Uint8
    public byte Hour;                        // Uint8
    public byte Minute;                        // Uint8
    public byte Second;                        // Uint8
    public byte Centiseconds;                // Uint8
    public byte HundredsofMicroseconds;        // Uint8
    public byte Microseconds;                // Uint8

    public Timestamp() {

    }

    public Timestamp(Calendar myCalendar) {
        set(myCalendar);
    }

    public void set(Calendar myCalendar) {
        int offsetInMillisecons = myCalendar.get(Calendar.ZONE_OFFSET) + myCalendar.get(Calendar.DST_OFFSET);
        int offsetInMinutes = (offsetInMillisecons / 60000);

        int twelveBitSignedValue = offsetInMinutes;

        if (twelveBitSignedValue < 0) {
            twelveBitSignedValue *= -1;
            twelveBitSignedValue ^= 0x0FFF;
            twelveBitSignedValue += 1;
        }

        //  4 bit == 1
        // 12 bit == signed two complement minutes offset from UTC
        TypeAndTimezone = 0x1000 | twelveBitSignedValue;

        Year = myCalendar.get(Calendar.YEAR);
        Month = (byte) (myCalendar.get(Calendar.MONTH) + 1);
        Day = (byte) myCalendar.get(Calendar.DAY_OF_MONTH);
        Hour = (byte) myCalendar.get(Calendar.HOUR_OF_DAY);
        Minute = (byte) myCalendar.get(Calendar.MINUTE);
        Second = (byte) myCalendar.get(Calendar.SECOND);
        Centiseconds = (byte) (myCalendar.get(Calendar.MILLISECOND) / 100);
        HundredsofMicroseconds = (byte) (myCalendar.get(Calendar.MILLISECOND) % 100);
        Microseconds = (byte) 0;
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        TypeAndTimezone = BinaryTools.readUInt16AsInt(myRandomAccessFile);
        Year = BinaryTools.readUInt16AsInt(myRandomAccessFile);
        Month = myRandomAccessFile.readByte();
        Day = myRandomAccessFile.readByte();
        Hour = myRandomAccessFile.readByte();
        Minute = myRandomAccessFile.readByte();
        Second = myRandomAccessFile.readByte();
        Centiseconds = myRandomAccessFile.readByte();
        HundredsofMicroseconds = myRandomAccessFile.readByte();
        Microseconds = myRandomAccessFile.readByte();
    }

    public void write(RandomAccessFile myRandomAccessFile)
            throws IOException {
        myRandomAccessFile.write(getBytes());
    }

    public byte[] getBytes() {
        byte rawBytes[] = new byte[12];

        int pos = 0;

        pos = BinaryTools.getUInt16BytesFromInt(TypeAndTimezone, rawBytes, pos);
        pos = BinaryTools.getUInt16BytesFromInt(Year, rawBytes, pos);

        rawBytes[pos++] = (byte) (Month & 0xFF);
        rawBytes[pos++] = (byte) (Day & 0xFF);
        rawBytes[pos++] = (byte) (Hour & 0xFF);
        rawBytes[pos++] = (byte) (Minute & 0xFF);
        rawBytes[pos++] = (byte) (Second & 0xFF);
        rawBytes[pos++] = (byte) (Centiseconds & 0xFF);
        rawBytes[pos++] = (byte) (HundredsofMicroseconds & 0xFF);
        rawBytes[pos++] = (byte) (Microseconds & 0xFF);

        return rawBytes;
    }
}
