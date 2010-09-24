/*
 *	VolumeStructureDescriptor.java
 *
 *	2006-06-01
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;


public class VolumeStructureDescriptor
{
	public byte	StructureType;				// Uint8
	public byte	StandardIdentifier[];		// byte[5]
	public byte StructureVersion;			// Uint8
	public byte StructureData[];			// byte[2041]
	
	public VolumeStructureDescriptor()
	{
		StandardIdentifier = new byte[5];
		StructureData = new byte[2041];
	}
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		StructureType = myRandomAccessFile.readByte();
		
		StandardIdentifier = new byte[5];
		myRandomAccessFile.read( StandardIdentifier );
		
		StructureVersion = myRandomAccessFile.readByte();
		
		StructureData = new byte[2041];
		myRandomAccessFile.read( StructureData );
	}

	public void write( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		myRandomAccessFile.writeByte( StructureType );
		myRandomAccessFile.write( StandardIdentifier );
		myRandomAccessFile.writeByte( StructureVersion );
		myRandomAccessFile.write( StructureData );
	}
	
	public byte[] getBytes()
	{
		byte[] rawBytes = new byte[2048];
		
		int pos = 0;
		
		rawBytes[ pos++ ] = StructureType;
		
		System.arraycopy( StandardIdentifier, 0, rawBytes, pos, StandardIdentifier.length );
		pos += StandardIdentifier.length;
		
		rawBytes[ pos++ ] = StructureVersion;

		System.arraycopy( StructureData, 0, rawBytes, pos, StructureData.length );
		pos += StructureData.length;
		
		return rawBytes;
	}
}
