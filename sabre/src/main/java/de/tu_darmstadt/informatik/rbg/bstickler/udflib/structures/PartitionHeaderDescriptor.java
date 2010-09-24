/*
 *	PartitionHeaderDescriptor.java
 *
 *	2006-06-17
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;


public class PartitionHeaderDescriptor
{
	public Short_ad	UnallocatedSpaceTable;		// struct short_ad
	public Short_ad	UnallocatedSpaceBitmap;		// struct short_ad
	public Short_ad	PartitionIntegrityTable;	// struct short_ad
	public Short_ad	FreedSpaceTable;			// struct short_ad
	public Short_ad	FreedSpaceBitmap;			// struct short_ad
	public byte		Reserved[];					// byte[88]

	public PartitionHeaderDescriptor()
	{
		UnallocatedSpaceTable = new Short_ad();
		UnallocatedSpaceBitmap = new Short_ad();
		PartitionIntegrityTable = new Short_ad();
		FreedSpaceTable = new Short_ad();
		FreedSpaceBitmap = new Short_ad();
		Reserved = new byte[88];
	}
	
	public void read( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		UnallocatedSpaceTable = new Short_ad();
		UnallocatedSpaceTable.read( myRandomAccessFile );
		
		UnallocatedSpaceBitmap = new Short_ad();
		UnallocatedSpaceBitmap.read( myRandomAccessFile );
		
		PartitionIntegrityTable = new Short_ad();
		PartitionIntegrityTable.read( myRandomAccessFile );
		
		FreedSpaceTable = new Short_ad();
		FreedSpaceTable.read( myRandomAccessFile );
		
		FreedSpaceBitmap = new Short_ad();
		FreedSpaceBitmap.read( myRandomAccessFile );
		
		Reserved = new byte[88];
		myRandomAccessFile.read( Reserved );
	}
	
	public byte[] getBytes()
	{
		byte UnallocatedSpaceTableBytes[] = UnallocatedSpaceTable.getBytes();
		byte UnallocatedSpaceBitmapBytes[] = UnallocatedSpaceBitmap.getBytes();
		byte PartitionIntegrityTableBytes[] = PartitionIntegrityTable.getBytes();
		byte FreedSpaceTableByte[] = FreedSpaceTable.getBytes();
		byte FreedSpaceBitmapByte[] = FreedSpaceBitmap.getBytes();
		
		byte rawBytes[] = new byte[ 88 
		                            + UnallocatedSpaceTableBytes.length
		                            + UnallocatedSpaceBitmapBytes.length
		                            + PartitionIntegrityTableBytes.length
		                            + FreedSpaceTableByte.length
		                            + FreedSpaceBitmapByte.length ];
		
		int pos = 0;
		
		System.arraycopy( UnallocatedSpaceTableBytes, 0, rawBytes, pos, UnallocatedSpaceTableBytes.length );
		pos += UnallocatedSpaceTableBytes.length;

		System.arraycopy( UnallocatedSpaceBitmapBytes, 0, rawBytes, pos, UnallocatedSpaceBitmapBytes.length );
		pos += UnallocatedSpaceBitmapBytes.length;

		System.arraycopy( PartitionIntegrityTableBytes, 0, rawBytes, pos, PartitionIntegrityTableBytes.length );
		pos += PartitionIntegrityTableBytes.length;
		
		System.arraycopy( FreedSpaceTableByte, 0, rawBytes, pos, FreedSpaceTableByte.length );
		pos += FreedSpaceTableByte.length;
		
		System.arraycopy( FreedSpaceBitmapByte, 0, rawBytes, pos, FreedSpaceBitmapByte.length );
		pos += FreedSpaceBitmapByte.length;

		System.arraycopy( Reserved, 0, rawBytes, pos, Reserved.length );
		pos += Reserved.length;
		
		return rawBytes;
	}
	
}
