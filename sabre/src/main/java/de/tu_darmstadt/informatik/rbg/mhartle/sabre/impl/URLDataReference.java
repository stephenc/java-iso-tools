package de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.io.LimitingInputStream;

public class URLDataReference implements DataReference {
	private URL url = null;
	private long position = 0;
	private long length = 0;
	
	public URLDataReference(URL url, long position, long length) {
		this.url = url;
		this.position = position;
		this.length = length;
	}
	
	public long getLength() {
		return this.length;
	}

	public InputStream createInputStream() throws IOException {
		InputStream urlInputStream = null;
		InputStream limitedInputStream = null;
		
		urlInputStream = this.url.openStream();
		urlInputStream.skip(this.position);
		limitedInputStream = new LimitingInputStream(urlInputStream, (int)this.length);
		
		return limitedInputStream;
	}
}
