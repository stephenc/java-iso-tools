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

package com.github.stephenc.javaisotools.iso9660.impl;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import com.github.stephenc.javaisotools.sabre.DataReference;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StreamHandler;
import com.github.stephenc.javaisotools.sabre.impl.FileFixup;
import com.github.stephenc.javaisotools.sabre.Element;
import com.github.stephenc.javaisotools.sabre.Fixup;

public class ISOImageFileHandler implements StreamHandler {

    private File file = null;
    private RandomAccessFile raFile = null;
    private DataOutputStream dataOutputStream = null;
    private long position = 0;

    /**
     * ISO Image File Handler
     *
     * @param file ISO image output file
     *
     * @throws FileNotFoundException File not found
     */
    public ISOImageFileHandler(File file) throws FileNotFoundException {
        this.file = file;
        this.raFile = new RandomAccessFile(file, "rw");
        this.dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.file)));
    }

    public void startDocument() throws HandlerException {
        // nothing to do here
    }

    public void startElement(Element element) throws HandlerException {
        // nothing to do here
    }

    public void data(DataReference reference) throws HandlerException {
        InputStream inputStream = null;
        byte[] buffer = null;
        int bytesToRead = 0;
        int bytesHandled = 0;
        int bufferLength = 65535;
        long lengthToWrite = 0;
        long length = 0;

        try {
            buffer = new byte[bufferLength];
            length = reference.getLength();
            lengthToWrite = length;
            inputStream = reference.createInputStream();
            while (lengthToWrite > 0) {
                if (lengthToWrite > bufferLength) {
                    bytesToRead = bufferLength;
                } else {
                    bytesToRead = (int) lengthToWrite;
                }

                bytesHandled = inputStream.read(buffer, 0, bytesToRead);
                if (bytesHandled == -1) {
                    throw new HandlerException("Cannot read all data from reference.");
                }

                dataOutputStream.write(buffer, 0, bytesHandled);
                lengthToWrite -= bytesHandled;
                position += bytesHandled;
            }
            dataOutputStream.flush();
        } catch (IOException e) {
            throw new HandlerException(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
            } catch (IOException e) {
            }
        }
    }

    public Fixup fixup(DataReference reference) throws HandlerException {
        Fixup fixup = null;
        fixup = new FileFixup(raFile, position, reference.getLength());
        data(reference);
        return fixup;
    }

    public long mark() throws HandlerException {
        return position;
    }

    public void endElement() throws HandlerException {
        // nothing to do here
    }

    public void endDocument() throws HandlerException {
        try {
        	this.raFile.close();
            this.dataOutputStream.close();
        } catch (IOException e) {
            throw new HandlerException(e);
        }
    }
}
