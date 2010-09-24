package net.didion.loopy.udf;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;

import net.didion.loopy.FileEntry;
import net.didion.loopy.LoopyException;
import net.didion.loopy.impl.AbstractBlockFileSystem;
import net.didion.loopy.impl.VolumeDescriptor;

public class UDFFileSystem extends AbstractBlockFileSystem implements Constants {

    public UDFFileSystem(File file, boolean readOnly) throws LoopyException {
        this(file, readOnly, DEFAULT_BLOCK_SIZE);
    }

    public UDFFileSystem(File file, boolean readOnly, int blockSize)
            throws LoopyException {
        super(file, readOnly, blockSize, RESERVED_BYTES);
    }

    public InputStream getInputStream(FileEntry entry) {
        return null;
    }

    protected Enumeration enumerate(FileEntry root) {
        return null;
    }

    protected VolumeDescriptor createVolumeDescriptor() {
        return new UDFVolumeDescriptor(this);
    }
}