/*
 * Copyright (c) 2010. Stephen Connolly
 * Copyright (C) 2006. Michael Hartle <mhartle@rbg.informatik.tu-darmstadt.de>.
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

package com.github.stephenc.javaisotools.sabre;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataReferenceHelper {

    public static void transfer(DataReference reference, OutputStream outputStream) throws IOException {
        InputStream inputStream = null;
        byte[] buffer = null;
        int bytesToRead = 0;
        int bytesHandled = 0;
        int bufferLength = 65535;
        long lengthToWrite = 0;
        long length = 0;

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
                throw new IOException();
            }

            outputStream.write(buffer, 0, bytesHandled);
            lengthToWrite -= bytesHandled;
        }
        outputStream.flush();
    }
}