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

public class CharSpec {

    public byte CharacterSetType;        // Uint8
    public byte CharacterSetInfo[];        // byte[63]

    public CharSpec() {
        CharacterSetType = 0;
        CharacterSetInfo = new byte[63];

        CharacterSetInfo[0] = 'O';
        CharacterSetInfo[1] = 'S';
        CharacterSetInfo[2] = 'T';
        CharacterSetInfo[3] = 'A';
        CharacterSetInfo[4] = ' ';
        CharacterSetInfo[5] = 'C';
        CharacterSetInfo[6] = 'o';
        CharacterSetInfo[7] = 'm';
        CharacterSetInfo[8] = 'p';
        CharacterSetInfo[9] = 'r';
        CharacterSetInfo[10] = 'e';
        CharacterSetInfo[11] = 's';
        CharacterSetInfo[12] = 's';
        CharacterSetInfo[13] = 'e';
        CharacterSetInfo[14] = 'd';
        CharacterSetInfo[15] = ' ';
        CharacterSetInfo[16] = 'U';
        CharacterSetInfo[17] = 'n';
        CharacterSetInfo[18] = 'i';
        CharacterSetInfo[19] = 'c';
        CharacterSetInfo[20] = 'o';
        CharacterSetInfo[21] = 'd';
        CharacterSetInfo[22] = 'e';
    }

    public void read(RandomAccessFile myRandomAccessFile)
            throws IOException {
        CharacterSetType = myRandomAccessFile.readByte();

        CharacterSetInfo = new byte[63];
        myRandomAccessFile.read(CharacterSetInfo);
    }

    public void write(RandomAccessFile myRandomAccessFile)
            throws IOException {
        myRandomAccessFile.write(getBytes());
    }

    public byte[] getBytes() {
        byte rawBytes[] = new byte[64];

        rawBytes[0] = CharacterSetType;
        System.arraycopy(CharacterSetInfo, 0, rawBytes, 1, CharacterSetInfo.length);

        return rawBytes;
    }
}
