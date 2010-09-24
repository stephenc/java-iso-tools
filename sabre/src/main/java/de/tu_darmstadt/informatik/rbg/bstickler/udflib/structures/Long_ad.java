/*
 *	Long_ad.java
 *
 *	2006-06-03
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class Long_ad
{
	public long		ExtentLength;			// Uint32
	public Lb_addr	ExtentLocation;			// Lb_addr
	public byte 	implementationUse[];	// byte[6]
	
	public Long_ad()
	{
		ExtentLocation = new Lb_addr();
		implementationUse = new byte[6];
	}
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		ExtentLength = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		ExtentLocation = new Lb_addr();
		ExtentLocation.read( myRandomAccessFile );
		
		implementationUse = new byte[6];
		myRandomAccessFile.read( implementationUse );
	}
	
	public int read( byte[] rawBytes, int startPosition )
	{	
		int position = startPosition;
		
		ExtentLength = (rawBytes[position++] & 0xFF)
					 + (rawBytes[position++] & 0xFF) * 256 
					 + (rawBytes[position++] & 0xFF) * 256 * 256 
					 + (rawBytes[position++] & 0xFF) * 256 * 256 * 256;
		
		ExtentLocation = new Lb_addr();
		position = ExtentLocation.read( rawBytes, position );
		
		implementationUse = new byte[6];
		System.arraycopy( rawBytes, position, implementationUse, 0, implementationUse.length );
		position += implementationUse.length;
		
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
		byte ExtentLocationBytes[] = ExtentLocation.getBytes();
		
		byte rawBytes[] = new byte[ 10 + ExtentLocationBytes.length ];

		int pos = 0;
		
		pos = BinaryTools.getUInt32BytesFromLong( ExtentLength, rawBytes, pos );
		
		System.arraycopy( ExtentLocationBytes, 0, rawBytes, pos, ExtentLocationBytes.length );
		pos += ExtentLocationBytes.length;

		System.arraycopy( implementationUse, 0, rawBytes, pos, implementationUse.length );
		pos += implementationUse.length;
		
		return rawBytes;
	}
}
