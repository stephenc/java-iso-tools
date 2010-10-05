/*
 *	LogicalVolumeHeaderDescriptor.java
 *
 *	2006-06-15
 *
 *	Bj�rn Stickler <bjoern@stickler.de>
 */

package com.github.stephenc.javaisotools.udflib.structures;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.github.stephenc.javaisotools.udflib.tools.BinaryTools;

public class LogicalVolumeHeaderDescriptor {

    public long UniqueID;        // Uint64 !
    public byte Reserved[];        // byte[24]

    public LogicalVolumeHeaderDescriptor() {
        Reserved = new byte[24];
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        UniqueID = BinaryTools.readUInt64AsLong(myRandomAccessFile);

        Reserved = new byte[24];
        myRandomAccessFile.read(Reserved);
    }

    public void write(RandomAccessFile myRandomAccessFile)
            throws IOException {
        byte[] rawBytes = getBytes();
        myRandomAccessFile.write(rawBytes);
    }

    public byte[] getBytes() {
        byte[] rawBytes = new byte[8 + Reserved.length];

        int pos = 0;

        pos = BinaryTools.getUInt64BytesFromLong(UniqueID, rawBytes, pos);

        System.arraycopy(Reserved, 0, rawBytes, pos, Reserved.length);
        pos += Reserved.length;

        return rawBytes;
    }
}
