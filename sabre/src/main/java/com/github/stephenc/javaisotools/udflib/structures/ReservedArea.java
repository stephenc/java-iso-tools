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

public class ReservedArea {

    public static boolean check(RandomAccessFile myRandomAccessFile)
            throws IOException {
        byte buffer[] = new byte[16 * 2048];

        myRandomAccessFile.read(buffer);

        for (int i = 0; i < buffer.length; ++i) {
            if (buffer[i] != 0x00) {
                return false;
            }
        }

        return true;
    }

    public static void write(RandomAccessFile myRandomAccessFile)
            throws IOException {
        byte buffer[] = new byte[16 * 2048];

        myRandomAccessFile.write(buffer);
    }
}
