/*
 *	LogicalVolumeDescriptor.java
 *
 *	2006-06-02
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class LogicalVolumeDescriptor extends VolumeDescriptorSequenceItem
{
	public CharSpec		DescriptorCharacterSet;			// struct charspec
	public byte			LogicalVolumeIdentifier[];		// dstring[128]
	public long			LogicalBlockSize;				// Uint32
	public EntityID		DomainIdentifier;				// struct EntityID
	
	//public byte		LogicalVolumeContentsUse[];		// dstring[16]
	public Long_ad		LogicalVolumeContentsUse;
	
	public long			MapTableLength;					// Uint32
	public long			NumberofPartitionMaps;			// Uint32
	public EntityID		ImplementationIdentifier;		// struct EntityID
	public byte			ImplementationUse[];			// byte[128]
	public Extend_ad	IntegritySequenceExtent;		// extend_ad
	public byte			PartitionMaps[];				// byte[]
	
	public LogicalVolumeDescriptor()
	{
		DescriptorTag = new Tag();
		DescriptorTag.TagIdentifier = 6;
	
		DescriptorCharacterSet = new CharSpec();
		LogicalVolumeIdentifier = new byte[128];
		DomainIdentifier = new EntityID();
		LogicalVolumeContentsUse = new Long_ad();
		ImplementationIdentifier = new EntityID();
		ImplementationUse = new byte[128];
		IntegritySequenceExtent = new Extend_ad();
		PartitionMaps = new byte[0];
	}
	
	public void setLogicalVolumeIdentifier( String volumeIdentifier )
	throws Exception
	{
		if( volumeIdentifier.length() > 126 )
		{
			throw new Exception( "error: logical volume identifier length > 126 characters" );
		}
			
			
		LogicalVolumeIdentifier = new byte[128];
		
		try
		{			
			byte volumeIdentifierBytes[] = volumeIdentifier.getBytes( "UTF-16" );
			
			int compId = OSTAUnicode.getBestCompressionId( volumeIdentifierBytes );
			
			byte tmpIdentifier[] = OSTAUnicode.CompressUnicodeByte( volumeIdentifierBytes, compId );
			
			int length = ( tmpIdentifier.length < 127 ) ? tmpIdentifier.length : 127; 
			
			System.arraycopy( tmpIdentifier, 0, LogicalVolumeIdentifier, 0, length );
			
			LogicalVolumeIdentifier[ LogicalVolumeIdentifier.length - 1 ] = (byte)length;
		}
		catch( Exception ex ) { /* never happens */ }		
	}
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		DescriptorTag = new Tag();
		DescriptorTag.read( myRandomAccessFile );
		
		VolumeDescriptorSequenceNumber = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		DescriptorCharacterSet = new CharSpec();
		DescriptorCharacterSet.read( myRandomAccessFile );
		
		LogicalVolumeIdentifier = new byte[128];
		myRandomAccessFile.read( LogicalVolumeIdentifier );
		
		LogicalBlockSize = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		DomainIdentifier = new EntityID();
		DomainIdentifier.read( myRandomAccessFile );
		
		//LogicalVolumeContentsUse = new byte[16];
		//myRandomAccessFile.read( LogicalVolumeContentsUse );
		
		LogicalVolumeContentsUse = new Long_ad();
		LogicalVolumeContentsUse.read( myRandomAccessFile );
		
		MapTableLength = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		NumberofPartitionMaps = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		ImplementationIdentifier = new EntityID();
		ImplementationIdentifier.read( myRandomAccessFile );
		
		ImplementationUse = new byte[128];
		myRandomAccessFile.read( ImplementationUse );
		
		IntegritySequenceExtent = new Extend_ad();
		IntegritySequenceExtent.read( myRandomAccessFile );
		
		PartitionMaps = new byte[ (int)MapTableLength ];
		myRandomAccessFile.read( PartitionMaps );
	}
	
	public byte[] getBytesWithoutDescriptorTag()
	{
		byte DescriptorCharacterSetBytes[] = DescriptorCharacterSet.getBytes();
		byte DomainIdentifierBytes[] = DomainIdentifier.getBytes();
		byte LogicalVolumeContentsUseBytes[] = LogicalVolumeContentsUse.getBytes();
		byte ImplementationIdentifierBytes[] = ImplementationIdentifier.getBytes();
		byte IntegritySequenceExtentBytes[] = IntegritySequenceExtent.getBytes();
		
		byte rawBytes[] = new byte[ 272
		                            + DescriptorCharacterSetBytes.length 
		                            + DomainIdentifierBytes.length 
		                            + LogicalVolumeContentsUseBytes.length
		                            + ImplementationIdentifierBytes.length
		                            + IntegritySequenceExtentBytes.length
		                            + PartitionMaps.length ];
		
		int pos = 0;
		
		pos = BinaryTools.getUInt32BytesFromLong( VolumeDescriptorSequenceNumber, rawBytes, pos );

		System.arraycopy( DescriptorCharacterSetBytes, 0, rawBytes, pos, DescriptorCharacterSetBytes.length );
		pos += DescriptorCharacterSetBytes.length;

		System.arraycopy( LogicalVolumeIdentifier, 0, rawBytes, pos, LogicalVolumeIdentifier.length );
		pos += LogicalVolumeIdentifier.length;

		pos = BinaryTools.getUInt32BytesFromLong( LogicalBlockSize, rawBytes, pos );		
		
		System.arraycopy( DomainIdentifierBytes, 0, rawBytes, pos, DomainIdentifierBytes.length );
		pos += DomainIdentifierBytes.length;		

		System.arraycopy( LogicalVolumeContentsUseBytes, 0, rawBytes, pos, LogicalVolumeContentsUseBytes.length );
		pos += LogicalVolumeContentsUseBytes.length;		
		
		pos = BinaryTools.getUInt32BytesFromLong( MapTableLength, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( NumberofPartitionMaps, rawBytes, pos );
		
		System.arraycopy( ImplementationIdentifierBytes, 0, rawBytes, pos, ImplementationIdentifierBytes.length );
		pos += ImplementationIdentifierBytes.length;		
		
		System.arraycopy( ImplementationUse, 0, rawBytes, pos, ImplementationUse.length );
		pos += ImplementationUse.length;		
		
		System.arraycopy( IntegritySequenceExtentBytes, 0, rawBytes, pos, IntegritySequenceExtentBytes.length );
		pos += IntegritySequenceExtentBytes.length;		
		
		System.arraycopy( PartitionMaps, 0, rawBytes, pos, PartitionMaps.length );
		pos += PartitionMaps.length;		

		return rawBytes;
	}
}
