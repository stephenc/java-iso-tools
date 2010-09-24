/*
 *	Short_ad.java
 *
 *	2006-06-17
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class Short_ad
{
	public long	ExtentLength;		// Uint32
	public long	ExtentPosition;		// Uint32
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		ExtentLength = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		ExtentPosition = BinaryTools.readUInt32AsLong( myRandomAccessFile );
	}

	public byte[] getBytes()
	{
		byte[] rawBytes = new byte[8];
		
		int pos = 0;
		
		pos = BinaryTools.getUInt32BytesFromLong( ExtentLength, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( ExtentPosition, rawBytes, pos );
		
		return rawBytes;
	}
}
