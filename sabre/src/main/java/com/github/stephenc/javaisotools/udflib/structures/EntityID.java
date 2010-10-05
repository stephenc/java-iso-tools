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

package com.github.stephenc.javaisotools.udflib.structures;

import java.io.IOException;
import java.io.RandomAccessFile;

public class EntityID {

    public byte Flags;                    // Uint8
    public byte Identifier[];            // char[23]
    public byte IdentifierSuffix[];        // char[8]

    public EntityID() {
        Identifier = new byte[23];
        IdentifierSuffix = new byte[8];
    }

    public void setIdentifier(String identifier)
            throws Exception {
        if (identifier.length() > 23) {
            throw new Exception("error: identifier length exceeds maximum length of 23 characters");
        }

        Identifier = new byte[23];

        for (int i = 0; i < identifier.length() && i < 23; ++i) {
            Identifier[i] = (byte) identifier.charAt(i);
        }
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        Flags = myRandomAccessFile.readByte();

        Identifier = new byte[23];
        myRandomAccessFile.read(Identifier);

        IdentifierSuffix = new byte[8];
        myRandomAccessFile.read(IdentifierSuffix);
    }

    public void write(RandomAccessFile myRandomAccessFile)
            throws IOException {
        myRandomAccessFile.write(getBytes());
    }

    public byte[] getBytes() {
        byte rawBytes[] = new byte[32];

        rawBytes[0] = Flags;
        System.arraycopy(Identifier, 0, rawBytes, 1, Identifier.length);
        System.arraycopy(IdentifierSuffix, 0, rawBytes, Identifier.length + 1, IdentifierSuffix.length);

        return rawBytes;
    }

}
