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

public class Short_ad {

    public long ExtentLength;        // Uint32
    public long ExtentPosition;        // Uint32

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        ExtentLength = BinaryTools.readUInt32AsLong(myRandomAccessFile);
        ExtentPosition = BinaryTools.readUInt32AsLong(myRandomAccessFile);
    }

    public byte[] getBytes() {
        byte[] rawBytes = new byte[8];

        int pos = 0;

        pos = BinaryTools.getUInt32BytesFromLong(ExtentLength, rawBytes, pos);
        pos = BinaryTools.getUInt32BytesFromLong(ExtentPosition, rawBytes, pos);

        return rawBytes;
    }
}
