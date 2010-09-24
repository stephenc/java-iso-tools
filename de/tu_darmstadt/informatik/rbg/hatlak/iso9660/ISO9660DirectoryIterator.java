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

import java.util.*;

/**
 * ISO 9660 Directory Iterator<br>
 * Iterates over all subdirectories of a directory,
 * NOT including the start directory itself.
 * By default, directories are processed in ISO 9660 sorted order.
 */
public class ISO9660DirectoryIterator implements Iterator {
	private Vector dirCollection;
	private Iterator dirCollectionIterator;

	public ISO9660DirectoryIterator(ISO9660Directory start, boolean sort) {
		this.dirCollection = new Vector();
		if (sort) {
			setupSorted(start);
		} else {
			setupUnsorted(start);
		}
		reset();
	}

	public ISO9660DirectoryIterator(ISO9660Directory start) {
		this(start, true);
	}
	
	private void setupSorted(ISO9660Directory start) {
		// Sort according to ISO 9660 needs
		Vector dirs = start.getDirectories();
		if (dirs.size() > 0) {
			LinkedList queue = new LinkedList();
			dirCollection.addAll(dirs);
			queue.addAll(dirs);
			while (!queue.isEmpty()) {
				ISO9660Directory dir = (ISO9660Directory) queue.removeFirst();
				if (dir==dir.getRoot().getMovedDirectoriesStore()) {
					dirs = dir.getDirectories();
				} else {
					dirs = checkMoved(dir.getDirectories());
				}
				dirCollection.addAll(dirs);
				queue.addAll(dirs);
			}
		}
	}
	
	private Vector checkMoved(Vector dirs) {
		Vector copy = new Vector(dirs);
		Iterator it = dirs.iterator();
		while (it.hasNext()) {
			ISO9660Directory dir = (ISO9660Directory) it.next();
			if (dir.isMoved()) {
				copy.remove(dir);
			}
		}
		return copy;
	}

	private void setupUnsorted(ISO9660Directory start) {
		// No sorting needed, add recursively
		Iterator it = start.getDirectories().iterator();
		while (it.hasNext()) {
			setupUnsorted((ISO9660Directory) it.next());
		}
		dirCollection.add(start);
	}
	
	public boolean hasNext() {
		return dirCollectionIterator.hasNext();
	}

	public Object next() {
		return dirCollectionIterator.next();
	}

	public void remove() {
		dirCollectionIterator.remove();
	}
	
	public void reset() {
		dirCollectionIterator = dirCollection.iterator();
	}
}
