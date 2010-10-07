/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (C) 2007. Jens Hatlak <hatlak@rbg.informatik.tu-darmstadt.de>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.github.stephenc.javaisotools.iso9660;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * ISO 9660 Directory Iterator<br> Iterates over all subdirectories of a directory, NOT including the start directory
 * itself. By default, directories are processed in ISO 9660 sorted order.
 */
public class ISO9660DirectoryIterator implements Iterator<ISO9660Directory> {

    private List<ISO9660Directory> dirCollection;
    private Iterator<ISO9660Directory> dirCollectionIterator;

    public ISO9660DirectoryIterator(ISO9660Directory start, boolean sort) {
        this.dirCollection = new ArrayList<ISO9660Directory>();
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
        List<ISO9660Directory> dirs = start.getDirectories();
        if (!dirs.isEmpty()) {
            LinkedList<ISO9660Directory> queue = new LinkedList<ISO9660Directory>();
            dirCollection.addAll(dirs);
            queue.addAll(dirs);
            while (!queue.isEmpty()) {
                ISO9660Directory dir = queue.removeFirst();
                if (dir == dir.getRoot().getMovedDirectoriesStore()) {
                    dirs = dir.getDirectories();
                } else {
                    dirs = checkMoved(dir.getDirectories());
                }
                dirCollection.addAll(dirs);
                queue.addAll(dirs);
            }
        }
    }

    private List<ISO9660Directory> checkMoved(List<ISO9660Directory> dirs) {
        List<ISO9660Directory> copy = new ArrayList<ISO9660Directory>(dirs);
        for (ISO9660Directory dir : dirs) {
            if (dir.isMoved()) {
                copy.remove(dir);
            }
        }
        return copy;
    }

    private void setupUnsorted(ISO9660Directory start) {
        // No sorting needed, add recursively
        for (ISO9660Directory iso9660Directory : start.getDirectories()) {
            setupUnsorted(iso9660Directory);
        }
        dirCollection.add(start);
    }

    public boolean hasNext() {
        return dirCollectionIterator.hasNext();
    }

    public ISO9660Directory next() {
        return dirCollectionIterator.next();
    }

    public void remove() {
        dirCollectionIterator.remove();
    }

    public void reset() {
        dirCollectionIterator = dirCollection.iterator();
    }
}
