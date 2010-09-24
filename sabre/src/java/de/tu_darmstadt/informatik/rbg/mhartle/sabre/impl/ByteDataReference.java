package de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;

public class ByteDataReference implements DataReference {
	private long value = 0;

	public ByteDataReference(long value) {
		this.value = value;
	}
	
	public long getLength() {
		// TODO Auto-generated method stub
		return 1;
	}

	public InputStream createInputStream() throws IOException {
		byte[] buffer = null;
		
		buffer = new byte[1];
		buffer[0] = (byte)(this.value & 0xFF);
		return new ByteArrayInputStream(buffer);
	}
}
