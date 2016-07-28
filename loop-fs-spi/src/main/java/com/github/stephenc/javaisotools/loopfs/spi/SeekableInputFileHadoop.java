package com.github.stephenc.javaisotools.loopfs.spi;

import org.apache.hadoop.fs.FSDataInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * A Seekable Input File to work with Apache Hadoop HDFS
 */
public class SeekableInputFileHadoop implements SeekableInput {

    private FSDataInputStream channel;

    public SeekableInputFileHadoop(InputStream is) throws IOException {
        this.channel = new FSDataInputStream(is);
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
