package com.github.stephenc.javaisotools.loopfs.spi;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * SeekableInput is an interface to abstract from using only Files as the pass-in to File Systems,
 * this allows for any SeekableInput to be used instead.
 */
public interface SeekableInput {

    /**
     * @see java.io.RandomAccessFile#seek(long)
     */
    void seek(long pos) throws IOException;

    /**
     * @see java.io.RandomAccessFile#read(byte[], int, int)
     */
    int read(byte[] b, int off, int len) throws IOException;

    /**
     * @see RandomAccessFile#close()
     */
    void close() throws IOException;
}
