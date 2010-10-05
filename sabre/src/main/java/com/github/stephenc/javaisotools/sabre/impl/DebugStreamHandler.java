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

import com.github.stephenc.javaisotools.sabre.ContentHandler;
import com.github.stephenc.javaisotools.sabre.DataReference;
import com.github.stephenc.javaisotools.sabre.Element;
import com.github.stephenc.javaisotools.sabre.Fixup;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StructureHandler;

public class DebugStreamHandler extends ChainingStreamHandler {

    private long position = 0;

    public DebugStreamHandler(StructureHandler chainedStructureHandler, ContentHandler chainedContentHandler) {
        super(chainedStructureHandler, chainedContentHandler);
    }

    public void startDocument() throws HandlerException {
        // Show what is happening
        System.out.println("document starts");

        // Delegate to super class
        super.startDocument();
    }

    public void startElement(Element element) throws HandlerException {
        // Show what is happening
        System.out.println("node(" + element + ") @" + position);
        this.position += 8;

        // Delegate to super class
        super.startElement(element);
    }

    public void data(DataReference reference) throws HandlerException {
        long length = 0;

        // Show what is happening
        length = reference.getLength();
        System.out.println("data @" + this.position + " for " + length);
        this.position += length;

        // Delegate to super class
        super.data(reference);
    }

    public Fixup fixup(DataReference reference) throws HandlerException {
        Fixup fixup = null;
        long length = 0;

        // Show what is happening
        length = reference.getLength();
        if (length == -1) {
            throw new HandlerException("Cannot fixup unknown length.");
        }
        System.out.println("fixup @" + this.position + " for " + length);

        // Delegate to super class
        fixup = super.fixup(reference);

        return fixup;
    }

    public void endElement() throws HandlerException {
        super.endElement();
    }

    public void endDocument() throws HandlerException {
        super.endDocument();
    }

}
