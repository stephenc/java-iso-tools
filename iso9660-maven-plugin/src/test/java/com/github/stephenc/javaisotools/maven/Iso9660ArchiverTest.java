package com.github.stephenc.javaisotools.maven;

import static org.junit.Assert.*;

import java.io.IOException;

import org.codehaus.plexus.archiver.ArchiverException;
import org.junit.Test;

public class Iso9660ArchiverTest {

	@Test
	public void mustHaveDestination() throws ArchiverException, IOException {
		try {
			new Iso9660Archiver().execute();

			fail("Should have noticed the destination was missing");
		} catch (Iso9660ArchiverException e) {
			assertEquals(Iso9660ArchiverException.Type.MissingDestination,
					e.getType());
		}
	}

	@Test
	public void testGetArchiveType() {
		assertEquals("iso9660", new Iso9660Archiver().getArchiveType());
	}

}
