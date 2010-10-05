package com.github.stephenc.javaisotools.sabre;

public interface Fixup extends ContentHandler {

    public void close() throws HandlerException;

    public boolean isClosed();
}
