/*
Copyright (C) 2006-2007 loopy project (http://loopy.sourceforge.net)

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.github.stephenc.javaisotools.loopfs.iso9660;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import com.github.stephenc.javaisotools.loopfs.util.LittleEndian;

/**
 * A breadth-first Enumeration of the entries in a ISO9660 file system.
 */
class EntryEnumeration implements Enumeration {

    private final ISO9660FileSystem fileSystem;
    private final List queue;

    public EntryEnumeration(final ISO9660FileSystem fileSystem, final ISO9660FileEntry rootEntry) {
        this.fileSystem = fileSystem;
        this.queue = new LinkedList();
        this.queue.add(rootEntry);
    }

    public boolean hasMoreElements() {
        return !this.queue.isEmpty();
    }

    public Object nextElement() {
        if (!hasMoreElements()) {
            throw new NoSuchElementException();
        }

        // pop next entry from the queue
        final ISO9660FileEntry entry = (ISO9660FileEntry) this.queue.remove(0);

        // if the entry is a directory, queue all its children
        if (entry.isDirectory()) {
            final byte[] content;

            try {
                content = this.fileSystem.getBytes(entry);
            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            int offset = 0;
            boolean paddingMode = false;

            while (offset < content.length) {
                if (LittleEndian.getUInt8(content, offset) <= 0) {
                    paddingMode = true;
                    offset += 2;
                    continue;
                }

                ISO9660FileEntry child = new ISO9660FileEntry(
                        this.fileSystem, entry.getPath(), content, offset + 1);

                if (paddingMode && child.getSize() < 0) {
                    continue;
                }

                offset += child.getEntryLength();

                // It doesn't seem useful to include the . and .. entries
                if (!".".equals(child.getName()) && !"..".equals(child.getName())) {
                    this.queue.add(child);
                }
            }
        }

        return entry;
    }
}
