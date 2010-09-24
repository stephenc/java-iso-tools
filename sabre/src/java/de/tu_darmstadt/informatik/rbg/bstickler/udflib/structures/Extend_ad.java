/*
 *	Extend_ad.java
 *
 *	2006-06-01
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class Extend_ad
{
	public long	len;		// Uint32
	public long loc;		// Uint32
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		len = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		loc = BinaryTools.readUInt32AsLong( myRandomAccessFile );
	}

	public void write( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		myRandomAccessFile.write( getBytes() );		
	}	
	
	public byte[] getBytes()
	{
		byte rawBytes[] = new byte[8];
	
		int pos = 0;
		
		pos = BinaryTools.getUInt32BytesFromLong( len, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( loc, rawBytes, pos );		
		
		return rawBytes;
	}
}
