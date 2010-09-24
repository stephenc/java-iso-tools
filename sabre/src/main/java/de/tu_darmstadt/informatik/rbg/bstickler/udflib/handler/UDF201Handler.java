/*
 *	UDF201Handler.java
 *
 *	2006-07-12
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.SabreUDFElement;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.SabreUDFElement.UDFElementType;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures.*;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.BinaryTools;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.Permissions;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;


public class UDF201Handler extends UDF102Handler
{	
	public UDF201Handler( StructureHandler myStructureHandler, ContentHandler myContentHandler )
	{		
		super( myStructureHandler, myContentHandler );

		// set version related information
		udfVersionIdentifierSuffix = new byte[]{ 0x01, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		minimumUDFReadRevision = 0x0201;
		minimumUDFWriteRevision = 0x0201;
		maximumUDFWriteRevision = 0x0201;
		descriptorVersion = 3;
	}
	
	protected void createAndPassVRS()
	throws HandlerException
	{
		VolumeRecognitionSequence myVolumeRecognitionSequene = new VolumeRecognitionSequence( VolumeRecognitionSequence.NSRVersion.NSR03 );
		super.data( new ByteArrayDataReference( myVolumeRecognitionSequene.getBytes() ) );
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
		
		myPartitionDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myPartitionDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myPartitionDescriptor.DescriptorTag.TagLocation = tagLocation;
		
		myPartitionDescriptor.VolumeDescriptorSequenceNumber = volumeDescriptorSequenceNumber;
		myPartitionDescriptor.PartitionFlags = 1;
		myPartitionDescriptor.PartitionNumber = 0;
		
		try
		{
			myPartitionDescriptor.PartitionContents.setIdentifier( "+NSR03" );
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
		super.data( new ByteArrayDataReference( myPartitionDescriptor.getBytes( blockSize ) ) );
		*/
				
		// without descriptor tag (handled in next pipeline section)
		super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
		super.data( new WordDataReference( 5 ) );						// tag identifier
		super.data( new WordDataReference( tagLocation ) );				// tag location
		super.data( new WordDataReference( tagSerialNumber ) );			// tag serial number
		super.data( new WordDataReference( descriptorVersion ) );		// descriptor version
		super.data( new ByteArrayDataReference( myPartitionDescriptor.getBytesWithoutDescriptorTag() ) );
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
		
		ExtendedFileEntry myExtendedFileEntry = new ExtendedFileEntry();

		myExtendedFileEntry.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myExtendedFileEntry.DescriptorTag.DescriptorVersion = descriptorVersion;
		myExtendedFileEntry.DescriptorTag.TagLocation = tagLocation;
		
		myExtendedFileEntry.Uid = 0xFFFFFFFF;	// TODO: get current uid and gid if java supports it
		myExtendedFileEntry.Gid = 0xFFFFFFFF;
		
		// TODO: get real file permission if java supports it
		myExtendedFileEntry.Permissions = Permissions.OTHER_Read | Permissions.GROUP_Read | Permissions.OWNER_Read; 
		
		myExtendedFileEntry.FileLinkCount = fileLinkCount;
				
		myExtendedFileEntry.RecordFormat = 0;
		myExtendedFileEntry.RecordDisplayAttributes = 0;
		myExtendedFileEntry.RecordLength = 0;
			
		myExtendedFileEntry.AccessTime = new Timestamp( accessTime );
		myExtendedFileEntry.ModificationTime = new Timestamp( modificationTime ); 
		myExtendedFileEntry.AttributeTime = new Timestamp( attributeTime );
		myExtendedFileEntry.CreationTime = new Timestamp( creationTime );
		
		myExtendedFileEntry.Checkpoint = 1;
		
		try
		{
			myExtendedFileEntry.ImplementationIdentifier.setIdentifier( applicationIdentifier );
		}
		catch( Exception myException )
		{
			throw new HandlerException( myException );
		}
		
		myExtendedFileEntry.ImplementationIdentifier.IdentifierSuffix = applicationIdentifierSuffix;
		
		myExtendedFileEntry.ICBTag.PriorRecordedNumberofDirectEntries = 0;		
		myExtendedFileEntry.ICBTag.NumberofEntries = 1;
		myExtendedFileEntry.ICBTag.StrategyType = 4;
		
		myExtendedFileEntry.UniqueID =  uniqueId;
		
		if( fileType == 0 )			// normal file
		{
			myExtendedFileEntry.ICBTag.FileType = 5;
			createAndPassNormalFE( myExtendedFileEntry );
		}
		else if( fileType == 1 )	// directory
		{
			myExtendedFileEntry.ICBTag.FileType = 4;
			createAndPassDirectoryFE( myExtendedFileEntry );
		}
	}
	
	private void createAndPassNormalFE( ExtendedFileEntry myExtendedFileEntry )
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
		
		myExtendedFileEntry.ICBTag.FileType = 5;	// normal file		
		
		myExtendedFileEntry.InformationLength = fileSize;
		myExtendedFileEntry.ObjectSize = myExtendedFileEntry.InformationLength;
		

		if( fileSize <= ( blockSize - ExtendedFileEntry.fixedPartLength ) )
		{
			// store as inline embedded file data
			myExtendedFileEntry.ICBTag.Flags = 3;							// storage type inline		
			myExtendedFileEntry.LogicalBlocksRecorded = 0;
			myExtendedFileEntry.LengthofAllocationDescriptors = fileSize;
			myExtendedFileEntry.AllocationDescriptors = fileData;					
		}
		else
		{
			// store as exernal file data with Long_ad
			myExtendedFileEntry.ICBTag.Flags = 1;							// storage type long_ad
			
			myExtendedFileEntry.LogicalBlocksRecorded = (long)(fileSize / blockSize);
			if( fileSize % blockSize != 0 )
			{
				myExtendedFileEntry.LogicalBlocksRecorded++;
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

			myExtendedFileEntry.AllocationDescriptors = allocationDescriptorBytes;
			myExtendedFileEntry.LengthofAllocationDescriptors = allocationDescriptorBytes.length;			
		}
		
		/*
		// full element with descriptor tag				
		super.data( new ByteArrayDataReference( myExtendedFileEntry.getBytes( blockSize ) ) );
		*/
				
		// without descriptor tag (handled in next pipeline section)
		super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
		super.data( new WordDataReference( 266 ) );													// tag identifier
		super.data( new WordDataReference( myExtendedFileEntry.DescriptorTag.TagLocation ) );		// tag location
		super.data( new WordDataReference( tagSerialNumber ) );										// tag serial number
		super.data( new WordDataReference( descriptorVersion ) );									// descriptor version
		super.data( new ByteArrayDataReference( myExtendedFileEntry.getBytesWithoutDescriptorTag() ) );
		super.endElement();		
	}

	private void createAndPassDirectoryFE( ExtendedFileEntry myExtendedFileEntry )
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
			
			parentDirectoryFileIdentifierDescriptor.DescriptorTag.TagLocation = myExtendedFileEntry.DescriptorTag.TagLocation;
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
				
				childFileIdentifierDescriptor.DescriptorTag.TagLocation = myExtendedFileEntry.DescriptorTag.TagLocation;
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
		
		myExtendedFileEntry.InformationLength = directoryFileDataLength;
		myExtendedFileEntry.ObjectSize = myExtendedFileEntry.InformationLength;
					
		if( directoryFileDataLength <= blockSize - ExtendedFileEntry.fixedPartLength )
		{				
			// inline embedded file data
			myExtendedFileEntry.ICBTag.Flags = 3;				// storage type inline		
			myExtendedFileEntry.LogicalBlocksRecorded = 0; 
			myExtendedFileEntry.LengthofAllocationDescriptors = directoryFileDataLength;
			myExtendedFileEntry.AllocationDescriptors = new byte[ directoryFileDataLength ];			

			int pos = 0;
			for( int i = 0; i < childFileIdentifierDescriptors.size(); ++i )
			{
				byte childFileIdentifierDescriptorBytes[] = childFileIdentifierDescriptors.get( i ).getBytes();
				
				System.arraycopy( childFileIdentifierDescriptorBytes, 0, myExtendedFileEntry.AllocationDescriptors, pos, childFileIdentifierDescriptorBytes.length );
				pos += childFileIdentifierDescriptorBytes.length;
			}
			
			/*
			// full element with descriptor tag				
			super.data( new ByteArrayDataReference( myExtendedFileEntry.getBytes( blockSize ) ) );
			*/
					
			// without descriptor tag (handled in next pipeline section)
			super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
			super.data( new WordDataReference( 266 ) );													// tag identifier
			super.data( new WordDataReference( myExtendedFileEntry.DescriptorTag.TagLocation ) );		// tag location
			super.data( new WordDataReference( tagSerialNumber ) );										// tag serial number
			super.data( new WordDataReference( descriptorVersion ) );									// descriptor version
			super.data( new ByteArrayDataReference( myExtendedFileEntry.getBytesWithoutDescriptorTag() ) );
			super.endElement();
		}
		else
		{
			// store as exernal file data with Short_ad
			myExtendedFileEntry.ICBTag.Flags = 0;		// storage type short_ad
			
			myExtendedFileEntry.LogicalBlocksRecorded = (long)(directoryFileDataLength / blockSize);				
			if( directoryFileDataLength % blockSize != 0 )
			{
				myExtendedFileEntry.LogicalBlocksRecorded++;
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
			
			myExtendedFileEntry.AllocationDescriptors = allocationDescriptor.getBytes();
			myExtendedFileEntry.LengthofAllocationDescriptors = myExtendedFileEntry.AllocationDescriptors.length;
			
			/*
			// full element with descriptor tag				
			super.data( new ByteArrayDataReference( myExtendedFileEntry.getBytes( blockSize ) ) );
			*/
					
			// without descriptor tag (handled in next pipeline section)
			super.startElement( new SabreUDFElement( UDFElementType.DescriptorTag ) );
			super.data( new WordDataReference( 266 ) );													// tag identifier
			super.data( new WordDataReference( myExtendedFileEntry.DescriptorTag.TagLocation ) );		// tag location
			super.data( new WordDataReference( tagSerialNumber ) );										// tag serial number
			super.data( new WordDataReference( descriptorVersion ) );									// descriptor version
			super.data( new ByteArrayDataReference( myExtendedFileEntry.getBytesWithoutDescriptorTag() ) );
			super.endElement();

			super.data( new ByteArrayDataReference( data ) );
		}		
	}	
	
}
