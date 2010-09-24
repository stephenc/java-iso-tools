package de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;

public class DWordDataReference implements DataReference {
	private long value = 0;

	public DWordDataReference(long value) {
		this.value = value;
	}
	
	public long getLength() {
		return 8;
	}

	public InputStream createInputStream() throws IOException {
		byte[] buffer = null;
		
		buffer = new byte[8];
		buffer[0] = (byte)((this.value & 0xFF00000000000000L) >> 56);
		buffer[1] = (byte)((this.value & 0x00FF000000000000L) >> 48);
		buffer[2] = (byte)((this.value & 0x0000FF0000000000L) >> 40);
		buffer[3] = (byte)((this.value & 0x000000FF00000000L) >> 32);
		buffer[4] = (byte)((this.value & 0x00000000FF000000L) >> 24);
		buffer[5] = (byte)((this.value & 0x0000000000FF0000L) >> 16);
		buffer[6] = (byte)((this.value & 0x000000000000FF00L) >> 8);
		buffer[7] = (byte)((this.value & 0x00000000000000FFL));
		
		return new ByteArrayInputStream(buffer);
	}
}
