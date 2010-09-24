/*  
 *  JIIC: Java ISO Image Creator. Copyright (C) 2007, Jens Hatlak <hatlak@rbg.informatik.tu-darmstadt.de>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl;

import java.util.Vector;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ISO9660Directory;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ISO9660File;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.NamingConventions;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.HandlerException;

public class ISO9660NamingConventions extends NamingConventions {
	public static int INTERCHANGE_LEVEL = 1;
	public static boolean FORCE_ISO9660_CHARSET = true;
	public static boolean FORCE_DOT_DELIMITER = true;
	private boolean enforce8plus3;
	private int MAX_DIRECTORY_LENGTH, MAX_FILENAME_LENGTH, MAX_EXTENSION_LENGTH;

	public ISO9660NamingConventions() {
		super("ISO 9660");
	}
	
	public void apply(ISO9660Directory dir) throws HandlerException {
		// ISO 9660 directory restrictions:
		// - character set: uppercase letters, digits and underscore
		// - filename may NOT contain dot
		// - filename non-empty
		// - filename <= MAX_DIRECTORY_LENGTH bytes
		init();

		String filename = normalize(dir.getName());
		if (filename.length() > MAX_DIRECTORY_LENGTH) {
			filename = filename.substring(0, MAX_DIRECTORY_LENGTH);
		}

		if (filename.length()==0) {
			throw new HandlerException(getID() + ": Empty directory name encountered.");
		}
		
		setFilename(dir, filename);
	}

	public void apply(ISO9660File file) throws HandlerException {
		// ISO 9660 file name restrictions:
		// - character set: uppercase letters, digits, underscore, dot and semicolon
		// - filename + extension <= 30 bytes
		// - filename <= MAX_FILENAME_LENGTH
		// - extension <= MAX_EXTENSION_LENGTH
		// - either filename or extension must be non-empty 
		// - file version must be present and delimited by semicolon
		enforce8plus3(file.enforces8plus3());
		init();

		String filename = normalize(file.getFilename());
		String extension = normalize(file.getExtension());
		file.enforceDotDelimiter(FORCE_DOT_DELIMITER);

		if (filename.length()==0 && extension.length()==0) {
			throw new HandlerException(getID() + ": Empty file name encountered.");
		}
			
		if (enforces8plus3()) {
			if (filename.length() > MAX_FILENAME_LENGTH) {
				filename = filename.substring(0, MAX_FILENAME_LENGTH);
			}
			if (extension.length() > MAX_EXTENSION_LENGTH) {
				String mapping = getExtensionMapping(extension);
				if (mapping!=null && mapping.length() <= MAX_EXTENSION_LENGTH) {
					extension = normalize(mapping);
				} else {
					extension = extension.substring(0, MAX_EXTENSION_LENGTH);
				}
			}
		} else {
			// See ISO 9660:7.5.1
			if (filename.length() + extension.length() > 30) {
				if (filename.length() >= extension.length()) {
					// Shorten filename
					filename = filename.substring(0, 30-extension.length());
				} else {
					// Shorten extension
					String mapping = getExtensionMapping(extension);
					if (mapping!=null && mapping.length() <= MAX_EXTENSION_LENGTH) {
						extension = normalize(mapping);
					} else {
						extension = extension.substring(0, 30-filename.length());
					}
				}
			}
		}
		
		setFilename(file, filename, extension);
	}

	public void init() {
		if (INTERCHANGE_LEVEL==1) {
			enforce8plus3(true);
		}
		
		if (enforces8plus3()) {
			// Interchange Level 1 (or explicitly requested): Directories 8, files 8+3 characters
			MAX_DIRECTORY_LENGTH = 8;
			MAX_FILENAME_LENGTH = 8;
			MAX_EXTENSION_LENGTH = 3;
		} else {
			MAX_DIRECTORY_LENGTH = 31;
			MAX_FILENAME_LENGTH = 0;
			MAX_EXTENSION_LENGTH = 0;
		}
	}

	void enforce8plus3(boolean flag) {
		this.enforce8plus3 = flag;
	}
	
	boolean enforces8plus3() {
		return enforce8plus3;
	}

	private String normalize(String name) {
		if (FORCE_ISO9660_CHARSET) {
			name = name.toUpperCase();
			return name.replaceAll("[^A-Z0-9_]", "_");
		} // else

		// Note: Backslash escaped for both the RegEx and Java itself
		return name.replaceAll("[*/:;?\\\\]", "_");
	}
	
	public void addDuplicate(Vector duplicates, String name, int version) {
		String[] data = {name.toUpperCase(), version+""};
		duplicates.add(data);
	}

	public boolean checkFilenameEquality(String name1, String name2) {
		return name1.equalsIgnoreCase(name2);
	}

	public void checkPathLength(String isoPath) {
		// ISO 9660:6.8.2.1: 255 Byte (255 characters)
		if (isoPath.length() > 255) {
			System.out.println(getID() + ": Path length exceeds limit: " + isoPath);
		}
	}
}
