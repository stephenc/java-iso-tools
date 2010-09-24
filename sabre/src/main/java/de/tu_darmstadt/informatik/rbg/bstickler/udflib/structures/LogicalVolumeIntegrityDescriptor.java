/*
 *	LogicalVolumeIntegrityDescriptor.java
 *
 *	2006-06-02
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class LogicalVolumeIntegrityDescriptor
{
	public Tag								DescriptorTag;				// struct tag
	public Timestamp						RecordingDateAndTime;		// struct timestamp
	public long								IntegrityType;				// Uint32
	public Extend_ad						NextIntegrityExtent;		// struct extend_ad
	
	//public byte							LogicalVolumeContentsUse[];	// byte[32]
	public LogicalVolumeHeaderDescriptor	LogicalVolumeContensUse;
	
	public long								NumberOfPartitions;			// Uint32
	public long								LengthOfImplementationUse;	// Uint32
	public long								FreeSpaceTable[];			// Uint32[]
	public long								SizeTable[];				// Uint32[]
	public byte								ImplementationUse[];		// byte[]			

	
	public LogicalVolumeIntegrityDescriptor()
	{
		DescriptorTag = new Tag();
		DescriptorTag.TagIdentifier = 9;
		
		RecordingDateAndTime = new Timestamp();
		
		NextIntegrityExtent = new Extend_ad();
		
		//LogicalVolumeContentsUse = new byte[32];
		LogicalVolumeContensUse = new LogicalVolumeHeaderDescriptor();		
		
		FreeSpaceTable = new long[0];
		SizeTable = new long[0];
		
		ImplementationUse = new byte[0];
	}
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		DescriptorTag = new Tag();
		DescriptorTag.read( myRandomAccessFile );
		
		RecordingDateAndTime = new Timestamp();
		RecordingDateAndTime.read( myRandomAccessFile );
		
		IntegrityType = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		NextIntegrityExtent = new Extend_ad();
		NextIntegrityExtent.read( myRandomAccessFile );
		
		//LogicalVolumeContentsUse = new byte[32];
		//myRandomAccessFile.read( LogicalVolumeContentsUse );		
		LogicalVolumeContensUse = new LogicalVolumeHeaderDescriptor();
		LogicalVolumeContensUse.read( myRandomAccessFile );		
		
		NumberOfPartitions = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		LengthOfImplementationUse = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		FreeSpaceTable = new long[ (int)NumberOfPartitions ];
		for( int i = 0; i < FreeSpaceTable.length; ++i )
		{
			FreeSpaceTable[ i ] =  BinaryTools.readUInt32AsLong( myRandomAccessFile ); 
		}
	
		SizeTable = new long[ (int)NumberOfPartitions ];
		for( int i = 0; i < FreeSpaceTable.length; ++i )
		{
			SizeTable[ i ] =  BinaryTools.readUInt32AsLong( myRandomAccessFile ); 
		}
		
		ImplementationUse = new byte[ (int)LengthOfImplementationUse ];
		myRandomAccessFile.read( ImplementationUse );		
	}
	
	public void write( RandomAccessFile myRandomAccessFile, int blockSize )
	throws IOException
	{
		byte rawBytes[] = getBytesWithoutDescriptorTag();
	    
	    DescriptorTag.DescriptorCRCLength = rawBytes.length;
	    DescriptorTag.DescriptorCRC = Checksum.cksum( rawBytes );
		
		DescriptorTag.write( myRandomAccessFile );
			
		myRandomAccessFile.write( rawBytes );

		int bytesWritten = rawBytes.length + 16;

		byte emptyBytesInBlock[] = new byte[ blockSize - bytesWritten ];
		myRandomAccessFile.write( emptyBytesInBlock );		
	}

	public byte[] getBytesWithoutDescriptorTag()
	{
		byte RecordingDateAndTimeBytes[] = RecordingDateAndTime.getBytes();
		byte NextIntegrityExtentBytes[] = NextIntegrityExtent.getBytes();
		byte LogicalVolumeContentsUseBytes[] = LogicalVolumeContensUse.getBytes();
		
		byte rawBytes[] = new byte[ 12 
		                            + RecordingDateAndTimeBytes.length
		                            + NextIntegrityExtentBytes.length
		                            + LogicalVolumeContentsUseBytes.length
		                            + FreeSpaceTable.length * 4
		                            + SizeTable.length * 4
		                            + ImplementationUse.length ];
		
		int pos = 0;
		
		System.arraycopy( RecordingDateAndTimeBytes, 0, rawBytes, pos, RecordingDateAndTimeBytes.length );
		pos += RecordingDateAndTimeBytes.length;

		pos = BinaryTools.getUInt32BytesFromLong( IntegrityType, rawBytes, pos );
		
		System.arraycopy( NextIntegrityExtentBytes, 0, rawBytes, pos, NextIntegrityExtentBytes.length );
		pos += NextIntegrityExtentBytes.length;
		
		//System.arraycopy( LogicalVolumeContentsUse, 0, rawBytes, pos, LogicalVolumeContentsUse.length );
		//pos += LogicalVolumeContentsUse.length;
		System.arraycopy( LogicalVolumeContentsUseBytes, 0, rawBytes, pos, LogicalVolumeContentsUseBytes.length );
		pos += LogicalVolumeContentsUseBytes.length;
		
		pos = BinaryTools.getUInt32BytesFromLong( NumberOfPartitions, rawBytes, pos );		
		pos = BinaryTools.getUInt32BytesFromLong( LengthOfImplementationUse, rawBytes, pos );		
				
		for( int i = 0; i < FreeSpaceTable.length; ++i )
		{
			pos = BinaryTools.getUInt32BytesFromLong( FreeSpaceTable[ i ], rawBytes, pos );
		}

		for( int i = 0; i < SizeTable.length; ++i )
		{
			pos = BinaryTools.getUInt32BytesFromLong( SizeTable[ i ], rawBytes, pos );			
		}
		
		System.arraycopy( ImplementationUse, 0, rawBytes, pos, ImplementationUse.length );
		pos += ImplementationUse.length;
		
		return rawBytes;
	}

	public byte[] getBytes( int blockSize )
	{
		byte bytesWithoutDescriptorTag[] = getBytesWithoutDescriptorTag();
	    
	    DescriptorTag.DescriptorCRCLength = bytesWithoutDescriptorTag.length;
	    DescriptorTag.DescriptorCRC = Checksum.cksum( bytesWithoutDescriptorTag );
	    
	    byte descriptorTagBytes[] = DescriptorTag.getBytes();
	    
	    int paddedLength = descriptorTagBytes.length + bytesWithoutDescriptorTag.length;
	    if( paddedLength % blockSize != 0 )
	    {
	    	paddedLength += blockSize - (paddedLength % blockSize); 
	    }
	    
	    byte[] rawBytes = new byte[ paddedLength ];
	    
	    int pos = 0;
	    
	    System.arraycopy( descriptorTagBytes, 0, rawBytes, pos, descriptorTagBytes.length );
	    pos += descriptorTagBytes.length;
	    
	    System.arraycopy( bytesWithoutDescriptorTag, 0, rawBytes, pos, bytesWithoutDescriptorTag.length );
	    pos += bytesWithoutDescriptorTag.length;	    
		
	    return rawBytes;
	}

	public void setImplementationUse( EntityID implementationID, long numberOfFiles, long numberOfDirectories, int minimumUDFReadRevision, int minimumUDFWriteRevision, int maximumUDFWriteRevision )
	{
		ImplementationUse = new byte[46];
		
		byte[] implementationIDBytes = implementationID.getBytes();		
		System.arraycopy( implementationIDBytes, 0, ImplementationUse, 0, implementationIDBytes.length );
		
		int pos = implementationIDBytes.length;
		
		pos = BinaryTools.getUInt32BytesFromLong( numberOfFiles, ImplementationUse, pos );
		pos = BinaryTools.getUInt32BytesFromLong( numberOfDirectories, ImplementationUse, pos );		
		pos = BinaryTools.getUInt16BytesFromInt( minimumUDFReadRevision, ImplementationUse, pos );
		pos = BinaryTools.getUInt16BytesFromInt( minimumUDFWriteRevision, ImplementationUse, pos );
		pos = BinaryTools.getUInt16BytesFromInt( maximumUDFWriteRevision, ImplementationUse, pos );
	}
}
