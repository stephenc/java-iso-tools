/*
 *	ReservedArea.java
 *
 *	2006-06-01
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

public class ReservedArea
{
	public static boolean check( RandomAccessFile myRandomAccessFile )
	throws IOException
	{		
		byte buffer[] = new byte[16*2048];
		
		myRandomAccessFile.read( buffer );
		
		for( int i = 0; i < buffer.length; ++i )
		{
			if( buffer[ i ] != 0x00 )
			{
				return false;			
			}
		}
		
		return true;
	}
	
	public static void write( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		byte buffer[] = new byte[16*2048];
		
		myRandomAccessFile.write( buffer );
	}
}
