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

package de.tu_darmstadt.informatik.rbg.hatlak.iso9660;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.HandlerException;

public abstract class NamingConventions {
	public static boolean VERBOSE = false;
	private static HashMap extensionMapper;
	private String id;
	
	public NamingConventions(String id) {
		this.id = id;
		addExtensionMapping("tar.gz", "tgz");
		addExtensionMapping("tar.bz2", "tbz");
	}
	
	public String getID() {
		return id;
	}
	
	public static void addExtensionMapping(String extension, String mapping) {
		if (extensionMapper==null) {
			extensionMapper = new HashMap();
		}
		
		if (!extensionMapper.containsKey(extension)) {
			extensionMapper.put(extension, mapping);
		}
	}
	
	public static String getExtensionMapping(String extension) {
		if (extensionMapper==null) {
			return null;
		}
		
		return (String) extensionMapper.get(extension);
	}

	public void startRenaming(ISO9660Directory dir) {
		if (VERBOSE) {
			System.out.print(id + ": Renamed directory " + dir.getISOPath());
		}
	}

	public void startRenaming(ISO9660File file) {
		if (VERBOSE) {
			System.out.print(id + ": Renamed file " + file.getISOPath());
		}
	}

	public void endRenaming(ISO9660Directory dir) {
		if (VERBOSE) {
			System.out.println(" to " + dir.getName());
		}
	}

	public void endRenaming(ISO9660File file) {
		if (VERBOSE) {
			System.out.println(" to " + file.getFullName());
		}
	}
	
	public void setFilename(ISO9660Directory dir, String filename) {
		if (!filename.equals(dir.getName())) {
			startRenaming(dir);
			dir.setName(filename);
			endRenaming(dir);
		}
	}

	public void setFilename(ISO9660File file, String filename) {
		if (!filename.equals(file.getFilename())) {
			startRenaming(file);
			file.setFilename(filename);
			endRenaming(file);
		}
	}

	public void setFilename(ISO9660File file, String filename, String extension) {
		if (!filename.equals(file.getFilename()) || !extension.equals(file.getExtension())) {
			startRenaming(file);
			file.setFilename(filename);
			file.setExtension(extension);
			endRenaming(file);
		}
	}

	public void incrementFilename(ISO9660Directory dir) throws HandlerException {
		String filename = dir.getName();
		if (filename.length() > 0) {
			int number = -1;
			int position = filename.length()-1;
			while (position >= 0) {
				try {
					number = Integer.parseInt(filename.substring(position, filename.length()));
					position--;
				} catch (NumberFormatException e) {	
					break;
				}
			}
			
			if (number >= 0) {
				// Filename ends with a number -> overwrite with incremented number
				number++;
				if (position > 0) {
					filename = filename.substring(0, position+1);
				} else {
					filename = "";
				}
				filename += number;
			} else {
				// Filename does not end with a number -> append 2
				// First try to append the number
				ISO9660Directory copy = new ISO9660Directory(filename + "2");
				apply(copy);
				if (checkFilenameEquality(copy.getName(), filename)) {
					// Adding the number did not change the filename -> replace last character
					filename = filename.substring(0, filename.length()) + "2";
				} else {
					filename = copy.getName();
				}
			}
		} else {
			filename = "2";
		}

		setFilename(dir, filename);
	}
	
	public void incrementFilename(ISO9660File file) throws HandlerException {
		String filename = file.getFilename();
		if (filename.length() > 0) {
			int number = -1;
			int position = filename.length()-1;
			while (position >= 0) {
				try {
					number = Integer.parseInt(filename.substring(position, filename.length()));
					position--;
				} catch (NumberFormatException e) {	
					break;
				}
			}
			
			if (number >= 0) {
				// Filename ends with a number -> overwrite with incremented number
				number++;
				if (position > 0) {
					filename = filename.substring(0, position+1);
				} else {
					filename = "";
				}
				filename += number;
			} else {
				// Filename does not end with a number -> append 2
				// First try to append the number
				ISO9660File copy = null;
				try {
					copy = new ISO9660File(file);
				} catch (HandlerException e) {
					e.printStackTrace();
				}
				apply(copy);
				if (checkFilenameEquality(copy.getFilename(), filename)) {
					// Adding the number did not change the filename -> replace last character
					filename = filename.substring(0, filename.length()) + "2";
				} else {
					filename = copy.getFilename();
				}
			}
		} else {
			filename = "2";
		}

		setFilename(file, filename);
	}

	public boolean checkFilenameEquality(String name1, String name2) {
		return name1.equals(name2);
	}
	
	public void processDirectory(ISO9660Directory dir) throws HandlerException {
		Vector duplicates = new Vector();
		
		// Prepare files and directories to be processed in sorted order
		Vector contents = new Vector();
		contents.addAll(dir.getDirectories());
		contents.addAll(dir.getFiles());
		Collections.sort(contents);

		boolean duplicate;
		Iterator it = contents.iterator();
		while (it.hasNext()) {
			duplicate = false;
			Object object = it.next();
			if (object instanceof ISO9660Directory) {
				ISO9660Directory subdir = (ISO9660Directory) object;
				apply(subdir);
				while (checkDuplicate(duplicates, subdir.getName(), -1)) {
					incrementFilename(subdir);
					duplicate = true;
				}
				if (!duplicate) {
					duplicates.clear();
				}
				addDuplicate(duplicates, subdir.getName(), -1);
				checkPathLength(subdir.getISOPath());
			} else
			if (object instanceof ISO9660File) {
				ISO9660File file = (ISO9660File) object;
				apply(file);
				while (checkDuplicate(duplicates, file.getName(), file.getVersion())) {
					incrementFilename(file);
					duplicate = true;
				}
				if (!duplicate) {
					duplicates.clear();
				}
				addDuplicate(duplicates, file.getName(), file.getVersion());
				checkPathLength(file.getISOPath());
			} else {
				throw new HandlerException("Neither file nor directory: " + object);
			}
		}
	}

	public boolean checkDuplicate(Vector duplicates, String name, int version) {
		return checkDuplicate(duplicates, name, version, true);
	}

	public boolean checkDuplicate(Vector duplicates, String name, int version, boolean checkVersion) {
		for (int i=0; i<duplicates.size(); i++) {
			String[] data = (String[]) duplicates.get(i);
			// Check for name equality
			if (checkFilenameEquality(data[0], name)) {
				int aVersion = Integer.parseInt(data[1]);
				// Require version equality for files
				if (!checkVersion || aVersion==-1 || version==aVersion) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void addDuplicate(Vector duplicates, String name, int version) {
		String[] data = {name, version+""};
		duplicates.add(data);
	}

	public abstract void apply(ISO9660Directory dir) throws HandlerException;

	public abstract void apply(ISO9660File file) throws HandlerException;
	
	public abstract void checkPathLength(String isoPath);
}
