package com.github.stephenc.javaisotools.sabre.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.stephenc.javaisotools.sabre.DataReference;

public class IntegerArrayDataReference implements DataReference {

    private byte[] buffer = null;

    public IntegerArrayDataReference(int[] buffer) {
        this.buffer = new byte[buffer.length];
        for (int i = 0; i < buffer.length; i++) {
            this.buffer[i] = (byte) buffer[i];
        }
    }

    public long getLength() {
        return this.buffer.length;
    }

    public InputStream createInputStream() throws IOException {
        return new ByteArrayInputStream(this.buffer);
    }
}
