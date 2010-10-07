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

import java.util.Iterator;

public class ISO9660RootDirectory extends ISO9660Directory {

    /**
     * Name of the directory containing relocated directories
     */
    public static String MOVED_DIRECTORIES_STORE_NAME = "rr_moved";
    private ISO9660Directory movedDirectoriesStore;

    /**
     * Root of the directory hierarchy<br> Use addDirectory(), addFile(), addRecursively() and addContentsRecursively()
     * on an object of this class or one of the objects returned by those functions
     */
    public ISO9660RootDirectory() {
        super("");
        setRoot(this);
    }

    /**
     * Create and Add Moved Directories Store to Directory Hierarchy
     */
    public void setMovedDirectoryStore() {
        if (movedDirectoriesStore == null) {
            movedDirectoriesStore = new ISO9660Directory(MOVED_DIRECTORIES_STORE_NAME);
            addDirectory(movedDirectoriesStore);
            // Force iterator recreation
            sortedIterator = null;
        }
    }

    /**
     * Active Moved Directory Store
     *
     * @return Active moved directories store
     */
    public ISO9660Directory getMovedDirectoriesStore() {
        return movedDirectoriesStore;
    }

    public int deepLevelCount() {
        int count = getLevel();
        for (ISO9660Directory dir : getDirectories()) {
            count = Math.max(count, dir.deepLevelCount());
        }
        return count;
    }

    public int deepFileCount() {
        int count = getFiles().size();
        for (ISO9660Directory dir : getDirectories()) {
            count += dir.deepFileCount();
        }
        return count;
    }

    public int deepDirCount() {
        int count = getDirectories().size();
        for (ISO9660Directory dir : getDirectories()) {
            count += dir.deepDirCount();
        }
        return count;
    }

    public Object clone() {
        ISO9660RootDirectory clone = (ISO9660RootDirectory) super.clone();
        clone.setParentDirectory(clone);
        clone.setRoot(clone);

        // Update Root for subdirectories
        Iterator it = clone.unsortedIterator();
        while (it.hasNext()) {
            ISO9660Directory dir = (ISO9660Directory) it.next();
            dir.setRoot(clone);
        }

        return clone;
    }
}
