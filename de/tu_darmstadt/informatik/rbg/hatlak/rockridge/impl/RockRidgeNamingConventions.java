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

package de.tu_darmstadt.informatik.rbg.hatlak.rockridge.impl;

import java.util.Vector;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.*;

public class RockRidgeNamingConventions extends NamingConventions {
	public static boolean HIDE_MOVED_DIRECTORIES_STORE = true;
	public static boolean FORCE_PORTABLE_FILENAME_CHARACTER_SET = true;
	// Filename lengths are not restricted by Rock Ridge,
	// these are just safe defaults
	public static int MAX_DIRECTORY_LENGTH = 255;
	public static int MAX_FILENAME_LENGTH = 255;
	
	public RockRidgeNamingConventions() {
		super("Rock Ridge");
	}
	
	public void apply(ISO9660Directory dir) {
		String filename = normalize(dir.getName());

		if (filename.length() > MAX_DIRECTORY_LENGTH) {
			// Shorten filename
			filename = filename.substring(0, MAX_DIRECTORY_LENGTH);
		}
		
		setFilename(dir, filename);
	}

	public void apply(ISO9660File file) {
		String filename = normalize(file.getFilename());
		String extension = normalize(file.getExtension());
		int length = filename.length() + extension.length();

		if (extension.length() == 0) {
			if (length > MAX_FILENAME_LENGTH) {
				// Shorten filename
				filename = filename.substring(0, MAX_FILENAME_LENGTH);				
			}
		} else {
			if (length + 1 > MAX_FILENAME_LENGTH) {
				// Shorten filename
				filename = filename.substring(0, MAX_FILENAME_LENGTH-extension.length()-1);
			}
		}
		
		setFilename(file, filename, extension);
	}

	public boolean checkDuplicate(Vector duplicates, String name, int version) {
		return checkDuplicate(duplicates, name, version, false);
	}

	public void endRenaming(ISO9660File file) {
		if (VERBOSE) {
			System.out.println(" to " + file.getName());
		}
	}

	private String normalize(String name) {
		if (FORCE_PORTABLE_FILENAME_CHARACTER_SET) {
			return name.replaceAll("[^-A-Za-z0-9._]", "_");
		}
		return name;
	}
	
	public void checkPathLength(String isoPath) {
		// Nothing to do here (Rock Ridge has no own path length constraint -> ISO 9660 check is sufficient)
	}
}
