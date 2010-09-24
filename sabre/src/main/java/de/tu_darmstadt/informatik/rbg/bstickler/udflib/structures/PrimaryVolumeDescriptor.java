/*
 *	PrimaryVolumeDescriptor.java
 *
 *	2006-06-01
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class PrimaryVolumeDescriptor extends VolumeDescriptorSequenceItem
{
	public long			PrimaryVolumeDescriptorNumber;					// Uint32
	public byte			VolumeIdentifier[];								// dstring[32]
	public int			VolumeSequenceNumber;							// Uint16
	public int			MaximumVolumeSequenceNumber;					// Uint16
	public int			InterchangeLevel;								// Uint16
	public int			MaximumInterchangeLevel;						// Uint16
	public long			CharacterSetList;								// Uint32
	public long			MaximumCharacterSetList;						// Uint32
	public byte			VolumeSetIdentifier[];							// dstring[128]
	public CharSpec		DescriptorCharacterSet;							// struct charspec
	public CharSpec		ExplanatoryCharacterSet;						// struct charspec
	public Extend_ad	VolumeAbstract;									// struct extent_ad
	public Extend_ad	VolumeCopyrightNotice;							// struct extent_ad
	public EntityID		ApplicationIdentifier;							// struct EntityID
	public Timestamp	RecordingDateandTime;							// struct timestamp
	public EntityID		ImplementationIdentifier;						// struct EntityID
	public byte			ImplementationUse[];							// byte[64]
	public long			PredecessorVolumeDescriptorSequenceLocation;	// Uint32
	public int			Flags;											// Uint16
	public byte			Reserved[];										// byte[22]
	
	public PrimaryVolumeDescriptor()
	{
		DescriptorTag = new Tag();
		DescriptorTag.TagIdentifier = 1;		
		
		VolumeIdentifier = new byte[32];
		VolumeSetIdentifier = new byte[128];
		
		DescriptorCharacterSet = new CharSpec();
		ExplanatoryCharacterSet = new CharSpec();
		
		VolumeAbstract = new Extend_ad();
		VolumeCopyrightNotice = new Extend_ad();
		
		ApplicationIdentifier = new EntityID();
		
		RecordingDateandTime = new Timestamp();
	
		ImplementationIdentifier = new EntityID();
		
		ImplementationUse = new byte[64];
		
		Reserved = new byte[22];
	}
	
	public void setVolumeIdentifier( String volumeIdentifier )
	throws Exception
	{
		if( volumeIdentifier.length() > 30 )
		{
			throw new Exception( "error: volume identifier length > 30 characters" );
		}
		
		VolumeIdentifier = new byte[32];
		
		try
		{
			byte volumeIdentifierBytes[] = volumeIdentifier.getBytes( "UTF-16" );
			
			int compId = OSTAUnicode.getBestCompressionId( volumeIdentifierBytes );
			
			byte tmpIdentifier[] = OSTAUnicode.CompressUnicodeByte( volumeIdentifierBytes, compId );

			int length = ( tmpIdentifier.length < 31 ) ? tmpIdentifier.length : 31;
			
			System.arraycopy( tmpIdentifier, 0, VolumeIdentifier, 0, length );
			
			VolumeIdentifier[ VolumeIdentifier.length - 1 ] = (byte)length;
		}
		catch( Exception ex ) { /* never happens */ }		
	}

	public void setVolumeSetIdentifier( String volumeSetIdentifier )
	throws Exception
	{
		if( volumeSetIdentifier.length() > 126 )
		{
			throw new Exception( "error: volume set identifier length > 126 characters" );
		}
		
		VolumeSetIdentifier = new byte[128];
		
		try
		{
			byte volumeSetIdentifierBytes[] = volumeSetIdentifier.getBytes( "UTF-16" );
			
			int compId = OSTAUnicode.getBestCompressionId( volumeSetIdentifierBytes );
			
			byte[] tmpIdentifier = OSTAUnicode.CompressUnicodeByte( volumeSetIdentifierBytes, compId );
			
			int length = ( tmpIdentifier.length < 127 ) ? tmpIdentifier.length : 127;
			
			System.arraycopy( tmpIdentifier, 0, VolumeSetIdentifier, 0, length );
			
			VolumeSetIdentifier[ VolumeSetIdentifier.length - 1 ] = (byte)length;
		}
		catch( Exception ex ) { /* never happens */ }		
	}
	
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		DescriptorTag = new Tag();
		DescriptorTag.read( myRandomAccessFile );
		
		VolumeDescriptorSequenceNumber = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		PrimaryVolumeDescriptorNumber = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		VolumeIdentifier = new byte[32];
		myRandomAccessFile.read( VolumeIdentifier );
		
		VolumeSequenceNumber = BinaryTools.readUInt16AsInt( myRandomAccessFile );
		MaximumVolumeSequenceNumber = BinaryTools.readUInt16AsInt( myRandomAccessFile );
		InterchangeLevel = BinaryTools.readUInt16AsInt( myRandomAccessFile );
		MaximumInterchangeLevel = BinaryTools.readUInt16AsInt( myRandomAccessFile );
		CharacterSetList = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		MaximumCharacterSetList = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		VolumeSetIdentifier = new byte[128];
		myRandomAccessFile.read( VolumeSetIdentifier );
		
		DescriptorCharacterSet = new CharSpec();
		DescriptorCharacterSet.read( myRandomAccessFile );
		
		ExplanatoryCharacterSet = new CharSpec();
		ExplanatoryCharacterSet.read( myRandomAccessFile );
		
		VolumeAbstract = new Extend_ad();
		VolumeAbstract.read( myRandomAccessFile );
		
		VolumeCopyrightNotice = new Extend_ad();
		VolumeCopyrightNotice.read( myRandomAccessFile );
		
		ApplicationIdentifier = new EntityID();
		ApplicationIdentifier.read( myRandomAccessFile );
		
		RecordingDateandTime = new Timestamp();
		RecordingDateandTime.read( myRandomAccessFile );
		
		ImplementationIdentifier = new EntityID();
		ImplementationIdentifier.read( myRandomAccessFile );
		
		ImplementationUse = new byte[64];
		myRandomAccessFile.read( ImplementationUse );
		
		PredecessorVolumeDescriptorSequenceLocation = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		Flags = BinaryTools.readUInt16AsInt( myRandomAccessFile );
		
		Reserved = new byte[22];
		myRandomAccessFile.read( Reserved );
	}
		
	public byte[] getBytesWithoutDescriptorTag()
	{
		byte DescriptorCharacterSetBytes[] = DescriptorCharacterSet.getBytes();
		byte ExplanatoryCharacterSetBytes[] = ExplanatoryCharacterSet.getBytes();		
		byte VolumeAbstractBytes[] = VolumeAbstract.getBytes();
		byte VolumeCopyrightNoticeBytes[] = VolumeCopyrightNotice.getBytes();
		byte ApplicationIdentifierBytes[] = ApplicationIdentifier.getBytes();		
		byte RecordingDateandTimeBytes[] = RecordingDateandTime.getBytes();
		byte ImplementationIdentifierBytes[] = ImplementationIdentifier.getBytes();

		byte rawBytes[] = new byte[ 276 
		                            + DescriptorCharacterSetBytes.length 
		                            + ExplanatoryCharacterSetBytes.length 
		                            + VolumeAbstractBytes.length 
		                            + VolumeCopyrightNoticeBytes.length 
		                            + ApplicationIdentifierBytes.length 
		                            + RecordingDateandTimeBytes.length 
		                            + ImplementationIdentifierBytes.length ];
		
		int pos = 0;
		
		pos = BinaryTools.getUInt32BytesFromLong( VolumeDescriptorSequenceNumber, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( PrimaryVolumeDescriptorNumber, rawBytes, pos );		
		
		System.arraycopy( VolumeIdentifier, 0, rawBytes, pos, VolumeIdentifier.length );
		pos += VolumeIdentifier.length;

		pos = BinaryTools.getUInt16BytesFromInt( VolumeSequenceNumber, rawBytes, pos );		
		pos = BinaryTools.getUInt16BytesFromInt( MaximumVolumeSequenceNumber, rawBytes, pos );
		pos = BinaryTools.getUInt16BytesFromInt( InterchangeLevel, rawBytes, pos );
		pos = BinaryTools.getUInt16BytesFromInt( MaximumInterchangeLevel, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( CharacterSetList, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( MaximumCharacterSetList, rawBytes, pos );

		System.arraycopy( VolumeSetIdentifier, 0, rawBytes, pos, VolumeSetIdentifier.length );
		pos += VolumeSetIdentifier.length;

		System.arraycopy( DescriptorCharacterSetBytes, 0, rawBytes, pos, DescriptorCharacterSetBytes.length );
		pos += DescriptorCharacterSetBytes.length;
		
		System.arraycopy( ExplanatoryCharacterSetBytes, 0, rawBytes, pos, ExplanatoryCharacterSetBytes.length );
		pos += ExplanatoryCharacterSetBytes.length;

		System.arraycopy( VolumeAbstractBytes, 0, rawBytes, pos, VolumeAbstractBytes.length );
		pos += VolumeAbstractBytes.length;

		System.arraycopy( VolumeCopyrightNoticeBytes, 0, rawBytes, pos, VolumeCopyrightNoticeBytes.length );
		pos += VolumeCopyrightNoticeBytes.length;

		System.arraycopy( ApplicationIdentifierBytes, 0, rawBytes, pos, ApplicationIdentifierBytes.length );
		pos += ApplicationIdentifierBytes.length;

		System.arraycopy( RecordingDateandTimeBytes, 0, rawBytes, pos, RecordingDateandTimeBytes.length );
		pos += RecordingDateandTimeBytes.length;
		
		System.arraycopy( ImplementationIdentifierBytes, 0, rawBytes, pos, ImplementationIdentifierBytes.length );
		pos += ImplementationIdentifierBytes.length;

		System.arraycopy( ImplementationUse, 0, rawBytes, pos, ImplementationUse.length );
		pos += ImplementationUse.length;
		
		pos = BinaryTools.getUInt32BytesFromLong( PredecessorVolumeDescriptorSequenceLocation, rawBytes, pos );
		pos = BinaryTools.getUInt16BytesFromInt( Flags, rawBytes, pos );

		System.arraycopy( Reserved, 0, rawBytes, pos, Reserved.length );
		pos += Reserved.length;
		
		return rawBytes;
	}
	
}
