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

package com.github.stephenc.javaisotools.udflib.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import com.github.stephenc.javaisotools.sabre.ContentHandler;
import com.github.stephenc.javaisotools.sabre.DataReference;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.impl.ByteArrayDataReference;
import com.github.stephenc.javaisotools.sabre.impl.WordDataReference;
import com.github.stephenc.javaisotools.udflib.SabreUDFElement;
import com.github.stephenc.javaisotools.udflib.structures.Short_ad;
import com.github.stephenc.javaisotools.udflib.structures.Timestamp;
import com.github.stephenc.javaisotools.udflib.tools.BinaryTools;
import com.github.stephenc.javaisotools.udflib.structures.ExtendedFileEntry;
import com.github.stephenc.javaisotools.sabre.StructureHandler;

public class UDF260Handler extends UDF201Handler {

    public UDF260Handler(StructureHandler myStructureHandler, ContentHandler myContentHandler) {
        super(myStructureHandler, myContentHandler);

        // set version related information
        udfVersionIdentifierSuffix = new byte[]{0x60, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        minimumUDFReadRevision = 0x0250;
        minimumUDFWriteRevision = 0x0260;
        maximumUDFWriteRevision = 0x0260;
        descriptorVersion = 3;
    }

    protected void createAndPassMetadataFile()
            throws HandlerException {
        InputStream myInputStream = null;
        Calendar recordingTimeCalendar = Calendar.getInstance();
        String applicationIdentifier = "";
        byte[] applicationIdentifierSuffix = new byte[0];
        long metadataFileLocation = 0;
        long physicalPartitionStartingBlock = 0;
        long metadataPartitionStartingBlock = 0;
        long metadataPartitionEndingBlock = 0;
        byte fileType = 0;

        try {
            myInputStream = dataReferenceStack.pop().createInputStream();
            fileType = (byte) myInputStream.read();
            myInputStream.close();
            myInputStream = null;

            myInputStream = dataReferenceStack.pop().createInputStream();
            metadataFileLocation = BinaryTools.readUInt32AsLong(myInputStream);
            myInputStream.close();
            myInputStream = null;

            myInputStream = dataReferenceStack.pop().createInputStream();
            physicalPartitionStartingBlock = BinaryTools.readUInt32AsLong(myInputStream);
            myInputStream.close();
            myInputStream = null;

            myInputStream = dataReferenceStack.pop().createInputStream();
            metadataPartitionEndingBlock = BinaryTools.readUInt32AsLong(myInputStream);
            myInputStream.close();
            myInputStream = null;

            myInputStream = dataReferenceStack.pop().createInputStream();
            metadataPartitionStartingBlock = BinaryTools.readUInt32AsLong(myInputStream);
            myInputStream.close();
            myInputStream = null;

            DataReference myDataReference = dataReferenceStack.pop();
            myInputStream = myDataReference.createInputStream();
            applicationIdentifierSuffix = BinaryTools.readByteArray(myInputStream, (int) myDataReference.getLength());
            myInputStream.close();
            myInputStream = null;

            myDataReference = dataReferenceStack.pop();
            myInputStream = myDataReference.createInputStream();
            applicationIdentifier =
                    new String(BinaryTools.readByteArray(myInputStream, (int) myDataReference.getLength()));
            myInputStream.close();
            myInputStream = null;

            myInputStream = dataReferenceStack.pop().createInputStream();
            recordingTimeCalendar.setTimeInMillis(BinaryTools.readUInt64AsLong(myInputStream));
            myInputStream.close();
            myInputStream = null;
        }
        catch (Exception myException) {
            throw new HandlerException(myException);
        }
        finally {
            if (myInputStream != null) {
                try {
                    myInputStream.close();
                }
                catch (IOException myIOException) {
                }
            }
        }

        ExtendedFileEntry metadataExtendedFileEntry = new ExtendedFileEntry();

        metadataExtendedFileEntry.Uid = 0xFFFFFFFF;
        metadataExtendedFileEntry.Gid = 0xFFFFFFFF;

        metadataExtendedFileEntry.AccessTime = new Timestamp(recordingTimeCalendar);
        metadataExtendedFileEntry.ModificationTime = new Timestamp(recordingTimeCalendar);
        metadataExtendedFileEntry.AttributeTime = new Timestamp(recordingTimeCalendar);
        metadataExtendedFileEntry.CreationTime = new Timestamp(recordingTimeCalendar);

        metadataExtendedFileEntry.Checkpoint = 1;

        try {
            metadataExtendedFileEntry.ImplementationIdentifier.setIdentifier(applicationIdentifier);
        }
        catch (Exception myException) {
            throw new HandlerException(myException);
        }

        metadataExtendedFileEntry.ImplementationIdentifier.IdentifierSuffix = applicationIdentifierSuffix;

        metadataExtendedFileEntry.ICBTag.Flags = 0;                                    // storage type short_ad
        metadataExtendedFileEntry.ICBTag.PriorRecordedNumberofDirectEntries = 0;
        metadataExtendedFileEntry.ICBTag.NumberofEntries = 1;
        metadataExtendedFileEntry.ICBTag.StrategyType = 4;

        long metadataFileLength = (metadataPartitionEndingBlock - metadataPartitionStartingBlock);

        Short_ad metadataAllocationDescriptor = new Short_ad();
        metadataAllocationDescriptor.ExtentPosition = metadataPartitionStartingBlock - physicalPartitionStartingBlock;
        metadataAllocationDescriptor.ExtentLength = metadataFileLength * blockSize;

        metadataExtendedFileEntry.LogicalBlocksRecorded = metadataFileLength;
        metadataExtendedFileEntry.InformationLength = metadataFileLength * blockSize;
        metadataExtendedFileEntry.ObjectSize = metadataFileLength * blockSize;
        metadataExtendedFileEntry.AllocationDescriptors = metadataAllocationDescriptor.getBytes();
        metadataExtendedFileEntry.LengthofAllocationDescriptors =
                metadataExtendedFileEntry.AllocationDescriptors.length;

        metadataExtendedFileEntry.DescriptorTag.TagLocation = metadataFileLocation;
        metadataExtendedFileEntry.ICBTag.FileType = fileType;

        /*
          // full element with descriptor tag
          metadataExtendedFileEntry.DescriptorTag.TagSerialNumber = tagSerialNumber;
          metadataExtendedFileEntry.DescriptorTag.DescriptorVersion = descriptorVersion;
          super.data( new ByteArrayDataReference( metadataExtendedFileEntry.getBytes( blockSize ) ) );
          */

        // without descriptor tag (handled in next pipeline section)
        super.startElement(new SabreUDFElement(SabreUDFElement.UDFElementType.DescriptorTag));
        super.data(new WordDataReference(266));                            // tag identifier
        super.data(new WordDataReference(metadataFileLocation));        // tag location
        super.data(new WordDataReference(tagSerialNumber));                // tag serial number
        super.data(new WordDataReference(descriptorVersion));            // descriptor version
        super.data(new ByteArrayDataReference(metadataExtendedFileEntry.getBytesWithoutDescriptorTag()));
        super.endElement();
    }

}
