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

import java.io.File;
import java.util.*;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.HandlerException;

public class ISO9660Directory implements ISO9660HierarchyObject {
	private String name;
	private int level;
	private Vector files, directories;
	private ISO9660Directory parent;
	private ISO9660RootDirectory root;
	private long lastModified;
	private boolean sorted;
	private Object id;
	ISO9660DirectoryIterator sortedIterator, unsortedIterator;
	
	/**
	 * Create directory
	 * 
	 * @param name Name of the directory
	 */
	public ISO9660Directory(String name) {
		init();
		this.name = name;
	}

	/**
	 * Create directory
	 * 
	 * @param file Directory
	 * @throws HandlerException Not a directory
	 */
	public ISO9660Directory(File file) throws HandlerException {
		init();
		if (file.isDirectory()) {
			this.name = file.getName();
			this.lastModified = file.lastModified();
		} else {
			throw new HandlerException("Not a directory: " + file);
		}
	}

	private void init() {
		this.files = new Vector();
		this.directories = new Vector();
		this.level = 1;
		this.parent = this;
		this.name = "";
		this.lastModified = (new Date()).getTime();
		this.sorted = false;
		this.id = new Object();
	}	
	
	public void setName(String name) {
		this.name = name;

		if (parent!=this) {
			// Force sort of parent only if is contains this directory
			parent.forceSort();
		}
	}
	
	public String getName() {
		return name;
	}
	
	private void setLevel(int level) {
		this.level = level;
	}

	/**
	 * Returns this directory's level of the directory hierarchy 
	 * 
	 * @return Directory's level
	 */
	public int getLevel() {
		return this.level;
	}

	void setRoot(ISO9660RootDirectory root) {
		this.root = root;
	}
	
	public ISO9660RootDirectory getRoot() {
		return root;
	}
	
	public ISO9660Directory getParentDirectory() {
		return parent;
	}

	void setParentDirectory(ISO9660Directory parent) {
		this.parent = parent;
	}
	
	/**
	 * Returns a Vector of the directory's files
	 * 
	 * @return Vector containing ISO9660File objects
	 */
	public Vector getFiles() {
		if (!sorted) {
			sort();
		}
		return files;
	}

	/**
	 * Returns a Vector of the directory's subdirectories
	 * 
	 * @return Vector containing ISO9660Directory objects
	 */
	public Vector getDirectories() {
		if (!sorted) {
			sort();
		}
		return directories;
	}
	
	/**
	 * Returns whether this directory contains subdirectories
	 * 
	 * @return Whether the directory contains subdirectories
	 */
	public boolean hasSubDirs() {
		return (directories.size() > 0);
	}

	public String getISOPath() {
		StringBuffer buf = new StringBuffer();
		getISOPath(buf);
		
		return buf.toString();
	}
	
	private void getISOPath(StringBuffer buf) {
		ISO9660Directory parent = getParentDirectory();
		if (parent==this) {
			// Root reached
			return;
		}
		
		// Depth-first recursion
		parent.getISOPath(buf);

		// Append path component separator
		buf.append(File.separator);

		// Append name of current directory
		buf.append(getName());
	}
	
	int deepLevelCount() {
		int count = level;
		Iterator it = directories.iterator();
		while (it.hasNext()) {
			ISO9660Directory dir = (ISO9660Directory) it.next();
			count = Math.max(count, dir.deepLevelCount());
		}
		return count;
	}

	int deepFileCount() {
		int count = files.size();
		Iterator it = directories.iterator();
		while (it.hasNext()) {
			ISO9660Directory dir = (ISO9660Directory) it.next();
			count += dir.deepFileCount();
		}
		return count;
	}

	int deepDirCount() {
		int count = directories.size();
		Iterator it = directories.iterator();
		while (it.hasNext()) {
			ISO9660Directory dir = (ISO9660Directory) it.next();
			count += dir.deepDirCount();
		}
		return count;
	}

	/**
	 * Add directory
	 * 
	 * @param dir Directory to be added
	 * @return Added directory
	 */
	public ISO9660Directory addDirectory(ISO9660Directory dir) {
		dir.setLevel(level+1);
		dir.setParentDirectory(this);
		dir.setRoot(root);
		directories.add(dir);
		sorted = false;
		return dir;
	}

	/**
	 * Add directory
	 * 
	 * @param file Directory to be added
	 * @return Added directory
	 * @throws HandlerException Not a directory
	 */
	public ISO9660Directory addDirectory(File file) throws HandlerException {
		ISO9660Directory dir = new ISO9660Directory(file);
		return addDirectory(dir);
	}
	
	/**
	 * Add directory
	 * 
	 * @param name Name of the directory to created and added 
	 * @return Added directory
	 */
	public ISO9660Directory addDirectory(String name) {
		ISO9660Directory dir = new ISO9660Directory(name);
		return addDirectory(dir);
	}

	/**
	 * Add path
	 * 
	 * @param path Filesystem-specific path to be added recursively
	 * @return Topmost added directory
	 * @throws HandlerException
	 */
	public ISO9660Directory addPath(String path) throws HandlerException {
		ISO9660Directory dir = null;
		if (path.indexOf(File.separator) == -1) {
			// Path is a directory - add it if not already listed
			return checkDirectory(path);
		}

		// Add path recursively
		int frontSeparatorIndex = path.indexOf(File.separator);
		String dirName = path.substring(0, frontSeparatorIndex);
		dir = checkDirectory(dirName);
		String rest = path.substring(frontSeparatorIndex+1);
		dir = dir.addPath(rest);
		
		return dir;
	}
	
	private ISO9660Directory checkDirectory(String name) throws HandlerException {
		Iterator it = directories.iterator();
		while (it.hasNext()) {
			ISO9660Directory dir = (ISO9660Directory) it.next();
			if (dir.getName().equals(name)) {
				return dir;
			}
		}
		// Not listed -> create a new one and add it to the hierarchy
		return addDirectory(name);
	}
	
	/**
	 * Force a sort of this directory's files and subdirectories
	 */
	public void forceSort() {
		sorted = false;
	}
	
	private void sort() {
		Collections.sort(files);
		Collections.sort(directories);
		sorted = true;
	}
	
	/**
	 * Add file
	 * 
	 * @param file File to be added
	 */
	public void addFile(ISO9660File file) {
		file.setParentDirectory(this);
		files.add(file);
		sorted = false;
	}

	/**
	 * Add file
	 * 
	 * @param file File to be added
	 * @param version File version
	 * @throws HandlerException Problems converting to ISO9660File
	 */
	public void addFile(File file, int version) throws HandlerException {
		addFile(new ISO9660File(file, version));
	}

	/**
	 * Add file
	 * 
	 * @param file File to be added
	 * @throws HandlerException Problems converting to ISO9660File
	 */
	public void addFile(File file) throws HandlerException {
		addFile(new ISO9660File(file));
	}
	
	/**
	 * Add file
	 * 
	 * @param pathname File to be added
	 * @param version File version
	 * @throws HandlerException Problems converting to ISO9660File
	 */
	public void addFile(String pathname, int version) throws HandlerException {
		addFile(new ISO9660File(pathname, version));
	}

	/**
	 * Add file
	 * 
	 * @param pathname File to be added
	 * @throws HandlerException Problems converting to ISO9660File
	 */
	public void addFile(String pathname) throws HandlerException {
		addFile(new ISO9660File(pathname));
	}

	/**
	 * Add file or directory recursively
	 * 
	 * @param file File or directory to be added recursively
	 * @throws HandlerException Problems converting to ISO9660File
	 */
	public void addRecursively(File file) throws HandlerException {
		addRecursively(file, true, this);
	}
	
	/**
	 * Add contents of directory recursively
	 * 
	 * @param file Directory the contents of which are to be added recursively
	 * @throws HandlerException Problems converting to ISO9660File
	 */
	public void addContentsRecursively(File file) throws HandlerException {
		addRecursively(file, false, this);
	}

	private void addRecursively(File file, boolean addItself, ISO9660Directory parent) throws HandlerException {
		if (!file.isDirectory() && addItself) {
			// Add file
			parent.addFile(file);
			return;
		}
		
		ISO9660Directory dir = parent;
		if (addItself) {
			// Add directory itself
			dir = parent.addDirectory(file);
		}
		
		// Add directory contents recursively
		File[] files = file.listFiles();
		for (int i=0; i<files.length; i++) {
			addRecursively(files[i], true, dir);
		}
	}
	
	public int compareTo(Object object) throws ClassCastException, NullPointerException {
		// Alphanumerical case-insensitive sort (according to ISO9660 needs)
		if (object==null) {
			throw new NullPointerException();
		} else
		if (object.equals(this)) {
			return 0;
		} else
		if (object instanceof ISO9660Directory) {
			ISO9660Directory dir = (ISO9660Directory) object;
			return getName().toUpperCase().compareTo(dir.getName().toUpperCase());
		} else
		if (object instanceof ISO9660File) {
			ISO9660File file = (ISO9660File) object;
			return getName().toUpperCase().compareTo(file.getFullName().toUpperCase());
		} else {
			throw new ClassCastException();
		}		
	}
	
	/**
	 * Returns date of last modification
	 * 
	 * @return Date of last modification
	 */
	public long lastModified() {
		return lastModified;
	}

	public boolean isDirectory() {
		return true;
	}
	
	/**
	 * Returns whether this directory has been relocated
	 * 
	 * @return Whether this is a relocated directory
	 */
	public boolean isMoved() {
		return parent==root.getMovedDirectoriesStore();
	}
	
	/**
	 * Relocate directory
	 * 
	 * @return Old parent directory
	 */
	public ISO9660Directory relocate() {
		ISO9660Directory movedDirectoriesStore = root.getMovedDirectoriesStore();
		ISO9660Directory oldParent = parent;
		int oldLevel = level;
		
		// Relocate directory (updates level and parent pointer)
		movedDirectoriesStore.addDirectory(this);
		
		// Update level of all subdirectories
		int difference = oldLevel - getLevel();
		Iterator it = unsortedIterator();
		while (it.hasNext()) {
			ISO9660Directory subdir = (ISO9660Directory) it.next();
			subdir.setLevel(subdir.getLevel() - difference);
		}

		return oldParent;
	}

	public Object getID() {
		// Identification of the ISO9660Directory, survives cloning
		return id;
	}

	public Object clone() {
		ISO9660Directory clone = null;
		try {
			clone = (ISO9660Directory) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		clone.level = level;
		clone.directories = new Vector();
		clone.files = new Vector();
		clone.id = id;
		clone.sortedIterator = null;
		clone.sorted = false;

		Iterator dit = directories.iterator();
		while (dit.hasNext()) {
			ISO9660Directory subdir = (ISO9660Directory) dit.next();
			ISO9660Directory subdirClone = (ISO9660Directory) subdir.clone();
			subdirClone.setParentDirectory(clone);
			subdirClone.setLevel(level + 1);
			subdirClone.id = subdir.id;
			subdirClone.sortedIterator = null;
			subdirClone.sorted = false;
			clone.directories.add(subdirClone);
		}

		Iterator fit = files.iterator();
		while (fit.hasNext()) {
			ISO9660File file = (ISO9660File) fit.next();
			ISO9660File fileClone = (ISO9660File) file.clone();
			fileClone.setParentDirectory(clone);
			clone.files.add(fileClone);
		}

		return clone;
	}
	
	/**
	 * Returns a directory iterator to traverse the directory hierarchy
	 * according to the needs of ISO 9660 (sort order of Path Tables and
	 * Directory Records)
	 * 
	 * @return Iterator
	 */
	public Iterator sortedIterator() {
		if (sortedIterator==null) {
			sortedIterator = new ISO9660DirectoryIterator(this, true);
		}
		sortedIterator.reset();
		return sortedIterator;
	}
	
	/**
	 * Returns a directory iterator to traverse the directory hierarchy
	 * using a recursive method
	 * 
	 * @return Iterator
	 */
	public Iterator unsortedIterator() {
		if (unsortedIterator==null) {
			unsortedIterator = new ISO9660DirectoryIterator(this, false);
		}
		unsortedIterator.reset();
		return unsortedIterator;
	}
}
