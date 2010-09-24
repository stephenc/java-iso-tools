/*
 *	UDFImageBuilder.java
 *
 *	2006-06-07
 *
 *	Björn Stickler <bjoern@stickler.de>
 */ 

package de.tu_darmstadt.informatik.rbg.bstickler.udflib;

import java.io.*;
import java.util.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures.*;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class UDFImageBuilder
{
	private int blockSize = 2048;
	
	private String imageIdentifier = "UDFImageBuilder Disc";
	
	private String applicationIdentifier = "*UDFImageBuilder";	
	private byte applicationIdentifierSuffix[] = new byte[]{ 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
	
	private UDFImageBuilderFile rootUDFImageBuilderFile;
	private UniqueIdDisposer myUniqueIdDisposer;
	private long maximumAllocationLength = 1073739776;

	
	public UDFImageBuilder()
	{
		rootUDFImageBuilderFile = new UDFImageBuilderFile( "" );
		myUniqueIdDisposer = new UniqueIdDisposer();		
	}
		
	public void setImageIdentifier( String imageIdentifier )
	throws Exception
	{
		if( imageIdentifier.length() > 30 )
		{
			throw new Exception( "error: image identifier length > 30 characters" );
		}

		this.imageIdentifier = imageIdentifier;
	}
			
	public void addFileToRootDirectory( UDFImageBuilderFile myUDFImageBuilderFile )
	throws Exception
	{
		rootUDFImageBuilderFile.addChild( myUDFImageBuilderFile );
	}

	public void addFileToRootDirectory( File myFile )
	throws Exception
	{
		rootUDFImageBuilderFile.addChild( myFile );
	}
	
	public void writeImage( String filename, UDFRevision udfRevision )
	throws Exception
	{
		if( udfRevision == UDFRevision.Revision102 )
		{
			writeImageV102( filename );
		}
		else if( udfRevision == UDFRevision.Revision201 )
		{
			writeImageV201( filename );
		}
		else if( udfRevision == UDFRevision.Revision260 )
		{
			writeImageV260( filename );
		}
	}
	
	private void writeImageV102( String filename )
	throws Exception
	{
		/*
		 *	fixed prerequisites
		 */		
		int serialNumberForTags = 1;	// all tag serial numbers should be equal to the avdp descriptor tag serial number
		int descriptorVersion = 2;
		Calendar recordingTimeCalendar = Calendar.getInstance();
		byte udfVersionIdentifierSuffix[] = new byte[]{ 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		int minimumUDFReadRevision = 0x0102;
		int minimumUDFWriteRevision = (udfVersionIdentifierSuffix[1] << 8) | udfVersionIdentifierSuffix[0];
		int maximumUDFWriteRevision = (udfVersionIdentifierSuffix[1] << 8) | udfVersionIdentifierSuffix[0];

		
		// delete output file if already exists
		File outFile = new File( filename );
		if( outFile.exists() )
		{
			outFile.delete();			
		}
			
		RandomAccessFile myRandomAccessFile = new RandomAccessFile( filename, "rw" );
		
		//	write 32kb reserved area
		ReservedArea.write( myRandomAccessFile );
		
		//	write vrs sequence
		VolumeRecognitionSequence myVolumeRecognitionSequence = new VolumeRecognitionSequence( VolumeRecognitionSequence.NSRVersion.NSR02 );
		myVolumeRecognitionSequence.write( myRandomAccessFile );
		
		
		// setup logical volume integrity descriptor sequence location
		int LVIDSequenceStartingBlock = 273;
		int LVIDSequenceLength = blockSize * 4;

		
		// set partition start block, metadata file block and current block
		long partitionStartingBlock = 277;
		long currentBlock = partitionStartingBlock + 1;
		
		
		// setup and write fileset descriptor
		writeFilesetDescriptor( myRandomAccessFile,
							    currentBlock,				// target block
							    currentBlock + 1,			// root directory block
							    0,							// partition number
							    partitionStartingBlock,
							    recordingTimeCalendar,
							    serialNumberForTags,
							    udfVersionIdentifierSuffix,
							    descriptorVersion );
		currentBlock++;
		
		
		// write FIDs and FEs
		long nextFreeBlock = recursiveWriteFilesystem( myRandomAccessFile,
													  partitionStartingBlock,
													  blockSize,
													  serialNumberForTags,
													  rootUDFImageBuilderFile,
													  currentBlock,
													  null,
													  0,
													  false,
													  descriptorVersion );
		
		long partitionEndingBlock = nextFreeBlock;

		
		/*
		 *	set location for anchor volume descriptor pointers, main and reserver volume descriptor sequence
		 */
		long AVDP1Block = 256;
		long AVDP2Block = partitionEndingBlock + 16;
		long MVDSBlock = 257;
		long RVDSBlock = partitionEndingBlock;

		// write AVDPs at block 256 and block n
		writeAnchorVolumeDescriptorPointer( myRandomAccessFile, AVDP1Block, MVDSBlock, RVDSBlock, serialNumberForTags, descriptorVersion );
		writeAnchorVolumeDescriptorPointer( myRandomAccessFile, AVDP2Block, MVDSBlock, RVDSBlock, serialNumberForTags, descriptorVersion );
		
		
		/*
		 * 	set locations for volume descriptor sequence items
		 */
		long PVD1Block	= MVDSBlock;		// primary volume descriptor location (main volume descriptor sequence)
		long PVD2Block	= RVDSBlock;		// primary volume descriptor location (reserve volume descriptor sequence)
		long PD1Block	= MVDSBlock + 1;	// partition descriptor location (main volume descriptor sequence)
		long PD2Block	= RVDSBlock + 1;	// partition descriptor location (reserve volume descriptor sequence)
		long LVD1Block	= MVDSBlock + 2;	// logical volume descriptor location (main volume descriptor sequence)
		long LVD2Block	= RVDSBlock + 2;	// logical volume descriptor location (reserve volume descriptor sequence)
		long USD1Block	= MVDSBlock + 3;	// unallocated space descriptor location (main volume descriptor sequence)
		long USD2Block	= RVDSBlock + 3;	// unallocated space descriptor location (reserve volume descriptor sequence)
		long IUVD1Block	= MVDSBlock + 4;	// implementation use volume descriptor location (main volume descriptor sequence)
		long IUVD2Block	= RVDSBlock + 4;	// implementation use volume descriptor location (reserve volume descriptor sequence)
		long TD1Block	= MVDSBlock + 5;	// terminating descriptor location (main volume descriptor sequence)
		long TD2Block	= RVDSBlock + 5;	// terminating descriptor location (reserve volume descriptor sequence)
		
		
		// write PVDs
		writePrimaryVolumeDescriptor( myRandomAccessFile, 1, PVD1Block, recordingTimeCalendar, serialNumberForTags, descriptorVersion );
		writePrimaryVolumeDescriptor( myRandomAccessFile, 1, PVD2Block, recordingTimeCalendar, serialNumberForTags, descriptorVersion );
			
		// write PDs
		writePartitionDescriptor( myRandomAccessFile, 2, PD1Block, partitionStartingBlock, partitionEndingBlock, serialNumberForTags, descriptorVersion );
		writePartitionDescriptor( myRandomAccessFile, 2, PD2Block, partitionStartingBlock, partitionEndingBlock, serialNumberForTags, descriptorVersion );
		
		// write LVDs
		writeLogicalVolumeDescriptor( myRandomAccessFile, 3, LVD1Block, LVIDSequenceStartingBlock, LVIDSequenceLength, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
		writeLogicalVolumeDescriptor( myRandomAccessFile, 3, LVD2Block, LVIDSequenceStartingBlock, LVIDSequenceLength, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );

		// write USDs
		writeUnallocatedSpaceDescriptor( myRandomAccessFile, 4, USD1Block, 19, 256, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
		writeUnallocatedSpaceDescriptor( myRandomAccessFile, 4, USD2Block, 19, 256, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
				
		// write IUVDs
		writeImplementationUseVolumeDescriptor( myRandomAccessFile, 5, IUVD1Block, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
		writeImplementationUseVolumeDescriptor( myRandomAccessFile, 5, IUVD2Block, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
		
		// write TDs
		writeTerminatingDescriptor( myRandomAccessFile, TD1Block, serialNumberForTags, descriptorVersion );
		writeTerminatingDescriptor( myRandomAccessFile, TD2Block, serialNumberForTags, descriptorVersion );
		
		/*
		 * 	write LVIDS
		 */
		int currentLVIDSBlock = LVIDSequenceStartingBlock;		

		//write LVID
		long[] sizeTable = new long[1];
		long[] freeSpaceTable = new long[1];
		sizeTable[0] = partitionEndingBlock - partitionStartingBlock;
		freeSpaceTable[0] = 0;
		writeLogicalVolumeIntegrityDescriptor( myRandomAccessFile, currentLVIDSBlock, recordingTimeCalendar, serialNumberForTags, minimumUDFReadRevision, minimumUDFWriteRevision, maximumUDFWriteRevision, descriptorVersion, sizeTable, freeSpaceTable );		
		
		currentLVIDSBlock++;

		// write LVIDS TD
		writeTerminatingDescriptor( myRandomAccessFile, currentLVIDSBlock, serialNumberForTags, descriptorVersion );
		
		myRandomAccessFile.close();	
	}	

	private void writeImageV201( String filename )
	throws Exception
	{
		/*
		 *	fixed prerequisites
		 */		
		int serialNumberForTags = 1;	// all tag serial numbers should be equal to the avdp descriptor tag serial number
		int descriptorVersion = 3;
		Calendar recordingTimeCalendar = Calendar.getInstance();
		byte udfVersionIdentifierSuffix[] = new byte[]{ 0x01, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		int minimumUDFReadRevision = 0x0201;
		int minimumUDFWriteRevision = (udfVersionIdentifierSuffix[1] << 8) | udfVersionIdentifierSuffix[0];
		int maximumUDFWriteRevision = (udfVersionIdentifierSuffix[1] << 8) | udfVersionIdentifierSuffix[0];

		
		// delete output file if already exists
		File outFile = new File( filename );
		if( outFile.exists() )
		{
			outFile.delete();			
		}
			
		RandomAccessFile myRandomAccessFile = new RandomAccessFile( filename, "rw" );
		
		//	write 32kb reserved area
		ReservedArea.write( myRandomAccessFile );
		
		//	write vrs sequence		 
		VolumeRecognitionSequence myVolumeRecognitionSequence = new VolumeRecognitionSequence( VolumeRecognitionSequence.NSRVersion.NSR03 );
		myVolumeRecognitionSequence.write( myRandomAccessFile );
		
		
		// setup logical volume integrity descriptor sequence location
		int LVIDSequenceStartingBlock = 273;
		int LVIDSequenceLength = blockSize * 4;

		
		// set partition start block, metadata file block and current block
		long partitionStartingBlock = 277;
		long currentBlock = partitionStartingBlock + 1;
		
		
		// setup and write fileset descriptor
		writeFilesetDescriptor( myRandomAccessFile,
							    currentBlock,				// target block
							    currentBlock + 1,			// root directory block
							    0,							// partition number
							    partitionStartingBlock,
							    recordingTimeCalendar,
							    serialNumberForTags,
							    udfVersionIdentifierSuffix,
							    descriptorVersion );
		currentBlock++;
		
		
		// write FIDs and FEs
		long nextFreeBlock = recursiveWriteFilesystem( myRandomAccessFile,
													  partitionStartingBlock,
													  blockSize,
													  serialNumberForTags,
													  rootUDFImageBuilderFile,
													  currentBlock,
													  null,
													  0,
													  true,
													  descriptorVersion );
		
		long partitionEndingBlock = nextFreeBlock;

		
		/*
		 *	set location for anchor volume descriptor pointers, main and reserver volume descriptor sequence
		 */
		long AVDP1Block = 256;
		long AVDP2Block = partitionEndingBlock + 16;
		long MVDSBlock = 257;
		long RVDSBlock = partitionEndingBlock;

		// write AVDPs at block 256 and block n
		writeAnchorVolumeDescriptorPointer( myRandomAccessFile, AVDP1Block, MVDSBlock, RVDSBlock, serialNumberForTags, descriptorVersion );
		writeAnchorVolumeDescriptorPointer( myRandomAccessFile, AVDP2Block, MVDSBlock, RVDSBlock, serialNumberForTags, descriptorVersion );
		
		
		/*
		 * 	set locations for volume descriptor sequence items
		 */
		long PVD1Block	= MVDSBlock;		// primary volume descriptor location (main volume descriptor sequence)
		long PVD2Block	= RVDSBlock;		// primary volume descriptor location (reserve volume descriptor sequence)
		long PD1Block	= MVDSBlock + 1;	// partition descriptor location (main volume descriptor sequence)
		long PD2Block	= RVDSBlock + 1;	// partition descriptor location (reserve volume descriptor sequence)
		long LVD1Block	= MVDSBlock + 2;	// logical volume descriptor location (main volume descriptor sequence)
		long LVD2Block	= RVDSBlock + 2;	// logical volume descriptor location (reserve volume descriptor sequence)
		long USD1Block	= MVDSBlock + 3;	// unallocated space descriptor location (main volume descriptor sequence)
		long USD2Block	= RVDSBlock + 3;	// unallocated space descriptor location (reserve volume descriptor sequence)
		long IUVD1Block	= MVDSBlock + 4;	// implementation use volume descriptor location (main volume descriptor sequence)
		long IUVD2Block	= RVDSBlock + 4;	// implementation use volume descriptor location (reserve volume descriptor sequence)
		long TD1Block	= MVDSBlock + 5;	// terminating descriptor location (main volume descriptor sequence)
		long TD2Block	= RVDSBlock + 5;	// terminating descriptor location (reserve volume descriptor sequence)
		
		
		// write PVDs
		writePrimaryVolumeDescriptor( myRandomAccessFile, 1, PVD1Block, recordingTimeCalendar, serialNumberForTags, descriptorVersion );
		writePrimaryVolumeDescriptor( myRandomAccessFile, 1, PVD2Block, recordingTimeCalendar, serialNumberForTags, descriptorVersion );
			
		// write PDs
		writePartitionDescriptor( myRandomAccessFile, 2, PD1Block, partitionStartingBlock, partitionEndingBlock, serialNumberForTags, descriptorVersion );
		writePartitionDescriptor( myRandomAccessFile, 2, PD2Block, partitionStartingBlock, partitionEndingBlock, serialNumberForTags, descriptorVersion );
		
		// write LVDs
		writeLogicalVolumeDescriptor( myRandomAccessFile, 3, LVD1Block, LVIDSequenceStartingBlock, LVIDSequenceLength, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
		writeLogicalVolumeDescriptor( myRandomAccessFile, 3, LVD2Block, LVIDSequenceStartingBlock, LVIDSequenceLength, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );

		// write USDs
		writeUnallocatedSpaceDescriptor( myRandomAccessFile, 4, USD1Block, 19, 256, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
		writeUnallocatedSpaceDescriptor( myRandomAccessFile, 4, USD2Block, 19, 256, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
				
		// write IUVDs
		writeImplementationUseVolumeDescriptor( myRandomAccessFile, 5, IUVD1Block, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
		writeImplementationUseVolumeDescriptor( myRandomAccessFile, 5, IUVD2Block, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
		
		// write TDs
		writeTerminatingDescriptor( myRandomAccessFile, TD1Block, serialNumberForTags, descriptorVersion );
		writeTerminatingDescriptor( myRandomAccessFile, TD2Block, serialNumberForTags, descriptorVersion );
		
		/*
		 * 	write LVIDS
		 */
		int currentLVIDSBlock = LVIDSequenceStartingBlock;		

		//write LVID
		long[] sizeTable = new long[1];
		long[] freeSpaceTable = new long[1];
		sizeTable[0] = partitionEndingBlock - partitionStartingBlock;
		freeSpaceTable[0] = 0;
		writeLogicalVolumeIntegrityDescriptor( myRandomAccessFile, currentLVIDSBlock, recordingTimeCalendar, serialNumberForTags, minimumUDFReadRevision, minimumUDFWriteRevision, maximumUDFWriteRevision, descriptorVersion, sizeTable, freeSpaceTable );		

		currentLVIDSBlock++;

		// write LVIDS TD
		writeTerminatingDescriptor( myRandomAccessFile, currentLVIDSBlock, serialNumberForTags, descriptorVersion );
		
		myRandomAccessFile.close();	
	}
	
	private void writeImageV260( String filename )
	throws Exception
	{
		/*
		 *	fixed prerequisites
		 */		
		int serialNumberForTags = 1;	// all tag serial numbers should be equal to the avdp descriptor tag serial number
		int descriptorVersion = 3;
		Calendar recordingTimeCalendar = Calendar.getInstance();
		byte udfVersionIdentifierSuffix[] = new byte[]{ 0x60, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		int minimumUDFReadRevision = 0x0250;
		int minimumUDFWriteRevision = (udfVersionIdentifierSuffix[1] << 8) | udfVersionIdentifierSuffix[0];
		int maximumUDFWriteRevision = (udfVersionIdentifierSuffix[1] << 8) | udfVersionIdentifierSuffix[0];		
		
		
		// delete output file if already exists
		File outFile = new File( filename );
		if( outFile.exists() )
		{
			outFile.delete();			
		}
			
		RandomAccessFile myRandomAccessFile = new RandomAccessFile( filename, "rw" );
		
		//	write 32kb reserved area
		ReservedArea.write( myRandomAccessFile );
		
		//	write vrs sequence		 
		VolumeRecognitionSequence myVolumeRecognitionSequence = new VolumeRecognitionSequence( VolumeRecognitionSequence.NSRVersion.NSR03 );
		myVolumeRecognitionSequence.write( myRandomAccessFile );
		
		
		// setup logical volume integrity descriptor sequence location
		int LVIDSequenceStartingBlock = 273;
		int LVIDSequenceLength = blockSize * 4;

		
		// setup some locations
		long partitionStartingBlock = 277;
		long mainMetadataFileBlock = partitionStartingBlock + 1;		
		long metadataPartitionStartingBlock = partitionStartingBlock + 2; 
		long filesetDescriptorBlock = partitionStartingBlock + 2;
		long rootDirectoryBlock = partitionStartingBlock + 3;
		
		int metadataAllocationUnitSize = 32;
		int metadataAlignmentUnitSize = 1;
		
		
		// setup and write fileset descriptor
		writeFilesetDescriptor( myRandomAccessFile,
								filesetDescriptorBlock,			// target block
								rootDirectoryBlock,				// root directory block
								1,
							    metadataPartitionStartingBlock,
							    recordingTimeCalendar,
							    serialNumberForTags,
							    udfVersionIdentifierSuffix,
							    descriptorVersion );
		
		
		// setup metadata file
		ExtendedFileEntry metadataExtendedFileEntry = new ExtendedFileEntry();
		
		metadataExtendedFileEntry.DescriptorTag.TagSerialNumber = serialNumberForTags;
		metadataExtendedFileEntry.DescriptorTag.DescriptorVersion = 3;		

		metadataExtendedFileEntry.Uid = 0xFFFFFFFF;
		metadataExtendedFileEntry.Gid = 0xFFFFFFFF;
		
		metadataExtendedFileEntry.AccessTime = new Timestamp( recordingTimeCalendar );
		metadataExtendedFileEntry.ModificationTime = new Timestamp( recordingTimeCalendar );
		metadataExtendedFileEntry.AttributeTime = new Timestamp( recordingTimeCalendar );
		metadataExtendedFileEntry.CreationTime = new Timestamp( recordingTimeCalendar );
		
		metadataExtendedFileEntry.Checkpoint = 1;
				
		metadataExtendedFileEntry.ImplementationIdentifier.setIdentifier( applicationIdentifier );
		metadataExtendedFileEntry.ImplementationIdentifier.IdentifierSuffix = applicationIdentifierSuffix;
				
		metadataExtendedFileEntry.ICBTag.Flags = 0;									// storage type short_ad		
		metadataExtendedFileEntry.ICBTag.PriorRecordedNumberofDirectEntries = 0;		
		metadataExtendedFileEntry.ICBTag.NumberofEntries = 1;
		metadataExtendedFileEntry.ICBTag.StrategyType = 4;		
		
		long metadataFileLength = 1 + recursiveGetMetadataFileLength( rootUDFImageBuilderFile, blockSize );
		if( metadataFileLength % metadataAllocationUnitSize != 0 )
		{
			metadataFileLength += metadataAllocationUnitSize - (metadataFileLength % metadataAllocationUnitSize);  
		}
		
		Short_ad metadataAllocationDescriptor = new Short_ad();				
		metadataAllocationDescriptor.ExtentPosition = metadataPartitionStartingBlock - partitionStartingBlock;
		metadataAllocationDescriptor.ExtentLength = metadataFileLength * blockSize;
		
		metadataExtendedFileEntry.LogicalBlocksRecorded = metadataFileLength;
		metadataExtendedFileEntry.InformationLength = metadataFileLength * blockSize;
		metadataExtendedFileEntry.ObjectSize = metadataFileLength * blockSize;
		metadataExtendedFileEntry.AllocationDescriptors = metadataAllocationDescriptor.getBytes();
		metadataExtendedFileEntry.LengthofAllocationDescriptors = metadataExtendedFileEntry.AllocationDescriptors.length;		
		
		// write main metadata file
		metadataExtendedFileEntry.DescriptorTag.TagLocation = mainMetadataFileBlock - partitionStartingBlock;
		metadataExtendedFileEntry.ICBTag.FileType = (byte)250; 						//  main metadata file
		myRandomAccessFile.seek( mainMetadataFileBlock * blockSize );
		metadataExtendedFileEntry.write( myRandomAccessFile, blockSize );
		
		long currentMetadataBlock = filesetDescriptorBlock + 1;
		long currentFiledataBlock = filesetDescriptorBlock + 1 + metadataFileLength; 

		
		// write FIDs and FEs
		long nextFreeBlocks[] = recursiveWriteFilesystemWithMetadata( myRandomAccessFile,
													  		 	   	  partitionStartingBlock,
													  		 	   	  metadataPartitionStartingBlock,
													  		 	   	  blockSize,
													  		 	   	  serialNumberForTags,
													  		 	   	  rootUDFImageBuilderFile,
													  		 	   	  currentMetadataBlock,
													  		 	   	  currentFiledataBlock,
													  		 	   	  null,
													  		 	   	  0,
													  		 	   	  descriptorVersion );
		
		// write mirror metadata file
		long mirrorMetadataFileBlock = nextFreeBlocks[ 1 ];
		metadataExtendedFileEntry.DescriptorTag.TagLocation = mirrorMetadataFileBlock - partitionStartingBlock;
		metadataExtendedFileEntry.ICBTag.FileType = (byte)251; 						//  mirror metadata file
		myRandomAccessFile.seek( mirrorMetadataFileBlock * blockSize );
		metadataExtendedFileEntry.write( myRandomAccessFile, blockSize );		
		
		long partitionEndingBlock = mirrorMetadataFileBlock + 1;

		
		/*
		 *	set location for anchor volume descriptor pointers, main and reserver volume descriptor sequence
		 */
		long AVDP1Block = 256;
		long AVDP2Block = partitionEndingBlock + 16;
		long MVDSBlock = 257;
		long RVDSBlock = partitionEndingBlock;

		// write AVDPs at block 256 and block n
		writeAnchorVolumeDescriptorPointer( myRandomAccessFile, AVDP1Block, MVDSBlock, RVDSBlock, serialNumberForTags, descriptorVersion );
		writeAnchorVolumeDescriptorPointer( myRandomAccessFile, AVDP2Block, MVDSBlock, RVDSBlock, serialNumberForTags, descriptorVersion );
		
		
		/*
		 * 	set locations for volume descriptor sequence items
		 */
		long PVD1Block	= MVDSBlock;		// primary volume descriptor location (main volume descriptor sequence)
		long PVD2Block	= RVDSBlock;		// primary volume descriptor location (reserve volume descriptor sequence)
		long PD1Block	= MVDSBlock + 1;	// partition descriptor location (main volume descriptor sequence)
		long PD2Block	= RVDSBlock + 1;	// partition descriptor location (reserve volume descriptor sequence)
		long LVD1Block	= MVDSBlock + 2;	// logical volume descriptor location (main volume descriptor sequence)
		long LVD2Block	= RVDSBlock + 2;	// logical volume descriptor location (reserve volume descriptor sequence)
		long USD1Block	= MVDSBlock + 3;	// unallocated space descriptor location (main volume descriptor sequence)
		long USD2Block	= RVDSBlock + 3;	// unallocated space descriptor location (reserve volume descriptor sequence)
		long IUVD1Block	= MVDSBlock + 4;	// implementation use volume descriptor location (main volume descriptor sequence)
		long IUVD2Block	= RVDSBlock + 4;	// implementation use volume descriptor location (reserve volume descriptor sequence)
		long TD1Block	= MVDSBlock + 5;	// terminating descriptor location (main volume descriptor sequence)
		long TD2Block	= RVDSBlock + 5;	// terminating descriptor location (reserve volume descriptor sequence)
		
		
		// write PVDs
		writePrimaryVolumeDescriptor( myRandomAccessFile, 1, PVD1Block, recordingTimeCalendar, serialNumberForTags, descriptorVersion );
		writePrimaryVolumeDescriptor( myRandomAccessFile, 1, PVD2Block, recordingTimeCalendar, serialNumberForTags, descriptorVersion );
			
		// write PDs
		writePartitionDescriptor( myRandomAccessFile, 2, PD1Block, partitionStartingBlock, partitionEndingBlock, serialNumberForTags, descriptorVersion );
		writePartitionDescriptor( myRandomAccessFile, 2, PD2Block, partitionStartingBlock, partitionEndingBlock, serialNumberForTags, descriptorVersion );
		
		// write LVDs
		writeLogicalVolumeDescriptor( myRandomAccessFile, 3, LVD1Block, LVIDSequenceStartingBlock, LVIDSequenceLength, serialNumberForTags, mainMetadataFileBlock - partitionStartingBlock, mirrorMetadataFileBlock - partitionStartingBlock, metadataAllocationUnitSize, metadataAlignmentUnitSize, udfVersionIdentifierSuffix, descriptorVersion, 1, filesetDescriptorBlock - metadataPartitionStartingBlock );
		writeLogicalVolumeDescriptor( myRandomAccessFile, 3, LVD2Block, LVIDSequenceStartingBlock, LVIDSequenceLength, serialNumberForTags, mainMetadataFileBlock - partitionStartingBlock, mirrorMetadataFileBlock - partitionStartingBlock, metadataAllocationUnitSize, metadataAlignmentUnitSize, udfVersionIdentifierSuffix, descriptorVersion, 1, filesetDescriptorBlock - metadataPartitionStartingBlock );

		// write USDs
		writeUnallocatedSpaceDescriptor( myRandomAccessFile, 4, USD1Block, 19, 256, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
		writeUnallocatedSpaceDescriptor( myRandomAccessFile, 4, USD2Block, 19, 256, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
				
		// write IUVDs
		writeImplementationUseVolumeDescriptor( myRandomAccessFile, 5, IUVD1Block, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
		writeImplementationUseVolumeDescriptor( myRandomAccessFile, 5, IUVD2Block, serialNumberForTags, udfVersionIdentifierSuffix, descriptorVersion );
		
		// write TDs
		writeTerminatingDescriptor( myRandomAccessFile, TD1Block, serialNumberForTags, descriptorVersion );
		writeTerminatingDescriptor( myRandomAccessFile, TD2Block, serialNumberForTags, descriptorVersion );
		
		/*
		 * 	write LVIDS
		 */
		int currentLVIDSBlock = LVIDSequenceStartingBlock;		

		//write LVID
		long[] sizeTable = new long[2];
		long[] freeSpaceTable = new long[2];
		sizeTable[0] = partitionEndingBlock - partitionStartingBlock;
		sizeTable[1] = metadataFileLength;
		freeSpaceTable[0] = 0;
		freeSpaceTable[1] = 0;
		writeLogicalVolumeIntegrityDescriptor( myRandomAccessFile, currentLVIDSBlock, recordingTimeCalendar, serialNumberForTags, minimumUDFReadRevision, minimumUDFWriteRevision, maximumUDFWriteRevision, descriptorVersion, sizeTable, freeSpaceTable );		

		currentLVIDSBlock++;

		// write LVIDS TD
		writeTerminatingDescriptor( myRandomAccessFile, currentLVIDSBlock, serialNumberForTags, descriptorVersion );
		
		myRandomAccessFile.close();	
	}
	
	
	private long recursiveWriteFilesystem( RandomAccessFile myRandomAccessFile, long partitionStartingBlock, int blockSize, int serialNumberForTags, UDFImageBuilderFile currentUDFImageBuilderFile, long currentBlock,	FileEntry parentFileEntry, long uniqueID, boolean writeExtendedFileEntries, int descriptorVersion )
	throws Exception
	{	
		FileEntry myFileEntry = null;
		
		if( !writeExtendedFileEntries )
		{
			myFileEntry = new FileEntry();
		}
		else
		{
			myFileEntry = new ExtendedFileEntry();
		}

		myFileEntry.DescriptorTag.TagSerialNumber = serialNumberForTags;
		myFileEntry.DescriptorTag.DescriptorVersion = descriptorVersion;
		myFileEntry.DescriptorTag.TagLocation = currentBlock - partitionStartingBlock;
		
		myFileEntry.Uid = 0xFFFFFFFF;	// TODO: get current uid and gid if java supports it
		myFileEntry.Gid = 0xFFFFFFFF;
		
		// TODO: get real file permission if java supports it
		myFileEntry.Permissions = Permissions.OTHER_Read | Permissions.GROUP_Read | Permissions.OWNER_Read; 
		
		myFileEntry.FileLinkCount = currentUDFImageBuilderFile.getFileLinkCount();
				
		myFileEntry.RecordFormat = 0;
		myFileEntry.RecordDisplayAttributes = 0;
		myFileEntry.RecordLength = 0;
			
		myFileEntry.AccessTime = new Timestamp( currentUDFImageBuilderFile.getAccessTime() );
		myFileEntry.ModificationTime = new Timestamp( currentUDFImageBuilderFile.getModificationTime() ); 
		myFileEntry.AttributeTime = new Timestamp( currentUDFImageBuilderFile.getAttributeTime() );		
		
		myFileEntry.Checkpoint = 1;
				
		myFileEntry.ImplementationIdentifier.setIdentifier( applicationIdentifier );
		myFileEntry.ImplementationIdentifier.IdentifierSuffix = applicationIdentifierSuffix;
		
		myFileEntry.ICBTag.PriorRecordedNumberofDirectEntries = 0;		
		myFileEntry.ICBTag.NumberofEntries = 1;
		myFileEntry.ICBTag.StrategyType = 4;
		
		myFileEntry.UniqueID =  uniqueID;
		
		long nextFreeBlock = currentBlock + 1;
		
		/*
		 *	if file is a directory
		 */ 
		if( currentUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.Directory )
		{	
			myFileEntry.ICBTag.FileType = 4;	// directory
			
			myFileEntry.Permissions |= Permissions.OTHER_Execute | Permissions.GROUP_Execute | Permissions.OWNER_Execute;
			
			// create file identifier descriptors for all child files
			UDFImageBuilderFile childUDFImageBuilderFiles[] = currentUDFImageBuilderFile.getChilds();
			
			ArrayList<FileIdentifierDescriptor> childFileIdentifierDescriptors = new ArrayList<FileIdentifierDescriptor>();

			// parent directory FID
			FileIdentifierDescriptor parentDirectoryFileIdentifierDescriptor = new FileIdentifierDescriptor();
			
			parentDirectoryFileIdentifierDescriptor.DescriptorTag.TagLocation = currentBlock - partitionStartingBlock;
			parentDirectoryFileIdentifierDescriptor.DescriptorTag.TagSerialNumber = serialNumberForTags;
			parentDirectoryFileIdentifierDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
			
			parentDirectoryFileIdentifierDescriptor.ICB.ExtentLength = blockSize;
			parentDirectoryFileIdentifierDescriptor.ICB.ExtentLocation.part_num = 0;
			
			parentDirectoryFileIdentifierDescriptor.FileVersionNumber = 1;
			parentDirectoryFileIdentifierDescriptor.FileCharacteristics = 10; // file is directory and parent

			// if root directory
			if( parentFileEntry == null )
			{
				parentDirectoryFileIdentifierDescriptor.ICB.ExtentLocation.lb_num = currentBlock - partitionStartingBlock;
			}
			// if non root directory			
			else
			{
				parentDirectoryFileIdentifierDescriptor.ICB.ExtentLocation.lb_num = parentFileEntry.DescriptorTag.TagLocation;

				parentDirectoryFileIdentifierDescriptor.ICB.implementationUse = new byte[ 6 ];				
				parentDirectoryFileIdentifierDescriptor.ICB.implementationUse[ 2 ] = (byte)(parentFileEntry.UniqueID & 0xFF);
				parentDirectoryFileIdentifierDescriptor.ICB.implementationUse[ 3 ] = (byte)((parentFileEntry.UniqueID >> 8) & 0xFF);
				parentDirectoryFileIdentifierDescriptor.ICB.implementationUse[ 4 ] = (byte)((parentFileEntry.UniqueID >> 16) & 0xFF);
				parentDirectoryFileIdentifierDescriptor.ICB.implementationUse[ 5 ] = (byte)((parentFileEntry.UniqueID >> 32) & 0xFF);
			}
			
			childFileIdentifierDescriptors.add( parentDirectoryFileIdentifierDescriptor );
			
			// child file FIDs
			for( int i = 0; i < childUDFImageBuilderFiles.length; ++i )
			{
				long childFileUniqueID = myUniqueIdDisposer.getNextUniqueId();
				
				FileIdentifierDescriptor childFileIdentifierDescriptor = new FileIdentifierDescriptor();
				
				childFileIdentifierDescriptor.DescriptorTag.TagLocation = currentBlock - partitionStartingBlock;
				childFileIdentifierDescriptor.DescriptorTag.TagSerialNumber = serialNumberForTags;
				childFileIdentifierDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
				
				childFileIdentifierDescriptor.ICB.ExtentLength = blockSize;
				childFileIdentifierDescriptor.ICB.ExtentLocation.lb_num = nextFreeBlock - partitionStartingBlock;
				childFileIdentifierDescriptor.ICB.ExtentLocation.part_num = 0;

				childFileIdentifierDescriptor.ICB.implementationUse = new byte[ 6 ];				
				childFileIdentifierDescriptor.ICB.implementationUse[ 2 ] = (byte)(childFileUniqueID & 0xFF);
				childFileIdentifierDescriptor.ICB.implementationUse[ 3 ] = (byte)((childFileUniqueID >> 8) & 0xFF);
				childFileIdentifierDescriptor.ICB.implementationUse[ 4 ] = (byte)((childFileUniqueID >> 16) & 0xFF);
				childFileIdentifierDescriptor.ICB.implementationUse[ 5 ] = (byte)((childFileUniqueID >> 32) & 0xFF);
				
				childFileIdentifierDescriptor.FileVersionNumber = 1;
				
				childFileIdentifierDescriptor.setFileIdentifier( childUDFImageBuilderFiles[ i ].getIdentifier() );

				if( childUDFImageBuilderFiles[ i ].getFileType() == UDFImageBuilderFile.FileType.Directory )
				{
					childFileIdentifierDescriptor.FileCharacteristics = 2;
				}
				
				childFileIdentifierDescriptors.add( childFileIdentifierDescriptor );
				
				nextFreeBlock = recursiveWriteFilesystem( myRandomAccessFile, partitionStartingBlock, blockSize, serialNumberForTags, childUDFImageBuilderFiles[ i ], nextFreeBlock, myFileEntry, childFileUniqueID, writeExtendedFileEntries, descriptorVersion );				
			}
			
			
			// get directory file data length
			int directoryFileDataLength = 0;
			for( int i = 0; i < childFileIdentifierDescriptors.size(); ++i )
			{
				directoryFileDataLength += childFileIdentifierDescriptors.get( i ).getLength();
			}
			
			myFileEntry.InformationLength = directoryFileDataLength;			
						
			if( ( writeExtendedFileEntries && directoryFileDataLength <= blockSize - ExtendedFileEntry.fixedPartLength )
				|| ( !writeExtendedFileEntries && directoryFileDataLength <= blockSize - FileEntry.fixedPartLength ) )
			{				
				// inline embedded file data
				myFileEntry.ICBTag.Flags = 3;		// storage type inline		
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
				allocationDescriptor.ExtentPosition = nextFreeBlock - partitionStartingBlock;					
				
				long currentRealPosition = nextFreeBlock * blockSize;
				myRandomAccessFile.seek( currentRealPosition  );
				
				for( int i = 0; i < childFileIdentifierDescriptors.size(); ++i )
				{
					long tagLocationBlock = (long)(currentRealPosition / blockSize) - partitionStartingBlock;
					
					FileIdentifierDescriptor childFileIdentifierDescriptor = childFileIdentifierDescriptors.get( i );
					
					childFileIdentifierDescriptor.DescriptorTag.TagLocation = tagLocationBlock;
					
					byte childFileIdentifierDescriptorBytes[] = childFileIdentifierDescriptors.get( i ).getBytes();
					myRandomAccessFile.write( childFileIdentifierDescriptorBytes );
					
					currentRealPosition += childFileIdentifierDescriptorBytes.length;
				}				
				
				nextFreeBlock += myFileEntry.LogicalBlocksRecorded;
				
				myFileEntry.AllocationDescriptors = allocationDescriptor.getBytes();
				myFileEntry.LengthofAllocationDescriptors = myFileEntry.AllocationDescriptors.length;
			}			
		}
		
		/*
		 *	if file is a "normal" file
		 */ 
		else if(  currentUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.File )
		{
			myFileEntry.ICBTag.FileType = 5;	// normal file
			
			long fileSize = currentUDFImageBuilderFile.getFileLength();
			
			myFileEntry.InformationLength = fileSize;
			

			if( ( writeExtendedFileEntries && fileSize <= ( blockSize - ExtendedFileEntry.fixedPartLength ) )
				|| ( !writeExtendedFileEntries && fileSize <= ( blockSize - FileEntry.fixedPartLength ) ) )
			{
				// store as inline embedded file data
				myFileEntry.ICBTag.Flags = 3;		// storage type inline		
				myFileEntry.LogicalBlocksRecorded = 0;
				myFileEntry.LengthofAllocationDescriptors = fileSize;
				myFileEntry.AllocationDescriptors = new byte[(int)fileSize];
				currentUDFImageBuilderFile.readFileData( myFileEntry.AllocationDescriptors );				
			}
			else
			{
				// store as exernal file data with Long_ad
				myFileEntry.ICBTag.Flags = 1;		// storage type long_ad
				
				myFileEntry.LogicalBlocksRecorded = (long)(fileSize / blockSize);
				if( fileSize % blockSize != 0 )
				{
					myFileEntry.LogicalBlocksRecorded++;
				}
				
				ArrayList<Long_ad> allocationDescriptors = new ArrayList<Long_ad>();
				
				long restFileSize = fileSize;
				long currentExtentPosition = nextFreeBlock - partitionStartingBlock;
				
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
								
				myRandomAccessFile.seek( nextFreeBlock * blockSize );
				writeFileData( myRandomAccessFile, currentUDFImageBuilderFile.getSourceFile() );				
				
				nextFreeBlock += myFileEntry.LogicalBlocksRecorded;
				
				myFileEntry.AllocationDescriptors = allocationDescriptorBytes;
				myFileEntry.LengthofAllocationDescriptors = allocationDescriptorBytes.length;				
			}
				
		}		
		
		if( writeExtendedFileEntries )
		{
			ExtendedFileEntry myExtendedFileEntry = (ExtendedFileEntry)myFileEntry;
			myExtendedFileEntry.ObjectSize = myFileEntry.InformationLength;
			myExtendedFileEntry.CreationTime = new Timestamp( currentUDFImageBuilderFile.getCreationTime() );
		}
		
		myRandomAccessFile.seek( currentBlock * blockSize );
		myFileEntry.write( myRandomAccessFile, blockSize );
	
		return nextFreeBlock;	
	}

	private long[] recursiveWriteFilesystemWithMetadata( RandomAccessFile myRandomAccessFile, long partitionStartingBlock, long metadataPartitionStartingBlock, int blockSize, int serialNumberForTags, UDFImageBuilderFile currentUDFImageBuilderFile, long currentMetadataBlock,	long currentFiledataBlock, FileEntry parentFileEntry, long uniqueID, int descriptorVersion )
	throws Exception
	{
		long[] nextFreeBlocks = new long[2];
		
		ExtendedFileEntry myExtendedFileEntry = new ExtendedFileEntry();		

		myExtendedFileEntry.DescriptorTag.TagSerialNumber = serialNumberForTags;
		myExtendedFileEntry.DescriptorTag.DescriptorVersion = descriptorVersion;
		myExtendedFileEntry.DescriptorTag.TagLocation = currentMetadataBlock - metadataPartitionStartingBlock;
		
		myExtendedFileEntry.Uid = 0xFFFFFFFF;	// TODO: get current uid and gid if java supports it
		myExtendedFileEntry.Gid = 0xFFFFFFFF;
		
		// TODO: get real file permission if java supports it
		myExtendedFileEntry.Permissions = Permissions.OTHER_Read | Permissions.GROUP_Read | Permissions.OWNER_Read; 
		
		myExtendedFileEntry.FileLinkCount = currentUDFImageBuilderFile.getFileLinkCount();
				
		myExtendedFileEntry.RecordFormat = 0;
		myExtendedFileEntry.RecordDisplayAttributes = 0;
		myExtendedFileEntry.RecordLength = 0;
			
		myExtendedFileEntry.AccessTime = new Timestamp( currentUDFImageBuilderFile.getAccessTime() );
		myExtendedFileEntry.ModificationTime = new Timestamp( currentUDFImageBuilderFile.getModificationTime() ); 
		myExtendedFileEntry.AttributeTime = new Timestamp( currentUDFImageBuilderFile.getAttributeTime() );		
		
		myExtendedFileEntry.Checkpoint = 1;
				
		myExtendedFileEntry.ImplementationIdentifier.setIdentifier( applicationIdentifier );
		myExtendedFileEntry.ImplementationIdentifier.IdentifierSuffix = applicationIdentifierSuffix;
		
		myExtendedFileEntry.ICBTag.PriorRecordedNumberofDirectEntries = 0;		
		myExtendedFileEntry.ICBTag.NumberofEntries = 1;
		myExtendedFileEntry.ICBTag.StrategyType = 4;
		
		myExtendedFileEntry.UniqueID =  uniqueID;
		
		nextFreeBlocks[0] = currentMetadataBlock + 1;
		nextFreeBlocks[1] = currentFiledataBlock;
		
		/*
		 *	if file is a directory
		 */ 
		if( currentUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.Directory )
		{	
			myExtendedFileEntry.ICBTag.FileType = 4;	// directory
			
			myExtendedFileEntry.Permissions |= Permissions.OTHER_Execute | Permissions.GROUP_Execute | Permissions.OWNER_Execute;
			
			// create file identifier descriptors for all child files
			UDFImageBuilderFile childUDFImageBuilderFiles[] = currentUDFImageBuilderFile.getChilds();
			
			ArrayList<FileIdentifierDescriptor> childFileIdentifierDescriptors = new ArrayList<FileIdentifierDescriptor>();

			// parent directory FID
			FileIdentifierDescriptor parentDirectoryFileIdentifierDescriptor = new FileIdentifierDescriptor();
			
			parentDirectoryFileIdentifierDescriptor.DescriptorTag.TagLocation = currentMetadataBlock - metadataPartitionStartingBlock;
			parentDirectoryFileIdentifierDescriptor.DescriptorTag.TagSerialNumber = serialNumberForTags;
			parentDirectoryFileIdentifierDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
			
			parentDirectoryFileIdentifierDescriptor.ICB.ExtentLength = blockSize;
			parentDirectoryFileIdentifierDescriptor.ICB.ExtentLocation.part_num = 1;
			
			parentDirectoryFileIdentifierDescriptor.FileVersionNumber = 1;
			parentDirectoryFileIdentifierDescriptor.FileCharacteristics = 10; // file is directory and parent

			// if root directory
			if( parentFileEntry == null )
			{
				parentDirectoryFileIdentifierDescriptor.ICB.ExtentLocation.lb_num = currentMetadataBlock - metadataPartitionStartingBlock;
			}
			// if non root directory			
			else
			{
				parentDirectoryFileIdentifierDescriptor.ICB.ExtentLocation.lb_num = parentFileEntry.DescriptorTag.TagLocation;

				parentDirectoryFileIdentifierDescriptor.ICB.implementationUse = new byte[ 6 ];				
				parentDirectoryFileIdentifierDescriptor.ICB.implementationUse[ 2 ] = (byte)(parentFileEntry.UniqueID & 0xFF);
				parentDirectoryFileIdentifierDescriptor.ICB.implementationUse[ 3 ] = (byte)((parentFileEntry.UniqueID >> 8) & 0xFF);
				parentDirectoryFileIdentifierDescriptor.ICB.implementationUse[ 4 ] = (byte)((parentFileEntry.UniqueID >> 16) & 0xFF);
				parentDirectoryFileIdentifierDescriptor.ICB.implementationUse[ 5 ] = (byte)((parentFileEntry.UniqueID >> 32) & 0xFF);
			}
			
			childFileIdentifierDescriptors.add( parentDirectoryFileIdentifierDescriptor );
			
			// child file FIDs
			for( int i = 0; i < childUDFImageBuilderFiles.length; ++i )
			{
				long childFileUniqueID = myUniqueIdDisposer.getNextUniqueId();
				
				FileIdentifierDescriptor childFileIdentifierDescriptor = new FileIdentifierDescriptor();
				
				childFileIdentifierDescriptor.DescriptorTag.TagLocation = currentMetadataBlock - metadataPartitionStartingBlock;
				childFileIdentifierDescriptor.DescriptorTag.TagSerialNumber = serialNumberForTags;
				childFileIdentifierDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
				
				childFileIdentifierDescriptor.ICB.ExtentLength = blockSize;
				childFileIdentifierDescriptor.ICB.ExtentLocation.lb_num = nextFreeBlocks[ 0 ] - metadataPartitionStartingBlock;
				childFileIdentifierDescriptor.ICB.ExtentLocation.part_num = 1;

				childFileIdentifierDescriptor.ICB.implementationUse = new byte[ 6 ];				
				childFileIdentifierDescriptor.ICB.implementationUse[ 2 ] = (byte)(childFileUniqueID & 0xFF);
				childFileIdentifierDescriptor.ICB.implementationUse[ 3 ] = (byte)((childFileUniqueID >> 8) & 0xFF);
				childFileIdentifierDescriptor.ICB.implementationUse[ 4 ] = (byte)((childFileUniqueID >> 16) & 0xFF);
				childFileIdentifierDescriptor.ICB.implementationUse[ 5 ] = (byte)((childFileUniqueID >> 32) & 0xFF);
				
				childFileIdentifierDescriptor.FileVersionNumber = 1;
				
				childFileIdentifierDescriptor.setFileIdentifier( childUDFImageBuilderFiles[ i ].getIdentifier() );

				if( childUDFImageBuilderFiles[ i ].getFileType() == UDFImageBuilderFile.FileType.Directory )
				{
					childFileIdentifierDescriptor.FileCharacteristics = 2;
				}
				
				childFileIdentifierDescriptors.add( childFileIdentifierDescriptor );
				
				nextFreeBlocks = recursiveWriteFilesystemWithMetadata( myRandomAccessFile, partitionStartingBlock, metadataPartitionStartingBlock, blockSize, serialNumberForTags, childUDFImageBuilderFiles[ i ], nextFreeBlocks[ 0 ], nextFreeBlocks[ 1 ], myExtendedFileEntry, childFileUniqueID, descriptorVersion );				
			}
			
			
			// get directory file data length
			int directoryFileDataLength = 0;
			for( int i = 0; i < childFileIdentifierDescriptors.size(); ++i )
			{
				directoryFileDataLength += childFileIdentifierDescriptors.get( i ).getLength();
			}
			
			myExtendedFileEntry.InformationLength = directoryFileDataLength;			
						
			if( directoryFileDataLength <= blockSize - ExtendedFileEntry.fixedPartLength )
			{				
				// inline embedded file data
				myExtendedFileEntry.ICBTag.Flags = 3;		// storage type inline		
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
				allocationDescriptor.ExtentPosition = nextFreeBlocks[ 0 ] - metadataPartitionStartingBlock;					
				
				long currentRealPosition = nextFreeBlocks[ 0 ] * blockSize;
				myRandomAccessFile.seek( currentRealPosition  );
				
				for( int i = 0; i < childFileIdentifierDescriptors.size(); ++i )
				{
					long tagLocationBlock = (long)(currentRealPosition / blockSize) - metadataPartitionStartingBlock;
					
					FileIdentifierDescriptor childFileIdentifierDescriptor = childFileIdentifierDescriptors.get( i );
					
					childFileIdentifierDescriptor.DescriptorTag.TagLocation = tagLocationBlock;
					
					byte childFileIdentifierDescriptorBytes[] = childFileIdentifierDescriptors.get( i ).getBytes();
					myRandomAccessFile.write( childFileIdentifierDescriptorBytes );
					
					currentRealPosition += childFileIdentifierDescriptorBytes.length;
				}				
				
				nextFreeBlocks[ 0 ] += myExtendedFileEntry.LogicalBlocksRecorded;
				
				myExtendedFileEntry.AllocationDescriptors = allocationDescriptor.getBytes();
				myExtendedFileEntry.LengthofAllocationDescriptors = myExtendedFileEntry.AllocationDescriptors.length;
			}			
		}
		
		/*
		 *	if file is a "normal" file
		 */ 
		else if(  currentUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.File )
		{
			myExtendedFileEntry.ICBTag.FileType = 5;	// normal file
			
			long fileSize = currentUDFImageBuilderFile.getFileLength();
			
			myExtendedFileEntry.InformationLength = fileSize;
			

			if( fileSize <= ( blockSize - ExtendedFileEntry.fixedPartLength ) )
			{
				// store as inline embedded file data
				myExtendedFileEntry.ICBTag.Flags = 3;		// storage type inline		
				myExtendedFileEntry.LogicalBlocksRecorded = 0;
				myExtendedFileEntry.LengthofAllocationDescriptors = fileSize;
				myExtendedFileEntry.AllocationDescriptors = new byte[(int)fileSize];
				currentUDFImageBuilderFile.readFileData( myExtendedFileEntry.AllocationDescriptors );				
			}
			else
			{
				// store as exernal file data with Long_ad
				myExtendedFileEntry.ICBTag.Flags = 1;		// storage type long_ad
				
				myExtendedFileEntry.LogicalBlocksRecorded = (long)(fileSize / blockSize);
				if( fileSize % blockSize != 0 )
				{
					myExtendedFileEntry.LogicalBlocksRecorded++;
				}
				
				ArrayList<Long_ad> allocationDescriptors = new ArrayList<Long_ad>();
				
				long restFileSize = fileSize;
				long currentExtentPosition = nextFreeBlocks[ 1 ] - partitionStartingBlock;
				
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
								
				myRandomAccessFile.seek( nextFreeBlocks[ 1 ] * blockSize );
				writeFileData( myRandomAccessFile, currentUDFImageBuilderFile.getSourceFile() );				
				
				nextFreeBlocks[ 1 ] += myExtendedFileEntry.LogicalBlocksRecorded;
				
				myExtendedFileEntry.AllocationDescriptors = allocationDescriptorBytes;
				myExtendedFileEntry.LengthofAllocationDescriptors = allocationDescriptorBytes.length;				
			}
				
		}		
		
		myExtendedFileEntry.ObjectSize = myExtendedFileEntry.InformationLength;
		myExtendedFileEntry.CreationTime = new Timestamp( currentUDFImageBuilderFile.getCreationTime() );
		
		myRandomAccessFile.seek( currentMetadataBlock * blockSize );
		myExtendedFileEntry.write( myRandomAccessFile, blockSize );
		
		return nextFreeBlocks;
	}
	
	
	private void writeFileData( RandomAccessFile myRandomAccessFile, File sourceFile )
	throws IOException
	{
		RandomAccessFile sourceRandomAccessFile = new RandomAccessFile( sourceFile, "r" );
		
		byte buffer[] = new byte[ 32768 ];
		int bytesRead = 0;
		
		while( ( bytesRead = sourceRandomAccessFile.read( buffer ) ) > 0 )
		{
			myRandomAccessFile.write( buffer, 0, bytesRead );		
		}
		
		sourceRandomAccessFile.close();
	}
	
	private void writeFilesetDescriptor( RandomAccessFile myRandomAccessFile, long targetBlock, long rootDirectoryBlock, int partitionNumber, long partitionStartingBlock, Calendar recordingTimeCalendar, int tagSerialNumber, byte[] udfVersionIdentifierSuffix, int descriptorVersion )
	throws Exception
	{
		FileSetDescriptor myFilesetDescriptor = new FileSetDescriptor();
		
		myFilesetDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myFilesetDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myFilesetDescriptor.DescriptorTag.TagLocation = targetBlock - partitionStartingBlock;
		
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
		myFilesetDescriptor.RootDirectoryICB.ExtentLocation.part_num = partitionNumber;
		myFilesetDescriptor.RootDirectoryICB.ExtentLocation.lb_num = rootDirectoryBlock - partitionStartingBlock;
		
		myFilesetDescriptor.DomainIdentifier.setIdentifier( "*OSTA UDF Compliant" );
		myFilesetDescriptor.DomainIdentifier.IdentifierSuffix = udfVersionIdentifierSuffix;
		
		myRandomAccessFile.seek( targetBlock * blockSize );
		myFilesetDescriptor.write( myRandomAccessFile, blockSize );
	}
	
	private void writeAnchorVolumeDescriptorPointer( RandomAccessFile myRandomAccessFile, long targetBlock, long MVDSBlock, long RVDSBlock, int tagSerialNumber, int descriptorVersion )
	throws IOException
	{
		AnchorVolumeDescriptorPointer myAnchorVolumeDescriptorPointer = new AnchorVolumeDescriptorPointer();
		
		myAnchorVolumeDescriptorPointer.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myAnchorVolumeDescriptorPointer.DescriptorTag.DescriptorVersion = descriptorVersion; 
		myAnchorVolumeDescriptorPointer.DescriptorTag.TagLocation = targetBlock;
		
		myAnchorVolumeDescriptorPointer.MainVolumeDescriptorSequenceExtend.len = 16 * blockSize;
		myAnchorVolumeDescriptorPointer.MainVolumeDescriptorSequenceExtend.loc = MVDSBlock;
		
		myAnchorVolumeDescriptorPointer.ReserveVolumeDescriptorSequenceExtend.len = 16 * blockSize;
		myAnchorVolumeDescriptorPointer.ReserveVolumeDescriptorSequenceExtend.loc = RVDSBlock;
		
		myRandomAccessFile.seek( targetBlock * blockSize );		
		myAnchorVolumeDescriptorPointer.write( myRandomAccessFile, blockSize );		
	}
	
	private void writePrimaryVolumeDescriptor( RandomAccessFile myRandomAccessFile, long volumeDescriptorSequenceNumber, long targetBlock, Calendar recordingTimeCalendar, int tagSerialNumber, int descriptorVersion )
	throws Exception
	{
		PrimaryVolumeDescriptor myPrimaryVolumeDescriptor = new PrimaryVolumeDescriptor();
		
		myPrimaryVolumeDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myPrimaryVolumeDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myPrimaryVolumeDescriptor.DescriptorTag.TagLocation = targetBlock;
		
		myPrimaryVolumeDescriptor.VolumeDescriptorSequenceNumber = volumeDescriptorSequenceNumber;
		myPrimaryVolumeDescriptor.PrimaryVolumeDescriptorNumber = 0;		
		myPrimaryVolumeDescriptor.setVolumeIdentifier( imageIdentifier );
		myPrimaryVolumeDescriptor.VolumeSequenceNumber = 1;
		myPrimaryVolumeDescriptor.MaximumVolumeSequenceNumber = 1;
		myPrimaryVolumeDescriptor.InterchangeLevel = 2;
		myPrimaryVolumeDescriptor.MaximumInterchangeLevel = 3;
		myPrimaryVolumeDescriptor.CharacterSetList = 1;
		myPrimaryVolumeDescriptor.MaximumCharacterSetList = 1;
				
		String volumeSetIdentifier =  Long.toHexString( recordingTimeCalendar.getTimeInMillis() ) + " " + imageIdentifier;
		myPrimaryVolumeDescriptor.setVolumeSetIdentifier( volumeSetIdentifier );
		
		myPrimaryVolumeDescriptor.ApplicationIdentifier.setIdentifier( applicationIdentifier );
		myPrimaryVolumeDescriptor.ApplicationIdentifier.IdentifierSuffix = applicationIdentifierSuffix;
		
		myPrimaryVolumeDescriptor.RecordingDateandTime.set( recordingTimeCalendar );
		
		myPrimaryVolumeDescriptor.ImplementationIdentifier.setIdentifier( applicationIdentifier );
		
		myPrimaryVolumeDescriptor.PredecessorVolumeDescriptorSequenceLocation = 0;
		myPrimaryVolumeDescriptor.Flags = 1;

		myRandomAccessFile.seek( targetBlock * blockSize );		
		myPrimaryVolumeDescriptor.write( myRandomAccessFile, blockSize );		
	}
	
	private void writePartitionDescriptor( RandomAccessFile myRandomAccessFile, long volumeDescriptorSequenceNumber, long targetBlock, long partitionStartingBlock, long partitionEndingBlock, int tagSerialNumber, int descriptorVersion )
	throws Exception
	{
		PartitionDescriptor myPartitionDescriptor = new PartitionDescriptor();
		
		myPartitionDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myPartitionDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myPartitionDescriptor.DescriptorTag.TagLocation = targetBlock;
		
		myPartitionDescriptor.VolumeDescriptorSequenceNumber = volumeDescriptorSequenceNumber;
		myPartitionDescriptor.PartitionFlags = 1;
		myPartitionDescriptor.PartitionNumber = 0;
		
		if( descriptorVersion == 2 )
		{
			myPartitionDescriptor.PartitionContents.setIdentifier( "+NSR02" );
		}
		else //if( descriptorVersion == 3 )
		{
			myPartitionDescriptor.PartitionContents.setIdentifier( "+NSR03" );
		}
		
		myPartitionDescriptor.AccessType = 1; // read only
		myPartitionDescriptor.PartitonStartingLocation = partitionStartingBlock;
		myPartitionDescriptor.PartitionLength = partitionEndingBlock - partitionStartingBlock;
		myPartitionDescriptor.ImplementationIdentifier.setIdentifier( applicationIdentifier );
		myPartitionDescriptor.ImplementationIdentifier.IdentifierSuffix = applicationIdentifierSuffix;

		myRandomAccessFile.seek( targetBlock * blockSize );		
		myPartitionDescriptor.write( myRandomAccessFile, blockSize );
	}

	private void writeLogicalVolumeDescriptor( RandomAccessFile myRandomAccessFile, long volumeDescriptorSequenceNumber, long targetBlock, long LVIDSequenceStartingBlock, long LVIDSequenceLength, int tagSerialNumber, byte[] udfVersionIdentifierSuffix, int descriptorVersion )	
	throws Exception
	{
		writeLogicalVolumeDescriptor( myRandomAccessFile, volumeDescriptorSequenceNumber, targetBlock, LVIDSequenceStartingBlock, LVIDSequenceLength, tagSerialNumber, -1, -1, 0, 0, udfVersionIdentifierSuffix, descriptorVersion, 0, 1 );
	}
	
	
	private void writeLogicalVolumeDescriptor( RandomAccessFile myRandomAccessFile, long volumeDescriptorSequenceNumber, long targetBlock, long LVIDSequenceStartingBlock, long LVIDSequenceLength, int tagSerialNumber, long metadataFileLocation1, long metadataFileLocation2, int metadataAllocationUnitSize, int metadataAlignmentUnitSize, byte[] udfVersionIdentifierSuffix, int descriptorVersion, int filesetPartition, long filesetLocation )
	throws Exception
	{
		LogicalVolumeDescriptor myLogicalVolumeDescriptor = new LogicalVolumeDescriptor();
		
		myLogicalVolumeDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myLogicalVolumeDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myLogicalVolumeDescriptor.DescriptorTag.TagLocation = targetBlock;

		myLogicalVolumeDescriptor.VolumeDescriptorSequenceNumber = volumeDescriptorSequenceNumber;
		myLogicalVolumeDescriptor.setLogicalVolumeIdentifier( imageIdentifier );
		myLogicalVolumeDescriptor.LogicalBlockSize = blockSize;
		
		myLogicalVolumeDescriptor.DomainIdentifier.setIdentifier( "*OSTA UDF Compliant" );
		myLogicalVolumeDescriptor.DomainIdentifier.IdentifierSuffix = udfVersionIdentifierSuffix;
		
		myLogicalVolumeDescriptor.LogicalVolumeContentsUse.ExtentLength = blockSize;
		myLogicalVolumeDescriptor.LogicalVolumeContentsUse.ExtentLocation.part_num = filesetPartition;
		myLogicalVolumeDescriptor.LogicalVolumeContentsUse.ExtentLocation.lb_num = filesetLocation;
			
		myLogicalVolumeDescriptor.ImplementationIdentifier.setIdentifier( applicationIdentifier );
		myLogicalVolumeDescriptor.ImplementationIdentifier.IdentifierSuffix = applicationIdentifierSuffix;

		// partition map type 1, length 6, volume sequence number 0, partition number 0
		PartitionMapType1 myPartitionMapType1 = new PartitionMapType1();		
		byte myPartitionMapType1Bytes[] = myPartitionMapType1.getBytes();		
		
		if( metadataFileLocation1 > 0 )
		{			
			PartitionMapType2 myPartitionMapType2 = new PartitionMapType2();
			EntityID partitionTypeIdentifier = new EntityID();
			partitionTypeIdentifier.setIdentifier( "*UDF Metadata Partition" );
			partitionTypeIdentifier.IdentifierSuffix = udfVersionIdentifierSuffix;
			myPartitionMapType2.setupMetadataPartitionMap( partitionTypeIdentifier, 1, 0, metadataFileLocation1, metadataFileLocation2, 0xFFFFFFFF, metadataAllocationUnitSize, metadataAlignmentUnitSize, (byte)0 );
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
		
		myLogicalVolumeDescriptor.MapTableLength = myLogicalVolumeDescriptor.PartitionMaps.length;
		
		myLogicalVolumeDescriptor.IntegritySequenceExtent.loc = LVIDSequenceStartingBlock;
		myLogicalVolumeDescriptor.IntegritySequenceExtent.len = LVIDSequenceLength;

		myRandomAccessFile.seek( targetBlock * blockSize );
		myLogicalVolumeDescriptor.write( myRandomAccessFile, blockSize );		
	}
	
	private void writeUnallocatedSpaceDescriptor( RandomAccessFile myRandomAccessFile, long volumeDescriptorSequenceNumber, long targetBlock, long unallocatedSpaceStartBlock, long unallocatedSpaceEndBlock, int tagSerialNumber, byte[] udfVersionIdentifierSuffix, int descriptorVersion )
	throws IOException
	{
		UnallocatedSpaceDescriptor myUnallocatedSpaceDescriptor = new UnallocatedSpaceDescriptor();
		
		myUnallocatedSpaceDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myUnallocatedSpaceDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myUnallocatedSpaceDescriptor.DescriptorTag.TagLocation = targetBlock;
		
		myUnallocatedSpaceDescriptor.VolumeDescriptorSequenceNumber = volumeDescriptorSequenceNumber;
		myUnallocatedSpaceDescriptor.NumberofAllocationDescriptors = 1;
		myUnallocatedSpaceDescriptor.AllocationDescriptors = new Extend_ad[1];
		
		// unallocated space #1
		// TODO: extend method for describing more than one unallocated space area
		myUnallocatedSpaceDescriptor.AllocationDescriptors[0] = new Extend_ad();
		myUnallocatedSpaceDescriptor.AllocationDescriptors[0].loc = unallocatedSpaceStartBlock;
		myUnallocatedSpaceDescriptor.AllocationDescriptors[0].len = (unallocatedSpaceEndBlock - unallocatedSpaceStartBlock) * blockSize;
		
		myRandomAccessFile.seek( targetBlock * blockSize );		
		myUnallocatedSpaceDescriptor.write( myRandomAccessFile, blockSize );
	}
	
	private void writeImplementationUseVolumeDescriptor( RandomAccessFile myRandomAccessFile, long volumeDescriptorSequenceNumber, long targetBlock, int tagSerialNumber, byte[] udfVersionIdentifierSuffix, int descriptorVersion )
	throws Exception
	{
		ImplementationUseVolumeDescriptor myImplementationUseVolumeDescriptor = new ImplementationUseVolumeDescriptor();
		
		myImplementationUseVolumeDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myImplementationUseVolumeDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myImplementationUseVolumeDescriptor.DescriptorTag.TagLocation = targetBlock;
		
		myImplementationUseVolumeDescriptor.VolumeDescriptorSequenceNumber = volumeDescriptorSequenceNumber;
		myImplementationUseVolumeDescriptor.ImplementationIdentifier.setIdentifier( "*UDF LV Info" );
		myImplementationUseVolumeDescriptor.ImplementationIdentifier.IdentifierSuffix = udfVersionIdentifierSuffix;
		
		myImplementationUseVolumeDescriptor.ImplementationUse.ImplementationID.setIdentifier( applicationIdentifier );
		myImplementationUseVolumeDescriptor.ImplementationUse.ImplementationID.IdentifierSuffix = applicationIdentifierSuffix;
	
		myImplementationUseVolumeDescriptor.ImplementationUse.setLogicalVolumeIdentifier( imageIdentifier );
		// TODO: maybe set the LVInfo1 - 3 fields of ImplementationUse (f.ex. owner, organization, contact)

		myRandomAccessFile.seek( targetBlock * blockSize );
		myImplementationUseVolumeDescriptor.write( myRandomAccessFile, blockSize );		
	}
	
	private void writeTerminatingDescriptor( RandomAccessFile myRandomAccessFile, long targetBlock, int tagSerialNumber, int descriptorVersion )
	throws IOException
	{
		TerminatingDescriptor myTerminatingDescriptor = new TerminatingDescriptor();
		
		myTerminatingDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		myTerminatingDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myTerminatingDescriptor.DescriptorTag.TagLocation = targetBlock;
		
		myRandomAccessFile.seek( targetBlock * blockSize );
		myTerminatingDescriptor.write( myRandomAccessFile, blockSize );
	}
	
	private void writeLogicalVolumeIntegrityDescriptor( RandomAccessFile myRandomAccessFile, long targetBlock, Calendar recordingTimeCalendar, int tagSerialNumber, int minimumUDFReadRevision, int minimumUDFWriteRevision, int maximumUDFWriteRevision, int descriptorVersion, long[] sizeTable, long[] freeSpaceTable )
	throws Exception
	{
		LogicalVolumeIntegrityDescriptor myLogicalVolumeIntegrityDescriptor = new LogicalVolumeIntegrityDescriptor();
		
		myLogicalVolumeIntegrityDescriptor.DescriptorTag.TagLocation = targetBlock;
		myLogicalVolumeIntegrityDescriptor.DescriptorTag.DescriptorVersion = descriptorVersion;
		myLogicalVolumeIntegrityDescriptor.DescriptorTag.TagSerialNumber = tagSerialNumber;
		
		myLogicalVolumeIntegrityDescriptor.RecordingDateAndTime.set( recordingTimeCalendar );
		myLogicalVolumeIntegrityDescriptor.IntegrityType = 1;
		myLogicalVolumeIntegrityDescriptor.NumberOfPartitions = sizeTable.length;
		
		myLogicalVolumeIntegrityDescriptor.FreeSpaceTable = freeSpaceTable;
		myLogicalVolumeIntegrityDescriptor.SizeTable = sizeTable;

		myLogicalVolumeIntegrityDescriptor.LogicalVolumeContensUse.UniqueID = myUniqueIdDisposer.getNextUniqueId();		
		
		myLogicalVolumeIntegrityDescriptor.LengthOfImplementationUse = 46;
		
		EntityID implementationId = new EntityID();
		implementationId.setIdentifier( applicationIdentifier );
		implementationId.IdentifierSuffix = applicationIdentifierSuffix;
		
		long numberOfFiles = rootUDFImageBuilderFile.getFileCount();
		long numberOfDirectories = rootUDFImageBuilderFile.getDirectoryCount();
				
		myLogicalVolumeIntegrityDescriptor.setImplementationUse( implementationId, numberOfFiles, numberOfDirectories, minimumUDFReadRevision, minimumUDFWriteRevision, maximumUDFWriteRevision );
		
		myRandomAccessFile.seek( targetBlock * blockSize );
		myLogicalVolumeIntegrityDescriptor.write( myRandomAccessFile, blockSize );
	}
	
	private long recursiveGetMetadataFileLength( UDFImageBuilderFile myUDFImageBuilderFile, int blockSize )
	throws Exception
	{
		long wholeMetadataLengthInBlocks = 0;
		
		if( myUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.File )
		{
			wholeMetadataLengthInBlocks = 1;			
		}
		else //if( myUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.Directory )
		{			
			long FileIdentifierDescriptorsLength = 0;
			
			wholeMetadataLengthInBlocks += 1;
						
			FileIdentifierDescriptor parentDirectoryFileIdentifierDescriptor = new FileIdentifierDescriptor();			
			FileIdentifierDescriptorsLength += parentDirectoryFileIdentifierDescriptor.getLength();
			
			UDFImageBuilderFile[] childUDFImageBuilderFiles = myUDFImageBuilderFile.getChilds();
			
			for( int i = 0; i < childUDFImageBuilderFiles.length; ++i )
			{
				FileIdentifierDescriptor childFileIdentifierDescriptor = new FileIdentifierDescriptor();
				childFileIdentifierDescriptor.setFileIdentifier( childUDFImageBuilderFiles[ i ].getIdentifier() );
				FileIdentifierDescriptorsLength += childFileIdentifierDescriptor.getLength();
				
				wholeMetadataLengthInBlocks += recursiveGetMetadataFileLength( childUDFImageBuilderFiles[ i ], blockSize );
			}
			
			if( FileIdentifierDescriptorsLength > ( blockSize - ExtendedFileEntry.fixedPartLength ) )
			{
				long additionalBlocks = (long)(FileIdentifierDescriptorsLength / blockSize);
				if( FileIdentifierDescriptorsLength % blockSize != 0 )
				{
					additionalBlocks++;
				}
				wholeMetadataLengthInBlocks += additionalBlocks;
			}
		}
		
		return wholeMetadataLengthInBlocks;
	}
}
