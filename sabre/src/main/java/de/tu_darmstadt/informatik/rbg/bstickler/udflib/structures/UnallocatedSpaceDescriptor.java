/*
 *	UnallocatedSpaceDescriptor.java
 *
 *	2006-06-02
 *
 *	Bj�rn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures;

import java.io.IOException;
import java.io.RandomAccessFile;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.BinaryTools;

public class UnallocatedSpaceDescriptor extends VolumeDescriptorSequenceItem {

    public long NumberofAllocationDescriptors;                // Uint32
    public Extend_ad AllocationDescriptors[];                    // struct extend_ad[]

    public UnallocatedSpaceDescriptor() {
        DescriptorTag = new Tag();
        DescriptorTag.TagIdentifier = 7;

        AllocationDescriptors = new Extend_ad[0];
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        DescriptorTag = new Tag();
        DescriptorTag.read(myRandomAccessFile);

        VolumeDescriptorSequenceNumber = BinaryTools.readUInt32AsLong(myRandomAccessFile);
        NumberofAllocationDescriptors = BinaryTools.readUInt32AsLong(myRandomAccessFile);

        AllocationDescriptors = new Extend_ad[(int) NumberofAllocationDescriptors];

        for (int i = 0; i < NumberofAllocationDescriptors; ++i) {
            AllocationDescriptors[i] = new Extend_ad();
            AllocationDescriptors[i].read(myRandomAccessFile);
        }
    }

    public byte[] getBytesWithoutDescriptorTag() {
        byte rawBytes[] = new byte[8 + (int) NumberofAllocationDescriptors * 8];

        int pos = 0;

        pos = BinaryTools.getUInt32BytesFromLong(VolumeDescriptorSequenceNumber, rawBytes, pos);
        pos = BinaryTools.getUInt32BytesFromLong(NumberofAllocationDescriptors, rawBytes, pos);

        for (int i = 0; i < AllocationDescriptors.length; ++i) {
            byte allocationDescriptorBytes[] = AllocationDescriptors[i].getBytes();
            System.arraycopy(allocationDescriptorBytes, 0, rawBytes, pos, allocationDescriptorBytes.length);
            pos += allocationDescriptorBytes.length;
        }

        return rawBytes;
    }
}
