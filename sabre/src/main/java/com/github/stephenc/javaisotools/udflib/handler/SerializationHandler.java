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

package com.github.stephenc.javaisotools.udflib.handler;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StreamHandler;
import com.github.stephenc.javaisotools.sabre.impl.FileFixup;
import com.github.stephenc.javaisotools.sabre.DataReference;
import com.github.stephenc.javaisotools.sabre.Element;
import com.github.stephenc.javaisotools.sabre.Fixup;

public class SerializationHandler implements StreamHandler {

    private File myOutputFile;
    private DataOutputStream myDataOutputStream;
    private long position;

    public SerializationHandler(File outputFile)
            throws HandlerException {
        myOutputFile = outputFile;
        position = 0;
    }

    public void startDocument()
            throws HandlerException {
        position = 0;

        try {
            myDataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(myOutputFile)));
        }
        catch (IOException myIOException) {
            throw new HandlerException(myIOException);
        }
    }

    public void endDocument()
            throws HandlerException {
        try {
            myDataOutputStream.close();
        }
        catch (IOException myIOException) {
            throw new HandlerException(myIOException);
        }
    }

    public void startElement(Element myElement)
            throws HandlerException {

    }

    public void endElement()
            throws HandlerException {

    }

    public void data(DataReference myDataReference)
            throws HandlerException {
        InputStream myInputStream = null;

        try {
            int bufferLength = 32768;
            byte[] buffer = new byte[bufferLength];
            long length = myDataReference.getLength();
            long lengthToWrite = length;
            int bytesToRead = 0;
            int bytesHandled = 0;

            myInputStream = myDataReference.createInputStream();

            while (lengthToWrite > 0) {
                if (lengthToWrite > bufferLength) {
                    bytesToRead = bufferLength;
                } else {
                    bytesToRead = (int) lengthToWrite;
                }

                bytesHandled = myInputStream.read(buffer, 0, bytesToRead);

                if (bytesHandled == -1) {
                    throw new HandlerException("Cannot read all data from reference.");
                }

                myDataOutputStream.write(buffer, 0, bytesHandled);
                lengthToWrite -= bytesHandled;
                this.position += bytesHandled;
            }

            myDataOutputStream.flush();

        }
        catch (IOException myIOException) {
            throw new HandlerException(myIOException);
        }
        finally {
            try {
                if (myInputStream != null) {
                    myInputStream.close();
                    myInputStream = null;
                }
            }
            catch (IOException myIOException) {
            }
        }
    }

    public Fixup fixup(DataReference myDataReference) throws HandlerException {
        try {
            Fixup fixup =
                    new FileFixup(new RandomAccessFile(myOutputFile, "rw"), position, myDataReference.getLength());
            data(myDataReference);
            return fixup;
        } catch (FileNotFoundException e) {
            throw new HandlerException(e);
        }
    }

    public long mark()
            throws HandlerException {
        return position;
    }

}
