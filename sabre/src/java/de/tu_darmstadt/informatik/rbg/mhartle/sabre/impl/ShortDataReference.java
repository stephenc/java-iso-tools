package de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;

public class ShortDataReference implements DataReference {
	private long value = 0;

	public ShortDataReference(long value) {
		this.value = value;
	}
	
	public long getLength() {
		// TODO Auto-generated method stub
		return 2;
	}

	public InputStream createInputStream() throws IOException {
		byte[] buffer = null;
		
		buffer = new byte[2];
		buffer[0] = (byte)((this.value & 0xFF00) >> 8);
		buffer[1] = (byte)(this.value & 0x00FF);
		
		return new ByteArrayInputStream(buffer);
	}
}
