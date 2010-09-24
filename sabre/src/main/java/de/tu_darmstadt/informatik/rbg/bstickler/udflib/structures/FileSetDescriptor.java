/*
 *	FileSetDescriptor.java
 *
 *	2006-06-03
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class FileSetDescriptor
{
	public Tag 			DescriptorTag;							// struct tag
	public Timestamp 	RecordingDateandTime;					// struct timestamp
	public int 			InterchangeLevel;						// Uint16
	public int			MaximumInterchangeLevel;				// Uint16
	public long			CharacterSetList;						// Uint32
	public long			MaximumCharacterSetList;				// Uint32
	public long			FileSetNumber;							// Uint32
	public long			FileSetDescriptorNumber;				// Uint32
	public CharSpec		LogicalVolumeIdentifierCharacterSet;	// struct charspec
	public byte			LogicalVolumeIdentifier[];				// dstring[128]
	public CharSpec		FileSetCharacterSet;					// struct charspec
	public byte			FileSetIdentifier[];					// dstring[32]
	public byte			CopyrightFileIdentifier[];				// dstring[32]
	public byte			AbstractFileIdentifier[];				// dstring[32]
	public Long_ad		RootDirectoryICB;						// struct long_ad
	public EntityID		DomainIdentifier;						// struct EntityID
	public Long_ad		NextExtent;								// struct long_ad
	public byte			Reserved[];								// byte[48]
	
	public FileSetDescriptor()
	{
		DescriptorTag = new Tag();
		DescriptorTag.TagIdentifier = 256;		
		
		RecordingDateandTime = new Timestamp();
		LogicalVolumeIdentifierCharacterSet = new CharSpec();
		LogicalVolumeIdentifier = new byte[128];
		FileSetCharacterSet = new CharSpec();
		FileSetIdentifier = new byte[32];
		CopyrightFileIdentifier = new byte[32];
		AbstractFileIdentifier = new byte[32];
		RootDirectoryICB = new Long_ad();
		DomainIdentifier = new EntityID();
		NextExtent = new Long_ad();
		Reserved = new byte[48];
	}
	
	public void setLogicalVolumeIdentifier( String logicalVolumeIdentifier )
	{
		LogicalVolumeIdentifier = new byte[128];
		
		try
		{
			byte logicalVolumeIdentifierBytes[] = logicalVolumeIdentifier.getBytes( "UTF-16" );
			
			int compId = OSTAUnicode.getBestCompressionId( logicalVolumeIdentifierBytes );
			
			byte tmpIdentifier[] = OSTAUnicode.CompressUnicodeByte( logicalVolumeIdentifierBytes, compId );
			
			int length = ( tmpIdentifier.length < 127 ) ? tmpIdentifier.length : 127;
			
			System.arraycopy( tmpIdentifier, 0, LogicalVolumeIdentifier, 0, length );
			
			LogicalVolumeIdentifier[ LogicalVolumeIdentifier.length - 1 ] = (byte)length;
		}
		catch( Exception ex ) { /* never happens */ }
	}

	public void setFileSetIdentifier( String fileSetIdentifier )
	{
		FileSetIdentifier = new byte[32];
		
		try
		{			
			byte fileSetIdentifierBytes[] = fileSetIdentifier.getBytes( "UTF-16" );
			
			int compId = OSTAUnicode.getBestCompressionId( fileSetIdentifierBytes );
			
			byte tmpIdentifier[] = OSTAUnicode.CompressUnicodeByte( fileSetIdentifierBytes, compId );
			
			int length = ( tmpIdentifier.length < 31 ) ? tmpIdentifier.length : 31;
			
			System.arraycopy( tmpIdentifier, 0, FileSetIdentifier, 0, length );
			
			FileSetIdentifier[ FileSetIdentifier.length - 1 ] = (byte)length;
		}
		catch( Exception ex ) { /* never happens */ }
	}	
	
	public void setAbstractFileIdentifier( String abstractFileIdentifier )
	{
		AbstractFileIdentifier = new byte[32];
		
		try
		{
			byte abstractFileIdentifierBytes[] = abstractFileIdentifier.getBytes( "UTF-16" );
			
			int compId = OSTAUnicode.getBestCompressionId( abstractFileIdentifierBytes );
			
			byte tmpIdentifier[] = OSTAUnicode.CompressUnicodeByte( abstractFileIdentifierBytes, compId );
			
			int length = ( tmpIdentifier.length < 31 ) ? tmpIdentifier.length : 31;
			
			System.arraycopy( tmpIdentifier, 0, AbstractFileIdentifier, 0, length );
			
			AbstractFileIdentifier[ AbstractFileIdentifier.length - 1 ] = (byte)length;
		}
		catch( Exception ex ) { /* never happens */ }
	}
	
	public void setCopyrightFileIdentifier( String copyrightFileIdentifier )
	{
		CopyrightFileIdentifier = new byte[32];
		
		try
		{
			byte copyrightFileIdentifierBytes[] = copyrightFileIdentifier.getBytes( "UTF-16" );
			
			int compId = OSTAUnicode.getBestCompressionId( copyrightFileIdentifierBytes );
			
			byte tmpIdentifier[] = OSTAUnicode.CompressUnicodeByte( copyrightFileIdentifierBytes, compId );
			
			int length = ( tmpIdentifier.length < 31 ) ? tmpIdentifier.length : 31;
			
			System.arraycopy( tmpIdentifier, 0, CopyrightFileIdentifier, 0, length );
			
			CopyrightFileIdentifier[ CopyrightFileIdentifier.length - 1 ] = (byte)length;
		}
		catch( Exception ex ) { /* never happens */ }
	}

	
	public void Load( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		DescriptorTag = new Tag();
		DescriptorTag.read( myRandomAccessFile );
		
		RecordingDateandTime = new Timestamp();
		RecordingDateandTime.read( myRandomAccessFile );
		
		InterchangeLevel = BinaryTools.readUInt16AsInt( myRandomAccessFile );		
		MaximumInterchangeLevel = BinaryTools.readUInt16AsInt( myRandomAccessFile );
		CharacterSetList = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		MaximumCharacterSetList = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		FileSetNumber = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		FileSetDescriptorNumber = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		LogicalVolumeIdentifierCharacterSet = new CharSpec();
		LogicalVolumeIdentifierCharacterSet.read( myRandomAccessFile );
		
		LogicalVolumeIdentifier = new byte[128];
		myRandomAccessFile.read( LogicalVolumeIdentifier );
		
		FileSetCharacterSet = new CharSpec();
		FileSetCharacterSet.read( myRandomAccessFile );
		
		FileSetIdentifier = new byte[32];
		myRandomAccessFile.read( FileSetIdentifier );
		
		CopyrightFileIdentifier = new byte[32];
		myRandomAccessFile.read( CopyrightFileIdentifier );
		
		AbstractFileIdentifier = new byte[32];
		myRandomAccessFile.read( AbstractFileIdentifier );
		
		RootDirectoryICB = new Long_ad();
		RootDirectoryICB.read( myRandomAccessFile );
		
		DomainIdentifier = new EntityID();
		DomainIdentifier.read( myRandomAccessFile );
		
		NextExtent = new Long_ad();
		NextExtent.read( myRandomAccessFile );
		
		Reserved = new byte[48];
		myRandomAccessFile.read( Reserved );
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
		byte RecordingDateandTimeBytes[] = RecordingDateandTime.getBytes();
		byte LogicalVolumeIdentifierCharacterSetBytes[] = LogicalVolumeIdentifierCharacterSet.getBytes();
		byte FileSetCharacterSetBytes[] = FileSetCharacterSet.getBytes();
		byte RootDirectoryICBBytes[] = RootDirectoryICB.getBytes(); 
		byte DomainIdentifierBytes[] = DomainIdentifier.getBytes();
		byte NextExtentBytes[] = NextExtent.getBytes();
		
		byte rawBytes[] = new byte[ 292
		                            + RecordingDateandTimeBytes.length
		                            + LogicalVolumeIdentifierCharacterSetBytes.length
		                            + FileSetCharacterSetBytes.length
		                            + RootDirectoryICBBytes.length
		                            + DomainIdentifierBytes.length
		                            + NextExtentBytes.length ];
		
		int pos = 0;
		
		System.arraycopy( RecordingDateandTimeBytes, 0, rawBytes, pos, RecordingDateandTimeBytes.length );
		pos += RecordingDateandTimeBytes.length;

		pos = BinaryTools.getUInt16BytesFromInt( InterchangeLevel, rawBytes, pos );
		pos = BinaryTools.getUInt16BytesFromInt( MaximumInterchangeLevel, rawBytes, pos );		
		pos = BinaryTools.getUInt32BytesFromLong( CharacterSetList, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( MaximumCharacterSetList, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( FileSetNumber, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( FileSetDescriptorNumber, rawBytes, pos );		

		System.arraycopy( LogicalVolumeIdentifierCharacterSetBytes, 0, rawBytes, pos, LogicalVolumeIdentifierCharacterSetBytes.length );
		pos += LogicalVolumeIdentifierCharacterSetBytes.length;
		
		System.arraycopy( LogicalVolumeIdentifier, 0, rawBytes, pos, LogicalVolumeIdentifier.length );
		pos += LogicalVolumeIdentifier.length;
		
		System.arraycopy( FileSetCharacterSetBytes, 0, rawBytes, pos, FileSetCharacterSetBytes.length );
		pos += FileSetCharacterSetBytes.length;

		System.arraycopy( FileSetIdentifier, 0, rawBytes, pos, FileSetIdentifier.length );
		pos += FileSetIdentifier.length;
		
		System.arraycopy( CopyrightFileIdentifier, 0, rawBytes, pos, CopyrightFileIdentifier.length );
		pos += CopyrightFileIdentifier.length;
		
		System.arraycopy( AbstractFileIdentifier, 0, rawBytes, pos, AbstractFileIdentifier.length );
		pos += AbstractFileIdentifier.length;
		
		System.arraycopy( RootDirectoryICBBytes, 0, rawBytes, pos, RootDirectoryICBBytes.length );
		pos += RootDirectoryICBBytes.length;
		
		System.arraycopy( DomainIdentifierBytes, 0, rawBytes, pos, DomainIdentifierBytes.length );
		pos += DomainIdentifierBytes.length;
		
		System.arraycopy( NextExtentBytes, 0, rawBytes, pos, NextExtentBytes.length );
		pos += NextExtentBytes.length;
		
		System.arraycopy( Reserved, 0, rawBytes, pos, Reserved.length );
		pos += Reserved.length;
		
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
	
}
