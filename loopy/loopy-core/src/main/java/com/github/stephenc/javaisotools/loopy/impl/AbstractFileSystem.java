package com.github.stephenc.javaisotools.loopy.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.github.stephenc.javaisotools.loopy.FileSystem;
import com.github.stephenc.javaisotools.loopy.LoopyException;

public abstract class AbstractFileSystem implements FileSystem {

    /**
     * The file containing the file system image.
     */
    private File file;
    /**
     * Whether the image is writeable. Currently, only read-only images are supported.
     */
    private boolean readOnly;
    /**
     * Channel to the open file.
     */
    private RandomAccessFile channel;

    protected AbstractFileSystem(File file, boolean readOnly) throws LoopyException {
        if (!readOnly) {
            throw new IllegalArgumentException(
                    "Currrently, only read-only is supported");
        }
        this.file = file;
        this.readOnly = readOnly;
        try {
            // check that the underlying file is valid
            checkFile();
            // open the channel
            this.channel = new RandomAccessFile(this.file, getMode(readOnly));
        } catch (IOException ex) {
            throw new LoopyException("Error opening the file", ex);
        }
    }

    private void checkFile() throws FileNotFoundException {
        if (this.readOnly && !file.exists()) {
            throw new FileNotFoundException("File does not exist: " + this.file);
        }
    }

    private String getMode(boolean readOnly) {
        return (readOnly) ? "r" : "rw";
    }

    // TODO: close open streams automatically

    public synchronized void close() throws LoopyException {
        if (isClosed()) {
            return;
        }
        try {
            this.channel.close();
        } catch (IOException ex) {
            throw new LoopyException("Error closing file system", ex);
        } finally {
            this.channel = null;
        }
    }

    public boolean isClosed() {
        return null == this.channel;
    }

    protected void ensureOpen() throws IllegalStateException {
        if (isClosed()) {
            throw new IllegalStateException("File has been closed");
        }
    }

    protected void seek(long pos) throws IOException {
        ensureOpen();
        this.channel.seek(pos);
    }

    protected int read(byte[] buffer, int bufferOffset, int len) throws IOException {
        ensureOpen();
        return this.channel.read(buffer, bufferOffset, len);
    }
}