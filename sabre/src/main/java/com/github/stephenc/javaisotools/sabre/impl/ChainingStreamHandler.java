/*
 * Copyright (c) 2010. Stephen Connolly.
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

import com.github.stephenc.javaisotools.sabre.Fixup;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StreamHandler;
import com.github.stephenc.javaisotools.sabre.ContentHandler;
import com.github.stephenc.javaisotools.sabre.DataReference;
import com.github.stephenc.javaisotools.sabre.Element;
import com.github.stephenc.javaisotools.sabre.StructureHandler;

public class ChainingStreamHandler implements StreamHandler {

    private StructureHandler chainedStructureHandler = null;
    private ContentHandler chainedContentHandler = null;

    public ChainingStreamHandler(StructureHandler chainedStructureHandler, ContentHandler chainedContentHandler) {
        this.chainedStructureHandler = chainedStructureHandler;
        this.chainedContentHandler = chainedContentHandler;
    }

    public void startDocument() throws HandlerException {
        // Call chained structure handler
        if (this.chainedStructureHandler != null) {
            this.chainedStructureHandler.startDocument();
        }
    }

    public void startElement(Element element) throws HandlerException {
        // Call chained structure handler
        if (this.chainedStructureHandler != null) {
            this.chainedStructureHandler.startElement(element);
        }
    }

    public void data(DataReference reference) throws HandlerException {
        // Call chained content handler
        if (this.chainedContentHandler != null) {
            this.chainedContentHandler.data(reference);
        }
    }

    public Fixup fixup(DataReference reference) throws HandlerException {
        Fixup fixup = null;

        // Call chained content handler
        if (this.chainedContentHandler != null) {
            fixup = this.chainedContentHandler.fixup(reference);
        }

        return fixup;
    }

    public long mark() throws HandlerException {
        long mark = -1;

        if (this.chainedContentHandler != null) {
            mark = this.chainedContentHandler.mark();
        }

        return mark;
    }

    public void endElement() throws HandlerException {
        // Call chained structure handler
        if (this.chainedStructureHandler != null) {
            this.chainedStructureHandler.endElement();
        }
    }

    public void endDocument() throws HandlerException {
        // Call chained structure handler
        if (this.chainedStructureHandler != null) {
            this.chainedStructureHandler.endDocument();
        }
    }
}
