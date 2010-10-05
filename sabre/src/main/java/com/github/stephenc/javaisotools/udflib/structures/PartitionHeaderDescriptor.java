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

public class PartitionHeaderDescriptor {

    public Short_ad UnallocatedSpaceTable;        // struct short_ad
    public Short_ad UnallocatedSpaceBitmap;        // struct short_ad
    public Short_ad PartitionIntegrityTable;    // struct short_ad
    public Short_ad FreedSpaceTable;            // struct short_ad
    public Short_ad FreedSpaceBitmap;            // struct short_ad
    public byte Reserved[];                    // byte[88]

    public PartitionHeaderDescriptor() {
        UnallocatedSpaceTable = new Short_ad();
        UnallocatedSpaceBitmap = new Short_ad();
        PartitionIntegrityTable = new Short_ad();
        FreedSpaceTable = new Short_ad();
        FreedSpaceBitmap = new Short_ad();
        Reserved = new byte[88];
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        UnallocatedSpaceTable = new Short_ad();
        UnallocatedSpaceTable.read(myRandomAccessFile);

        UnallocatedSpaceBitmap = new Short_ad();
        UnallocatedSpaceBitmap.read(myRandomAccessFile);

        PartitionIntegrityTable = new Short_ad();
        PartitionIntegrityTable.read(myRandomAccessFile);

        FreedSpaceTable = new Short_ad();
        FreedSpaceTable.read(myRandomAccessFile);

        FreedSpaceBitmap = new Short_ad();
        FreedSpaceBitmap.read(myRandomAccessFile);

        Reserved = new byte[88];
        myRandomAccessFile.read(Reserved);
    }

    public byte[] getBytes() {
        byte UnallocatedSpaceTableBytes[] = UnallocatedSpaceTable.getBytes();
        byte UnallocatedSpaceBitmapBytes[] = UnallocatedSpaceBitmap.getBytes();
        byte PartitionIntegrityTableBytes[] = PartitionIntegrityTable.getBytes();
        byte FreedSpaceTableByte[] = FreedSpaceTable.getBytes();
        byte FreedSpaceBitmapByte[] = FreedSpaceBitmap.getBytes();

        byte rawBytes[] = new byte[88
                + UnallocatedSpaceTableBytes.length
                + UnallocatedSpaceBitmapBytes.length
                + PartitionIntegrityTableBytes.length
                + FreedSpaceTableByte.length
                + FreedSpaceBitmapByte.length];

        int pos = 0;

        System.arraycopy(UnallocatedSpaceTableBytes, 0, rawBytes, pos, UnallocatedSpaceTableBytes.length);
        pos += UnallocatedSpaceTableBytes.length;

        System.arraycopy(UnallocatedSpaceBitmapBytes, 0, rawBytes, pos, UnallocatedSpaceBitmapBytes.length);
        pos += UnallocatedSpaceBitmapBytes.length;

        System.arraycopy(PartitionIntegrityTableBytes, 0, rawBytes, pos, PartitionIntegrityTableBytes.length);
        pos += PartitionIntegrityTableBytes.length;

        System.arraycopy(FreedSpaceTableByte, 0, rawBytes, pos, FreedSpaceTableByte.length);
        pos += FreedSpaceTableByte.length;

        System.arraycopy(FreedSpaceBitmapByte, 0, rawBytes, pos, FreedSpaceBitmapByte.length);
        pos += FreedSpaceBitmapByte.length;

        System.arraycopy(Reserved, 0, rawBytes, pos, Reserved.length);
        pos += Reserved.length;

        return rawBytes;
    }

}
