package de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;

public class WordDataReference implements DataReference {
	private long value = 0;

	public WordDataReference(long value) {
		this.value = value;
	}
	
	public long getLength() {
		return 4;
	}

	public InputStream createInputStream() throws IOException {
		byte[] buffer = null;
		
		buffer = new byte[4];
		buffer[0] = (byte)((this.value & 0xFF000000) >> 24);
		buffer[1] = (byte)((this.value & 0x00FF0000) >> 16);
		buffer[2] = (byte)((this.value & 0x0000FF00) >> 8);
		buffer[3] = (byte)(this.value & 0x000000FF);
		
		return new ByteArrayInputStream(buffer);
	}
}
