/*
 *	FileIdentifierDescriptor.java
 *
 *	2006-06-03
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class FileIdentifierDescriptor
{
	public	Tag			DescriptorTag;					// struct tag
	public 	int			FileVersionNumber;				// Uint16
	public 	short		FileCharacteristics;			// Uint8
	public 	short		LengthofFileIdentifier;			// Uint8
	public 	Long_ad		ICB;							// struct long_ad
	public 	int			LengthofImplementationUse;		// Uint16
	public 	byte		ImplementationUse[];			// byte[]
	public 	byte		FileIdentifier[];				// char[]
	private byte		Padding[];						// byte[]
	
	public FileIdentifierDescriptor()
	{
		DescriptorTag = new Tag();
		DescriptorTag.TagIdentifier = 257;		
		
		FileVersionNumber = 1;
		
		ICB = new Long_ad();
		
		ImplementationUse = new byte[0];
		FileIdentifier = new byte[0];
		Padding = new byte[0];
	}
	
	public int read( byte rawBytes[], int startPosition )
	{
		int position = startPosition;
		
		DescriptorTag = new Tag();
		position = DescriptorTag.read( rawBytes, position );
		
		FileVersionNumber = (rawBytes[position++] & 0xFF) + (rawBytes[position++] & 0xFF) * 256;
		FileCharacteristics = (short)(rawBytes[position++] & 0xFF);
		LengthofFileIdentifier = (short)(rawBytes[position++] & 0xFF);
		
		ICB = new Long_ad();
		position = ICB.read( rawBytes, position );
		
		LengthofImplementationUse = (rawBytes[position++] & 0xFF) + (rawBytes[position++] & 0xFF) * 256;
		
		ImplementationUse = new byte[LengthofImplementationUse];
		System.arraycopy( rawBytes, position, ImplementationUse, 0, ImplementationUse.length );
		position += ImplementationUse.length;
		
		FileIdentifier = new byte[LengthofFileIdentifier];
		System.arraycopy( rawBytes, position, FileIdentifier, 0, FileIdentifier.length );
		position += FileIdentifier.length;
		
		Padding = new byte[ 4 - ((position - startPosition) % 4) ];
		System.arraycopy( rawBytes, position, Padding, 0, Padding.length );
		position += Padding.length;
		
		return position;	
	}
	
	public byte[] getBytes()
	{
		byte rawBytesWithoutDescriptorTag[] = getBytesWithoutDescriptorTag();
		
		DescriptorTag.DescriptorCRCLength = rawBytesWithoutDescriptorTag.length;
		DescriptorTag.DescriptorCRC = Checksum.cksum( rawBytesWithoutDescriptorTag );
		
		byte descriptorTagBytes[] = DescriptorTag.getBytes();
	
		byte rawBytes[] = new byte[ 16 + rawBytesWithoutDescriptorTag.length ];
		
		System.arraycopy( descriptorTagBytes, 0, rawBytes, 0, descriptorTagBytes.length );
		System.arraycopy( rawBytesWithoutDescriptorTag, 0, rawBytes, descriptorTagBytes.length, rawBytesWithoutDescriptorTag.length );
		
		return rawBytes;
	}
	
	public byte[] getBytesWithoutDescriptorTag()
	{
		byte ICBBytes[] = ICB.getBytes();
		
		int lengthWithoutPadding = 6 + ICBBytes.length + ImplementationUse.length + FileIdentifier.length;		
		
		Padding = ( lengthWithoutPadding % 4 != 0 ) ? new byte[ 4 - (lengthWithoutPadding % 4) ] : new byte[0];
				
		byte rawBytes[] = new byte[ lengthWithoutPadding + Padding.length ];
		
		int pos = 0;
		
		pos = BinaryTools.getUInt16BytesFromInt( FileVersionNumber, rawBytes, pos );
		
		rawBytes[pos++] = (byte)(FileCharacteristics & 0xFF);
		rawBytes[pos++] = (byte)(LengthofFileIdentifier & 0xFF);
		
		System.arraycopy( ICBBytes, 0, rawBytes, pos, ICBBytes.length );
		pos += ICBBytes.length;
		
		rawBytes[pos++] = (byte)(LengthofImplementationUse & 0xFF);
		rawBytes[pos++] = (byte)((LengthofImplementationUse >> 8) & 0xFF);

		System.arraycopy( ImplementationUse, 0, rawBytes, pos, ImplementationUse.length );
		pos += ImplementationUse.length;

		System.arraycopy( FileIdentifier, 0, rawBytes, pos, FileIdentifier.length );
		pos += FileIdentifier.length;
		
		System.arraycopy( Padding, 0, rawBytes, pos, Padding.length );
		pos += Padding.length;

		return rawBytes;
	}
	
	public int getLength()
	{		
		int lengthWithoutPadding = 38 + ImplementationUse.length + FileIdentifier.length;		
		int paddingLength = ( lengthWithoutPadding % 4 != 0 ) ? 4 - (lengthWithoutPadding % 4) : 0;
		return lengthWithoutPadding + paddingLength;
	}
	
	public void setFileIdentifier( String fileIdentifier )
	throws Exception
	{
		if( fileIdentifier.length() > 255 )
		{
			throw new Exception( "FileIdentifier length > 255 characters not allowed" );
		}
		try
		{
			byte fileIdentiferBytes[] = fileIdentifier.getBytes( "UTF-16" );
			
			int compId = OSTAUnicode.getBestCompressionId( fileIdentiferBytes );
			
			FileIdentifier = OSTAUnicode.CompressUnicodeByte( fileIdentiferBytes, compId );
		}
		catch( UnsupportedEncodingException uee ) {} // never happens
		
		LengthofFileIdentifier = (short)FileIdentifier.length;
	}
	
}
