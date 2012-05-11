package com.github.stephenc.javaisotools.maven;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.archiver.ArchiveEntry;

import com.github.stephenc.javaisotools.sabre.DataReference;

public class ArchiveEntryDataReference implements DataReference {

	private final ArchiveEntry entry;
	
	public ArchiveEntryDataReference(ArchiveEntry entry) {
		this.entry = entry;
	}

	public long getLength() {
		return entry.getResource().getSize();
	}

	public InputStream createInputStream() throws IOException {
		return entry.getInputStream();
	}

}
