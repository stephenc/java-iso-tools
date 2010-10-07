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

package com.github.stephenc.javaisotools.iso9660;

import java.util.Iterator;

import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StreamHandler;
import com.github.stephenc.javaisotools.sabre.impl.ChainingStreamHandler;

public class StandardHandler extends ChainingStreamHandler {

    private ISO9660Directory root;
    private StandardConfig config;

    public StandardHandler(StreamHandler streamHandler, ISO9660Directory root, StandardConfig config) {
        super(streamHandler, streamHandler);
        this.root = root;
        this.config = config;
    }

    public void checkMetadataFiles() throws HandlerException {
        // Add files to Root Directory (if not already present)
        for (ISO9660File file : config.getFiles()) {
            // Metadata Files must conform to 8+3 naming scheme
            if (file != null && !root.getFiles().contains(file)) {
                file.enforce8plus3(true);
                root.addFile(file);
            }
        }
    }
}
