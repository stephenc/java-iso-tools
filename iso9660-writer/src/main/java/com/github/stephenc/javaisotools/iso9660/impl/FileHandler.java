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

package com.github.stephenc.javaisotools.iso9660.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.github.stephenc.javaisotools.iso9660.ISO9660Directory;
import com.github.stephenc.javaisotools.iso9660.ISO9660File;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StreamHandler;
import com.github.stephenc.javaisotools.sabre.impl.FileDataReference;
import com.github.stephenc.javaisotools.sabre.Element;
import com.github.stephenc.javaisotools.sabre.impl.ChainingStreamHandler;

public class FileHandler extends ChainingStreamHandler {

    private ISO9660RootDirectory root;

    public FileHandler(StreamHandler streamHandler, ISO9660RootDirectory root) {
        super(streamHandler, streamHandler);
        this.root = root;
    }

    public void startElement(Element element) throws HandlerException {
        if (element instanceof ISO9660Element) {
            String id = (String) element.getId();
            process(id);
        }
        super.startElement(element);
    }

    private void process(String id) throws HandlerException {
        if (id.equals("FCA")) {
            doFCA();
        }
    }

    private void doFCA() throws HandlerException {
        doFCADirs(root);

        Iterator<ISO9660Directory> it = root.sortedIterator();
        while (it.hasNext()) {
            ISO9660Directory dir = it.next();
            doFCADirs(dir);
        }
    }

    private void doFCADirs(ISO9660Directory dir) throws HandlerException {
        for (ISO9660File file : dir.getFiles()) {
            doFile(file);
        }
    }

    private void doFile(ISO9660File file) throws HandlerException {
        super.startElement(new FileElement(file));

        data(file.getDataReference());

        super.endElement();
    }
}
