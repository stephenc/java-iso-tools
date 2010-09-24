/*
 *	SabreUDFElementFactory.java
 *
 *	2006-07-14
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.SabreUDFElement.UDFElementType;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class SabreUDFElementFactory
{
	private StreamHandler myStreamHandler = null;
	
	public SabreUDFElementFactory( StreamHandler myStreamHandler )
	{
		this.myStreamHandler = myStreamHandler;
	}

	public void startUDFImage()
	throws HandlerException
	{
		myStreamHandler.startDocument();
	}
	
	public void endUDFImage()
	throws HandlerException
	{
		myStreamHandler.endDocument();
	}
	
	public void startReservedArea()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.ReservedArea ) );
		myStreamHandler.data( new ByteArrayDataReference( new byte[32768] ) );
	}
	
	public void endReservedArea()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}
	
	public void startVRS()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.VolumeRecognitionSequence ) );		
	}

	public void endVRS()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}
	
	public void startEmptyArea()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.EmptyArea ) );
	}
	
	public void doEmptyArea( int lengthInBytes )
	throws HandlerException
	{
		myStreamHandler.data( new ByteArrayDataReference( new byte[ lengthInBytes ] ) );
	}
	
	public void endEmptyArea()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}
	

	public void startAVDP()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.AnchorVolumeDescriptorPointer ) );
	}
	
	public void doAVDP( long selfBlock, long MVDSBlock, long RVDSBlock )
	throws HandlerException
	{
		myStreamHandler.data( new WordDataReference( selfBlock ) );		// element location
		myStreamHandler.data( new WordDataReference( MVDSBlock ) );		// main volume descriptor sequence start block
		myStreamHandler.data( new WordDataReference( RVDSBlock ) );		// reserve volume descriptor sequence start block
	}
		
	public void endAVDP()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}

	public void startPVD()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.PrimaryVolumeDescriptor ) );
	}
	
	public void doPVD( long selfBlock, long volumeDescriptorSequenceNumber, long recordingTimeMillis, String imageIdentifier, String applicationIdentifier, byte[] applicationIdentifierSuffix )
	throws HandlerException
	{
		myStreamHandler.data( new WordDataReference( selfBlock ) );								// element location
		myStreamHandler.data( new WordDataReference( volumeDescriptorSequenceNumber ) );		// volume descriptor sequence number
		myStreamHandler.data( new DWordDataReference( recordingTimeMillis ) );					// recording time
		myStreamHandler.data( new ByteArrayDataReference( imageIdentifier.getBytes() ) );		// image identifier
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifier.getBytes() ) );	// application identifier
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifierSuffix ) );		// application identifier suffix
	}
		
	public void endPVD()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}

	public void startPD()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.PartitionDescriptor ) );
	}
	
	public void doPD( long selfBlock, long volumeDescriptorSequenceNumber, long partitionStartingBlock, long partitionEndingBlock, String applicationIdentifier, byte[] applicationIdentifierSuffix )
	throws HandlerException
	{
		myStreamHandler.data( new WordDataReference( selfBlock ) );								// element location
		myStreamHandler.data( new WordDataReference( volumeDescriptorSequenceNumber ) );		// volume descriptor sequence number
		myStreamHandler.data( new WordDataReference( partitionStartingBlock ) );				// partition starting block
		myStreamHandler.data( new WordDataReference( partitionEndingBlock ) );					// partition ending block
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifier.getBytes() ) );	// application identifier
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifierSuffix ) );		// application identifier suffix
	}
		
	public void endPD()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}

	public void startLVD()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.LogicalVolumeDescriptor ) );
	}
	
	public void doLVD( long selfBlock, long volumeDescriptorSequenceNumber, long logicalVolumeIntegritySequenceStartingBlock, long logicalVolumeIntegritySequenceEndingBlock, int metadataAllocationUnitSize, int metadataAlignmentUnitSize, long mainMetadataFileLocation, long mirrorMetadataFileLocation, int fileSetDescriptorPartition, long fileSetDescriptorLocation, String applicationIdentifier, byte[] applicationIdentifierSuffix, String imageIdentifier )
	throws HandlerException
	{
		myStreamHandler.data( new WordDataReference( selfBlock ) );										// element location
		myStreamHandler.data( new WordDataReference( volumeDescriptorSequenceNumber ) );				// volume descriptor sequence number
		myStreamHandler.data( new WordDataReference( logicalVolumeIntegritySequenceStartingBlock ) );	// logical volume integrity sequence starting block
		myStreamHandler.data( new WordDataReference( logicalVolumeIntegritySequenceEndingBlock ) );		// logical volume integrity sequence ending block
		myStreamHandler.data( new WordDataReference( metadataAllocationUnitSize ) );					// metadata allocation unit size
		myStreamHandler.data( new WordDataReference( metadataAlignmentUnitSize ) );						// metadata alignment unit size
		myStreamHandler.data( new WordDataReference( mainMetadataFileLocation ) );						// main metadata file location
		myStreamHandler.data( new WordDataReference( mirrorMetadataFileLocation ) );					// mirror metadata file location
		myStreamHandler.data( new WordDataReference( fileSetDescriptorPartition ) );					// fileset descriptor partition
		myStreamHandler.data( new WordDataReference( fileSetDescriptorLocation ) );						// fileset descriptor location
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifier.getBytes() ) );			// application identifier
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifierSuffix ) );				// application identifier suffix
		myStreamHandler.data( new ByteArrayDataReference( imageIdentifier.getBytes() ) );				// image identifier		
	}
		
	public void endLVD()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}

	public void startUSD()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.UnallocatedSpaceDescriptor ) );
	}
	
	public void doUSD( long selfBlock, long volumeDescriptorSequenceNumber, long unallocatedSpaceStartBlock, long unallocatedSpaceEndBlock )
	throws HandlerException
	{
		myStreamHandler.data( new WordDataReference( selfBlock ) );							// element location
		myStreamHandler.data( new WordDataReference( volumeDescriptorSequenceNumber ) );	// volume descriptor sequence number
		myStreamHandler.data( new WordDataReference( unallocatedSpaceStartBlock ) );		// unalocated space start block
		myStreamHandler.data( new WordDataReference( unallocatedSpaceEndBlock ) );			// unalocated space end block
	}
		
	public void endUSD()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}

	public void startIUVD()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.ImplementationUseVolumeDescriptor ) );
	}
	
	public void doIUVD( long selfBlock, long volumeDescriptorSequenceNumber, String applicationIdentifier, byte[] applicationIdentifierSuffix, String imageIdentifier )
	throws HandlerException
	{
		myStreamHandler.data( new WordDataReference( selfBlock ) );									// element location
		myStreamHandler.data( new WordDataReference( volumeDescriptorSequenceNumber ) );			// volume descriptor sequence number
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifier.getBytes() ) );		// application identifier
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifierSuffix ) );			// application identifier suffix
		myStreamHandler.data( new ByteArrayDataReference( imageIdentifier.getBytes() ) );			// image identifier		
	}
		
	public void endIUVD()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}

	public void startTD()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.TerminatingDescriptor ) );
	}

	public void doTD( long selfBlock )
	throws HandlerException
	{
		myStreamHandler.data( new WordDataReference( selfBlock ) );		// element location
	}
			
	public void endTD()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}

	public void startLVID()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.LogicalVolumeIntegrityDescriptor ) );
	}

	public void doLVID( long selfBlock, long recordingTimeMillis, long numberOfFiles, long numberOfDirectories, String applicationIdentifier, byte[] applicationIdentifierSuffix, long[] sizeTable, long[] freespaceTable, long nextUniqueId )
	throws HandlerException
	{
		myStreamHandler.data( new WordDataReference( selfBlock ) );									// element location
		myStreamHandler.data( new DWordDataReference( recordingTimeMillis ) );						// recording time
		myStreamHandler.data( new WordDataReference( numberOfFiles ) );								// number of files
		myStreamHandler.data( new WordDataReference( numberOfDirectories ) );						// number of directories
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifier.getBytes() ) );		// application identifier
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifierSuffix ) );			// application identifier suffix		
		
		for( int i = 0; i < sizeTable.length; ++i )
		{
			myStreamHandler.data( new WordDataReference( sizeTable[ i ] ) );						// partition[ i ] size
			myStreamHandler.data( new WordDataReference( freespaceTable[ i ] ) );					// partition[ i ] freespace
		}
		
		myStreamHandler.data( new WordDataReference( sizeTable.length ) );							// number of partitions (== size of freespaceTable and sizeTable)
		
		myStreamHandler.data( new WordDataReference( nextUniqueId ) );								// next unique id
	}
				
	public void endLVID()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}
	
	public void startFSD()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.FileSetDescriptor ) );
	}

	public void doFSD( long selfBlock, long recordingTimeMillis, long rootDirectoryLocation, int partitionToStoreMetadataOn, String imageIdentifier )
	throws HandlerException
	{
		myStreamHandler.data( new WordDataReference( selfBlock ) );							// element location		
		myStreamHandler.data( new DWordDataReference( recordingTimeMillis ) );				// recording time
		myStreamHandler.data( new WordDataReference( rootDirectoryLocation ) );				// root directory block
		myStreamHandler.data( new WordDataReference( partitionToStoreMetadataOn ) );		// the number of the partition to store the metadata on
		myStreamHandler.data( new ByteArrayDataReference( imageIdentifier.getBytes() ) );	// image identifier
	}
				
	public void endFSD()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}
	
	public void startFE()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.FileEntry ) );
	}

	public void doFE( UDFImageBuilderFile myUDFImageBuilderFile, UDFLayoutInformation myUDFLayoutInformation, String applicationIdentifier, byte[] applicationIdentifierSuffix )
	throws HandlerException
	{		
		long selfBlock = myUDFLayoutInformation.fileEntryPositions.get( myUDFImageBuilderFile ).entryLocation;
		long dataLocation = myUDFLayoutInformation.fileEntryPositions.get( myUDFImageBuilderFile ).dataLocation;
		long uniqueId = myUDFLayoutInformation.uniqueIds.get( myUDFImageBuilderFile );
		int fileLinkCount = myUDFImageBuilderFile.getFileLinkCount();
		long accessTimeMillis = myUDFImageBuilderFile.getAccessTime().getTimeInMillis();
		long modificationTimeMillis = myUDFImageBuilderFile.getModificationTime().getTimeInMillis();
		long attributeTimeMillis = myUDFImageBuilderFile.getAttributeTime().getTimeInMillis();
		long creationTimeMillis =  myUDFImageBuilderFile.getCreationTime().getTimeInMillis();
		
		int fileType = -1;
		
		if( myUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.File )
		{
			fileType = 0;
		}
		else if( myUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.Directory )
		{
			fileType = 1;
		}
				
		if( myUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.File )
		{
			myStreamHandler.data( new FileDataReference( myUDFImageBuilderFile.getSourceFile() ) );		// source file			
			myStreamHandler.data( new WordDataReference( dataLocation ) );								// data location
		}
		else if( myUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.Directory )
		{
			// get child files and pass required information
			UDFImageBuilderFile[] childUDFImageBuilderFiles = myUDFImageBuilderFile.getChilds();
			
			for( int i = childUDFImageBuilderFiles.length - 1; i >= 0 ; --i )
			{
				long childFileUniqueId = myUDFLayoutInformation.uniqueIds.get( childUDFImageBuilderFiles[ i ] );
				long childFileLocation = myUDFLayoutInformation.fileEntryPositions.get( childUDFImageBuilderFiles[ i ] ).entryLocation;

				int childFileType = -1;
				
				if( childUDFImageBuilderFiles[ i ].getFileType() == UDFImageBuilderFile.FileType.File )
				{
					childFileType = 0;
				}
				else if( childUDFImageBuilderFiles[ i ].getFileType() == UDFImageBuilderFile.FileType.Directory )
				{
					childFileType = 1;
				}
				
				myStreamHandler.data( new DWordDataReference( childFileUniqueId ) );											// child file unique id
				myStreamHandler.data( new WordDataReference( childFileLocation ) );											// child fileentry location
				myStreamHandler.data( new ByteArrayDataReference( childUDFImageBuilderFiles[ i ].getIdentifier().getBytes() ) );	// child file identifier
				myStreamHandler.data( new WordDataReference( childFileType ) );														// child file type
			}
			
			myStreamHandler.data( new WordDataReference( childUDFImageBuilderFiles.length ) );		// number of child files
			
			// get and pass parent directory information
			long parentDirectoryUniqueId = 0;
			long parentDirectoryLocation = myUDFLayoutInformation.fileEntryPositions.get( myUDFImageBuilderFile ).entryLocation;
			
			if( myUDFImageBuilderFile.getParent() != null )
			{
				parentDirectoryUniqueId = myUDFLayoutInformation.uniqueIds.get( myUDFImageBuilderFile.getParent() );
				parentDirectoryLocation = myUDFLayoutInformation.fileEntryPositions.get( myUDFImageBuilderFile.getParent() ).entryLocation;
			}
			
			myStreamHandler.data( new DWordDataReference( parentDirectoryUniqueId ) );
			myStreamHandler.data( new WordDataReference( parentDirectoryLocation ) );
			
			myStreamHandler.data( new WordDataReference( myUDFLayoutInformation.partitionToStoreMetadataOn ) );	// partition to store metadata on
			myStreamHandler.data( new WordDataReference( dataLocation ) );										// data location
		}
		
		myStreamHandler.data( new WordDataReference( selfBlock ) );									// element location
		myStreamHandler.data( new WordDataReference( fileLinkCount ) );								// file link count
		myStreamHandler.data( new DWordDataReference( accessTimeMillis ) );							// access time
		myStreamHandler.data( new DWordDataReference( modificationTimeMillis ) );					// modification time
		myStreamHandler.data( new DWordDataReference( attributeTimeMillis ) );						// attribute time
		myStreamHandler.data( new DWordDataReference( creationTimeMillis ) );						// creation time
		myStreamHandler.data( new DWordDataReference( uniqueId ) );									// unique id
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifier.getBytes() ) );		// application identifier
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifierSuffix ) );			// application identifier suffix		
		myStreamHandler.data( new WordDataReference( fileType ) );									// file type
		myStreamHandler.data( new WordDataReference( myUDFLayoutInformation.partitionToStoreMetadataOn ) ); // partition number of partition to store metadata on			
	}
				
	public void endFE()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}

	public void startRawFileData()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.RawFileData ) );
	}

	public void doRawFileData( File sourceFile )
	throws HandlerException
	{
		myStreamHandler.data( new FileDataReference( sourceFile ) );
	}
				
	public void endRawFileData()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}
	
	public void startMetadataFile()
	throws HandlerException
	{
		myStreamHandler.startElement( new SabreUDFElement( UDFElementType.MetadataFile ) );
	}

	public void doMetadataFile( long recordingTimeMillis, String applicationIdentifier, byte[] applicationIdentifierSuffix, UDFLayoutInformation myUDFLayoutInformation, long metadataFileLocation, byte fileType )
	throws HandlerException
	{
		myStreamHandler.data( new DWordDataReference( recordingTimeMillis ) );
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifier.getBytes() ) );
		myStreamHandler.data( new ByteArrayDataReference( applicationIdentifierSuffix ) );
		myStreamHandler.data( new WordDataReference( myUDFLayoutInformation.metadataPartitionStartingBlock ) );
		myStreamHandler.data( new WordDataReference( myUDFLayoutInformation.metadataPartitionEndingBlock ) );
		myStreamHandler.data( new WordDataReference( myUDFLayoutInformation.physicalPartitionStartingBlock ) );
		myStreamHandler.data( new WordDataReference( metadataFileLocation ) );
		myStreamHandler.data( new ByteDataReference( fileType ) );
	}
				
	public void endMetadataFile()
	throws HandlerException
	{
		myStreamHandler.endElement();
	}
	
	
}
