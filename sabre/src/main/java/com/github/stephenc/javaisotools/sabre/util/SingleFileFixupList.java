/*
 * Copyright (c) 2010. Stephen Connolly
 * Copyright (c) 2006. Michael Hartle <mhartle@rbg.informatik.tu-darmstadt.de>.
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

package com.github.stephenc.javaisotools.sabre.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.github.stephenc.javaisotools.sabre.Fixup;
import com.github.stephenc.javaisotools.sabre.impl.FileFixup;

public class SingleFileFixupList {

    private static int fixupLength = 16;
    private File originalIndexFile = null;
    private RandomAccessFile indexFile = null;
    private RandomAccessFile fixedFile = null;
    private int fixupCount = 0;

    public SingleFileFixupList(RandomAccessFile fixedFile, File indexFile) throws FileNotFoundException {
        this.fixedFile = fixedFile;
        this.originalIndexFile = indexFile;
        this.indexFile = new RandomAccessFile(indexFile, "rw");
    }

    public synchronized void addLast(Fixup fixup) {
        FileFixup fileFixup = null;

        if (fixup instanceof FileFixup) {
            fileFixup = (FileFixup) fixup;
            if (this.fixedFile != null) {
                if (this.fixedFile != fileFixup.getFile()) {
                    throw new RuntimeException();
                }
            } else {
                // The first fixup file is used if undefined
                this.fixedFile = fileFixup.getFile();
            }

            try {
                this.indexFile.seek(this.fixupCount * fixupLength);
                this.indexFile.writeLong(fileFixup.getPosition());
                this.indexFile.writeLong(fileFixup.getAvailable());
                this.fixupCount++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized Fixup get(int index) {
        Fixup result = null;
        long position = 0;
        long available = 0;

        try {
            if (index < this.fixupCount) {
                this.indexFile.seek(index * fixupLength);
                position = this.indexFile.readLong();
                available = this.indexFile.readLong();
                result = new FileFixup(this.fixedFile, position, available);
            } else {
                System.out.println("Autsch");
                // throw new IndexOutOfBoundsException();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public void delete() throws IOException {
        this.indexFile.close();
        this.originalIndexFile.delete();
    }
}
