/*
 *	LVInformation.java
 *
 *	2006-06-04
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;

public class LVInformation
{
	public CharSpec	LVICharset;						// struct charspec
	public byte		LogicalVolumeIdentifier[];		// dstring[128]
	public byte		LVInfo1[];						// dstring[36]
	public byte		LVInfo2[];						// dstring[36]
	public byte		LVInfo3[];						// dstring[36]
	public EntityID	ImplementationID;				// struct EntityID
	public byte		ImplementationUse[];			// byte[128];
	
	
	public LVInformation()
	{
		LVICharset = new CharSpec();
		LogicalVolumeIdentifier = new byte[128];
		LVInfo1 = new byte[36];
		LVInfo2 = new byte[36];
		LVInfo3 = new byte[36];
		ImplementationID = new EntityID();
		ImplementationUse = new byte[128];
	}
	
	public void setLogicalVolumeIdentifier( String volumeIdentifier )
	throws Exception
	{
		if( volumeIdentifier.length() > 126 )
		{
			throw new Exception( "error: logical volume identifier length > 126 characters" );
		}
		
		LogicalVolumeIdentifier = new byte[128];
		
		try
		{
			byte volumeIdentifierBytes[] = volumeIdentifier.getBytes( "UTF-16" );
			
			int compId = OSTAUnicode.getBestCompressionId( volumeIdentifierBytes );
			
			byte tmpIdentifier[] = OSTAUnicode.CompressUnicodeByte( volumeIdentifierBytes, compId );

			int length = ( tmpIdentifier.length < 127 ) ? tmpIdentifier.length : 127; 
			
			System.arraycopy( tmpIdentifier, 0, LogicalVolumeIdentifier, 0, length );
			
			LogicalVolumeIdentifier[ LogicalVolumeIdentifier.length - 1 ] = (byte)length;
		}
		catch( Exception ex ) { /* never happens */ }		
	}

	public void setLVInfo1( String lvInfo )
	throws Exception
	{
		if( lvInfo.length() >  34 )
		{
			throw new Exception( "error: lvInfo length > 34 characters" );
		}
				
		LVInfo1 = new byte[36];
		
		try
		{
			byte lvInfoBytes[] = lvInfo.getBytes( "UTF-16" );
			
			int compId = OSTAUnicode.getBestCompressionId( lvInfoBytes );
			
			byte tmpIdentifier[] = OSTAUnicode.CompressUnicodeByte( lvInfoBytes, compId );
			
			int length = ( tmpIdentifier.length < 35 ) ? tmpIdentifier.length : 35;
			
			System.arraycopy( tmpIdentifier, 0, LVInfo1, 0, length );
			
			LogicalVolumeIdentifier[ LVInfo1.length - 1 ] = (byte)length;
		}
		catch( Exception ex ) { /* never happens */ }		
	}
	
	public void setLVInfo2( String lvInfo )
	throws Exception
	{
		if( lvInfo.length() >  34 )
		{
			throw new Exception( "error: lvInfo length > 34 characters" );
		}		
		
		LVInfo2 = new byte[36];
		
		try
		{
			byte lvInfoBytes[] = lvInfo.getBytes( "UTF-16" );
			
			int compId = OSTAUnicode.getBestCompressionId( lvInfoBytes );
			
			byte tmpIdentifier[] = OSTAUnicode.CompressUnicodeByte( lvInfoBytes, compId );

			int length = ( tmpIdentifier.length < 35 ) ? tmpIdentifier.length : 35;
			
			System.arraycopy( tmpIdentifier, 0, LVInfo2, 0, length );
			
			LogicalVolumeIdentifier[ LVInfo2.length - 1 ] = (byte)length;
		}
		catch( Exception ex ) { /* never happens */ }		
	}

	public void setLVInfo3( String lvInfo )
	throws Exception
	{
		if( lvInfo.length() >  34 )
		{
			throw new Exception( "error: lvInfo length > 34 characters" );
		}
		
		LVInfo3 = new byte[36];
		
		try
		{
			byte lvInfoBytes[] = lvInfo.getBytes( "UTF-16" );
			
			int compId = OSTAUnicode.getBestCompressionId( lvInfoBytes );
			
			byte tmpIdentifier[] = OSTAUnicode.CompressUnicodeByte( lvInfoBytes, compId );

			int length = ( tmpIdentifier.length < 35 ) ? tmpIdentifier.length : 35; 
			
			System.arraycopy( tmpIdentifier, 0, LVInfo3, 0, length );
			
			LogicalVolumeIdentifier[ LVInfo3.length - 1 ] = (byte)length;
		}
		catch( Exception ex ) { /* never happens */ }		
	}
	
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		LVICharset = new CharSpec();
		LVICharset.read( myRandomAccessFile );
		
		LogicalVolumeIdentifier = new byte[128];
		myRandomAccessFile.read( LogicalVolumeIdentifier );
		
		LVInfo1 = new byte[36];
		myRandomAccessFile.read( LVInfo1 );
		
		LVInfo2 = new byte[36];
		myRandomAccessFile.read( LVInfo2 );
		
		LVInfo3 = new byte[36];
		myRandomAccessFile.read( LVInfo3 );
		
		ImplementationID = new EntityID();
		ImplementationID.read( myRandomAccessFile );
		
		ImplementationUse = new byte[128];
		myRandomAccessFile.read( ImplementationUse );
	}	
	
	public byte[] getBytes()
	{
		byte LVICharsetBytes[] = LVICharset.getBytes();
		byte ImplementationIDBytes[] = ImplementationID.getBytes();
		
		byte rawBytes[] = new byte[ 364
		                      + LVICharsetBytes.length
		                      + ImplementationIDBytes.length ];
		
		int pos = 0;
		
		System.arraycopy( LVICharsetBytes, 0, rawBytes, pos, LVICharsetBytes.length );
		pos += LVICharsetBytes.length;

		System.arraycopy( LogicalVolumeIdentifier, 0, rawBytes, pos, LogicalVolumeIdentifier.length );
		pos += LogicalVolumeIdentifier.length;
		
		System.arraycopy( LVInfo1, 0, rawBytes, pos, LVInfo1.length );
		pos += LVInfo1.length;
		
		System.arraycopy( LVInfo2, 0, rawBytes, pos, LVInfo2.length );
		pos += LVInfo2.length;

		System.arraycopy( LVInfo3, 0, rawBytes, pos, LVInfo3.length );
		pos += LVInfo3.length;

		System.arraycopy( ImplementationIDBytes, 0, rawBytes, pos, ImplementationIDBytes.length );
		pos += ImplementationIDBytes.length;
		
		System.arraycopy( ImplementationUse, 0, rawBytes, pos, ImplementationUse.length );
		pos += ImplementationUse.length;
		
		return rawBytes;
	}
}
