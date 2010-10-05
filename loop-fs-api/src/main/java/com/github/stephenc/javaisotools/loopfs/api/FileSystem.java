/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (c) 2006-2007. loopy project (http://loopy.sourceforge.net).
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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.github.stephenc.javaisotools.loopfs.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 * A loop-fs file system, which is deserialize-only and consists of zero or more entries. The data for each entry can be
 * retrieved using {@link #getInputStream(FileEntry)}.
 */
public interface FileSystem<T extends FileEntry> extends Iterable<T>, Closeable {

    /**
     * Returns an input stream that reads the data for the given entry.
     *
     * @param entry the FileEntry
     *
     * @return an input stream that reads the contents of the given entry
     */
    InputStream getInputStream(T entry);

    /**
     * Returns whether or not this FileSystem has been closed.
     *
     * @return true if {@link FileSystem#close()} has been called on this * FileSystem, otherwise false.
     */
    boolean isClosed();
}