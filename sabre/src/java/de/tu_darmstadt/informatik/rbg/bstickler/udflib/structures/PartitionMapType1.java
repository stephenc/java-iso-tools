/*
 *	PartitionMapTyp1.java
 *
 *	2006-06-02
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class PartitionMapType1
{
	public byte PartitionMapType;		// Uint8
	public byte PartitionMapLength;		// Uint8
	public int	VolumeSequenceNumber;	// Uint16 
	public int	PartitionNumber;		// Uint16
	
	public PartitionMapType1()
	{
		PartitionMapType = 1;
		PartitionMapLength = 6;
		VolumeSequenceNumber = 1;
		PartitionNumber = 0;
	}
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		PartitionMapType = myRandomAccessFile.readByte();
		PartitionMapLength = myRandomAccessFile.readByte();
		VolumeSequenceNumber = BinaryTools.readUInt16AsInt( myRandomAccessFile );
		PartitionNumber = BinaryTools.readUInt16AsInt( myRandomAccessFile );
	}	

	public void write( RandomAccessFile myRandomAccessFile )
	throws IOException
	{		
		myRandomAccessFile.write( getBytes() );
	}
	
	public byte[] getBytes()
	{
		byte rawBytes[] = new byte[6];
		
		int pos = 0;
		
		rawBytes[pos++] = PartitionMapType;
		rawBytes[pos++] = PartitionMapLength;
		
		pos = BinaryTools.getUInt16BytesFromInt( VolumeSequenceNumber, rawBytes, pos );
		BinaryTools.getUInt16BytesFromInt( PartitionNumber, rawBytes, pos );

		return rawBytes;
	}
	
}
