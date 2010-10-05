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

public class UnallocatedSpaceDescriptor extends VolumeDescriptorSequenceItem {

    public long NumberofAllocationDescriptors;                // Uint32
    public Extend_ad AllocationDescriptors[];                    // struct extend_ad[]

    public UnallocatedSpaceDescriptor() {
        DescriptorTag = new Tag();
        DescriptorTag.TagIdentifier = 7;

        AllocationDescriptors = new Extend_ad[0];
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        DescriptorTag = new Tag();
        DescriptorTag.read(myRandomAccessFile);

        VolumeDescriptorSequenceNumber = BinaryTools.readUInt32AsLong(myRandomAccessFile);
        NumberofAllocationDescriptors = BinaryTools.readUInt32AsLong(myRandomAccessFile);

        AllocationDescriptors = new Extend_ad[(int) NumberofAllocationDescriptors];

        for (int i = 0; i < NumberofAllocationDescriptors; ++i) {
            AllocationDescriptors[i] = new Extend_ad();
            AllocationDescriptors[i].read(myRandomAccessFile);
        }
    }

    public byte[] getBytesWithoutDescriptorTag() {
        byte rawBytes[] = new byte[8 + (int) NumberofAllocationDescriptors * 8];

        int pos = 0;

        pos = BinaryTools.getUInt32BytesFromLong(VolumeDescriptorSequenceNumber, rawBytes, pos);
        pos = BinaryTools.getUInt32BytesFromLong(NumberofAllocationDescriptors, rawBytes, pos);

        for (int i = 0; i < AllocationDescriptors.length; ++i) {
            byte allocationDescriptorBytes[] = AllocationDescriptors[i].getBytes();
            System.arraycopy(allocationDescriptorBytes, 0, rawBytes, pos, allocationDescriptorBytes.length);
            pos += allocationDescriptorBytes.length;
        }

        return rawBytes;
    }
}
