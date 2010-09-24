/*
 *	UDFLayoutInformation.java
 *
 *	2006-07-13
 *
 *	Björn Stickler <bjoern@stickler.de>
 */


package de.tu_darmstadt.informatik.rbg.bstickler.udflib;

import java.util.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures.*;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;

public class UDFLayoutInformation
{
	private UniqueIdDisposer myUniqueIdDisposer;
	
	public int blockSize;
	
	public int metadataAllocationUnitSize;
	public int metadataAlignmentUnitSize;
	
	public long fileCount;
	public long directoryCount;

	public long AVDP1Block;								// anchor volume descriptor sequence pointer 1
	public long AVDP2Block;								// anchor volume descriptor sequence pointer 2

	public long MVDSStartingBlock;						// main volume descriptor sequence start
	public long MVDSEndingBlock;						// main volume descriptor sequence end
	
	public long RVDSStartingBlock;						// reserve volume descriptor sequence start
	public long RVDSEndingBlock;						// reserve volume descriptor sequence end
	
	public long LVIDSStartingBlock;						// logical volume integrity sequence start
	public long LVIDSEndingBlock;						// logical volume integrity sequence end
		
	public long physicalPartitionStartingBlock;
	public long physicalPartitionEndingBlock;
	
	public long metadataPartitionStartingBlock;
	public long metadataPartitionEndingBlock;
	
	public long mainMetadataFileBlock;
	public long mainMetadataFileLocation;
	
	public long mirrorMetadataFileBlock;
	public long mirrorMetadataFileLocation;
	
	public long metadataEmptyArea;
	
	public int	partitionToStoreMetadataOn;
	
	public long FSDBlock;		// fileset descriptor location
	public long FSDLocation;	// fileset descriptor location inside the partition
	
	public long rootFEBlock;	// root directory file entry location
	public long rootFELocation;	// root directory file entry location inside the partition

	public long PVD1Block;		// primary volume descriptor location (main volume descriptor sequence)
	public long PVD2Block;		// primary volume descriptor location (reserve volume descriptor sequence)
	public long PD1Block;		// partition descriptor location (main volume descriptor sequence)
	public long PD2Block;		// partition descriptor location (reserve volume descriptor sequence)
	public long LVD1Block;		// logical volume descriptor location (main volume descriptor sequence)
	public long LVD2Block;		// logical volume descriptor location (reserve volume descriptor sequence)
	public long USD1Block;		// unallocated space descriptor location (main volume descriptor sequence)
	public long USD2Block;		// unallocated space descriptor location (reserve volume descriptor sequence)
	public long IUVD1Block;		// implementation use volume descriptor location (main volume descriptor sequence)
	public long IUVD2Block;		// implementation use volume descriptor location (reserve volume descriptor sequence)
	public long TD1Block;		// terminating descriptor location (main volume descriptor sequence)
	public long TD2Block;		// terminating descriptor location (reserve volume descriptor sequence)
	
	public long[] sizeTable;		// partition size information
	public long[] freespaceTable;	// partition freespace information
	
	public long nextUniqueId;
	
	public class FileEntryPosition
	{
		long	entryBlock;		// location of the (extended-) file entry itself
		long	entryLocation;	// location of the (extended-) file entry itself inside the partition
		long	dataBlock;		// location of the file data if not embedded inline
		long	dataLocation;	// location of the file data if not embedded inline inside the partition
	}
		
	public Hashtable<UDFImageBuilderFile, FileEntryPosition> fileEntryPositions;
	public ArrayList<UDFImageBuilderFile> linearUDFImageBuilderFileOrdering;
	public Hashtable<UDFImageBuilderFile, Long> uniqueIds;
	
	public UDFLayoutInformation( UDFImageBuilderFile rootUDFImageBuilderFile, UDFRevision myUDFRevision, int blockSize )
	throws Exception
	{
		myUniqueIdDisposer = new UniqueIdDisposer();
		
		fileEntryPositions = new Hashtable<UDFImageBuilderFile, FileEntryPosition>();
		linearUDFImageBuilderFileOrdering = new ArrayList<UDFImageBuilderFile>();
		uniqueIds = new Hashtable<UDFImageBuilderFile, Long>();
		
		this.blockSize = blockSize;
		metadataAllocationUnitSize = 32;
		metadataAlignmentUnitSize = 1;
		
		fileCount 		= rootUDFImageBuilderFile.getFileCount();
		directoryCount	= rootUDFImageBuilderFile.getDirectoryCount();
		
		AVDP1Block = 256;
		
		MVDSStartingBlock = 257;
		MVDSEndingBlock = MVDSStartingBlock + 16;
		
		// setup main volume descriptor sequence element locations
		PVD1Block	= MVDSStartingBlock;
		PD1Block	= MVDSStartingBlock + 1;
		LVD1Block	= MVDSStartingBlock + 2;
		USD1Block	= MVDSStartingBlock + 3;
		IUVD1Block	= MVDSStartingBlock + 4;
		TD1Block	= MVDSStartingBlock + 5;
		
		LVIDSStartingBlock = MVDSEndingBlock;
		LVIDSEndingBlock = LVIDSStartingBlock + 4;
		
		physicalPartitionStartingBlock = LVIDSEndingBlock;
		
		if( myUDFRevision == UDFRevision.Revision260 )
		{
			partitionToStoreMetadataOn 		= 1;
			
			mainMetadataFileBlock 			= physicalPartitionStartingBlock + 1;
			mainMetadataFileLocation        = mainMetadataFileBlock - physicalPartitionStartingBlock;
			
			metadataPartitionStartingBlock 	= physicalPartitionStartingBlock + 2;
			
			FSDBlock						= physicalPartitionStartingBlock + 2;
			FSDLocation						= 0;

			rootFEBlock						= physicalPartitionStartingBlock + 3;
			rootFELocation					= 1;
		}
		else
		{
			partitionToStoreMetadataOn 		= 0;
			
			FSDBlock						= physicalPartitionStartingBlock + 1;
			FSDLocation						= 1;			
			
			rootFEBlock						= physicalPartitionStartingBlock + 2;
			rootFELocation					= 2;
		}
				
		long[] currentBlock  = recursiveGetFileEntryLocation( rootUDFImageBuilderFile, new long[]{ rootFEBlock, 0 }, myUDFRevision );
		
		nextUniqueId = myUniqueIdDisposer.getNextUniqueId();
		
		if( myUDFRevision == UDFRevision.Revision260 )
		{
			if( ( ( currentBlock[ 0 ] ) - metadataPartitionStartingBlock ) % metadataAllocationUnitSize != 0 )
			{
				metadataEmptyArea = metadataAllocationUnitSize - ( ( ( currentBlock[ 0 ] ) - metadataPartitionStartingBlock ) % metadataAllocationUnitSize );
				currentBlock[ 0 ] += metadataEmptyArea;  
			}
			
			metadataPartitionEndingBlock = currentBlock[ 0 ];
			
			mirrorMetadataFileBlock 	= currentBlock[ 0 ] + currentBlock[ 1 ];
			mirrorMetadataFileLocation  = mirrorMetadataFileBlock - physicalPartitionStartingBlock;
			
			physicalPartitionEndingBlock = mirrorMetadataFileBlock + 1;
		
		}
		else
		{
			physicalPartitionEndingBlock = currentBlock[ 0 ] + currentBlock[ 1 ];	
		}		
		
		// update data locations
		Enumeration myEnumeration = fileEntryPositions.keys();
		while( myEnumeration.hasMoreElements() )
		{
			UDFImageBuilderFile myUDFImageBuilderFile = (UDFImageBuilderFile)myEnumeration.nextElement();
			if( myUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.File )
			{
				FileEntryPosition myFileEntryPosition = fileEntryPositions.get( myUDFImageBuilderFile );
				
				if( myFileEntryPosition.dataBlock != -1 )
				{
					myFileEntryPosition.dataBlock += currentBlock[ 0 ];
					myFileEntryPosition.dataLocation += myFileEntryPosition.dataBlock - physicalPartitionStartingBlock;
				}
			}
		}		

		// calculate partition sizes and freespace
		if( myUDFRevision == UDFRevision.Revision260 )
		{
			sizeTable = new long[ 2 ];
			sizeTable[ 0 ] = ( physicalPartitionEndingBlock - physicalPartitionStartingBlock );
			sizeTable[ 1 ] = ( metadataPartitionEndingBlock - metadataPartitionStartingBlock ) ;
			
			freespaceTable = new long[ 2 ]; // no freespace			
		}
		else
		{
			sizeTable = new long[ 1 ];
			sizeTable[ 0 ] = ( physicalPartitionEndingBlock - physicalPartitionStartingBlock );
			
			freespaceTable = new long[ 1 ]; // no freespace
		}
		
		RVDSStartingBlock 	= physicalPartitionEndingBlock;
		RVDSEndingBlock		= RVDSStartingBlock + 16; 

		// setup reserve volume descriptor sequence element locations		
		PVD2Block	= RVDSStartingBlock;		
		PD2Block	= RVDSStartingBlock + 1;
		LVD2Block	= RVDSStartingBlock + 2;
		USD2Block	= RVDSStartingBlock + 3;
		IUVD2Block	= RVDSStartingBlock + 4;
		TD2Block	= RVDSStartingBlock + 5;
		
		AVDP2Block = RVDSEndingBlock + 1;
	}
	
	private long[] recursiveGetFileEntryLocation( UDFImageBuilderFile currentUDFImageBuilderFile, long[] currentBlock, UDFRevision myUDFRevision )
	throws Exception
	{
		if( currentUDFImageBuilderFile.getIdentifier().equals( "" ) )
		{
			uniqueIds.put( currentUDFImageBuilderFile, new Long( 0 ) );
		}
		else
		{
			uniqueIds.put( currentUDFImageBuilderFile, new Long( myUniqueIdDisposer.getNextUniqueId() ) );
		}
		
		linearUDFImageBuilderFileOrdering.add( currentUDFImageBuilderFile );

		FileEntryPosition currentFileEntryPosition = new FileEntryPosition();
		
		currentFileEntryPosition.entryBlock = currentBlock[ 0 ];
		currentBlock[ 0 ]++;
		
		if( myUDFRevision == UDFRevision.Revision260 )
		{
			currentFileEntryPosition.entryLocation = currentFileEntryPosition.entryBlock - metadataPartitionStartingBlock;
		}
		else
		{
			currentFileEntryPosition.entryLocation = currentFileEntryPosition.entryBlock - physicalPartitionStartingBlock; 
		}
		
		
		if( currentUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.File )
		{			
			// if filedata cannot be stored inline reserve data blocks and setup data location
			if(	( ( myUDFRevision == UDFRevision.Revision102 ) && currentUDFImageBuilderFile.getFileLength() > ( blockSize - FileEntry.fixedPartLength ) )
				|| ( currentUDFImageBuilderFile.getFileLength() > ( blockSize - ExtendedFileEntry.fixedPartLength ) ) )			
			{
				currentFileEntryPosition.dataBlock = currentBlock[ 1 ];
				currentBlock[ 1 ] += currentUDFImageBuilderFile.getFileLength() / blockSize;
				if( currentUDFImageBuilderFile.getFileLength() % blockSize != 0 )
				{
					currentBlock[ 1 ]++;
				}
			}
			else
			{
				currentFileEntryPosition.dataBlock = -1;
			}			
		}
		else //if( myUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.Directory )
		{
			long FileIdentifierDescriptorsLength = 0;
						
			FileIdentifierDescriptor parentDirectoryFileIdentifierDescriptor = new FileIdentifierDescriptor();			
			FileIdentifierDescriptorsLength += parentDirectoryFileIdentifierDescriptor.getLength();			

			UDFImageBuilderFile[] childUDFImageBuilderFiles = currentUDFImageBuilderFile.getChilds();
			
			for( int i = 0; i < childUDFImageBuilderFiles.length; ++i )
			{
				FileIdentifierDescriptor childFileIdentifierDescriptor = new FileIdentifierDescriptor();
				childFileIdentifierDescriptor.setFileIdentifier( childUDFImageBuilderFiles[ i ].getIdentifier() );
				FileIdentifierDescriptorsLength += childFileIdentifierDescriptor.getLength();
			}
			
			
			if(	( ( myUDFRevision == UDFRevision.Revision102 ) && FileIdentifierDescriptorsLength > ( blockSize - FileEntry.fixedPartLength ) )
					|| ( FileIdentifierDescriptorsLength > ( blockSize - ExtendedFileEntry.fixedPartLength ) ) )
			{
				currentFileEntryPosition.dataBlock = currentBlock[ 0 ];
				
				if( myUDFRevision == UDFRevision.Revision260 )
				{
					currentFileEntryPosition.dataLocation = currentFileEntryPosition.dataBlock - metadataPartitionStartingBlock;
				}
				else
				{
					currentFileEntryPosition.dataLocation = currentFileEntryPosition.dataBlock - physicalPartitionStartingBlock; 
				}
				
				currentBlock[ 0 ] += FileIdentifierDescriptorsLength / blockSize;
				if( FileIdentifierDescriptorsLength % blockSize != 0 )
				{
					currentBlock[ 0 ]++;
				}				
			}
			else
			{
				currentFileEntryPosition.dataBlock = -1;
			}

			for( int i = 0; i < childUDFImageBuilderFiles.length; ++i )
			{
				currentBlock = recursiveGetFileEntryLocation( childUDFImageBuilderFiles[ i ], currentBlock, myUDFRevision );
			}
			
		}
		
		fileEntryPositions.put( currentUDFImageBuilderFile, currentFileEntryPosition );
		
		return currentBlock;
	}
	
}
