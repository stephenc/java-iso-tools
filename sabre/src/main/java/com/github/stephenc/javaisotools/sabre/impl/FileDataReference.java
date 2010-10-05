/*
 * Copyright (c) 2010. Stephen Connolly.
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

package com.github.stephenc.javaisotools.sabre.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.stephenc.javaisotools.sabre.DataReference;
import com.github.stephenc.javaisotools.sabre.io.LimitingInputStream;

public class FileDataReference implements DataReference {

    private File file = null;
    private long position = 0;
    private long length = 0;

    public FileDataReference(File file) {
        this.file = file;
        this.position = 0;
        this.length = -1;
    }

    public FileDataReference(File file, int position, int length) {
        this.file = file;
        this.position = position;
        this.length = length;
    }

    public long getLength() {
        long length = 0;

        if (this.length == -1) {
            length = this.file.length();
        } else {
            length = this.length;
        }

        return length;
    }

    public InputStream createInputStream() throws IOException {
        InputStream fileInputStream = null;

        fileInputStream = new FileInputStream(file);

        // Reposition input stream, if necessary
        if (this.position > 0) {
            long skipped = 0;
            // System.out.print("Skipping to " + position + "/" + this.file.length() + " ");
            while (skipped != this.position) {
                skipped = fileInputStream.skip(this.position - skipped);
            }
        }

        // Limit input stream, if necessary
        if (this.length != -1) {
            // System.out.println("Limiting to " + this.length + " ");
            fileInputStream = new LimitingInputStream(fileInputStream, (int) this.length);
        }

        // System.out.println();

        // Buffer input stream
        fileInputStream = new BufferedInputStream(fileInputStream);

        return fileInputStream;
    }

}
