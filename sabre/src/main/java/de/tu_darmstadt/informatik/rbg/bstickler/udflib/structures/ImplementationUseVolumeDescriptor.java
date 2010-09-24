/*
 *	ImplementationUseVolumeDescriptor.java
 *
 *	2006-06-04
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class ImplementationUseVolumeDescriptor extends VolumeDescriptorSequenceItem
{
	public EntityID			ImplementationIdentifier;			// struct EntityID	
	//public byte			ImplementationUse[];				// byte[460]
	public LVInformation	ImplementationUse;
	
	public ImplementationUseVolumeDescriptor()
	{
		DescriptorTag = new Tag();
		DescriptorTag.TagIdentifier = 4;
		
		ImplementationIdentifier = new EntityID();

		//ImplementationUse = new byte[460];
		ImplementationUse = new LVInformation();		
	}
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		DescriptorTag = new Tag();
		DescriptorTag.read( myRandomAccessFile );
		
		VolumeDescriptorSequenceNumber = BinaryTools.readUInt32AsLong( myRandomAccessFile );

		ImplementationIdentifier = new EntityID();
		ImplementationIdentifier.read( myRandomAccessFile );
		
		//ImplementationUse = new byte[460];
		//myRandomAccessFile.read( ImplementationUse );
		
		ImplementationUse = new LVInformation();
		ImplementationUse.read( myRandomAccessFile );
	}	
		
	public byte[] getBytesWithoutDescriptorTag()
	{
		byte ImplementationIdentifierBytes[] = ImplementationIdentifier.getBytes();
		byte ImplementationUseBytes[] = ImplementationUse.getBytes(); 
		
		byte rawBytes[] = new byte[ 4
		                            + ImplementationIdentifierBytes.length
		                            + ImplementationUseBytes.length ];
		
		int pos = 0;
		
		pos = BinaryTools.getUInt32BytesFromLong( VolumeDescriptorSequenceNumber, rawBytes, pos );

		System.arraycopy( ImplementationIdentifierBytes, 0, rawBytes, pos, ImplementationIdentifierBytes.length );
		pos += ImplementationIdentifierBytes.length;
		
		//System.arraycopy( ImplementationUse, 0, rawBytes, pos, ImplementationUse.length );
		System.arraycopy( ImplementationUseBytes, 0, rawBytes, pos, ImplementationUseBytes.length );
		
		return rawBytes;
	}
}
