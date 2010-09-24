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

public class POSIXFileModesFactory {
	public static int USER_READ = 0400;
	public static int USER_WRITE = 0200;
	public static int USER_EXEC = 0100;
	public static int GROUP_READ = 040;
	public static int GROUP_WRITE = 020;
	public static int GROUP_EXEC = 010;
	public static int OTHER_READ = 04;
	public static int OTHER_WRITE = 02;
	public static int OTHER_EXEC = 01;
	public static int SETUID = 04000;
	public static int SETGID = 02000;
	public static int ENFORCE_FILE_LOCKING = 02000;
	public static int SAVE_SWAPPED_AFTER_USE = 01000;
	public static int SOCKET = 0140000;
	public static int SYMLINK = 0120000;
	public static int REGULAR = 0100000;
	public static int BLOCK_SPECIAL = 060000;
	public static int CHAR_SPECIAL = 020000;
	public static int DIRECTORY = 040000;
	public static int PIPE_FIFO = 010000;
	private int fileMode;
	
	public POSIXFileModesFactory() {
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
		
		// u+rw,go+r
		setPermission(USER_READ);
		setPermission(USER_WRITE);
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
