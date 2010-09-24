package de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;

public class ByteArrayDataReference implements DataReference {
	private byte[] buffer = null;
	private int start = 0;
	private int length = 0;
	
	public ByteArrayDataReference(byte[] buffer) {
		this.buffer = buffer;
		this.start = 0;
		this.length = this.buffer.length;
	}
	
	public ByteArrayDataReference(byte[] buffer, int start, int length) {
		this.buffer = buffer;
		this.start = start;
		this.length = length;
	}
	
	public long getLength() {
		return this.length;
	}

	public InputStream createInputStream() throws IOException {
		return new ByteArrayInputStream(this.buffer, this.start, this.length);
	}

}
