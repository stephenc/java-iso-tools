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
import com.github.stephenc.javaisotools.udflib.tools.Checksum;

public class SpaceBitmapDescriptor {

    public Tag DescriptorTag;            // struct tag
    public long NumberOfBits;            // Uint32
    public long NumberOfBytes;            // Uint32
    public byte Bitmap[];                // byte[]

    public SpaceBitmapDescriptor() {
        DescriptorTag = new Tag();
        DescriptorTag.DescriptorVersion = 3;
        DescriptorTag.TagIdentifier = 264;
        Bitmap = new byte[0];
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        DescriptorTag = new Tag();
        DescriptorTag.read(myRandomAccessFile);

        NumberOfBits = BinaryTools.readUInt32AsLong(myRandomAccessFile);

        NumberOfBytes = BinaryTools.readUInt32AsLong(myRandomAccessFile);

        Bitmap = new byte[(int) NumberOfBytes];
        myRandomAccessFile.read(Bitmap);
    }

    public void write(RandomAccessFile myRandomAccessFile, int blockSize)
            throws IOException {
        byte rawBytes[] = getBytesWithoutDescriptorTag();

        DescriptorTag.DescriptorCRCLength =
                8; // not rawBytes.length according to errata DCN-5108 for UDF 2.50 and lower
        DescriptorTag.DescriptorCRC = Checksum.cksum(getFirst8Bytes()); // not rawBytes ^^

        DescriptorTag.write(myRandomAccessFile);

        myRandomAccessFile.write(rawBytes);

        int bytesWritten = rawBytes.length + 16;

        byte emptyBytesInBlock[] = new byte[blockSize - (bytesWritten % blockSize)];
        myRandomAccessFile.write(emptyBytesInBlock);
    }

    public byte[] getFirst8Bytes() {
        byte rawBytes[] = new byte[8];

        int pos = 0;

        pos = BinaryTools.getUInt32BytesFromLong(NumberOfBits, rawBytes, pos);
        pos = BinaryTools.getUInt32BytesFromLong(NumberOfBytes, rawBytes, pos);

        return rawBytes;
    }

    public byte[] getBytesWithoutDescriptorTag() {
        byte rawBytes[] = new byte[8 + Bitmap.length];

        int pos = 0;

        pos = BinaryTools.getUInt32BytesFromLong(NumberOfBits, rawBytes, pos);
        pos = BinaryTools.getUInt32BytesFromLong(NumberOfBytes, rawBytes, pos);

        System.arraycopy(Bitmap, 0, rawBytes, pos, Bitmap.length);
        pos += Bitmap.length;

        return rawBytes;
    }

    public long getFullBlockLength(int blockSize) {
        long length = 24 + Bitmap.length;

        if (length % blockSize != 0) {
            length += blockSize - length % blockSize;
        }

        return length;
    }

}
