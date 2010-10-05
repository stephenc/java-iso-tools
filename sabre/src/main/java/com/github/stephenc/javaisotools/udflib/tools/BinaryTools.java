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

package com.github.stephenc.javaisotools.udflib.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class BinaryTools {

    public static long readUInt64AsLong(RandomAccessFile myRandomAccessFile)
            throws IOException {
        long result = myRandomAccessFile.readUnsignedByte()
                | (myRandomAccessFile.readUnsignedByte() << 8)
                | (myRandomAccessFile.readUnsignedByte() << 16)
                | (myRandomAccessFile.readUnsignedByte() << 24)
                | (myRandomAccessFile.readUnsignedByte() << 32)
                | (myRandomAccessFile.readUnsignedByte() << 40)
                | (myRandomAccessFile.readUnsignedByte() << 48)
                | (myRandomAccessFile.readUnsignedByte() << 56);

        return result;
    }

    public static long readUInt32AsLong(RandomAccessFile myRandomAccessFile)
            throws IOException {
        long result = myRandomAccessFile.readUnsignedByte()
                | (myRandomAccessFile.readUnsignedByte() << 8)
                | (myRandomAccessFile.readUnsignedByte() << 16)
                | (myRandomAccessFile.readUnsignedByte() << 24);

        return result;
    }

    public static int readUInt32AsInt(RandomAccessFile myRandomAccessFile)
            throws IOException {
        int result = myRandomAccessFile.readUnsignedByte()
                | (myRandomAccessFile.readUnsignedByte() << 8)
                | (myRandomAccessFile.readUnsignedByte() << 16)
                | (myRandomAccessFile.readUnsignedByte() << 24);

        return result;
    }

    public static int readUInt16AsInt(RandomAccessFile myRandomAccessFile)
            throws IOException {
        int result = myRandomAccessFile.readUnsignedByte()
                | (myRandomAccessFile.readUnsignedByte() << 8);

        return result;
    }

    public static int getUInt64BytesFromLong(long value, byte target[], int start) {
        int pos = start;

        target[pos++] = (byte) (value & 0xFF);
        target[pos++] = (byte) ((value >> 8) & 0xFF);
        target[pos++] = (byte) ((value >> 16) & 0xFF);
        target[pos++] = (byte) ((value >> 24) & 0xFF);
        target[pos++] = (byte) ((value >> 32) & 0xFF);
        target[pos++] = (byte) ((value >> 40) & 0xFF);
        target[pos++] = (byte) ((value >> 48) & 0xFF);
        target[pos++] = (byte) ((value >> 56) & 0xFF);

        return pos;
    }

    public static int getUInt32BytesFromLong(long value, byte target[], int start) {
        int pos = start;

        target[pos++] = (byte) (value & 0xFF);
        target[pos++] = (byte) ((value >> 8) & 0xFF);
        target[pos++] = (byte) ((value >> 16) & 0xFF);
        target[pos++] = (byte) ((value >> 24) & 0xFF);

        return pos;
    }

    public static int getUInt16BytesFromInt(int value, byte target[], int start) {
        int pos = start;

        target[pos++] = (byte) (value & 0xFF);
        target[pos++] = (byte) ((value >> 8) & 0xFF);

        return pos;
    }

    public static long readUInt32AsLong(InputStream myInputStream)
            throws IOException {
        long result = ((myInputStream.read() & 0xFF) << 24)
                | ((myInputStream.read() & 0xFF) << 16)
                | ((myInputStream.read() & 0xFF) << 8)
                | (myInputStream.read() & 0xFF);

        return result;
    }

    public static long readUInt64AsLong(InputStream myInputStream)
            throws IOException {
        long result = ((long) (myInputStream.read() & 0xFF) << 56)
                | ((long) (myInputStream.read() & 0xFF) << 48)
                | ((long) (myInputStream.read() & 0xFF) << 40)
                | ((long) (myInputStream.read() & 0xFF) << 32)
                | ((long) (myInputStream.read() & 0xFF) << 24)
                | ((long) (myInputStream.read() & 0xFF) << 16)
                | ((long) (myInputStream.read() & 0xFF) << 8)
                | (long) (myInputStream.read() & 0xFF);

        return result;
    }

    public static byte[] readByteArray(InputStream myInputStream, int length)
            throws IOException {
        byte[] result = new byte[length];

        int currentPosition = 0;

        while (currentPosition < length) {
            int bytesRead = myInputStream.read(result, currentPosition, length - currentPosition);
            currentPosition += bytesRead;
        }

        return result;
    }
}
