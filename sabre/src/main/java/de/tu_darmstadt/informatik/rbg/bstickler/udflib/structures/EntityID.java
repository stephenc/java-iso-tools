/*
 *	EntityID.java
 *
 *	2006-06-01
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;


public class EntityID
{
	public byte Flags;					// Uint8
	public byte	Identifier[];			// char[23]
	public byte	IdentifierSuffix[];		// char[8]
	
	public EntityID()
	{
		Identifier = new byte[23];
		IdentifierSuffix = new byte[8];
	}
	
	public void setIdentifier( String identifier )
	throws Exception
	{
		if( identifier.length() > 23 )
		{
			throw new Exception( "error: identifier length exceeds maximum length of 23 characters" );
		}
		
		Identifier = new byte[23];
		
		for( int i = 0; i < identifier.length() && i < 23; ++i )
		{
			Identifier[ i ] = (byte)identifier.charAt( i ); 
		}
	}
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		Flags = myRandomAccessFile.readByte();
		
		Identifier = new byte[23];
		myRandomAccessFile.read( Identifier );
		
		IdentifierSuffix = new byte[8];
		myRandomAccessFile.read( IdentifierSuffix );
	}

	public void write( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		myRandomAccessFile.write( getBytes() );
	}
	
	public byte[] getBytes()
	{
		byte rawBytes[] = new byte[32];
		
		rawBytes[0] = Flags;
		System.arraycopy( Identifier, 0, rawBytes, 1, Identifier.length);
		System.arraycopy( IdentifierSuffix, 0, rawBytes, Identifier.length + 1, IdentifierSuffix.length);
		
		return rawBytes;
	}
	
}
