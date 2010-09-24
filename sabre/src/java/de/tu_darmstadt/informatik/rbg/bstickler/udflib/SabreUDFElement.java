/*
 *	SabreUDFElement.java
 *
 *	2006-07-06
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;

public class SabreUDFElement extends Element
{
	private UDFElementType udfElementType;
	
	public enum UDFElementType
	{
		EmptyArea,
		ReservedArea,
		VolumeRecognitionSequence,
		AnchorVolumeDescriptorPointer,
		PrimaryVolumeDescriptor,
		LogicalVolumeDescriptor,
		PartitionDescriptor,
		ImplementationUseVolumeDescriptor,
		UnallocatedSpaceDescriptor,
		TerminatingDescriptor,
		FileSetDescriptor,		
		LogicalVolumeIntegrityDescriptor,
		FileEntry,
		RawFileData,
		MetadataFile,
		
		DescriptorTag		// not used on "frontend"
	}
	
	public SabreUDFElement( UDFElementType udfElementType )
	{
		this.udfElementType = udfElementType;
	}

	public Object getId()
	{
		return udfElementType;
	}

	

}
