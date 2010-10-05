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

package com.github.stephenc.javaisotools.vfs.provider.iso;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import com.github.stephenc.javaisotools.loopfs.iso9660.ISO9660FileEntry;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;

/**
 * Implementation of {@link org.apache.commons.vfs.FileObject} for ISO9660 (.iso) files.
 */
public class IsoFileObject extends AbstractFileObject {

    private ISO9660FileEntry entry;
    private FileType type;
    private final Set children;

    /**
     * Creates an IsoFileObject without a ISO9660FileEntry. The entry must be set before calling getContent(). The
     * FileType is set to IMAGINARY until the underlying entry is set.
     */
    IsoFileObject(final FileName name, final IsoFileSystem fs) {
        super(name, fs);
        this.type = FileType.IMAGINARY;
        this.children = new HashSet();
    }

    IsoFileObject(final FileName name, final ISO9660FileEntry entry, final IsoFileSystem fs) {
        super(name, fs);
        setIsoEntry(entry);
        this.children = new HashSet();
    }

    /**
     * Sets the ISO9660FileEntry that backs this FileObject. This method is package-private because IsoFileSystem
     * pre-creates some directory entries when building the file index, and it needs to set the backing entry after the
     * fact.
     */
    void setIsoEntry(final ISO9660FileEntry entry) {
        if (null != this.entry) {
            throw new RuntimeException("Cannot change the underlying entry once it has been set");
        }
        if (null == entry) {
            throw new IllegalArgumentException("'entry' cannot be null");
        }

        this.entry = entry;
        this.type = (entry.isDirectory()) ? FileType.FOLDER : FileType.FILE;
    }

    /**
     * Attaches a child to this file. The parent IsoFileSystem calls this method when building the file index.
     */
    void attachChild(final FileName childName) {
        this.children.add(childName.getBaseName());
    }

    /**
     * Always returns false; currently ISO files are only readable.
     */
    public boolean isWriteable() {
        return false;
    }

    protected FileType doGetType() {
        return this.type;
    }

    protected String[] doListChildren() {
        return (String[]) this.children.toArray(new String[this.children.size()]);
    }

    protected long doGetContentSize() {
        return this.entry.getSize();
    }

    protected long doGetLastModifiedTime() throws Exception {
        return this.entry.getLastModifiedTime();
    }

    protected InputStream doGetInputStream() throws Exception {
        return ((IsoFileSystem) getFileSystem()).getInputStream(this.entry);
    }
}