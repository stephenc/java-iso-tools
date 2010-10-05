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

public class ImplementationUseVolumeDescriptor extends VolumeDescriptorSequenceItem {

    public EntityID ImplementationIdentifier;            // struct EntityID
    //public byte			ImplementationUse[];				// byte[460]
    public LVInformation ImplementationUse;

    public ImplementationUseVolumeDescriptor() {
        DescriptorTag = new Tag();
        DescriptorTag.TagIdentifier = 4;

        ImplementationIdentifier = new EntityID();

        //ImplementationUse = new byte[460];
        ImplementationUse = new LVInformation();
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        DescriptorTag = new Tag();
        DescriptorTag.read(myRandomAccessFile);

        VolumeDescriptorSequenceNumber = BinaryTools.readUInt32AsLong(myRandomAccessFile);

        ImplementationIdentifier = new EntityID();
        ImplementationIdentifier.read(myRandomAccessFile);

        //ImplementationUse = new byte[460];
        //myRandomAccessFile.read( ImplementationUse );

        ImplementationUse = new LVInformation();
        ImplementationUse.read(myRandomAccessFile);
    }

    public byte[] getBytesWithoutDescriptorTag() {
        byte ImplementationIdentifierBytes[] = ImplementationIdentifier.getBytes();
        byte ImplementationUseBytes[] = ImplementationUse.getBytes();

        byte rawBytes[] = new byte[4
                + ImplementationIdentifierBytes.length
                + ImplementationUseBytes.length];

        int pos = 0;

        pos = BinaryTools.getUInt32BytesFromLong(VolumeDescriptorSequenceNumber, rawBytes, pos);

        System.arraycopy(ImplementationIdentifierBytes, 0, rawBytes, pos, ImplementationIdentifierBytes.length);
        pos += ImplementationIdentifierBytes.length;

        //System.arraycopy( ImplementationUse, 0, rawBytes, pos, ImplementationUse.length );
        System.arraycopy(ImplementationUseBytes, 0, rawBytes, pos, ImplementationUseBytes.length);

        return rawBytes;
    }
}
