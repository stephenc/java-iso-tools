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

package com.github.stephenc.javaisotools.udflib;

import com.github.stephenc.javaisotools.sabre.Element;

public class SabreUDFElement extends Element {

    private UDFElementType udfElementType;

    public enum UDFElementType {

        EmptyArea,
        ReservedArea,
        VolumeRecognitionSequence,
        AnchorVolumeDescriptorPointer,
        PrimaryVolumeDescriptor,
        LogicalVolumeDescriptor,
        PartitionDescriptor,
        ImplementationUseVolumeDescriptor,
        UnallocatedSpaceDescriptor,
        TerminatingDescriptor,
        FileSetDescriptor,
        LogicalVolumeIntegrityDescriptor,
        FileEntry,
        RawFileData,
        MetadataFile,

        DescriptorTag        // not used on "frontend"
    }

    public SabreUDFElement(UDFElementType udfElementType) {
        this.udfElementType = udfElementType;
    }

    public Object getId() {
        return udfElementType;
    }

}
