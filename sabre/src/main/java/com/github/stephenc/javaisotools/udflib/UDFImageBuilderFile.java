/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (c) 2006. Björn Stickler <bjoern@stickler.de>.
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

package com.github.stephenc.javaisotools.udflib;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class UDFImageBuilderFile implements Comparable<UDFImageBuilderFile> {

    private String identifier = null;
    private File sourceFile = null;
    private ArrayList<UDFImageBuilderFile> childs;
    private FileType fileType;
    private Calendar AccessTime;
    private Calendar AttributeTime;
    private Calendar CreationTime;
    private Calendar ModificationTime;
    private int FileLinkCount = 1;
    private UDFImageBuilderFile parent;

    public enum FileType {

        File,
        Directory,
        // Symlink			// TODO: maybe support symlinks
    }

    public UDFImageBuilderFile(File sourceFile)
            throws Exception {
        childs = new ArrayList<UDFImageBuilderFile>();

        identifier = sourceFile.getName();
        this.sourceFile = sourceFile;

        // TODO: better way to get real access time
        AccessTime = Calendar.getInstance();

        // TODO: better way to get real attribute time
        AttributeTime = Calendar.getInstance();
        AttributeTime.setTimeInMillis(sourceFile.lastModified());

        // TODO: better way to get real creation time
        CreationTime = Calendar.getInstance();
        CreationTime.setTimeInMillis(sourceFile.lastModified());

        ModificationTime = Calendar.getInstance();
        ModificationTime.setTimeInMillis(sourceFile.lastModified());

        if (sourceFile.isDirectory()) {
            fileType = FileType.Directory;

            File childFiles[] = sourceFile.listFiles();

            for (int i = 0; i < childFiles.length; ++i) {
                addChild(childFiles[i]);
            }
        } else {
            fileType = FileType.File;
        }
    }

    public UDFImageBuilderFile(String directoryIdentifier) {
        childs = new ArrayList<UDFImageBuilderFile>();

        AccessTime = Calendar.getInstance();
        AttributeTime = Calendar.getInstance();
        CreationTime = Calendar.getInstance();
        ModificationTime = Calendar.getInstance();

        identifier = directoryIdentifier;
        fileType = FileType.Directory;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void addChild(UDFImageBuilderFile childUDFImageBuilderFile)
            throws Exception {
        if (this.fileType != FileType.Directory) {
            throw new Exception("error: trying to add child file to non-directory file");
        }

        if (this.getChild(childUDFImageBuilderFile.identifier) != null) {
            throw new Exception("error: trying to add child file with an already existing identifer");
        }

        if (childUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.Directory) {
            FileLinkCount++;
        }

        childUDFImageBuilderFile.setParent(this);

        childs.add(childUDFImageBuilderFile);
    }

    public void addChild(File childFile)
            throws Exception {
        if (this.fileType != FileType.Directory) {
            throw new Exception("error: trying to add child file to non-directory file");
        }

        if (this.getChild(childFile.getName()) != null) {
            throw new Exception("error: trying to add child file with an already existing identifer");
        }

        UDFImageBuilderFile childUDFImageBuilderFile = new UDFImageBuilderFile(childFile);

        if (childUDFImageBuilderFile.getFileType() == UDFImageBuilderFile.FileType.Directory) {
            FileLinkCount++;
        }

        childUDFImageBuilderFile.setParent(this);

        childs.add(childUDFImageBuilderFile);
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getFileLinkCount() {
        return FileLinkCount;
    }

    public int compareTo(UDFImageBuilderFile myUDFImageBuilderFile) {
        return identifier.compareTo(myUDFImageBuilderFile.identifier);
    }

    public void removeChild(String identifier) {
        for (int i = 0; i < childs.size(); ++i) {
            UDFImageBuilderFile myUDFImageBuilderFile = (UDFImageBuilderFile) childs.get(i);
            if (myUDFImageBuilderFile.identifier == identifier) {
                childs.remove(i);
                break;
            }
        }
    }

    public UDFImageBuilderFile getChild(String identifier) {
        for (int i = 0; i < childs.size(); ++i) {
            UDFImageBuilderFile myUDFImageBuilderFile = (UDFImageBuilderFile) childs.get(i);
            if (myUDFImageBuilderFile.identifier == identifier) {
                return myUDFImageBuilderFile;
            }
        }
        return null;
    }

    public UDFImageBuilderFile[] getChilds() {
        Collections.sort(childs);

        return (UDFImageBuilderFile[]) childs.toArray(new UDFImageBuilderFile[childs.size()]);
    }

    public Calendar getModificationTime() {
        return ModificationTime;
    }

    public Calendar getAccessTime() {
        return AccessTime;
    }

    public Calendar getCreationTime() {
        return CreationTime;
    }

    public Calendar getAttributeTime() {
        return AttributeTime;
    }

    public long getFileLength() {
        if (sourceFile != null) {
            return sourceFile.length();
        }
        return 0;
    }

    public void readFileData(byte buffer[])
            throws IOException {
        if (sourceFile != null) {
            RandomAccessFile sourceRandomAccessFile = new RandomAccessFile(sourceFile, "r");
            sourceRandomAccessFile.read(buffer);
            sourceRandomAccessFile.close();
        }
    }

    public long getDirectoryCount() {
        long directoryCount = 0;

        if (fileType == FileType.Directory) {
            directoryCount++;

            for (int i = 0; i < childs.size(); ++i) {
                UDFImageBuilderFile childUDFImageBuilderFile = childs.get(i);
                if (childUDFImageBuilderFile.getFileType() == FileType.Directory) {
                    directoryCount += childUDFImageBuilderFile.getDirectoryCount();
                }
            }
        }

        return directoryCount;
    }

    public long getFileCount() {
        long fileCount = 0;

        if (fileType == FileType.Directory) {
            for (int i = 0; i < childs.size(); ++i) {
                UDFImageBuilderFile childUDFImageBuilderFile = childs.get(i);
                if (childUDFImageBuilderFile.getFileType() == FileType.Directory) {
                    fileCount += childUDFImageBuilderFile.getFileCount();
                } else if (childUDFImageBuilderFile.getFileType() == FileType.File) {
                    fileCount++;
                }
            }
        } else if (fileType == FileType.File) {
            fileCount = 1;
        }

        return fileCount;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public UDFImageBuilderFile getParent() {
        return parent;
    }

    public void setParent(UDFImageBuilderFile parent) {
        this.parent = parent;
    }

}
