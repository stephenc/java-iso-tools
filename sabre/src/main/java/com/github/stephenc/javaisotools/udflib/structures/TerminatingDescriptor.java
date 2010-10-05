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

public class TerminatingDescriptor {

    public Tag DescriptorTag;        // struct tag
    public byte Reserved[];            // byte[496]

    public TerminatingDescriptor() {
        DescriptorTag = new Tag();
        DescriptorTag.TagIdentifier = 8;

        Reserved = new byte[496];
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        DescriptorTag = new Tag();
        DescriptorTag.read(myRandomAccessFile);

        Reserved = new byte[496];
        myRandomAccessFile.read(Reserved);
    }

    public void write(RandomAccessFile myRandomAccessFile, int blockSize)
            throws IOException {
        DescriptorTag.DescriptorCRCLength = Reserved.length;
        DescriptorTag.DescriptorCRC = Checksum.cksum(Reserved);

        DescriptorTag.write(myRandomAccessFile);

        myRandomAccessFile.write(Reserved);

        int bytesWritten = Reserved.length + 16;

        byte emptyBytesInBlock[] = new byte[blockSize - bytesWritten];
        myRandomAccessFile.write(emptyBytesInBlock);
    }

    public byte[] getBytes(int blockSize) {
        DescriptorTag.DescriptorCRCLength = Reserved.length;
        DescriptorTag.DescriptorCRC = Checksum.cksum(Reserved);

        byte descriptorTagBytes[] = DescriptorTag.getBytes();

        int paddedLength = descriptorTagBytes.length + Reserved.length;
        if (paddedLength % blockSize != 0) {
            paddedLength += blockSize - (paddedLength % blockSize);
        }

        byte[] rawBytes = new byte[paddedLength];

        int pos = 0;

        System.arraycopy(descriptorTagBytes, 0, rawBytes, pos, descriptorTagBytes.length);
        pos += descriptorTagBytes.length;

        System.arraycopy(Reserved, 0, rawBytes, pos, Reserved.length);
        pos += Reserved.length;

        return rawBytes;
    }

    public byte[] getBytesWithoutDescriptorTag() {
        return Reserved;
    }
}
