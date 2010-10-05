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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import com.github.stephenc.javaisotools.loopfs.api.FileEntry;
import com.github.stephenc.javaisotools.loopfs.spi.AbstractBlockFileSystem;
import com.github.stephenc.javaisotools.loopfs.spi.VolumeDescriptorSet;

public class Iso9660FileSystem extends AbstractBlockFileSystem {

    public Iso9660FileSystem(File file, boolean readOnly) throws IOException {
        super(file, readOnly, Constants.DEFAULT_BLOCK_SIZE, Constants.RESERVED_SECTORS);
    }

    public String getEncoding() {
        return ((Iso9660VolumeDescriptorSet) getVolumeDescriptorSet()).getEncoding();
    }

    public InputStream getInputStream(FileEntry entry) {
        ensureOpen();
        return new EntryInputStream((Iso9660FileEntry) entry, this);
    }

    byte[] getBytes(Iso9660FileEntry entry) throws IOException {
        int size = entry.getSize();

        byte[] buf = new byte[size];

        readBytes(entry, 0, buf, 0, size);

        return buf;
    }

    int readBytes(Iso9660FileEntry entry, int entryOffset, byte[] buffer, int bufferOffset, int len)
            throws IOException {
        long startPos = (entry.getStartBlock() * Constants.DEFAULT_BLOCK_SIZE) + entryOffset;
        return readData(startPos, buffer, bufferOffset, len);
    }

    protected Enumeration enumerate(FileEntry rootEntry) {
        return new EntryEnumeration(this, (Iso9660FileEntry) rootEntry);
    }

    protected VolumeDescriptorSet createVolumeDescriptorSet() {
        return new Iso9660VolumeDescriptorSet(this);
    }
}
