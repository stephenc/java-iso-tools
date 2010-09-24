/*
 *	VolumeRecognitionSequence.java
 *
 *	2006-06-01
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.*;

public class VolumeRecognitionSequence
{
	public enum NSRVersion
	{
		NSR02,
		NSR03
	}
	
	private NSRVersion nsrVersion;
	
	public VolumeRecognitionSequence( NSRVersion nsrVersion )
	{
		this.nsrVersion = nsrVersion;
	}
	
	public void write( RandomAccessFile myRandomAccessFile )
	throws IOException
	{
		VolumeStructureDescriptor beginningExtendedAreaDescriptor = new VolumeStructureDescriptor();		
		beginningExtendedAreaDescriptor.StructureType = 0;
		beginningExtendedAreaDescriptor.StandardIdentifier = new byte[]{ 'B','E','A','0','1' };
		beginningExtendedAreaDescriptor.StructureVersion = 1;
		beginningExtendedAreaDescriptor.write( myRandomAccessFile );
				
		VolumeStructureDescriptor NSRDescriptor = new VolumeStructureDescriptor();
		NSRDescriptor.StructureType = 0;
		NSRDescriptor.StructureVersion = 1;
		
		if( nsrVersion == NSRVersion.NSR02 )
		{
			NSRDescriptor.StandardIdentifier = new byte[]{ 'N','S','R','0','2' };
		}
		else if( nsrVersion == NSRVersion.NSR03 )
		{
			NSRDescriptor.StandardIdentifier = new byte[]{ 'N','S','R','0','3' };
		}		
		
		NSRDescriptor.write( myRandomAccessFile );
				
		VolumeStructureDescriptor terminatingExtendedAreaDescriptor = new VolumeStructureDescriptor();
		terminatingExtendedAreaDescriptor.StructureType = 0;
		terminatingExtendedAreaDescriptor.StandardIdentifier = new byte[]{ 'T','E','A','0','1' };
		terminatingExtendedAreaDescriptor.StructureVersion = 1;
		terminatingExtendedAreaDescriptor.write( myRandomAccessFile );
	}
	
	
	public byte[] getBytes()
	{
		VolumeStructureDescriptor beginningExtendedAreaDescriptor = new VolumeStructureDescriptor();		
		beginningExtendedAreaDescriptor.StructureType = 0;
		beginningExtendedAreaDescriptor.StandardIdentifier = new byte[]{ 'B','E','A','0','1' };
		beginningExtendedAreaDescriptor.StructureVersion = 1;
		
		byte[] beginningExtendedAreaDescriptorBytes = beginningExtendedAreaDescriptor.getBytes();

				
		VolumeStructureDescriptor NSRDescriptor = new VolumeStructureDescriptor();
		NSRDescriptor.StructureType = 0;
		NSRDescriptor.StructureVersion = 1;
		
		if( nsrVersion == NSRVersion.NSR02 )
		{
			NSRDescriptor.StandardIdentifier = new byte[]{ 'N','S','R','0','2' };
		}
		else if( nsrVersion == NSRVersion.NSR03 )
		{
			NSRDescriptor.StandardIdentifier = new byte[]{ 'N','S','R','0','3' };
		}
		
		byte[] NSRDescriptorBytes = NSRDescriptor.getBytes();
		
		
		VolumeStructureDescriptor terminatingExtendedAreaDescriptor = new VolumeStructureDescriptor();
		terminatingExtendedAreaDescriptor.StructureType = 0;
		terminatingExtendedAreaDescriptor.StandardIdentifier = new byte[]{ 'T','E','A','0','1' };
		terminatingExtendedAreaDescriptor.StructureVersion = 1;
		
		byte[] terminatingExtendedAreaDescriptorBytes = terminatingExtendedAreaDescriptor.getBytes();

		byte[] rawBytes = new byte[ beginningExtendedAreaDescriptorBytes.length 
		                            + NSRDescriptorBytes.length 
		                            + terminatingExtendedAreaDescriptorBytes.length ];
		
		int pos = 0;
		
		System.arraycopy( beginningExtendedAreaDescriptorBytes, 0, rawBytes, pos, beginningExtendedAreaDescriptorBytes.length );
		pos += beginningExtendedAreaDescriptorBytes.length;

		System.arraycopy( NSRDescriptorBytes, 0, rawBytes, pos, NSRDescriptorBytes.length );
		pos += NSRDescriptorBytes.length;

		System.arraycopy( terminatingExtendedAreaDescriptorBytes, 0, rawBytes, pos, terminatingExtendedAreaDescriptorBytes.length );
		pos += terminatingExtendedAreaDescriptorBytes.length;

		return rawBytes;
	}
	
}
