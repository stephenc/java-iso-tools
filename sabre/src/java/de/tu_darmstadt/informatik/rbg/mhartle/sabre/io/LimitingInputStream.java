/*
 * Created on 01.10.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.tu_darmstadt.informatik.rbg.mhartle.sabre.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author hartle
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LimitingInputStream extends FilterInputStream {
	protected int limit = -1;
	
	public LimitingInputStream(InputStream inputStream, int limit) {
		super(inputStream);
		this.limit = limit;
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public int getLimit() {
		return this.limit;
	}
	
	public int read() throws IOException {
		int readByte = -1;
		
		// Limit the amount of bytes that can be read
		if ((this.limit > 0) || (this.limit == -1)) {
			readByte = super.read();
			if (this.limit > 0) {
				this.limit -=1;
			}
		}
		
		return readByte;
	}
	
	public int read(byte[] buffer) throws IOException {
		int result = -1;
		
		if (this.limit > 0) {
			result = super.read(buffer, 0, (this.limit < buffer.length) ? this.limit : buffer.length);
		} else if (this.limit == -1) {
			result = super.read(buffer);
		}

		if (result != -1) {
			this.limit -= result;
		}
		
		return result;
	}
	
	public int read(byte[] buffer, int position, int length) throws IOException {
		int result = -1;
		
		if (this.limit > 0) {
			result = super.read(buffer, position, (this.limit < length) ? this.limit : length);
		} else if (this.limit == -1) {
			result = super.read(buffer, position, length);
		}
		
		if (result != -1) {
			this.limit -= result;
		}
		
		return result;
	}
	
	public int available() throws IOException {
		int available = 0;
		
		available = super.available();
		if (this.limit != -1) {
			if (this.limit < available) {
				available = this.limit;
			}
		}
		
		return available;
	}
}
