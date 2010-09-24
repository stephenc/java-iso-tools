package net.didion.loopy.iso9660;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import net.didion.loopy.FileEntry;
import net.didion.loopy.LoopyException;
import net.didion.loopy.impl.AbstractBlockFileSystem;
import net.didion.loopy.impl.VolumeDescriptor;

public class ISO9660FileSystem extends AbstractBlockFileSystem implements Constants {

    public ISO9660FileSystem(File file, boolean readOnly) throws LoopyException {
        super(file, readOnly, BLOCK_SIZE, RESERVED_BYTES);
    }

    public InputStream getInputStream(FileEntry entry) {
        ensureOpen();
        return new EntryInputStream((ISO9660FileEntry) entry, this);
    }

    protected Enumeration enumerate(FileEntry rootEntry) {
        return new EntryEnumeration(this, (ISO9660FileEntry) rootEntry);
    }

    protected VolumeDescriptor createVolumeDescriptor() {
        return new ISO9660VolumeDescriptor(this);
    }

    byte[] readData(ISO9660FileEntry entry) throws IOException {
        int size = entry.getSize();
        byte[] buf = new byte[size];
        readData(entry, 0, buf, 0, size);
        return buf;
    }

    int readData(
            ISO9660FileEntry entry,
            int entryOffset,
            byte[] buffer,
            int bufferOffset,
            int len)
            throws IOException {
        long startPos = (entry.getStartBlock() * BLOCK_SIZE) + entryOffset;
        return readData(startPos, buffer, bufferOffset, len);
    }
}