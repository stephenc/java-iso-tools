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

import com.github.stephenc.javaisotools.udflib.tools.BinaryTools;

public class Long_ad {

    public long ExtentLength;            // Uint32
    public Lb_addr ExtentLocation;            // Lb_addr
    public byte implementationUse[];    // byte[6]

    public Long_ad() {
        ExtentLocation = new Lb_addr();
        implementationUse = new byte[6];
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        ExtentLength = BinaryTools.readUInt32AsLong(myRandomAccessFile);

        ExtentLocation = new Lb_addr();
        ExtentLocation.read(myRandomAccessFile);

        implementationUse = new byte[6];
        myRandomAccessFile.read(implementationUse);
    }

    public int read(byte[] rawBytes, int startPosition) {
        int position = startPosition;

        ExtentLength = (rawBytes[position++] & 0xFF)
                + (rawBytes[position++] & 0xFF) * 256
                + (rawBytes[position++] & 0xFF) * 256 * 256
                + (rawBytes[position++] & 0xFF) * 256 * 256 * 256;

        ExtentLocation = new Lb_addr();
        position = ExtentLocation.read(rawBytes, position);

        implementationUse = new byte[6];
        System.arraycopy(rawBytes, position, implementationUse, 0, implementationUse.length);
        position += implementationUse.length;

        return position;
    }

    public void write(RandomAccessFile myRandomAccessFile)
            throws IOException {
        byte rawBytes[] = getBytes();
        myRandomAccessFile.write(rawBytes);
    }

    public byte[] getBytes() {
        byte ExtentLocationBytes[] = ExtentLocation.getBytes();

        byte rawBytes[] = new byte[10 + ExtentLocationBytes.length];

        int pos = 0;

        pos = BinaryTools.getUInt32BytesFromLong(ExtentLength, rawBytes, pos);

        System.arraycopy(ExtentLocationBytes, 0, rawBytes, pos, ExtentLocationBytes.length);
        pos += ExtentLocationBytes.length;

        System.arraycopy(implementationUse, 0, rawBytes, pos, implementationUse.length);
        pos += implementationUse.length;

        return rawBytes;
    }
}
