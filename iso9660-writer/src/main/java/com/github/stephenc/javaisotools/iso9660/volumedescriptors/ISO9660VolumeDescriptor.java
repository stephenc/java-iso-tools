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

package com.github.stephenc.javaisotools.iso9660.volumedescriptors;

import com.github.stephenc.javaisotools.iso9660.LayoutHelper;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Constants;
import com.github.stephenc.javaisotools.sabre.impl.ByteArrayDataReference;
import com.github.stephenc.javaisotools.sabre.impl.ByteDataReference;
import com.github.stephenc.javaisotools.sabre.StreamHandler;

public abstract class ISO9660VolumeDescriptor {

    StreamHandler streamHandler;
    int type;
    LayoutHelper helper;

    public ISO9660VolumeDescriptor(StreamHandler streamHandler, int type, LayoutHelper helper) {
        this.streamHandler = streamHandler;
        this.type = type;
        this.helper = helper;
    }

    ByteDataReference getType() {
        return new ByteDataReference(type);
    }

    ByteArrayDataReference getStandardId() {
        return new ByteArrayDataReference(ISO9660Constants.STD_ID.getBytes());
    }

    ByteDataReference getVDVersion() {
        return new ByteDataReference(ISO9660Constants.VDV);
    }
}
