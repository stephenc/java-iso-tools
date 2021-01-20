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

package com.github.stephenc.javaisotools.loopfs.iso9660;

import com.github.stephenc.javaisotools.loopfs.api.FileEntry;

/**
 * Represents a file in an ISO9660 file system.
 */
public final class Iso9660FileEntry implements FileEntry {

    public static final char ID_SEPARATOR = ';';

    private Iso9660FileSystem fileSystem;
    private String parentPath;
    private final int entryLength;
    private final long startSector;
    private final long dataLength;
    private final long lastModifiedTime;
    private final int flags;
    private final String identifier;

    //private final int extAttributeLength;
    //private final int fileUnitSize;
    //private final int interleaveSize;

    public Iso9660FileEntry(final Iso9660FileSystem fileSystem, final byte[] block, final int pos) {
        this(fileSystem, null, block, pos);
    }

    /**
     * Initialize this instance.
     *
     * @param fileSystem the parent file system
     * @param parentPath the path of the parent directory
     * @param block      the bytes of the sector containing this file entry
     * @param startPos   the starting position of this file entry
     */
    public Iso9660FileEntry(final Iso9660FileSystem fileSystem, final String parentPath,
                            final byte[] block, final int startPos) {
        this.fileSystem = fileSystem;
        this.parentPath = parentPath;

        final int offset = startPos - 1;

        this.entryLength = Util.getUInt8(block, offset + 1);
        //this.extAttributeLength = Util.getUInt8(block, offset+2);
        this.startSector = Util.getUInt32LE(block, offset + 3);
        this.dataLength = Util.getUInt32LE(block, offset + 11);
        this.lastModifiedTime = Util.getDateTime(block, offset + 19);
        this.flags = Util.getUInt8(block, offset + 26);
        //this.fileUnitSize = Util.getUInt8(block, offset+27);
        //this.interleaveSize = Util.getUInt8(block, offset+28);
        this.identifier = getFileIdentifier(block, offset, isDirectory());
    }

    private String getFileIdentifier(final byte[] block, final int offset, final boolean isDir) {
        final int fidLength = Util.getUInt8(block, offset + 33);

        if (isDir) {
            final int buff34 = Util.getUInt8(block, offset + 34);

            if ((fidLength == 1) && (buff34 == 0x00)) {
                return ".";
            } else if ((fidLength == 1) && (buff34 == 0x01)) {
                return "..";
            }
        }

        final String id = Util.getDChars(
                block, offset + 34, fidLength, this.fileSystem.getEncoding());

        final int sepIdx = id.indexOf(ID_SEPARATOR);

        if (sepIdx >= 0) {
            return id.substring(0, sepIdx);
        } else {
            return id;
        }
    }

    public String getName() {
        return this.identifier;
    }

    public String getPath() {
        if (".".equals(this.getName())) {
            return "";
        }

        StringBuffer buf = new StringBuffer();

        if (null != this.parentPath) {
            buf.append(this.parentPath);
        }

        buf.append(getName());

        if (isDirectory()) {
            buf.append("/");
        }

        return buf.toString();
    }

    public long getLastModifiedTime() {
        return this.lastModifiedTime;
    }

    public boolean isDirectory() {
        return (this.flags & 0x03) != 0;
    }

    public long getSize() {
        return this.dataLength;
    }

    /**
     * Returns the block number where this entry starts.
     */
    public long getStartBlock() {
        return this.startSector;
    }

    /**
     * Returns the size this entry takes up in the file table.
     */
    public int getEntryLength() {
        return this.entryLength;
    }

    /**
     * Returns true if this is the last entry in the file system.
     */
    public final boolean isLastEntry() {
        return (this.flags & 0x40) == 0;
    }
}