/*
 *	TerminatingDescriptor.java
 *
 *	2006-06-11
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class TerminatingDescriptor
{
	public Tag	DescriptorTag;		// struct tag
	public byte	Reserved[];			// byte[496]
	
	public TerminatingDescriptor()
	{
		DescriptorTag = new Tag();
		DescriptorTag.TagIdentifier = 8;
		
		Reserved = new byte[496];
	}
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		DescriptorTag = new Tag();
		DescriptorTag.read( myRandomAccessFile );
		
		Reserved = new byte[ 496 ];
		myRandomAccessFile.read( Reserved );
	}
	
	public void write( RandomAccessFile myRandomAccessFile, int blockSize )
	throws IOException
	{		
	    DescriptorTag.DescriptorCRCLength = Reserved.length;
	    DescriptorTag.DescriptorCRC = Checksum.cksum( Reserved );
		
		DescriptorTag.write( myRandomAccessFile );
			
		myRandomAccessFile.write( Reserved );

		int bytesWritten = Reserved.length + 16;

		byte emptyBytesInBlock[] = new byte[ blockSize - bytesWritten ];
		myRandomAccessFile.write( emptyBytesInBlock );				
	}
	
	public byte[] getBytes( int blockSize )
	{
	    DescriptorTag.DescriptorCRCLength = Reserved.length;
	    DescriptorTag.DescriptorCRC = Checksum.cksum( Reserved );
	    
	    byte descriptorTagBytes[] = DescriptorTag.getBytes();
	    
	    int paddedLength = descriptorTagBytes.length + Reserved.length;
	    if( paddedLength % blockSize != 0 )
	    {
	    	paddedLength += blockSize - (paddedLength % blockSize); 
	    }
	    
	    byte[] rawBytes = new byte[ paddedLength ];
	    
	    int pos = 0;
	    
	    System.arraycopy( descriptorTagBytes, 0, rawBytes, pos, descriptorTagBytes.length );
	    pos += descriptorTagBytes.length;
	    
	    System.arraycopy( Reserved, 0, rawBytes, pos, Reserved.length );
	    pos += Reserved.length;	    
		
	    return rawBytes;
	}
	
	public byte[] getBytesWithoutDescriptorTag()
	{
		return Reserved;
	}
}
