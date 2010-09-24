/*
 *	FileEntry.java
 *
 *	2006-06-11
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.*;


public class FileEntry
{
	public Tag			DescriptorTag;					// struct tag
	public IcbTag		ICBTag;							// struct icbtag
	public long			Uid;							// Uint32
	public long			Gid;							// Uint32
	public long			Permissions;					// Uint32
	public int			FileLinkCount;					// Uint16
	public short		RecordFormat;					// Uint8
	public short		RecordDisplayAttributes;		// Uint8
	public long			RecordLength;					// Uint32
	public long			InformationLength;				// Uint64 !
	public long			LogicalBlocksRecorded;			// Uint64 !
	public Timestamp	AccessTime;						// struct timestamp
	public Timestamp	ModificationTime;				// struct timestamp
	public Timestamp	AttributeTime;					// struct timestamp
	public long			Checkpoint;						// Uint32
	public Long_ad		ExtendedAttributeICB;			// struct long_ad
	public EntityID		ImplementationIdentifier;		// struct EntityID
	public long			UniqueID;						// Uint64 !
	public long			LengthofExtendedAttributes;		// Uint32
	public long			LengthofAllocationDescriptors;	// Uint32
	public byte			ExtendedAttributes[];			// byte[]
	public byte			AllocationDescriptors[];		// byte[]
	
	public static int fixedPartLength = 176;
	
	public FileEntry()
	{
		DescriptorTag = new Tag();
		DescriptorTag.TagIdentifier = 261;		
	
		ICBTag = new IcbTag();
		AccessTime = new Timestamp();
		ModificationTime = new Timestamp();
		AttributeTime = new Timestamp();
		
		ExtendedAttributeICB = new Long_ad();
		ImplementationIdentifier = new EntityID();
		
		ExtendedAttributes = new byte[0];
		AllocationDescriptors = new byte[0];		
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
		LogicalBlocksRecorded = BinaryTools.readUInt64AsLong( myRandomAccessFile );		
	
		AccessTime = new Timestamp();
		AccessTime.read( myRandomAccessFile );
		
		ModificationTime = new Timestamp();
		ModificationTime.read( myRandomAccessFile );
		
		AttributeTime = new Timestamp();
		AttributeTime.read( myRandomAccessFile );
		
		Checkpoint = BinaryTools.readUInt32AsLong( myRandomAccessFile );
		
		ExtendedAttributeICB = new Long_ad();
		ExtendedAttributeICB.read( myRandomAccessFile );
		
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

	public byte[] getBytesWithoutDescriptorTag()
	{
		byte ICBTagBytes[] = ICBTag.getBytes();
		byte AccessTimeBytes[] = AccessTime.getBytes();
		byte ModificationTimeBytes[] = ModificationTime.getBytes();
		byte AttributeTimeBytes[] = AttributeTime.getBytes();
		byte ExtendedAttributeICBBytes[] = ExtendedAttributeICB.getBytes();
		byte ImplementationIdentifierBytes[] = ImplementationIdentifier.getBytes();
		
		byte rawBytes[] = new byte[ 56 
		                            + ICBTagBytes.length
		                            + AccessTimeBytes.length
		                            + ModificationTimeBytes.length
		                            + AttributeTimeBytes.length
		                            + ExtendedAttributeICBBytes.length
		                            + ImplementationIdentifierBytes.length 
		                            + ExtendedAttributes.length
		                            + AllocationDescriptors.length ];
		
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
		pos = BinaryTools.getUInt64BytesFromLong( LogicalBlocksRecorded, rawBytes, pos );
		
		System.arraycopy( AccessTimeBytes, 0, rawBytes, pos, AccessTimeBytes.length );
		pos += AccessTimeBytes.length;
		
		System.arraycopy( ModificationTimeBytes, 0, rawBytes, pos, ModificationTimeBytes.length );
		pos += ModificationTimeBytes.length;

		System.arraycopy( AttributeTimeBytes, 0, rawBytes, pos, AttributeTimeBytes.length );
		pos += AttributeTimeBytes.length;

		pos = BinaryTools.getUInt32BytesFromLong( Checkpoint, rawBytes, pos );		
		
		System.arraycopy( ExtendedAttributeICBBytes, 0, rawBytes, pos, ExtendedAttributeICBBytes.length );
		pos += ExtendedAttributeICBBytes.length;
		
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
