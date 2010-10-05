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

package com.github.stephenc.javaisotools.sabre.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.stephenc.javaisotools.sabre.DataReference;

public class IntegerArrayDataReference implements DataReference {

    private byte[] buffer = null;

    public IntegerArrayDataReference(int[] buffer) {
        this.buffer = new byte[buffer.length];
        for (int i = 0; i < buffer.length; i++) {
            this.buffer[i] = (byte) buffer[i];
        }
    }

    public long getLength() {
        return this.buffer.length;
    }

    public InputStream createInputStream() throws IOException {
        return new ByteArrayInputStream(this.buffer);
    }
}
