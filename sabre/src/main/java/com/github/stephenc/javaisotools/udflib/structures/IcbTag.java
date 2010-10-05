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

public class IcbTag {

    public long PriorRecordedNumberofDirectEntries;    // Uint32
    public int StrategyType;                        // Uint16
    public byte StrateryParameter[];                // byte[2]
    public int NumberofEntries;                    // Uint16
    public byte Reserved;                            // byte
    public byte FileType;                            // Uint8
    public Lb_addr ParentICBLocation;                    // Lb_addr
    public int Flags;                                // Uint16

    public IcbTag() {
        StrateryParameter = new byte[2];
        ParentICBLocation = new Lb_addr();
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        PriorRecordedNumberofDirectEntries = BinaryTools.readUInt32AsLong(myRandomAccessFile);
        StrategyType = BinaryTools.readUInt16AsInt(myRandomAccessFile);

        StrateryParameter = new byte[2];
        myRandomAccessFile.read(StrateryParameter);

        NumberofEntries = BinaryTools.readUInt16AsInt(myRandomAccessFile);
        Reserved = myRandomAccessFile.readByte();
        FileType = myRandomAccessFile.readByte();

        ParentICBLocation = new Lb_addr();
        ParentICBLocation.read(myRandomAccessFile);

        Flags = BinaryTools.readUInt16AsInt(myRandomAccessFile);
    }

    public void write(RandomAccessFile myRandomAccessFile)
            throws IOException {
        byte rawBytes[] = getBytes();
        myRandomAccessFile.write(rawBytes);
    }

    public byte[] getBytes() {
        byte ParentICBLocationBytes[] = ParentICBLocation.getBytes();

        byte rawBytes[] = new byte[14 + ParentICBLocationBytes.length];

        int pos = 0;

        pos = BinaryTools.getUInt32BytesFromLong(PriorRecordedNumberofDirectEntries, rawBytes, pos);
        pos = BinaryTools.getUInt16BytesFromInt(StrategyType, rawBytes, pos);

        System.arraycopy(StrateryParameter, 0, rawBytes, pos, StrateryParameter.length);
        pos += StrateryParameter.length;

        pos = BinaryTools.getUInt16BytesFromInt(NumberofEntries, rawBytes, pos);

        rawBytes[pos++] = Reserved;
        rawBytes[pos++] = FileType;

        System.arraycopy(ParentICBLocationBytes, 0, rawBytes, pos, ParentICBLocationBytes.length);
        pos += ParentICBLocationBytes.length;

        pos = BinaryTools.getUInt16BytesFromInt(Flags, rawBytes, pos);

        return rawBytes;
    }
}
