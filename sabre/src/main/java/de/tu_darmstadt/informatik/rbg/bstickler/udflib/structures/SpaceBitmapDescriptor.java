/*
 *	SpaceBitmapDescriptor.java
 *
 *	2006-06-17
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class SpaceBitmapDescriptor
{
	public Tag	DescriptorTag;			// struct tag
	public long	NumberOfBits;			// Uint32
	public long NumberOfBytes;			// Uint32
	public byte	Bitmap[];				// byte[]

	
	public SpaceBitmapDescriptor()
	{
		DescriptorTag = new Tag();
		DescriptorTag.DescriptorVersion = 3;
		DescriptorTag.TagIdentifier = 264;
		Bitmap = new byte[0];
	}
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		DescriptorTag = new Tag();
		DescriptorTag.read( myRandomAccessFile );
		
		NumberOfBits = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		NumberOfBytes = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		Bitmap = new byte[ (int)NumberOfBytes ];
		myRandomAccessFile.read( Bitmap );
	}
	
	public void write( RandomAccessFile myRandomAccessFile, int blockSize )
	throws IOException
	{
		byte rawBytes[] = getBytesWithoutDescriptorTag();
	    
	    DescriptorTag.DescriptorCRCLength = 8; // not rawBytes.length according to errata DCN-5108 for UDF 2.50 and lower
	    DescriptorTag.DescriptorCRC = Checksum.cksum( getFirst8Bytes() ); // not rawBytes ^^ 
		
		DescriptorTag.write( myRandomAccessFile );
			
		myRandomAccessFile.write( rawBytes );

		int bytesWritten = rawBytes.length + 16;

		byte emptyBytesInBlock[] = new byte[ blockSize - (bytesWritten % blockSize) ];
		myRandomAccessFile.write( emptyBytesInBlock );		
	}
	
	public byte[] getFirst8Bytes()
	{
		byte rawBytes[] = new byte[ 8 ];
		
		int pos = 0;
		
		pos = BinaryTools.getUInt32BytesFromLong( NumberOfBits, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( NumberOfBytes, rawBytes, pos );
		
		return rawBytes;
	}

	public byte[] getBytesWithoutDescriptorTag()
	{
		byte rawBytes[] = new byte[ 8 + Bitmap.length ];
		
		int pos = 0;
		
		pos = BinaryTools.getUInt32BytesFromLong( NumberOfBits, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( NumberOfBytes, rawBytes, pos );
		
		System.arraycopy( Bitmap, 0, rawBytes, pos, Bitmap.length );
		pos += Bitmap.length;
		
		return rawBytes;
	}
	
	public long getFullBlockLength( int blockSize )
	{
		long length = 24 + Bitmap.length;		
		
		if( length % blockSize != 0 )
		{
			length += blockSize - length % blockSize; 
		}
		
		return length;
	}
	
}
