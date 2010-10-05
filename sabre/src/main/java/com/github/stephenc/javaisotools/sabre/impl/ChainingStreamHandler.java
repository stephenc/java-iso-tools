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
