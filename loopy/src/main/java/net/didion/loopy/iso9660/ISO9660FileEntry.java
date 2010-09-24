package net.didion.loopy.iso9660;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.didion.loopy.LoopyException;
import net.didion.loopy.impl.AbstractBlockFileEntry;

public final class ISO9660FileEntry extends AbstractBlockFileEntry {

    private ISO9660FileSystem isoFile;
    private String parentPath;
    public final int entryLength;
    public final int extAttributeLength;
    public final long dataLocation;
    public final int dataLength;
    public final int fileUnitSize;
    public final int interleaveSize;
    public final long lastModifiedTime;
    public final int flags;
    public final String identifier;
    public final String encoding;

    public ISO9660FileEntry(ISO9660FileSystem isoFile, byte[] buff, int bp, String encoding) {
        this(isoFile, null, buff, bp, encoding);
    }

    /**
     * Initialize this instance.
     */
    public ISO9660FileEntry(ISO9660FileSystem isoFile, String parentPath, byte[] buff, int bp, String encoding) {
        this.isoFile = isoFile;
        this.parentPath = parentPath;
        final int offset = bp - 1;
        this.encoding = encoding;
        this.entryLength = Util.getUInt8(buff, offset + 1);
        this.extAttributeLength = Util.getUInt8(buff, offset + 2);
        this.dataLocation = Util.getUInt32LE(buff, offset + 3);
        this.dataLength = (int) Util.getUInt32LE(buff, offset + 11);
        this.lastModifiedTime = Util.getIntDate(buff, offset + 19);
        this.flags = Util.getUInt8(buff, offset + 26);
        this.fileUnitSize = Util.getUInt8(buff, offset + 27);
        this.interleaveSize = Util.getUInt8(buff, offset + 28);
        this.identifier = getFileIdentifier(buff, offset, isDirectory(), encoding);
    }

    private final String getFileIdentifier(
            byte[] buff, int offset, boolean isDir, String encoding) {
        final int fidLength = Util.getUInt8(buff, offset + 33);
        if (isDir) {
            final int buff34 = Util.getUInt8(buff, offset + 34);
            if ((fidLength == 1) && (buff34 == 0x00)) {
                return ".";
            } else if ((fidLength == 1) && (buff34 == 0x01)) {
                return "..";
            }
        }
        try {
            final String id = Util.getDChars(buff, offset + 34, fidLength, encoding);
            final int sep2Idx = id.indexOf(Constants.SEPARATOR2);
            if (sep2Idx >= 0) {
                return id.substring(0, sep2Idx);
            } else {
                return id;
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
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

    public long getLastModified() {
        return this.lastModifiedTime;
    }

    public boolean isDirectory() {
        return (this.flags & 0x03) != 0;
    }

    public int getSize() {
        return this.dataLength;
    }

    public long getStartBlock() {
        return this.dataLocation;
    }

    public final boolean isLastEntry() {
        return (this.flags & 0x40) == 0;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public int getEntryLength() {
        return this.entryLength;
    }

    public byte[] getFileData() throws LoopyException {
        try {
            return this.isoFile.readData(this);
        } catch (IOException ex) {
            throw new LoopyException("Error reading data for entry: " + this, ex);
        }
    }
}