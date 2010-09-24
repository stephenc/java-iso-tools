/*
 *	PartitionMapTyp2.java
 *
 *	2006-06-02
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class PartitionMapType2
{
	public byte PartitionMapType;		// Uint8	== 2
	public byte PartitionMapLength;		// Uint8	== 64
	public byte	PartitionIdentifier[];	// byte[62] 
	
	public PartitionMapType2()
	{
		PartitionMapType = 2;
		PartitionMapLength = 64;
		PartitionIdentifier = new byte[62];
	}
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		PartitionMapType = myRandomAccessFile.readByte();
		PartitionMapLength = myRandomAccessFile.readByte();
		
		PartitionIdentifier = new byte[62];
		myRandomAccessFile.read( PartitionIdentifier );
	}	

	public void write( RandomAccessFile myRandomAccessFile )
	throws IOException
	{		
		myRandomAccessFile.write( getBytes() );
	}
	
	public byte[] getBytes()
	{
		byte rawBytes[] = new byte[64];
		
		rawBytes[0] = PartitionMapType;
		rawBytes[1] = PartitionMapLength;
		
		System.arraycopy( PartitionIdentifier, 0, rawBytes, 2, PartitionIdentifier.length );

		return rawBytes;
	}
	
	public void setupMetadataPartitionMap( EntityID partitionTypeIdentifier, int VolumeSequenceNumber, int PartitionNumber, long MetadataFileLocation, long MetadataMirrorFileLocation, long MetadataBitmapFileLocation, long AllocationUnitSize, int AlignmentUnitSize, byte Flags )
	{
		byte Reserved1[] = new byte[]{ 0x00, 0x00 };						// byte
		/*
		EntityID partitionTypeIdentifier = new EntityID();					// struct EntityID
		int VolumeSequenceNumber;											// Uint16
		int PartitionNumber;												// Uint16
		long MetadataFileLocation;											// Uint32
		long MetadataMirrorFileLocation;									// Uint32
		long MetadataBitmapFileLocation;									// Uint32
		long AllocationUnitSize;											// Uint32
		int AlignmentUnitSize;												// Uint16
		byte Flags;															// Uint8
		*/
		byte Reserved2[] = new byte[]{ 0x00, 0x00, 0x00, 0x00, 0x00 };		// byte[5]
		
		byte partitionTypeIdentifierBytes[] = partitionTypeIdentifier.getBytes();
		
		int pos = 0;
		
		System.arraycopy( Reserved1, 0, PartitionIdentifier, pos, Reserved1.length );
		pos += Reserved1.length;
	
		System.arraycopy( partitionTypeIdentifierBytes, 0, PartitionIdentifier, pos, partitionTypeIdentifierBytes.length );
		pos += partitionTypeIdentifierBytes.length;
		
		pos = BinaryTools.getUInt16BytesFromInt( VolumeSequenceNumber, PartitionIdentifier, pos );
		pos = BinaryTools.getUInt16BytesFromInt( PartitionNumber, PartitionIdentifier, pos );
		pos = BinaryTools.getUInt32BytesFromLong( MetadataFileLocation, PartitionIdentifier, pos );
		pos = BinaryTools.getUInt32BytesFromLong( MetadataMirrorFileLocation, PartitionIdentifier, pos );
		pos = BinaryTools.getUInt32BytesFromLong( MetadataBitmapFileLocation, PartitionIdentifier, pos );
		pos = BinaryTools.getUInt32BytesFromLong( AllocationUnitSize, PartitionIdentifier, pos );
		pos = BinaryTools.getUInt16BytesFromInt( AlignmentUnitSize, PartitionIdentifier, pos );
		
		PartitionIdentifier[pos++] = Flags;

		System.arraycopy( Reserved2, 0, PartitionIdentifier, pos, Reserved2.length );
		pos += Reserved2.length;
	}
	
}
