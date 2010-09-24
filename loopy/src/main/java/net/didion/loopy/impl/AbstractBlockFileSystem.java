/*
 * Copyright (c) 2004-2006, Loudeye Corp. All Rights Reserved.
 * Last changed by: $Author: jdidion $
 * Last changed at: $DateTime$
 * Revision: $Revision: 1.1.1.1 $
 */

package net.didion.loopy.impl;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import net.didion.loopy.FileEntry;
import net.didion.loopy.LoopyException;

public abstract class AbstractBlockFileSystem extends AbstractFileSystem {

    private int blockSize;
    private int reservedBytes;
    private VolumeDescriptor volumeDescriptor;

    protected AbstractBlockFileSystem(
            File file, boolean readOnly, int blockSize, int reservedBytes)
            throws LoopyException {
        super(file, readOnly);
        this.blockSize = blockSize;
        this.reservedBytes = reservedBytes;
    }

    public Enumeration getEntries() {
        ensureOpen();
        // read the metadata if necessary
        if (null == this.volumeDescriptor) {
            try {
                readDescriptor();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return enumerate(this.volumeDescriptor.getRootEntry());
    }

    protected void readDescriptor() throws IOException {
        byte[] buffer = new byte[this.blockSize];
        // skip the reserved blocks
        int block = this.reservedBytes / this.blockSize;
        this.volumeDescriptor = createVolumeDescriptor();
        while (readBlock(block++, buffer) && volumeDescriptor.read(buffer)) {
            ;
        }
    }

    protected boolean readBlock(long block, byte[] buffer) throws IOException {
        int bytesRead = readData(block * this.blockSize, buffer, 0, this.blockSize);
        if (bytesRead <= 0) {
            return false;
        }
        if (this.blockSize != bytesRead) {
            throw new IOException(
                    "Could not read a complete block (" + this.blockSize + " bytes)");
        }
        return true;
    }

    protected synchronized int readData(
            long startPos, byte[] buffer, int bufferOffset, int len)
            throws IOException {
        seek(startPos);
        return read(buffer, bufferOffset, len);
    }

    protected abstract Enumeration enumerate(FileEntry root);

    protected abstract VolumeDescriptor createVolumeDescriptor();
}