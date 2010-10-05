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

package com.github.stephenc.javaisotools.udflib.tools;

public class OSTAUnicode {

    public static byte[] UncompressUnicodeByte(byte UDFCompressed[]) {
        byte unicode[] = new byte[0];

        int compId = UDFCompressed[0];

        if (compId == 16) {
            unicode = new byte[UDFCompressed.length - 1];

            System.arraycopy(UDFCompressed, 1, unicode, 0, UDFCompressed.length - 1);
        } else if (compId == 8) {
            unicode = new byte[(UDFCompressed.length - 1) * 2];

            for (int i = 0; i < (UDFCompressed.length - 1); ++i) {
                unicode[i * 2] = 0;
                unicode[i * 2 + 1] = UDFCompressed[i + 1];
            }
        }

        return unicode;
    }

    public static byte[] CompressUnicodeByte(byte utf16[], int compId) {
        // skip 2 byte java utf-16 heading bytes
        byte unicode[] = new byte[utf16.length - 2];
        System.arraycopy(utf16, 2, unicode, 0, unicode.length);

        byte UDFCompressed[] = new byte[0];

        if (utf16.length == 0) {
            return UDFCompressed;
        }

        if (compId == 8) {
            UDFCompressed = new byte[unicode.length / 2 + 1];

            UDFCompressed[0] = 8;

            for (int i = 0; i < unicode.length / 2; ++i) {
                UDFCompressed[i + 1] = unicode[i * 2 + 1];
            }
        } else if (compId == 16) {
            UDFCompressed = new byte[unicode.length + 1];

            UDFCompressed[0] = 8;

            System.arraycopy(unicode, 0, UDFCompressed, 1, unicode.length);
        }

        return UDFCompressed;
    }

    public static int getBestCompressionId(byte utf16[]) {
        //	skip 2 byte java utf-16 heading bytes
        for (int i = 2; i < utf16.length; i += 2) {
            if (utf16[i] != (byte) 0x00) {
                return 16;
            }
        }
        return 8;
    }
}
