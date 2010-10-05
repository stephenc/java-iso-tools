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

package com.github.stephenc.javaisotools.rockridge.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.stephenc.javaisotools.iso9660.ISO9660File;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.iso9660.FilenameDataReference;
import com.github.stephenc.javaisotools.iso9660.ISO9660Directory;

public class RockRidgeFilenameDataReference extends FilenameDataReference {

    public RockRidgeFilenameDataReference(ISO9660Directory dir) throws HandlerException {
        super(dir);
    }

    public RockRidgeFilenameDataReference(ISO9660File file) throws HandlerException {
        super(file);
        setName(file.getName());
    }

    public RockRidgeFilenameDataReference(String part) throws HandlerException {
        super(part);
    }

    public long getLength() {
        return getName().length();
    }

    public InputStream createInputStream() throws IOException {
        return new ByteArrayInputStream(getName().getBytes("ISO-8859-1"));
    }
}
