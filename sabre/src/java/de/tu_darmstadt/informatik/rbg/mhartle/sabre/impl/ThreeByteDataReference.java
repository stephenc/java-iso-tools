package de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;

public class ThreeByteDataReference implements DataReference {
	private long value = 0;

	public ThreeByteDataReference(long value) {
		this.value = value;
	}
	
	public long getLength() {
		// TODO Auto-generated method stub
		return 3;
	}

	public InputStream createInputStream() throws IOException {
		byte[] buffer = null;
		
		buffer = new byte[3];
		buffer[0] = (byte)((this.value & 0xFF0000) >> 16);
		buffer[1] = (byte)((this.value & 0x00FF00) >> 8);
		buffer[2] = (byte)(this.value & 0x0000FF);
		return new ByteArrayInputStream(buffer);
	}

}
