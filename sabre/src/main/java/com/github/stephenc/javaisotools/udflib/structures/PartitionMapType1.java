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

public class PartitionMapType1 {

    public byte PartitionMapType;        // Uint8
    public byte PartitionMapLength;        // Uint8
    public int VolumeSequenceNumber;    // Uint16
    public int PartitionNumber;        // Uint16

    public PartitionMapType1() {
        PartitionMapType = 1;
        PartitionMapLength = 6;
        VolumeSequenceNumber = 1;
        PartitionNumber = 0;
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        PartitionMapType = myRandomAccessFile.readByte();
        PartitionMapLength = myRandomAccessFile.readByte();
        VolumeSequenceNumber = BinaryTools.readUInt16AsInt(myRandomAccessFile);
        PartitionNumber = BinaryTools.readUInt16AsInt(myRandomAccessFile);
    }

    public void write(RandomAccessFile myRandomAccessFile)
            throws IOException {
        myRandomAccessFile.write(getBytes());
    }

    public byte[] getBytes() {
        byte rawBytes[] = new byte[6];

        int pos = 0;

        rawBytes[pos++] = PartitionMapType;
        rawBytes[pos++] = PartitionMapLength;

        pos = BinaryTools.getUInt16BytesFromInt(VolumeSequenceNumber, rawBytes, pos);
        BinaryTools.getUInt16BytesFromInt(PartitionNumber, rawBytes, pos);

        return rawBytes;
    }

}
