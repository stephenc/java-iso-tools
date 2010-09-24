/*
 *	CharSpec.java
 *
 *	2006-06-01
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;


public class CharSpec
{
	public byte	CharacterSetType;		// Uint8
	public byte	CharacterSetInfo[];		// byte[63]
	
	public CharSpec()
	{
		CharacterSetType = 0;
		CharacterSetInfo = new byte[63];
		
		CharacterSetInfo[0] = 'O';
		CharacterSetInfo[1] = 'S';
		CharacterSetInfo[2] = 'T';
		CharacterSetInfo[3] = 'A';
		CharacterSetInfo[4] = ' ';
		CharacterSetInfo[5] = 'C';
		CharacterSetInfo[6] = 'o';
		CharacterSetInfo[7] = 'm';
		CharacterSetInfo[8] = 'p';
		CharacterSetInfo[9] = 'r';
		CharacterSetInfo[10] = 'e';
		CharacterSetInfo[11] = 's';
		CharacterSetInfo[12] = 's';
		CharacterSetInfo[13] = 'e';
		CharacterSetInfo[14] = 'd';
		CharacterSetInfo[15] = ' ';
		CharacterSetInfo[16] = 'U';
		CharacterSetInfo[17] = 'n';
		CharacterSetInfo[18] = 'i';
		CharacterSetInfo[19] = 'c';
		CharacterSetInfo[20] = 'o';
		CharacterSetInfo[21] = 'd';
		CharacterSetInfo[22] = 'e';
	}
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		CharacterSetType = myRandomAccessFile.readByte();
		
		CharacterSetInfo = new byte[63];
		myRandomAccessFile.read( CharacterSetInfo );
	}
	
	public void write( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		myRandomAccessFile.write( getBytes() );		
	}
	
	public byte[] getBytes()
	{
		byte rawBytes[] = new byte[64];
		
		rawBytes[0] = CharacterSetType;
		System.arraycopy( CharacterSetInfo, 0, rawBytes, 1, CharacterSetInfo.length);
		
		return rawBytes;
	}	
}
