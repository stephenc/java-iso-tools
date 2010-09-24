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

public class POSIXFileMode {
	public static final int USER_READ = 0400;
	public static final int USER_WRITE = 0200;
	public static final int USER_EXEC = 0100;
	public static final int GROUP_READ = 040;
	public static final int GROUP_WRITE = 020;
	public static final int GROUP_EXEC = 010;
	public static final int OTHER_READ = 04;
	public static final int OTHER_WRITE = 02;
	public static final int OTHER_EXEC = 01;
	public static final int SETUID = 04000;
	public static final int SETGID = 02000;
	public static final int ENFORCE_FILE_LOCKING = 02000;
	public static final int SAVE_SWAPPED_AFTER_USE = 01000;
	// Type of File: Only in RRIP 1.09
	public static final int TYPE_OF_FILE = 0170000;
	public static final int SOCKET = 0140000;
	public static final int SYMLINK = 0120000;
	public static final int REGULAR = 0100000;
	public static final int BLOCK_SPECIAL = 060000;
	public static final int CHAR_SPECIAL = 020000;
	public static final int DIRECTORY = 040000;
	public static final int PIPE_FIFO = 010000;
	private int fileMode;
	
	public POSIXFileMode() {
		 init();
	}
	
	public void init() {
		fileMode = 0; // no permissions
	}
	
	public void initType() {
		// Clear all but lower 4 octal digits
		fileMode &= 07777;
	}
	
	public void setDefault(boolean isDirectory) {
		init();
		
		// a+r
		setPermission(USER_READ);
		setPermission(GROUP_READ);
		setPermission(OTHER_READ);
		
		if (isDirectory) {
			setPermission(DIRECTORY);
			// a+x
			setPermission(USER_EXEC);
			setPermission(GROUP_EXEC);
			setPermission(OTHER_EXEC);
		} else {
			setPermission(REGULAR);			
		}
	}
	
	public void setFile() {
		initType();
		setPermission(REGULAR);
	}
	
	public void setSymlink() {
		initType();
		setPermission(SYMLINK);
	}
	
	public void setDirectory() {
		initType();
		setPermission(DIRECTORY);
	}
	
	public void setUID() {
		setPermission(SETUID);
	}

	public void setGID() {
		setPermission(SETGID);
	}

	public void setPermission(int permission) {
		fileMode |= permission;
	}
	
	public void clearPermission(int permission) {
		fileMode &= ~permission;
	}
	
	public int getFileMode() {
		return fileMode;
	}
}
