package com.github.stephenc.javaisotools.loopfs.spi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A Seekable Input File which wraps around a File object
 */
public class SeekableInputFile implements SeekableInput {

    private RandomAccessFile channel;

    public SeekableInputFile(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file);
        }
        this.channel = new RandomAccessFile(file, "r");
    }

    public void seek(long pos) throws IOException {
        this.channel.seek(pos);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return this.channel.read(b, off, len);
    }

    public void close() throws IOException {
        this.channel.close();
    }
}
