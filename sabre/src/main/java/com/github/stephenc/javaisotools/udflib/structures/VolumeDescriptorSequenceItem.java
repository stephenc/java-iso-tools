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

import com.github.stephenc.javaisotools.udflib.tools.Checksum;

public abstract class VolumeDescriptorSequenceItem {

    public Tag DescriptorTag;                                // struct tag
    public long VolumeDescriptorSequenceNumber;                // Uint32

    public abstract byte[] getBytesWithoutDescriptorTag();

    public abstract void read(RandomAccessFile myRandomAccessFile)
            throws IOException;

    public void write(RandomAccessFile myRandomAccessFile, int blockSize)
            throws IOException {
        byte rawBytes[] = getBytesWithoutDescriptorTag();

        DescriptorTag.DescriptorCRCLength = rawBytes.length;
        DescriptorTag.DescriptorCRC = Checksum.cksum(rawBytes);

        DescriptorTag.write(myRandomAccessFile);

        myRandomAccessFile.write(rawBytes);

        int bytesWritten = rawBytes.length + 16;

        byte emptyBytesInBlock[] = new byte[blockSize - bytesWritten];
        myRandomAccessFile.write(emptyBytesInBlock);
    }

    public byte[] getBytes(int blockSize) {
        byte bytesWithoutDescriptorTag[] = getBytesWithoutDescriptorTag();

        DescriptorTag.DescriptorCRCLength = bytesWithoutDescriptorTag.length;
        DescriptorTag.DescriptorCRC = Checksum.cksum(bytesWithoutDescriptorTag);

        byte descriptorTagBytes[] = DescriptorTag.getBytes();

        int paddedLength = descriptorTagBytes.length + bytesWithoutDescriptorTag.length;
        if (paddedLength % blockSize != 0) {
            paddedLength += blockSize - (paddedLength % blockSize);
        }

        byte[] rawBytes = new byte[paddedLength];

        int pos = 0;

        System.arraycopy(descriptorTagBytes, 0, rawBytes, pos, descriptorTagBytes.length);
        pos += descriptorTagBytes.length;

        System.arraycopy(bytesWithoutDescriptorTag, 0, rawBytes, pos, bytesWithoutDescriptorTag.length);
        pos += bytesWithoutDescriptorTag.length;

        return rawBytes;
    }
}
