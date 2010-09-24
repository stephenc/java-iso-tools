/*
 *	PartitionDescriptor.java
 *
 *	2006-06-02
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class PartitionDescriptor extends VolumeDescriptorSequenceItem
{
	public int							PartitionFlags;						// Uint16
	public int 							PartitionNumber;					// Uint16
	public EntityID						PartitionContents;					// struct EntityID
	
	//public byte						PartitionContentsUse[];				// byte[128] 		== PartitionHeaderDescriptor
	public PartitionHeaderDescriptor	PartitionContentsUse;
	
	public long							AccessType;							// Uint32
	public long							PartitonStartingLocation;			// Uint32
	public long							PartitionLength;					// Uint32
	public EntityID						ImplementationIdentifier;			// struct EntityID
	public byte							ImplementationUse[];				// byte[128]
	public byte							Reserved[];							// byte[156]
	
	
	public PartitionDescriptor()
	{
		DescriptorTag = new Tag();
		DescriptorTag.TagIdentifier = 5;
		
		PartitionContents = new EntityID();
		
		//PartitionContentsUse = new byte[128];
		PartitionContentsUse = new PartitionHeaderDescriptor();
		
		ImplementationIdentifier = new EntityID();
		ImplementationUse = new byte[128];
		Reserved = new byte[156];
	}
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		DescriptorTag = new Tag();
		DescriptorTag.read( myRandomAccessFile );
		
		VolumeDescriptorSequenceNumber = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		PartitionFlags = BinaryTools.readUInt16AsInt( myRandomAccessFile );
		PartitionNumber = BinaryTools.readUInt16AsInt( myRandomAccessFile );
		
		PartitionContents = new EntityID();
		PartitionContents.read( myRandomAccessFile );
		
		//PartitionContentsUse = new byte[128];
		//myRandomAccessFile.read( PartitionContentsUse );
		PartitionContentsUse = new PartitionHeaderDescriptor();
		PartitionContentsUse.read( myRandomAccessFile );
		
		AccessType = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		PartitonStartingLocation = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		PartitionLength = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		ImplementationIdentifier = new EntityID();
		ImplementationIdentifier.read( myRandomAccessFile );
		
		ImplementationUse = new byte[128];
		myRandomAccessFile.read( ImplementationUse );
		
		Reserved = new byte[156];
		myRandomAccessFile.read( Reserved );
	}
	
	public byte[] getBytesWithoutDescriptorTag()
	{
		byte PartitionContentsBytes[] = PartitionContents.getBytes();
		byte ImplementationIdentifierBytes[] = ImplementationIdentifier.getBytes();
		byte PartitionContentsUseBytes[] = PartitionContentsUse.getBytes();
		
		byte rawBytes[] = new byte[ 304 
		                            + PartitionContentsBytes.length 
		                            + ImplementationIdentifierBytes.length
		                            + PartitionContentsUseBytes.length ];
		
		int pos = 0;
		
		pos = BinaryTools.getUInt32BytesFromLong( VolumeDescriptorSequenceNumber, rawBytes, pos );
		pos = BinaryTools.getUInt16BytesFromInt( PartitionFlags, rawBytes, pos );		
		pos = BinaryTools.getUInt16BytesFromInt( PartitionNumber, rawBytes, pos );
		
		System.arraycopy( PartitionContentsBytes, 0, rawBytes, pos, PartitionContentsBytes.length );
		pos += PartitionContentsBytes.length;

		//System.arraycopy( PartitionContentsUse, 0, rawBytes, pos, PartitionContentsUse.length );
		//pos += PartitionContentsUse.length;
		System.arraycopy( PartitionContentsUseBytes, 0, rawBytes, pos, PartitionContentsUseBytes.length );
		pos += PartitionContentsUseBytes.length;
		
		pos = BinaryTools.getUInt32BytesFromLong( AccessType, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( PartitonStartingLocation, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( PartitionLength, rawBytes, pos );
		
		System.arraycopy( ImplementationIdentifierBytes, 0, rawBytes, pos, ImplementationIdentifierBytes.length );
		pos += ImplementationIdentifierBytes.length;

		System.arraycopy( ImplementationUse, 0, rawBytes, pos, ImplementationUse.length );
		pos += ImplementationUse.length;
		
		System.arraycopy( Reserved, 0, rawBytes, pos, Reserved.length );
		pos += Reserved.length;
		
		return rawBytes;
	}
	
}
