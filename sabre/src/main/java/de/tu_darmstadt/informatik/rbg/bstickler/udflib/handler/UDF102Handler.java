/*
 *	UDF102Handler.java
 *
 *	2006-07-06
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.handler;

import java.io.*;
import java.util.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.SabreUDFElement;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.SabreUDFElement.UDFElementType;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures.*;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;


public class UDF102Handler extends ChainingStreamHandler
{
	protected int blockSize = 2048;
	protected long maximumAllocationLength = 1073739776;
	protected int tagSerialNumber = 1;
	
	// version related information
	protected byte udfVersionIdentifierSuffix[] = new byte[]{ 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
	protected int minimumUDFReadRevision = 0x0102;
	protected int minimumUDFWriteRevision = 0x0102;
	protected int maximumUDFWriteRevision = 0x0102;
	protected int descriptorVersion = 2;
	
	protected Stack<Element> elementStack;
	protected Stack<DataReference> dataReferenceStack;
	
	public UDF102Handler( StructureHandler myStructureHandler, ContentHandler myContentHandler )
	{
		super( myStructureHandler, myContentHandler );

		elementStack = new Stack<Element>();
		dataReferenceStack = new Stack<DataReference>();
	}
	
	public void startDocument()
	throws HandlerException
	{
		super.startDocument();
	}
	
	public void endDocument()
	throws HandlerException
	{
		super.endDocument();
	}
	
	public void startElement( Element myElement )
	throws HandlerException
	{
		elementStack.push( myElement );
		super.startElement( myElement );
	}

	public void endElement()
	throws HandlerException
	{
		Element myElement = elementStack.pop();
		
		// handle volume recognition sequence
		if( myElement.getId() == UDFElementType.VolumeRecognitionSequence )
		{
			createAndPassVRS();
		}
		// handle anchor volume descriptor pointer
		else if( myElement.getId() == UDFElementType.AnchorVolumeDescriptorPointer )
		{
			createAndPassAVDP();
		}
		// handle primary volume descriptor
		else if( myElement.getId() == UDFElementType.PrimaryVolumeDescriptor )
		{
			createAndPassPVD();
		}
		// handle partition descriptor
		else if( myElement.getId() == UDFElementType.PartitionDescriptor )
		{
			createAndPassPD();
		}
		// handle logical volume descriptor
		else if( myElement.getId() == UDFElementType.LogicalVolumeDescriptor )
		{
			createAndPassLVD();
		}		
		// handle unallocated space descriptor
		else if( myElement.getId() == UDFElementType.UnallocatedSpaceDescriptor )
		{
			createAndPassUSD();
		}		
		// handle implementation use volume descriptor
		else if( myElement.getId() == UDFElementType.ImplementationUseVolumeDescriptor )
		{
			createAndPassIUVD();
		}		
		// handle terminationg descriptor
		else if( myElement.getId() == UDFElementType.TerminatingDescriptor )
		{
			createAndPassTD();
		}		
		// handle logical volume integrity descriptor
		else if( myElement.getId() == UDFElementType.LogicalVolumeIntegrityDescriptor )
		{
			createAndPassLVID();
		}		
		// handle fileset descriptor
		else if( myElement.getId() == UDFElementType.FileSetDescriptor )
		{
			createAndPassFSD();
		}		
		// handle file entry
		else if( myElement.getId() == UDFElementType.FileEntry )
		{
			createAndPassFE();
		}		
		// handle metadata file 
		else if( myElement.getId() == UDFElementType.MetadataFile )
		{
			createAndPassMetadataFile();
		}
		
		super.endElement();
	}
	
	public void data( DataReference myDataReference )
	throws HandlerException
	{
		// handle anchor volume descriptor pointer data
		if( ( elementStack.size() > 0 ) && ( 
			( elementStack.peek().getId() == UDFElementType.AnchorVolumeDescriptorPointer )
		 || ( elementStack.peek().getId() == UDFElementType.PrimaryVolumeDescriptor )
		 || ( elementStack.peek().getId() == UDFElementType.PartitionDescriptor )		
		 || ( elementStack.peek().getId() == UDFElementType.LogicalVolumeDescriptor )
		 || ( elementStack.peek().getId() == UDFElementType.UnallocatedSpaceDescriptor )
		 || ( elementStack.peek().getId() == UDFElementType.ImplementationUseVolumeDescriptor )
		 || ( elementStack.peek().getId() == UDFElementType.TerminatingDescriptor )
		 || ( elementStack.peek().getId() == UDFElementType.LogicalVolumeIntegrityDescriptor )
		 || ( elementStack.peek().getId() == UDFElementType.FileSetDescriptor )
 		 || ( elementStack.peek().getId() == UDFElementType.FileEntry )
		 || ( elementStack.peek().getId() == UDFElementType.MetadataFile ) ) )
		{
			dataReferenceStack.push( myDataReference );
		}
		else
		{
			super.data( myDataReference );
		}
	}
	
	protected void createAndPassVRS()
	throws HandlerException
	{
		VolumeRecognitionSequence myVolumeRecognitionSequene = new VolumeRecognitionSequence( VolumeRecognitionSequence.NSRVersion.NSR02 );
		super.data( new ByteArrayDataReference( myVolumeRecognitionSequene.getBytes() ) );
	}
	
	protected void createAndPassAVDP()
	throws HandlerException
	{
		InputStream myInputStream = null;
		long tagLocation = 0;
		long MVDSBlock = 0;
		long RVDSBlock = 0;
		
		try
		{			
			myInputStream = dataReferenceStack.pop().createInputStream();
			RVDSBlock = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			MVDSBlock = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;			
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			tagLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
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
		
		AnchorVolumeDescriptorPointer myAnchorVolumeDescriptorPointer = new AnchorVolumeDescriptorPointer();
				
		myAnchorVolumeDescriptorPointer.MainVolumeDescriptorSequenceExtend.len = 16 * blockSize;
		myAnchorVolumeDescriptorPointer.MainVolumeDescriptorSequenceExtend.loc = MVDSBlock;
		
		myAnchorVolumeDescriptorPointer.ReserveVolumeDescriptorSequenceExtend.len = 16 * blockSize;
		myAnchorVolumeDescriptorPointer.ReserveVolumeDescriptorSequenceExtend.loc = RVDSBlock;
		
		/*
		// full element with descriptor tag
		myAnchorVolumeDescriptorPointer.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myAnchorVolumeDescriptorPointer.DescriptorTag.DescriptorVersion = descriptorVersion; 
		myAnchorVolumeDescriptorPointer.DescriptorTag.TagLocation = tagLocation;
		super.data( new ByteArrayDataReference( myAnchorVolumeDescriptorPointer.getBytes( blockSize ) ) );
		*/
		
		// without descriptor tag (handled in next pipeline section)
		super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
		super.data( new WordDataReference( 2 ) );					// tag identifier
		super.data( new WordDataReference( tagLocation ) );			// tag location
		super.data( new WordDataReference( tagSerialNumber ) );		// tag serial number
		super.data( new WordDataReference( descriptorVersion ) );	// descriptor version
		super.data( new ByteArrayDataReference( myAnchorVolumeDescriptorPointer.getBytesWithoutDescriptorTag() ) );
		super.endElement();
	}

	protected void createAndPassPVD()
	throws HandlerException
	{
		InputStream myInputStream = null;
		long tagLocation = 0;
		long volumeDescriptorSequenceNumber = 0;
		Calendar recordingTimeCalendar = Calendar.getInstance();
		String imageIdentifier = "";
		String applicationIdentifier = "";
		byte[] applicationIdentifierSuffix = new byte[0];
		
		try
		{
			DataReference myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			applicationIdentifierSuffix  = BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() );
			myInputStream.close();
			myInputStream = null;			

			myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			applicationIdentifier = new String( BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() ) );
			myInputStream.close();
			myInputStream = null;

			myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			imageIdentifier = new String( BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() ) );
			myInputStream.close();
			myInputStream = null;			
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			recordingTimeCalendar.setTimeInMillis( BinaryTools.readUInt64AsLong( myInputStream ) );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			volumeDescriptorSequenceNumber = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			tagLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
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
		
		PrimaryVolumeDescriptor myPrimaryVolumeDescriptor = new PrimaryVolumeDescriptor();
				
		myPrimaryVolumeDescriptor.VolumeDescriptorSequenceNumber = volumeDescriptorSequenceNumber;
		myPrimaryVolumeDescriptor.PrimaryVolumeDescriptorNumber = 0;				
		myPrimaryVolumeDescriptor.VolumeSequenceNumber = 1;
		myPrimaryVolumeDescriptor.MaximumVolumeSequenceNumber = 1;
		myPrimaryVolumeDescriptor.InterchangeLevel = 2;
		myPrimaryVolumeDescriptor.MaximumInterchangeLevel = 3;
		myPrimaryVolumeDescriptor.CharacterSetList = 1;
		myPrimaryVolumeDescriptor.MaximumCharacterSetList = 1;
				
		String volumeSetIdentifier =  Long.toHexString( recordingTimeCalendar.getTimeInMillis() ) + " " + imageIdentifier;
		
		try
		{
			myPrimaryVolumeDescriptor.setVolumeIdentifier( imageIdentifier );
			myPrimaryVolumeDescriptor.setVolumeSetIdentifier( volumeSetIdentifier );
			myPrimaryVolumeDescriptor.ApplicationIdentifier.setIdentifier( applicationIdentifier );
			myPrimaryVolumeDescriptor.ImplementationIdentifier.setIdentifier( applicationIdentifier );
		}
		catch( Exception myException )
		{
			throw new HandlerException( myException );
		}		
		
		myPrimaryVolumeDescriptor.ApplicationIdentifier.IdentifierSuffix = applicationIdentifierSuffix;
		
		myPrimaryVolumeDescriptor.RecordingDateandTime.set( recordingTimeCalendar );		
		
		myPrimaryVolumeDescriptor.PredecessorVolumeDescriptorSequenceLocation = 0;
		myPrimaryVolumeDescriptor.Flags = 1;
		
		/*
		// full element with descriptor tag
		myPrimaryVolumeDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myPrimaryVolumeDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myPrimaryVolumeDescriptor.DescriptorTag.TagLocation = tagLocation;
		super.data( new ByteArrayDataReference( myPrimaryVolumeDescriptor.getBytes( blockSize ) ) );
		*/
		
		// without descriptor tag (handled in next pipeline section)
		super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
		super.data( new WordDataReference( 1 ) );					// tag identifier
		super.data( new WordDataReference( tagLocation ) );			// tag location
		super.data( new WordDataReference( tagSerialNumber ) );		// tag serial number
		super.data( new WordDataReference( descriptorVersion ) );	// descriptor version
		super.data( new ByteArrayDataReference( myPrimaryVolumeDescriptor.getBytesWithoutDescriptorTag() ) );
		super.endElement();
	}
	
	protected void createAndPassPD()
	throws HandlerException
	{
		InputStream myInputStream = null;
		long tagLocation = 0;
		long volumeDescriptorSequenceNumber = 0;
		long partitionStartingBlock = 0;
		long partitionEndingBlock = 0;
		String applicationIdentifier = "";
		byte[] applicationIdentifierSuffix = new byte[0];		
		
		try
		{
			DataReference myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			applicationIdentifierSuffix  = BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() );
			myInputStream.close();
			myInputStream = null;			

			myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			applicationIdentifier = new String( BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() ) );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			partitionEndingBlock = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			partitionStartingBlock = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;			
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			volumeDescriptorSequenceNumber = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			tagLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
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
		
		PartitionDescriptor myPartitionDescriptor = new PartitionDescriptor();
				
		myPartitionDescriptor.VolumeDescriptorSequenceNumber = volumeDescriptorSequenceNumber;
		myPartitionDescriptor.PartitionFlags = 1;
		myPartitionDescriptor.PartitionNumber = 0;
		
		try
		{
			myPartitionDescriptor.PartitionContents.setIdentifier( "+NSR02" );
			myPartitionDescriptor.ImplementationIdentifier.setIdentifier( applicationIdentifier );
		}
		catch( Exception myException )
		{
			throw new HandlerException( myException );
		}
		
		myPartitionDescriptor.AccessType = 1; // read only
		myPartitionDescriptor.PartitonStartingLocation = partitionStartingBlock;
		myPartitionDescriptor.PartitionLength = partitionEndingBlock - partitionStartingBlock;		
		myPartitionDescriptor.ImplementationIdentifier.IdentifierSuffix = applicationIdentifierSuffix;

		
		/*
		// full element with descriptor tag		
		myPartitionDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myPartitionDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myPartitionDescriptor.DescriptorTag.TagLocation = tagLocation;
		super.data( new ByteArrayDataReference( myPartitionDescriptor.getBytes( blockSize ) ) );		
		*/
		
		// without descriptor tag (handled in next pipeline section)
		super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
		super.data( new WordDataReference( 5 ) );					// tag identifier
		super.data( new WordDataReference( tagLocation ) );			// tag location
		super.data( new WordDataReference( tagSerialNumber ) );		// tag serial number
		super.data( new WordDataReference( descriptorVersion ) );	// descriptor version
		super.data( new ByteArrayDataReference( myPartitionDescriptor.getBytesWithoutDescriptorTag() ) );
		super.endElement();		
	}
	
	protected void createAndPassLVD()
	throws HandlerException
	{
		InputStream myInputStream = null;
		long tagLocation = 0;
		long volumeDescriptorSequenceNumber = 0;
		long fileSetDescriptorLocation = 0;
		int  fileSetDescriptorPartition = 0;
		long mirrorMetadataFileLocation = 0;
		long mainMetadataFileLocation = 0;
		int  metadataAlignmentUnitSize = 0;
		int  metadataAllocationUnitSize = 0;
		long logicalVolumeIntegritySequenceStartingBlock = 0;
		long logicalVolumeIntegritySequenceEndingBlock = 0;
		String applicationIdentifier = "";
		byte[] applicationIdentifierSuffix = new byte[0];
		String imageIdentifier;
		
		try
		{
			DataReference myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			imageIdentifier = new String( BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() ) );
			myInputStream.close();
			myInputStream = null;
			
			myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			applicationIdentifierSuffix  = BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() );
			myInputStream.close();
			myInputStream = null;			

			myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			applicationIdentifier = new String( BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() ) );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			fileSetDescriptorLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			fileSetDescriptorPartition = (int)BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			mirrorMetadataFileLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			mainMetadataFileLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			metadataAlignmentUnitSize = (int)BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			metadataAllocationUnitSize = (int)BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			logicalVolumeIntegritySequenceEndingBlock = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			logicalVolumeIntegritySequenceStartingBlock = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			volumeDescriptorSequenceNumber = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			tagLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
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
		
		LogicalVolumeDescriptor myLogicalVolumeDescriptor = new LogicalVolumeDescriptor();
					
		try
		{		
			
	
			myLogicalVolumeDescriptor.VolumeDescriptorSequenceNumber = volumeDescriptorSequenceNumber;
			myLogicalVolumeDescriptor.setLogicalVolumeIdentifier( imageIdentifier );
			myLogicalVolumeDescriptor.LogicalBlockSize = blockSize;
			
			myLogicalVolumeDescriptor.DomainIdentifier.setIdentifier( "*OSTA UDF Compliant" );
			myLogicalVolumeDescriptor.DomainIdentifier.IdentifierSuffix = udfVersionIdentifierSuffix;
			
			myLogicalVolumeDescriptor.LogicalVolumeContentsUse.ExtentLength = blockSize;
			myLogicalVolumeDescriptor.LogicalVolumeContentsUse.ExtentLocation.part_num = fileSetDescriptorPartition;
			myLogicalVolumeDescriptor.LogicalVolumeContentsUse.ExtentLocation.lb_num = fileSetDescriptorLocation;
				
			myLogicalVolumeDescriptor.ImplementationIdentifier.setIdentifier( applicationIdentifier );
			myLogicalVolumeDescriptor.ImplementationIdentifier.IdentifierSuffix = applicationIdentifierSuffix;
	
			// partition map type 1, length 6, volume sequence number 0, partition number 0
			PartitionMapType1 myPartitionMapType1 = new PartitionMapType1();		
			byte myPartitionMapType1Bytes[] = myPartitionMapType1.getBytes();		
			
			if( mainMetadataFileLocation > 0 )
			{			
				PartitionMapType2 myPartitionMapType2 = new PartitionMapType2();
				EntityID partitionTypeIdentifier = new EntityID();
				partitionTypeIdentifier.setIdentifier( "*UDF Metadata Partition" );
				partitionTypeIdentifier.IdentifierSuffix = udfVersionIdentifierSuffix;
				myPartitionMapType2.setupMetadataPartitionMap( partitionTypeIdentifier, 1, 0, mainMetadataFileLocation, mirrorMetadataFileLocation, 0xFFFFFFFF, metadataAllocationUnitSize, metadataAlignmentUnitSize, (byte)0 );
				byte myPartitionMapType2Bytes[] = myPartitionMapType2.getBytes();
				
				myLogicalVolumeDescriptor.NumberofPartitionMaps = 2;			
				myLogicalVolumeDescriptor.PartitionMaps = new byte[ myPartitionMapType1Bytes.length + myPartitionMapType2Bytes.length ]; 
				
				System.arraycopy( myPartitionMapType1Bytes, 0, myLogicalVolumeDescriptor.PartitionMaps, 0, myPartitionMapType1Bytes.length );
				System.arraycopy( myPartitionMapType2Bytes, 0, myLogicalVolumeDescriptor.PartitionMaps, 6, myPartitionMapType2Bytes.length );
			}
			else
			{
				myLogicalVolumeDescriptor.NumberofPartitionMaps = 1;
				myLogicalVolumeDescriptor.PartitionMaps = myPartitionMapType1Bytes;
			}
		}
		catch( Exception myException )
		{
			throw new HandlerException( myException );
		}
		
		myLogicalVolumeDescriptor.MapTableLength = myLogicalVolumeDescriptor.PartitionMaps.length;
		
		myLogicalVolumeDescriptor.IntegritySequenceExtent.loc = logicalVolumeIntegritySequenceStartingBlock;
		myLogicalVolumeDescriptor.IntegritySequenceExtent.len = (logicalVolumeIntegritySequenceEndingBlock - logicalVolumeIntegritySequenceStartingBlock) * blockSize;

		/*
		// full element with descriptor tag				
		myLogicalVolumeDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myLogicalVolumeDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myLogicalVolumeDescriptor.DescriptorTag.TagLocation = tagLocation;
		super.data( new ByteArrayDataReference( myLogicalVolumeDescriptor.getBytes( blockSize ) ) );
		*/
		
		// without descriptor tag (handled in next pipeline section)
		super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
		super.data( new WordDataReference( 6 ) );					// tag identifier
		super.data( new WordDataReference( tagLocation ) );			// tag location
		super.data( new WordDataReference( tagSerialNumber ) );		// tag serial number
		super.data( new WordDataReference( descriptorVersion ) );	// descriptor version
		super.data( new ByteArrayDataReference( myLogicalVolumeDescriptor.getBytesWithoutDescriptorTag() ) );
		super.endElement();
	}

	protected void createAndPassUSD()
	throws HandlerException
	{
		InputStream myInputStream = null;
		long tagLocation = 0;
		long volumeDescriptorSequenceNumber = 0;
		long unallocatedSpaceStartBlock = 0;
		long unallocatedSpaceEndBlock = 0;
		
		try
		{
			myInputStream = dataReferenceStack.pop().createInputStream();
			unallocatedSpaceEndBlock = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;			

			myInputStream = dataReferenceStack.pop().createInputStream();
			unallocatedSpaceStartBlock = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;			
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			volumeDescriptorSequenceNumber = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			tagLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
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
		
		UnallocatedSpaceDescriptor myUnallocatedSpaceDescriptor = new UnallocatedSpaceDescriptor();
				
		myUnallocatedSpaceDescriptor.VolumeDescriptorSequenceNumber = volumeDescriptorSequenceNumber;
		myUnallocatedSpaceDescriptor.NumberofAllocationDescriptors = 1;
		myUnallocatedSpaceDescriptor.AllocationDescriptors = new Extend_ad[1];
		
		myUnallocatedSpaceDescriptor.AllocationDescriptors[0] = new Extend_ad();
		myUnallocatedSpaceDescriptor.AllocationDescriptors[0].loc = unallocatedSpaceStartBlock;
		myUnallocatedSpaceDescriptor.AllocationDescriptors[0].len = (unallocatedSpaceEndBlock - unallocatedSpaceStartBlock) * blockSize;		

		/*
		// full element with descriptor tag
		myUnallocatedSpaceDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myUnallocatedSpaceDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myUnallocatedSpaceDescriptor.DescriptorTag.TagLocation = tagLocation;
		super.data( new ByteArrayDataReference( myUnallocatedSpaceDescriptor.getBytes( blockSize ) ) );
		*/
				
		// without descriptor tag (handled in next pipeline section)
		super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
		super.data( new WordDataReference( 7 ) );					// tag identifier
		super.data( new WordDataReference( tagLocation ) );			// tag location
		super.data( new WordDataReference( tagSerialNumber ) );		// tag serial number
		super.data( new WordDataReference( descriptorVersion ) );	// descriptor version
		super.data( new ByteArrayDataReference( myUnallocatedSpaceDescriptor.getBytesWithoutDescriptorTag() ) );
		super.endElement();
	}

	protected void createAndPassIUVD()
	throws HandlerException
	{
		InputStream myInputStream = null;
		long tagLocation = 0;
		long volumeDescriptorSequenceNumber = 0;
		String applicationIdentifier = "";
		byte[] applicationIdentifierSuffix = new byte[0];
		String imageIdentifier;
		
		try
		{
			DataReference myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			imageIdentifier = new String( BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() ) );
			myInputStream.close();
			myInputStream = null;
			
			myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			applicationIdentifierSuffix  = BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() );
			myInputStream.close();
			myInputStream = null;			

			myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			applicationIdentifier = new String( BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() ) );
			myInputStream.close();
			myInputStream = null;			
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			volumeDescriptorSequenceNumber = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			tagLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
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
		
		ImplementationUseVolumeDescriptor myImplementationUseVolumeDescriptor = new ImplementationUseVolumeDescriptor();
				
		myImplementationUseVolumeDescriptor.VolumeDescriptorSequenceNumber = volumeDescriptorSequenceNumber;		
		myImplementationUseVolumeDescriptor.ImplementationIdentifier.IdentifierSuffix = udfVersionIdentifierSuffix;
		
		try
		{
			myImplementationUseVolumeDescriptor.ImplementationIdentifier.setIdentifier( "*UDF LV Info" );
			myImplementationUseVolumeDescriptor.ImplementationUse.ImplementationID.setIdentifier( applicationIdentifier );
			myImplementationUseVolumeDescriptor.ImplementationUse.setLogicalVolumeIdentifier( imageIdentifier );
		}
		catch( Exception myException )
		{
			throw new HandlerException( myException );
		}
		
		myImplementationUseVolumeDescriptor.ImplementationUse.ImplementationID.IdentifierSuffix = applicationIdentifierSuffix;	
		
		// TODO: maybe set the LVInfo1 - 3 fields of ImplementationUse (f.ex. owner, organization, contact)
				
		/*
		// full element with descriptor tag				
		myImplementationUseVolumeDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myImplementationUseVolumeDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myImplementationUseVolumeDescriptor.DescriptorTag.TagLocation = tagLocation;
		super.data( new ByteArrayDataReference( myImplementationUseVolumeDescriptor.getBytes( blockSize ) ) );
		*/

		// without descriptor tag (handled in next pipeline section)
		super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
		super.data( new WordDataReference( 4 ) );					// tag identifier
		super.data( new WordDataReference( tagLocation ) );			// tag location
		super.data( new WordDataReference( tagSerialNumber ) );		// tag serial number
		super.data( new WordDataReference( descriptorVersion ) );	// descriptor version
		super.data( new ByteArrayDataReference( myImplementationUseVolumeDescriptor.getBytesWithoutDescriptorTag() ) );
		super.endElement();
	}
	
	protected void createAndPassTD()
	throws HandlerException
	{
		InputStream myInputStream = null;
		long tagLocation = 0;

		try
		{
			myInputStream = dataReferenceStack.pop().createInputStream();
			tagLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
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
		
		TerminatingDescriptor myTerminatingDescriptor = new TerminatingDescriptor();

		/*
		// full element with descriptor tag				
		myTerminatingDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myTerminatingDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myTerminatingDescriptor.DescriptorTag.TagLocation = tagLocation;
		super.data( new ByteArrayDataReference( myTerminatingDescriptor.getBytes( blockSize ) ) );
		*/
		
		// without descriptor tag (handled in next pipeline section)
		super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
		super.data( new WordDataReference( 8 ) );					// tag identifier
		super.data( new WordDataReference( tagLocation ) );			// tag location
		super.data( new WordDataReference( tagSerialNumber ) );		// tag serial number
		super.data( new WordDataReference( descriptorVersion ) );	// descriptor version
		super.data( new ByteArrayDataReference( myTerminatingDescriptor.getBytesWithoutDescriptorTag() ) );
		super.endElement();
	}

	protected void createAndPassLVID()
	throws HandlerException
	{
		InputStream myInputStream = null;
		long tagLocation = 0;
		long numberOfFiles = 0;
		long numberOfDirectories = 0;
		Calendar recordingTimeCalendar = Calendar.getInstance();
		String applicationIdentifier = "";
		byte[] applicationIdentifierSuffix = new byte[0];
		long numberOfPartitions = 0;
		long[] sizeTable = new long[0];
		long[] freespaceTable = new long[0];
		long nextUniqueId = 0;

		try
		{
			myInputStream = dataReferenceStack.pop().createInputStream();
			nextUniqueId = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			numberOfPartitions = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			sizeTable = new long[ (int)numberOfPartitions ];
			freespaceTable = new long[ (int)numberOfPartitions ];
			
			for( int i = (int)numberOfPartitions - 1; i >= 0 ; --i )
			{
				myInputStream = dataReferenceStack.pop().createInputStream();
				freespaceTable[ i ] = BinaryTools.readUInt32AsLong( myInputStream );
				myInputStream.close();
				myInputStream = null;

				myInputStream = dataReferenceStack.pop().createInputStream();
				sizeTable[ i ] = BinaryTools.readUInt32AsLong( myInputStream );
				myInputStream.close();
				myInputStream = null;
			}
			
			DataReference myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			applicationIdentifierSuffix  = BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() );
			myInputStream.close();
			myInputStream = null;			

			myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			applicationIdentifier = new String( BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() ) );
			myInputStream.close();
			myInputStream = null;			

			myInputStream = dataReferenceStack.pop().createInputStream();
			numberOfDirectories = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			numberOfFiles = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			long debug = BinaryTools.readUInt64AsLong( myInputStream );
			recordingTimeCalendar.setTimeInMillis( debug );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			tagLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
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

		LogicalVolumeIntegrityDescriptor myLogicalVolumeIntegrityDescriptor = new LogicalVolumeIntegrityDescriptor();
		
		myLogicalVolumeIntegrityDescriptor.RecordingDateAndTime.set( recordingTimeCalendar );
		myLogicalVolumeIntegrityDescriptor.IntegrityType = 1;
		myLogicalVolumeIntegrityDescriptor.NumberOfPartitions = sizeTable.length;
		
		myLogicalVolumeIntegrityDescriptor.FreeSpaceTable = freespaceTable;
		myLogicalVolumeIntegrityDescriptor.SizeTable = sizeTable;

		myLogicalVolumeIntegrityDescriptor.LogicalVolumeContensUse.UniqueID = nextUniqueId;		
		
		myLogicalVolumeIntegrityDescriptor.LengthOfImplementationUse = 46;
		
		EntityID implementationId = new EntityID();
		
		try
		{
			implementationId.setIdentifier( applicationIdentifier );
			implementationId.IdentifierSuffix = applicationIdentifierSuffix;
		}
		catch( Exception myException )
		{
			throw new HandlerException( myException );
		}
						
		myLogicalVolumeIntegrityDescriptor.setImplementationUse( implementationId, numberOfFiles, numberOfDirectories, minimumUDFReadRevision, minimumUDFWriteRevision, maximumUDFWriteRevision );
		
		/*
		// full element with descriptor tag				
		myLogicalVolumeIntegrityDescriptor.DescriptorTag.TagLocation = tagLocation;
		myLogicalVolumeIntegrityDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myLogicalVolumeIntegrityDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;		
		super.data( new ByteArrayDataReference( myLogicalVolumeIntegrityDescriptor.getBytes( blockSize ) ) );
		*/
		
		// without descriptor tag (handled in next pipeline section)
		super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
		super.data( new WordDataReference( 9 ) );					// tag identifier
		super.data( new WordDataReference( tagLocation ) );			// tag location
		super.data( new WordDataReference( tagSerialNumber ) );		// tag serial number
		super.data( new WordDataReference( descriptorVersion ) );	// descriptor version
		super.data( new ByteArrayDataReference( myLogicalVolumeIntegrityDescriptor.getBytesWithoutDescriptorTag() ) );
		super.endElement();
	}
	
	protected void createAndPassFSD()
	throws HandlerException
	{
		InputStream myInputStream = null;
		long tagLocation = 0;		
		long rootDirectoryLocation = 0;
		int partitionToStoreMetadataOn = 0;
		Calendar recordingTimeCalendar = Calendar.getInstance();
		String imageIdentifier = "";

		try
		{
			DataReference myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			imageIdentifier = new String( BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() ) );
			myInputStream.close();
			myInputStream = null;			
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			partitionToStoreMetadataOn = (int)BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			rootDirectoryLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
						
			myInputStream = dataReferenceStack.pop().createInputStream();
			recordingTimeCalendar.setTimeInMillis( BinaryTools.readUInt64AsLong( myInputStream ) );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			tagLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
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
		
		FileSetDescriptor myFilesetDescriptor = new FileSetDescriptor();
				
		myFilesetDescriptor.RecordingDateandTime.set( recordingTimeCalendar );
		myFilesetDescriptor.InterchangeLevel = 3;
		myFilesetDescriptor.MaximumInterchangeLevel = 3;
		myFilesetDescriptor.CharacterSetList = 1;
		myFilesetDescriptor.MaximumCharacterSetList = 1;
		myFilesetDescriptor.FileSetNumber = 0;
		myFilesetDescriptor.FileSetDescriptorNumber = 0;
		
		myFilesetDescriptor.setLogicalVolumeIdentifier( imageIdentifier );
		myFilesetDescriptor.setFileSetIdentifier( imageIdentifier );
		
		myFilesetDescriptor.RootDirectoryICB.ExtentLength = blockSize;
		myFilesetDescriptor.RootDirectoryICB.ExtentLocation.part_num = partitionToStoreMetadataOn;
		myFilesetDescriptor.RootDirectoryICB.ExtentLocation.lb_num = rootDirectoryLocation;
		
		try
		{
			myFilesetDescriptor.DomainIdentifier.setIdentifier( "*OSTA UDF Compliant" );
		}
		catch( Exception myException )
		{
			throw new HandlerException( myException );
		}
		
		myFilesetDescriptor.DomainIdentifier.IdentifierSuffix = udfVersionIdentifierSuffix;

		/*
		// full element with descriptor tag				
		myFilesetDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myFilesetDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myFilesetDescriptor.DescriptorTag.TagLocation = tagLocation;
		super.data( new ByteArrayDataReference( myFilesetDescriptor.getBytes( blockSize ) ) );
		*/
		
		// without descriptor tag (handled in next pipeline section)
		super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
		super.data( new WordDataReference( 256 ) );					// tag identifier
		super.data( new WordDataReference( tagLocation ) );			// tag location
		super.data( new WordDataReference( tagSerialNumber ) );		// tag serial number
		super.data( new WordDataReference( descriptorVersion ) );	// descriptor version
		super.data( new ByteArrayDataReference( myFilesetDescriptor.getBytesWithoutDescriptorTag() ) );
		super.endElement();
	}
	
	protected void createAndPassFE()
	throws HandlerException
	{
		InputStream myInputStream = null;
		long tagLocation = 0;
		int fileLinkCount = 0;
		Calendar accessTime 		= Calendar.getInstance();
		Calendar modificationTime 	= Calendar.getInstance();
		Calendar attributeTime 		= Calendar.getInstance();
		Calendar creationTime 		= Calendar.getInstance();
		long uniqueId = 0;
		String applicationIdentifier = "";
		byte[] applicationIdentifierSuffix = new byte[0];
		int fileType = 0;

		try
		{
			// partitionToStoreMetadataOn is not used in revision 2.01
			dataReferenceStack.pop();

			myInputStream = dataReferenceStack.pop().createInputStream();
			fileType = (int)BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			DataReference myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			applicationIdentifierSuffix  = BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() );
			myInputStream.close();
			myInputStream = null;			

			myDataReference = dataReferenceStack.pop(); 
			myInputStream = myDataReference.createInputStream();
			applicationIdentifier = new String( BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() ) );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			uniqueId = BinaryTools.readUInt64AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			creationTime.setTimeInMillis( BinaryTools.readUInt64AsLong( myInputStream ) );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			attributeTime.setTimeInMillis( BinaryTools.readUInt64AsLong( myInputStream ) );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			modificationTime.setTimeInMillis( BinaryTools.readUInt64AsLong( myInputStream ) );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			accessTime.setTimeInMillis( BinaryTools.readUInt64AsLong( myInputStream ) );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			fileLinkCount = (int)BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			tagLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
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
		
		FileEntry myFileEntry = new FileEntry();

		myFileEntry.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myFileEntry.DescriptorTag.DescriptorVersion = descriptorVersion;
		myFileEntry.DescriptorTag.TagLocation = tagLocation;
		
		myFileEntry.Uid = 0xFFFFFFFF;	// TODO: get current uid and gid if java supports it
		myFileEntry.Gid = 0xFFFFFFFF;
		
		// TODO: get real file permission if java supports it
		myFileEntry.Permissions = Permissions.OTHER_Read | Permissions.GROUP_Read | Permissions.OWNER_Read; 
		
		myFileEntry.FileLinkCount = fileLinkCount;
				
		myFileEntry.RecordFormat = 0;
		myFileEntry.RecordDisplayAttributes = 0;
		myFileEntry.RecordLength = 0;
			
		myFileEntry.AccessTime = new Timestamp( accessTime );
		myFileEntry.ModificationTime = new Timestamp( modificationTime ); 
		myFileEntry.AttributeTime = new Timestamp( attributeTime );		
		
		myFileEntry.Checkpoint = 1;
		
		try
		{
			myFileEntry.ImplementationIdentifier.setIdentifier( applicationIdentifier );
		}
		catch( Exception myException )
		{
			throw new HandlerException( myException );
		}
		
		myFileEntry.ImplementationIdentifier.IdentifierSuffix = applicationIdentifierSuffix;
		
		myFileEntry.ICBTag.PriorRecordedNumberofDirectEntries = 0;		
		myFileEntry.ICBTag.NumberofEntries = 1;
		myFileEntry.ICBTag.StrategyType = 4;
		
		myFileEntry.UniqueID =  uniqueId;
		
		if( fileType == 0 )			// normal file
		{
			myFileEntry.ICBTag.FileType = 5;
			createAndPassNormalFE( myFileEntry );
		}
		else if( fileType == 1 )	// directory
		{
			myFileEntry.ICBTag.FileType = 4;
			createAndPassDirectoryFE( myFileEntry );
		}
	}
	
	private void createAndPassNormalFE( FileEntry myFileEntry )
	throws HandlerException
	{
		InputStream myInputStream = null;
		long fileSize = 0;
		long dataLocation = 0;
		byte[] fileData = new byte[0];
		
		try
		{
			myInputStream = dataReferenceStack.pop().createInputStream();
			dataLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			DataReference myDataReference = dataReferenceStack.pop();
			fileSize = myDataReference.getLength();
			myInputStream = myDataReference.createInputStream();
			if( fileSize <= ( blockSize - ExtendedFileEntry.fixedPartLength ) )
			{
				fileData = BinaryTools.readByteArray( myInputStream, (int)fileSize );
			}
			myInputStream.close();
			myInputStream = null;
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
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
		
		myFileEntry.ICBTag.FileType = 5;	// normal file		
		
		myFileEntry.InformationLength = fileSize;

		if( fileSize <= ( blockSize - ExtendedFileEntry.fixedPartLength ) )
		{
			// store as inline embedded file data
			myFileEntry.ICBTag.Flags = 3;							// storage type inline		
			myFileEntry.LogicalBlocksRecorded = 0;
			myFileEntry.LengthofAllocationDescriptors = fileSize;
			myFileEntry.AllocationDescriptors = fileData;					
		}
		else
		{
			// store as exernal file data with Long_ad
			myFileEntry.ICBTag.Flags = 1;							// storage type long_ad
			
			myFileEntry.LogicalBlocksRecorded = (long)(fileSize / blockSize);
			if( fileSize % blockSize != 0 )
			{
				myFileEntry.LogicalBlocksRecorded++;
			}
			
			ArrayList<Long_ad> allocationDescriptors = new ArrayList<Long_ad>();
			
			long restFileSize = fileSize;
			long currentExtentPosition = dataLocation;
			
			while( restFileSize > 0 )
			{
				Long_ad allocationDescriptor = new Long_ad();
				
				if( restFileSize < maximumAllocationLength )
				{
					allocationDescriptor.ExtentLength = restFileSize;
				}
				else
				{
					allocationDescriptor.ExtentLength = maximumAllocationLength;
				}
				
				allocationDescriptor.ExtentLocation.part_num = 0;
				allocationDescriptor.ExtentLocation.lb_num = currentExtentPosition;
				
				allocationDescriptors.add( allocationDescriptor );
									
				restFileSize -= maximumAllocationLength;
				
				currentExtentPosition += (maximumAllocationLength / blockSize);
				if( maximumAllocationLength % blockSize != 0 )
				{
					currentExtentPosition++;
				}
			}
			
			byte allocationDescriptorBytes[] = new byte[ allocationDescriptors.size() * 16 ];
			
			int allocationDescriptorBytesPosition = 0;
			
			for( int i = 0; i < allocationDescriptors.size(); ++i )
			{
				byte singleAllocationDescriptorBytes[] = allocationDescriptors.get( i ).getBytes();
				System.arraycopy( singleAllocationDescriptorBytes, 0, allocationDescriptorBytes, allocationDescriptorBytesPosition, singleAllocationDescriptorBytes.length );
				allocationDescriptorBytesPosition += singleAllocationDescriptorBytes.length;
			}

			myFileEntry.AllocationDescriptors = allocationDescriptorBytes;
			myFileEntry.LengthofAllocationDescriptors = allocationDescriptorBytes.length;			
		}

		/*
		// full element with descriptor tag				
		super.data( new ByteArrayDataReference( myFileEntry.getBytes( blockSize ) ) );
		*/
				
		// without descriptor tag (handled in next pipeline section)
		super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
		super.data( new WordDataReference( 261 ) );												// tag identifier
		super.data( new WordDataReference( myFileEntry.DescriptorTag.TagLocation ) );			// tag location
		super.data( new WordDataReference( tagSerialNumber ) );									// tag serial number
		super.data( new WordDataReference( descriptorVersion ) );								// descriptor version
		super.data( new ByteArrayDataReference( myFileEntry.getBytesWithoutDescriptorTag() ) );
		super.endElement();
	}

	private void createAndPassDirectoryFE( FileEntry myFileEntry )
	throws HandlerException
	{
		InputStream myInputStream = null;
		ArrayList<FileIdentifierDescriptor> childFileIdentifierDescriptors = new ArrayList<FileIdentifierDescriptor>();
		long dataLocation = 0;
		int partitionToStoreMetadataOn = 0;
		
		try
		{
			myInputStream = dataReferenceStack.pop().createInputStream();
			dataLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			partitionToStoreMetadataOn = (int)BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;			
			
			// build file identifier descriptor for parent directory
			myInputStream = dataReferenceStack.pop().createInputStream();
			long parentDirectoryLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			long parentDirectoryUniqueId = BinaryTools.readUInt64AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			FileIdentifierDescriptor parentDirectoryFileIdentifierDescriptor = new FileIdentifierDescriptor();
			
			parentDirectoryFileIdentifierDescriptor.DescriptorTag.TagLocation = myFileEntry.DescriptorTag.TagLocation;
			parentDirectoryFileIdentifierDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
			parentDirectoryFileIdentifierDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
			
			parentDirectoryFileIdentifierDescriptor.ICB.ExtentLength = blockSize;
			parentDirectoryFileIdentifierDescriptor.ICB.ExtentLocation.part_num = partitionToStoreMetadataOn;
			
			parentDirectoryFileIdentifierDescriptor.FileVersionNumber = 1;
			parentDirectoryFileIdentifierDescriptor.FileCharacteristics = 10; // file is directory and parent

			parentDirectoryFileIdentifierDescriptor.ICB.ExtentLocation.lb_num = parentDirectoryLocation;

			parentDirectoryFileIdentifierDescriptor.ICB.implementationUse = new byte[ 6 ];				
			parentDirectoryFileIdentifierDescriptor.ICB.implementationUse[ 2 ] = (byte)(parentDirectoryUniqueId & 0xFF);
			parentDirectoryFileIdentifierDescriptor.ICB.implementationUse[ 3 ] = (byte)((parentDirectoryUniqueId >> 8) & 0xFF);
			parentDirectoryFileIdentifierDescriptor.ICB.implementationUse[ 4 ] = (byte)((parentDirectoryUniqueId >> 16) & 0xFF);
			parentDirectoryFileIdentifierDescriptor.ICB.implementationUse[ 5 ] = (byte)((parentDirectoryUniqueId >> 32) & 0xFF);

			childFileIdentifierDescriptors.add( parentDirectoryFileIdentifierDescriptor );			
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			int numberOfChildFiles = (int)BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;			
			
			// build file identifier descriptor elements for child files
			for( int i = 0; i < numberOfChildFiles; ++i )
			{
				myInputStream = dataReferenceStack.pop().createInputStream();
				int childFileType = (int)BinaryTools.readUInt32AsLong( myInputStream );
				myInputStream.close();
				myInputStream = null;
				
				DataReference myDataReference = dataReferenceStack.pop();
				myInputStream = myDataReference.createInputStream();
				String childFileIdentifier = new String( BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() ) );
				myInputStream.close();
				myInputStream = null;

				myInputStream = dataReferenceStack.pop().createInputStream();
				long childFileLocation = BinaryTools.readUInt32AsLong( myInputStream );
				myInputStream.close();
				myInputStream = null;

				myInputStream = dataReferenceStack.pop().createInputStream();
				long childFileUniqueId = BinaryTools.readUInt64AsLong( myInputStream );
				myInputStream.close();
				myInputStream = null;
				
				FileIdentifierDescriptor childFileIdentifierDescriptor = new FileIdentifierDescriptor();
				
				childFileIdentifierDescriptor.DescriptorTag.TagLocation = myFileEntry.DescriptorTag.TagLocation;
				childFileIdentifierDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
				childFileIdentifierDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
				
				childFileIdentifierDescriptor.ICB.ExtentLength = blockSize;
				childFileIdentifierDescriptor.ICB.ExtentLocation.lb_num = childFileLocation;
				childFileIdentifierDescriptor.ICB.ExtentLocation.part_num = partitionToStoreMetadataOn;

				childFileIdentifierDescriptor.ICB.implementationUse = new byte[ 6 ];				
				childFileIdentifierDescriptor.ICB.implementationUse[ 2 ] = (byte)(childFileUniqueId & 0xFF);
				childFileIdentifierDescriptor.ICB.implementationUse[ 3 ] = (byte)((childFileUniqueId >> 8) & 0xFF);
				childFileIdentifierDescriptor.ICB.implementationUse[ 4 ] = (byte)((childFileUniqueId >> 16) & 0xFF);
				childFileIdentifierDescriptor.ICB.implementationUse[ 5 ] = (byte)((childFileUniqueId >> 32) & 0xFF);
				
				childFileIdentifierDescriptor.FileVersionNumber = 1;
				
				try
				{
					childFileIdentifierDescriptor.setFileIdentifier( childFileIdentifier );
				}
				catch( Exception myException )
				{
					throw new HandlerException( myException );
				}

				if( childFileType == 1 )	// directory
				{
					childFileIdentifierDescriptor.FileCharacteristics = 2;
				}
				
				childFileIdentifierDescriptors.add( childFileIdentifierDescriptor );
			}			
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
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
		
		
		// get directory file data length
		int directoryFileDataLength = 0;
		for( int i = 0; i < childFileIdentifierDescriptors.size(); ++i )
		{
			directoryFileDataLength += childFileIdentifierDescriptors.get( i ).getLength();
		}
		
		myFileEntry.InformationLength = directoryFileDataLength;
					
		if( directoryFileDataLength <= blockSize - ExtendedFileEntry.fixedPartLength )
		{				
			// inline embedded file data
			myFileEntry.ICBTag.Flags = 3;				// storage type inline		
			myFileEntry.LogicalBlocksRecorded = 0; 
			myFileEntry.LengthofAllocationDescriptors = directoryFileDataLength;
			myFileEntry.AllocationDescriptors = new byte[ directoryFileDataLength ];			

			int pos = 0;
			for( int i = 0; i < childFileIdentifierDescriptors.size(); ++i )
			{
				byte childFileIdentifierDescriptorBytes[] = childFileIdentifierDescriptors.get( i ).getBytes();
				
				System.arraycopy( childFileIdentifierDescriptorBytes, 0, myFileEntry.AllocationDescriptors, pos, childFileIdentifierDescriptorBytes.length );
				pos += childFileIdentifierDescriptorBytes.length;
			}
			
			/*
			// full element with descriptor tag				
			super.data( new ByteArrayDataReference( myFileEntry.getBytes( blockSize ) ) );
			*/
					
			// without descriptor tag (handled in next pipeline section)
			super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
			super.data( new WordDataReference( 261 ) );												// tag identifier
			super.data( new WordDataReference( myFileEntry.DescriptorTag.TagLocation ) );			// tag location
			super.data( new WordDataReference( tagSerialNumber ) );									// tag serial number
			super.data( new WordDataReference( descriptorVersion ) );								// descriptor version
			super.data( new ByteArrayDataReference( myFileEntry.getBytesWithoutDescriptorTag() ) );
			super.endElement();
		}
		else
		{
			// store as exernal file data with Short_ad
			myFileEntry.ICBTag.Flags = 0;		// storage type short_ad
			
			myFileEntry.LogicalBlocksRecorded = (long)(directoryFileDataLength / blockSize);				
			if( directoryFileDataLength % blockSize != 0 )
			{
				myFileEntry.LogicalBlocksRecorded++;
			}
			
			Short_ad allocationDescriptor = new Short_ad();
			
			allocationDescriptor.ExtentLength = directoryFileDataLength;
			allocationDescriptor.ExtentPosition = dataLocation;
			

			if( directoryFileDataLength % blockSize != 0 )
			{
				directoryFileDataLength += blockSize - (directoryFileDataLength % blockSize); 
				
			}
			byte[] data = new byte[ directoryFileDataLength ];			
			
			long currentRealPosition = dataLocation * blockSize;
			int pos = 0;
			
			for( int i = 0; i < childFileIdentifierDescriptors.size(); ++i )
			{
				long tagLocationBlock = (long)(currentRealPosition / blockSize);
				
				FileIdentifierDescriptor childFileIdentifierDescriptor = childFileIdentifierDescriptors.get( i );
				
				childFileIdentifierDescriptor.DescriptorTag.TagLocation = tagLocationBlock;
				
				byte childFileIdentifierDescriptorBytes[] = childFileIdentifierDescriptors.get( i ).getBytes();
				
				System.arraycopy( childFileIdentifierDescriptorBytes, 0, data, pos, childFileIdentifierDescriptorBytes.length );
				pos += childFileIdentifierDescriptorBytes.length;
				
				currentRealPosition += childFileIdentifierDescriptorBytes.length;
			}							
			
			myFileEntry.AllocationDescriptors = allocationDescriptor.getBytes();
			myFileEntry.LengthofAllocationDescriptors = myFileEntry.AllocationDescriptors.length;
			
			/*
			// full element with descriptor tag				
			super.data( new ByteArrayDataReference( myFileEntry.getBytes( blockSize ) ) );
			*/
					
			// without descriptor tag (handled in next pipeline section)
			super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
			super.data( new WordDataReference( 261 ) );												// tag identifier
			super.data( new WordDataReference( myFileEntry.DescriptorTag.TagLocation ) );			// tag location
			super.data( new WordDataReference( tagSerialNumber ) );									// tag serial number
			super.data( new WordDataReference( descriptorVersion ) );								// descriptor version
			super.data( new ByteArrayDataReference( myFileEntry.getBytesWithoutDescriptorTag() ) );
			super.endElement();

			super.data( new ByteArrayDataReference( data ) );
		}		
	}	
	
	protected void createAndPassMetadataFile()
	throws HandlerException
	{
		InputStream myInputStream = null;
		
		try
		{
			// not supported in this revision so just drop the given data
			dataReferenceStack.pop();
			dataReferenceStack.pop();
			dataReferenceStack.pop();
			dataReferenceStack.pop();
			dataReferenceStack.pop(); 
			dataReferenceStack.pop();
			dataReferenceStack.pop();
			dataReferenceStack.pop();
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
	}
	
}
