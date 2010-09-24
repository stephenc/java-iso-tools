/*
 *	Lb_addr.java
 *
 *	2006-06-03
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class Lb_addr
{
	public long	lb_num;		// Uint32
	public int	part_num;	// Uint16
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		lb_num = myRandomAccessFile.readUnsignedByte() + myRandomAccessFile.readUnsignedByte() * 256 + myRandomAccessFile.readUnsignedByte() * 256 * 256 + (int)myRandomAccessFile.readUnsignedByte() * 256 * 256 * 256;
		part_num = myRandomAccessFile.readUnsignedByte() + myRandomAccessFile.readUnsignedByte() * 256;
	}

	public int read( byte[] rawBytes, int startPosition )
	{	
		int position = startPosition;
		
		lb_num = (rawBytes[position++] & 0xFF)
			   + (rawBytes[position++] & 0xFF) * 256 
			   + (rawBytes[position++] & 0xFF) * 256 * 256 
			   + (rawBytes[position++] & 0xFF) * 256 * 256 * 256;
		
		part_num = (rawBytes[position++] & 0xFF) + (rawBytes[position++] & 0xFF) * 256;
		
		return position;
	}
	
	public void write( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		byte rawBytes[] = getBytes();
		myRandomAccessFile.write( rawBytes );
	}	
	
	public byte[] getBytes()
	{
		byte rawBytes[] = new byte[6];
		
		int pos = 0;
		
		pos = BinaryTools.getUInt32BytesFromLong( lb_num, rawBytes, pos );
		pos = BinaryTools.getUInt16BytesFromInt( part_num, rawBytes, pos );		
		
		return rawBytes;
	}	
}
