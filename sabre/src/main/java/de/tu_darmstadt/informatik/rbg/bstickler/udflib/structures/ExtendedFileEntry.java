/*
 *	ExtendedFileEntry.java
 *
 *	2006-06-11
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class ExtendedFileEntry extends FileEntry
{
	public long			ObjectSize;						// Uint64 !	
	public Timestamp	CreationTime;					// struct timestamp
    byte 				Reserved[];						// byte[4]	   
	public Long_ad		StreamDirectoryICB;				// struct long_ad

	public static int fixedPartLength = 224;
	
	public ExtendedFileEntry()
	{		
		super();
		
		DescriptorTag.TagIdentifier = 266;		
		
		CreationTime = new Timestamp();
		Reserved = new byte[4];
		StreamDirectoryICB = new Long_ad();
	}	
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		DescriptorTag = new Tag();
		DescriptorTag.read( myRandomAccessFile );

		ICBTag = new IcbTag();
		ICBTag.read( myRandomAccessFile );
		
		Uid = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		Gid = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		Permissions = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		FileLinkCount = BinaryTools.readUInt16AsInt( myRandomAccessFile );
		RecordFormat = (short)myRandomAccessFile.readUnsignedByte();
		RecordDisplayAttributes = (short)myRandomAccessFile.readUnsignedByte();
		RecordLength = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		InformationLength = BinaryTools.readUInt64AsLong( myRandomAccessFile );
	
		ObjectSize = BinaryTools.readUInt64AsLong( myRandomAccessFile );
		
		LogicalBlocksRecorded = BinaryTools.readUInt64AsLong( myRandomAccessFile );		
	
		AccessTime = new Timestamp();
		AccessTime.read( myRandomAccessFile );
		
		ModificationTime = new Timestamp();
		ModificationTime.read( myRandomAccessFile );
		
		CreationTime = new Timestamp();
		CreationTime.read( myRandomAccessFile );		

		AttributeTime = new Timestamp();
		AttributeTime.read( myRandomAccessFile );
		
		Checkpoint = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		Reserved = new byte[4];
		myRandomAccessFile.read( Reserved );
		
		ExtendedAttributeICB = new Long_ad();
		ExtendedAttributeICB.read( myRandomAccessFile );
		
		StreamDirectoryICB = new Long_ad();
		StreamDirectoryICB.read( myRandomAccessFile );		
		
		ImplementationIdentifier = new EntityID();
		ImplementationIdentifier.read( myRandomAccessFile );
				
		UniqueID = BinaryTools.readUInt64AsLong( myRandomAccessFile );	
		
		LengthofExtendedAttributes = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		LengthofAllocationDescriptors = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		ExtendedAttributes = new byte[(int)LengthofExtendedAttributes];
		myRandomAccessFile.read( ExtendedAttributes );
		
		AllocationDescriptors = new byte[(int)LengthofAllocationDescriptors];
		myRandomAccessFile.read( AllocationDescriptors );
	}
	
	public byte[] getBytesWithoutDescriptorTag()
	{
		byte ICBTagBytes[] = ICBTag.getBytes();
		byte AccessTimeBytes[] = AccessTime.getBytes();
		byte ModificationTimeBytes[] = ModificationTime.getBytes();
		byte AttributeTimeBytes[] = AttributeTime.getBytes();
		byte ExtendedAttributeICBBytes[] = ExtendedAttributeICB.getBytes();
		byte ImplementationIdentifierBytes[] = ImplementationIdentifier.getBytes();
		
		byte CreationTimeBytes[] = CreationTime.getBytes();
		byte StreamDirectoryICBBytes[] = StreamDirectoryICB.getBytes();
		
		byte rawBytes[] = new byte[ 68
		                            + ICBTagBytes.length
		                            + AccessTimeBytes.length
		                            + ModificationTimeBytes.length
		                            + AttributeTimeBytes.length
		                            + ExtendedAttributeICBBytes.length
		                            + ImplementationIdentifierBytes.length 
		                            + ExtendedAttributes.length
		                            + AllocationDescriptors.length
		                            + CreationTimeBytes.length
		                            + StreamDirectoryICBBytes.length ];
		
		int pos = 0;
		
		System.arraycopy( ICBTagBytes, 0, rawBytes, pos, ICBTagBytes.length );
		pos += ICBTagBytes.length;

		pos = BinaryTools.getUInt32BytesFromLong( Uid, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( Gid, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( Permissions, rawBytes, pos );
		pos = BinaryTools.getUInt16BytesFromInt( FileLinkCount, rawBytes, pos );
		
		rawBytes[pos++] = (byte)(RecordFormat & 0xFF);
		rawBytes[pos++] = (byte)(RecordDisplayAttributes & 0xFF);
		
		pos = BinaryTools.getUInt32BytesFromLong( RecordLength, rawBytes, pos );
		pos = BinaryTools.getUInt64BytesFromLong( InformationLength, rawBytes, pos );
		pos = BinaryTools.getUInt64BytesFromLong( ObjectSize, rawBytes, pos );		
		pos = BinaryTools.getUInt64BytesFromLong( LogicalBlocksRecorded, rawBytes, pos );		
		
		System.arraycopy( AccessTimeBytes, 0, rawBytes, pos, AccessTimeBytes.length );
		pos += AccessTimeBytes.length;
		
		System.arraycopy( ModificationTimeBytes, 0, rawBytes, pos, ModificationTimeBytes.length );
		pos += ModificationTimeBytes.length;

		System.arraycopy( CreationTimeBytes, 0, rawBytes, pos, CreationTimeBytes.length );
		pos += CreationTimeBytes.length;

		System.arraycopy( AttributeTimeBytes, 0, rawBytes, pos, AttributeTimeBytes.length );
		pos += AttributeTimeBytes.length;

		pos = BinaryTools.getUInt32BytesFromLong( Checkpoint, rawBytes, pos );

		System.arraycopy( Reserved, 0, rawBytes, pos, Reserved.length );
		pos += Reserved.length;

		System.arraycopy( ExtendedAttributeICBBytes, 0, rawBytes, pos, ExtendedAttributeICBBytes.length );
		pos += ExtendedAttributeICBBytes.length;

		System.arraycopy( StreamDirectoryICBBytes, 0, rawBytes, pos, StreamDirectoryICBBytes.length );
		pos += StreamDirectoryICBBytes.length;
				
		System.arraycopy( ImplementationIdentifierBytes, 0, rawBytes, pos, ImplementationIdentifierBytes.length );
		pos += ImplementationIdentifierBytes.length;

		pos = BinaryTools.getUInt64BytesFromLong( UniqueID, rawBytes, pos );		
		pos = BinaryTools.getUInt32BytesFromLong( LengthofExtendedAttributes, rawBytes, pos );
		pos = BinaryTools.getUInt32BytesFromLong( LengthofAllocationDescriptors, rawBytes, pos );		
		
		System.arraycopy( ExtendedAttributes, 0, rawBytes, pos, ExtendedAttributes.length );
		pos += ExtendedAttributes.length;
		
		System.arraycopy( AllocationDescriptors, 0, rawBytes, pos, AllocationDescriptors.length );
		pos += AllocationDescriptors.length;
	
		return rawBytes;
	}
	
}
