package com.github.stephenc.javaisotools.maven;

import org.codehaus.plexus.archiver.ArchiverException;

@SuppressWarnings("serial")
public class Iso9660ArchiverException extends ArchiverException {
	private final Type type;

	public Iso9660ArchiverException(Type type, String message) {
		super(message);
		this.type = type;
	}

	Type getType() {
		return type;
	}

	public enum Type {
		MissingDestination, DestinationNotAFile, DestinationReadOnly, UnsupportedEntryType
	}
}
