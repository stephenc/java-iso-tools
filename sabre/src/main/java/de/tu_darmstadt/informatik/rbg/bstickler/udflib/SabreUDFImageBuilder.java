/*
 *	SabreUDFImageBuilder.java
 *
 *	2006-07-06
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib;

import java.io.*;
import java.util.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.UDFLayoutInformation.FileEntryPosition;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.handler.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;


public class SabreUDFImageBuilder
{
	private String imageIdentifier = "SabreUDFImageBuilder Disc";
	
	private String applicationIdentifier = "*SabreUDFImageBuilder";	
	private byte applicationIdentifierSuffix[] = new byte[]{ 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
	
	private int blockSize = 2048;

	private UDFImageBuilderFile rootUDFImageBuilderFile;
	
	
	public SabreUDFImageBuilder()
	{
		rootUDFImageBuilderFile = new UDFImageBuilderFile( "" );
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
	
	private StreamHandler buildStreamHandlerPipeline( UDFRevision myUDFRevision, String outputFilename )
	throws HandlerException
	{
		// serialization handler
		StreamHandler myStreamHandler = new SerializationHandler( new File( outputFilename ) );
		
		// padding handler
		myStreamHandler = new PaddingHandler( myStreamHandler, myStreamHandler );
		
		// descriptor tag hander
		myStreamHandler = new DescriptorTagHandler( myStreamHandler, myStreamHandler );
		
		// version dependent handler
		if( myUDFRevision == UDFRevision.Revision102 )
		{
			myStreamHandler = new UDF102Handler( myStreamHandler, myStreamHandler );
		}
		else if( myUDFRevision == UDFRevision.Revision201 )
		{
			myStreamHandler = new UDF201Handler( myStreamHandler, myStreamHandler );
		}
		else if( myUDFRevision == UDFRevision.Revision260 )
		{
			myStreamHandler = new UDF260Handler( myStreamHandler, myStreamHandler );
		}
		
		return myStreamHandler;
	}
	
	public void writeImage( String outputFilename, UDFRevision myUDFRevision )
	throws HandlerException
	{
		long recordingTimeMillis = Calendar.getInstance().getTimeInMillis();
		
		// preprocess all information needed for linear-output-streaming
		UDFLayoutInformation myUDFLayoutInformation = null;
		try
		{
			myUDFLayoutInformation = new UDFLayoutInformation( rootUDFImageBuilderFile, myUDFRevision, blockSize );
		}
		catch( Exception ex )
		{
			throw new HandlerException( ex );
		}

		StreamHandler myStreamHandler = buildStreamHandlerPipeline( myUDFRevision, outputFilename );
		
		SabreUDFElementFactory mySabreUDFElementFactory = new SabreUDFElementFactory( myStreamHandler );

		mySabreUDFElementFactory.startUDFImage();
		
		// write reserved area
		mySabreUDFElementFactory.startReservedArea();
		mySabreUDFElementFactory.endReservedArea();
		
		// write volume recognition sequence
		mySabreUDFElementFactory.startVRS();
		mySabreUDFElementFactory.endVRS();
		
		// write empty area between vrs and avdp (block 19 to block 256)
		mySabreUDFElementFactory.startEmptyArea();
		mySabreUDFElementFactory.doEmptyArea( ( 256 - 19 ) * blockSize );
		mySabreUDFElementFactory.endEmptyArea();
		
		// write anchor volume descriptor pointer 1
		mySabreUDFElementFactory.startAVDP();
		mySabreUDFElementFactory.doAVDP( myUDFLayoutInformation.AVDP1Block, myUDFLayoutInformation.MVDSStartingBlock, myUDFLayoutInformation.RVDSStartingBlock );
		mySabreUDFElementFactory.endAVDP();

		
		/*
		 * 		WRITE MAIN VOLUME DESCRIPTOR SEQUENCE
		 */
		
		// write primary volume descriptor
		mySabreUDFElementFactory.startPVD();
		mySabreUDFElementFactory.doPVD( myUDFLayoutInformation.PVD1Block, 1, recordingTimeMillis, imageIdentifier, applicationIdentifier, applicationIdentifierSuffix );
		mySabreUDFElementFactory.endPVD();
		
		// write partition descriptor
		mySabreUDFElementFactory.startPD();
		mySabreUDFElementFactory.doPD( myUDFLayoutInformation.PD1Block, 2, myUDFLayoutInformation.physicalPartitionStartingBlock, myUDFLayoutInformation.physicalPartitionEndingBlock, applicationIdentifier, applicationIdentifierSuffix );
		mySabreUDFElementFactory.endPD();
		
		// write logical volume descriptor
		mySabreUDFElementFactory.startLVD();
		mySabreUDFElementFactory.doLVD( myUDFLayoutInformation.LVD1Block, 3, myUDFLayoutInformation.LVIDSStartingBlock, myUDFLayoutInformation.LVIDSEndingBlock, myUDFLayoutInformation.metadataAllocationUnitSize, myUDFLayoutInformation.metadataAlignmentUnitSize, myUDFLayoutInformation.mainMetadataFileLocation, myUDFLayoutInformation.mirrorMetadataFileLocation, myUDFLayoutInformation.partitionToStoreMetadataOn, myUDFLayoutInformation.FSDLocation, applicationIdentifier, applicationIdentifierSuffix, imageIdentifier );
		mySabreUDFElementFactory.endLVD();
		
		// write unallocated space descriptor
		mySabreUDFElementFactory.startUSD();
		mySabreUDFElementFactory.doUSD( myUDFLayoutInformation.USD1Block, 4, 19, 256 );
		mySabreUDFElementFactory.endUSD();
		
		// write implementation use volume descriptor
		mySabreUDFElementFactory.startIUVD();
		mySabreUDFElementFactory.doIUVD( myUDFLayoutInformation.IUVD1Block, 5, applicationIdentifier, applicationIdentifierSuffix, imageIdentifier );
		mySabreUDFElementFactory.endIUVD();
		
		// write terminating descriptor
		mySabreUDFElementFactory.startTD();
		mySabreUDFElementFactory.doTD( myUDFLayoutInformation.TD1Block );
		mySabreUDFElementFactory.endTD();
		
		// write empty blocks for remaining main volume descriptor sequence space
		mySabreUDFElementFactory.startEmptyArea();
		mySabreUDFElementFactory.doEmptyArea( (int)(myUDFLayoutInformation.MVDSEndingBlock -  myUDFLayoutInformation.MVDSStartingBlock - 6) * blockSize );
		mySabreUDFElementFactory.endEmptyArea();
		
		
		/*
		 * 		WRITE LOGICAL VOLUME INTEGRITY DESCRIPTOR SEQUENCE
		 */
		// write logical volume integrity descriptor
		mySabreUDFElementFactory.startLVID();
		mySabreUDFElementFactory.doLVID( myUDFLayoutInformation.LVIDSStartingBlock + 0, recordingTimeMillis, myUDFLayoutInformation.fileCount, myUDFLayoutInformation.directoryCount, applicationIdentifier, applicationIdentifierSuffix, myUDFLayoutInformation.sizeTable, myUDFLayoutInformation.freespaceTable, myUDFLayoutInformation.nextUniqueId );
		mySabreUDFElementFactory.endLVID();

		// write terminating descriptor
		mySabreUDFElementFactory.startTD();
		mySabreUDFElementFactory.doTD( myUDFLayoutInformation.LVIDSStartingBlock + 1 );
		mySabreUDFElementFactory.endTD();

		// write empty blocks for remaining logical volume integrity sequence space
		mySabreUDFElementFactory.startEmptyArea();
		mySabreUDFElementFactory.doEmptyArea( (int)(myUDFLayoutInformation.LVIDSEndingBlock -  myUDFLayoutInformation.LVIDSStartingBlock - 2) * blockSize );
		mySabreUDFElementFactory.endEmptyArea();
		

		/*
		 * 		WRITE METADATA
		 */		
		
		// one empty block at the start of the partition
		mySabreUDFElementFactory.startEmptyArea();
		mySabreUDFElementFactory.doEmptyArea( blockSize );
		mySabreUDFElementFactory.endEmptyArea();
		
		// write main metadata file (if revision needs it)
		mySabreUDFElementFactory.startMetadataFile();
		mySabreUDFElementFactory.doMetadataFile( recordingTimeMillis, applicationIdentifier, applicationIdentifierSuffix, myUDFLayoutInformation, myUDFLayoutInformation.mainMetadataFileLocation, (byte)250 );
		mySabreUDFElementFactory.endMetadataFile();
		
		// write fileset descriptor
		mySabreUDFElementFactory.startFSD();
		mySabreUDFElementFactory.doFSD( myUDFLayoutInformation.FSDLocation, recordingTimeMillis, myUDFLayoutInformation.rootFELocation, myUDFLayoutInformation.partitionToStoreMetadataOn, imageIdentifier );
		mySabreUDFElementFactory.endFSD();
				
		// write file entry elements (without their data if non-directory and non-embedded)
		Iterator<UDFImageBuilderFile> myIterator = myUDFLayoutInformation.linearUDFImageBuilderFileOrdering.iterator();
		while( myIterator.hasNext() )
		{
			UDFImageBuilderFile myUDFImageBuilderFile = myIterator.next();
			
			mySabreUDFElementFactory.startFE();
			mySabreUDFElementFactory.doFE( myUDFImageBuilderFile, myUDFLayoutInformation, applicationIdentifier, applicationIdentifierSuffix );
			mySabreUDFElementFactory.endFE();
		}
		
		// write metadata empty area (if metadata partition existent)
		mySabreUDFElementFactory.startEmptyArea();
		mySabreUDFElementFactory.doEmptyArea( (int)(myUDFLayoutInformation.metadataEmptyArea * blockSize) );
		mySabreUDFElementFactory.endEmptyArea();
		
		
		/*
		 * 		WRITE FILEDATA
		 */		
		
		// write raw file data
		myIterator = myUDFLayoutInformation.linearUDFImageBuilderFileOrdering.iterator();
		while( myIterator.hasNext() )
		{
			UDFImageBuilderFile myUDFImageBuilderFile = myIterator.next();
			
			FileEntryPosition myFileEntryPosition = myUDFLayoutInformation.fileEntryPositions.get( myUDFImageBuilderFile );
			
			if( myUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.File && myFileEntryPosition.dataBlock != -1 )
			{
				mySabreUDFElementFactory.startRawFileData();
				mySabreUDFElementFactory.doRawFileData( myUDFImageBuilderFile.getSourceFile() );
				mySabreUDFElementFactory.endRawFileData();
			}
		}
		
		
		// write mirror metadata file (if revision needs it)
		mySabreUDFElementFactory.startMetadataFile();
		mySabreUDFElementFactory.doMetadataFile( recordingTimeMillis, applicationIdentifier, applicationIdentifierSuffix, myUDFLayoutInformation, myUDFLayoutInformation.mirrorMetadataFileLocation, (byte)251 );
		mySabreUDFElementFactory.endMetadataFile();
		
		
		/*
		 * 		WRITE RESERVE VOLUME DESCRIPTOR SEQUENCE
		 */
		
		// write primary volume descriptor
		mySabreUDFElementFactory.startPVD();
		mySabreUDFElementFactory.doPVD( myUDFLayoutInformation.PVD2Block, 1, recordingTimeMillis, imageIdentifier, applicationIdentifier, applicationIdentifierSuffix );
		mySabreUDFElementFactory.endPVD();
		
		// write partition descriptor
		mySabreUDFElementFactory.startPD();
		mySabreUDFElementFactory.doPD( myUDFLayoutInformation.PD2Block, 2, myUDFLayoutInformation.physicalPartitionStartingBlock, myUDFLayoutInformation.physicalPartitionEndingBlock, applicationIdentifier, applicationIdentifierSuffix );
		mySabreUDFElementFactory.endPD();
		
		// write logical volume descriptor
		mySabreUDFElementFactory.startLVD();
		mySabreUDFElementFactory.doLVD( myUDFLayoutInformation.LVD2Block, 3, myUDFLayoutInformation.LVIDSStartingBlock, myUDFLayoutInformation.LVIDSEndingBlock, myUDFLayoutInformation.metadataAllocationUnitSize, myUDFLayoutInformation.metadataAlignmentUnitSize, myUDFLayoutInformation.mainMetadataFileLocation, myUDFLayoutInformation.mirrorMetadataFileLocation, myUDFLayoutInformation.partitionToStoreMetadataOn, myUDFLayoutInformation.FSDLocation, applicationIdentifier, applicationIdentifierSuffix, imageIdentifier );
		mySabreUDFElementFactory.endLVD();
		
		// write unallocated space descriptor
		mySabreUDFElementFactory.startUSD();
		mySabreUDFElementFactory.doUSD( myUDFLayoutInformation.USD2Block, 4, 19, 256 );
		mySabreUDFElementFactory.endUSD();
		
		// write implementation use volume descriptor
		mySabreUDFElementFactory.startIUVD();
		mySabreUDFElementFactory.doIUVD( myUDFLayoutInformation.IUVD2Block, 5, applicationIdentifier, applicationIdentifierSuffix, imageIdentifier );
		mySabreUDFElementFactory.endIUVD();
		
		// write terminating descriptor
		mySabreUDFElementFactory.startTD();
		mySabreUDFElementFactory.doTD( myUDFLayoutInformation.TD2Block );
		mySabreUDFElementFactory.endTD();
		
		// write empty blocks for remaining main volume descriptor sequence space
		mySabreUDFElementFactory.startEmptyArea();
		mySabreUDFElementFactory.doEmptyArea( (int)(myUDFLayoutInformation.RVDSEndingBlock -  myUDFLayoutInformation.RVDSStartingBlock - 5) * blockSize );
		mySabreUDFElementFactory.endEmptyArea();
		
		
		// write anchor volume descriptor pointer 2
		mySabreUDFElementFactory.startAVDP();
		mySabreUDFElementFactory.doAVDP( myUDFLayoutInformation.AVDP2Block, myUDFLayoutInformation.MVDSStartingBlock, myUDFLayoutInformation.RVDSStartingBlock );
		mySabreUDFElementFactory.endAVDP();
		
		mySabreUDFElementFactory.endUDFImage();
	}
	
}
