/*
 *	VolumeDescriptorSequenceItem.java
 *
 *	2006-06-30
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;



public abstract class VolumeDescriptorSequenceItem
{
	public Tag			DescriptorTag;								// struct tag
	public long			VolumeDescriptorSequenceNumber;				// Uint32
	
	public abstract byte[] getBytesWithoutDescriptorTag();
	
	public abstract void read( RandomAccessFile myRandomAccessFile )
	throws IOException;
	
	public void write( RandomAccessFile myRandomAccessFile, int blockSize )
	throws IOException
	{
		byte rawBytes[] = getBytesWithoutDescriptorTag();
	    
	    DescriptorTag.DescriptorCRCLength = rawBytes.length;
	    DescriptorTag.DescriptorCRC = Checksum.cksum( rawBytes );
		
		DescriptorTag.write( myRandomAccessFile );
			
		myRandomAccessFile.write( rawBytes );

		int bytesWritten = rawBytes.length + 16;

		byte emptyBytesInBlock[] = new byte[ blockSize - bytesWritten ];
		myRandomAccessFile.write( emptyBytesInBlock );		
	}	

	public byte[] getBytes( int blockSize )
	{
		byte bytesWithoutDescriptorTag[] = getBytesWithoutDescriptorTag();
	    
	    DescriptorTag.DescriptorCRCLength = bytesWithoutDescriptorTag.length;
	    DescriptorTag.DescriptorCRC = Checksum.cksum( bytesWithoutDescriptorTag );
	    
	    byte descriptorTagBytes[] = DescriptorTag.getBytes();
	    
	    int paddedLength = descriptorTagBytes.length + bytesWithoutDescriptorTag.length;
	    if( paddedLength % blockSize != 0 )
	    {
	    	paddedLength += blockSize - (paddedLength % blockSize); 
	    }
	    
	    byte[] rawBytes = new byte[ paddedLength ];
	    
	    int pos = 0;
	    
	    System.arraycopy( descriptorTagBytes, 0, rawBytes, pos, descriptorTagBytes.length );
	    pos += descriptorTagBytes.length;
	    
	    System.arraycopy( bytesWithoutDescriptorTag, 0, rawBytes, pos, bytesWithoutDescriptorTag.length );
	    pos += bytesWithoutDescriptorTag.length;	    
		
	    return rawBytes;
	}
}
