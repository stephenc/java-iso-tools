/*
 *	UDF260Handler.java
 *
 *	2006-07-12
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;


import de.tu_darmstadt.informatik.rbg.bstickler.udflib.SabreUDFElement;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.SabreUDFElement.UDFElementType;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures.ExtendedFileEntry;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures.Short_ad;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures.Timestamp;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.BinaryTools;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;


public class UDF260Handler extends UDF201Handler
{	
	public UDF260Handler( StructureHandler myStructureHandler, ContentHandler myContentHandler )
	{		
		super( myStructureHandler, myContentHandler );
		
		// set version related information
		udfVersionIdentifierSuffix = new byte[]{ 0x60, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		minimumUDFReadRevision = 0x0250;
		minimumUDFWriteRevision = 0x0260;
		maximumUDFWriteRevision = 0x0260;
		descriptorVersion = 3;		
	}
		
	protected void createAndPassMetadataFile()
	throws HandlerException
	{
		InputStream myInputStream = null;
		Calendar recordingTimeCalendar = Calendar.getInstance();
		String applicationIdentifier = "";
		byte[] applicationIdentifierSuffix = new byte[0];		
		long metadataFileLocation = 0;
		long physicalPartitionStartingBlock = 0;
		long metadataPartitionStartingBlock = 0;
		long metadataPartitionEndingBlock = 0;
		byte fileType = 0;
		
		try
		{
			myInputStream = dataReferenceStack.pop().createInputStream();
			fileType = (byte)myInputStream.read();
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			metadataFileLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			physicalPartitionStartingBlock = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			metadataPartitionEndingBlock = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			metadataPartitionStartingBlock = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			DataReference myDataReference = dataReferenceStack.pop();
			myInputStream = myDataReference.createInputStream();
			applicationIdentifierSuffix = BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() );
			myInputStream.close();
			myInputStream = null;
			
			myDataReference = dataReferenceStack.pop();
			myInputStream = myDataReference.createInputStream();
			applicationIdentifier = new String( BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() ) );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			recordingTimeCalendar.setTimeInMillis( BinaryTools.readUInt64AsLong( myInputStream ) );
			myInputStream.close();
			myInputStream = null;			
		}
		catch( Exception myException )
		{
			throw new HandlerException( myException );
		}
		finally
		{
			if( myInputStream != null )
			{
				try
				{
					myInputStream.close();
				}
				catch( IOException myIOException ) {}
			}
		}
		
		ExtendedFileEntry metadataExtendedFileEntry = new ExtendedFileEntry();

		metadataExtendedFileEntry.Uid = 0xFFFFFFFF;
		metadataExtendedFileEntry.Gid = 0xFFFFFFFF;
		
		metadataExtendedFileEntry.AccessTime = new Timestamp( recordingTimeCalendar );
		metadataExtendedFileEntry.ModificationTime = new Timestamp( recordingTimeCalendar );
		metadataExtendedFileEntry.AttributeTime = new Timestamp( recordingTimeCalendar );
		metadataExtendedFileEntry.CreationTime = new Timestamp( recordingTimeCalendar );
		
		metadataExtendedFileEntry.Checkpoint = 1;
	
		try
		{
			metadataExtendedFileEntry.ImplementationIdentifier.setIdentifier( applicationIdentifier );
		}
		catch( Exception myException )
		{
			throw new HandlerException( myException );
		}
		
		metadataExtendedFileEntry.ImplementationIdentifier.IdentifierSuffix = applicationIdentifierSuffix;
				
		metadataExtendedFileEntry.ICBTag.Flags = 0;									// storage type short_ad		
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
		metadataExtendedFileEntry.LengthofAllocationDescriptors = metadataExtendedFileEntry.AllocationDescriptors.length;		

		metadataExtendedFileEntry.DescriptorTag.TagLocation = metadataFileLocation;
		metadataExtendedFileEntry.ICBTag.FileType = fileType;

		/*
		// full element with descriptor tag
		metadataExtendedFileEntry.DescriptorTag.TagSerialNumber = tagSerialNumber;
		metadataExtendedFileEntry.DescriptorTag.DescriptorVersion = descriptorVersion;
		super.data( new ByteArrayDataReference( metadataExtendedFileEntry.getBytes( blockSize ) ) );
		*/
				
		// without descriptor tag (handled in next pipeline section)
		super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
		super.data( new WordDataReference( 266 ) );							// tag identifier
		super.data( new WordDataReference( metadataFileLocation ) );		// tag location
		super.data( new WordDataReference( tagSerialNumber ) );				// tag serial number
		super.data( new WordDataReference( descriptorVersion ) );			// descriptor version
		super.data( new ByteArrayDataReference( metadataExtendedFileEntry.getBytesWithoutDescriptorTag() ) );
		super.endElement();		
	}
	
}
