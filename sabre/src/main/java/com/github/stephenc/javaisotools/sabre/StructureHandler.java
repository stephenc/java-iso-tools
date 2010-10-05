package com.github.stephenc.javaisotools.sabre;

public interface StructureHandler {

    public void startDocument() throws HandlerException;

    public void startElement(Element element) throws HandlerException;

    public void endElement() throws HandlerException;

    public void endDocument() throws HandlerException;
}
