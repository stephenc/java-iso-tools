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

public class VolumeStructureDescriptor {

    public byte StructureType;                // Uint8
    public byte StandardIdentifier[];        // byte[5]
    public byte StructureVersion;            // Uint8
    public byte StructureData[];            // byte[2041]

    public VolumeStructureDescriptor() {
        StandardIdentifier = new byte[5];
        StructureData = new byte[2041];
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        StructureType = myRandomAccessFile.readByte();

        StandardIdentifier = new byte[5];
        myRandomAccessFile.read(StandardIdentifier);

        StructureVersion = myRandomAccessFile.readByte();

        StructureData = new byte[2041];
        myRandomAccessFile.read(StructureData);
    }

    public void write(RandomAccessFile myRandomAccessFile)
            throws IOException {
        myRandomAccessFile.writeByte(StructureType);
        myRandomAccessFile.write(StandardIdentifier);
        myRandomAccessFile.writeByte(StructureVersion);
        myRandomAccessFile.write(StructureData);
    }

    public byte[] getBytes() {
        byte[] rawBytes = new byte[2048];

        int pos = 0;

        rawBytes[pos++] = StructureType;

        System.arraycopy(StandardIdentifier, 0, rawBytes, pos, StandardIdentifier.length);
        pos += StandardIdentifier.length;

        rawBytes[pos++] = StructureVersion;

        System.arraycopy(StructureData, 0, rawBytes, pos, StructureData.length);
        pos += StructureData.length;

        return rawBytes;
    }
}
