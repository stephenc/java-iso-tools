/*
 *	PaddingHandler.java
 *
 *	2006-07-22
 *
 *	Bj�rn Stickler <bjoern@stickler.de>
 */

package com.github.stephenc.javaisotools.udflib.handler;

import com.github.stephenc.javaisotools.sabre.ContentHandler;
import com.github.stephenc.javaisotools.sabre.impl.ChainingStreamHandler;
import com.github.stephenc.javaisotools.sabre.DataReference;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StructureHandler;
import com.github.stephenc.javaisotools.sabre.impl.ByteArrayDataReference;

public class PaddingHandler extends ChainingStreamHandler {

    private int blockSize = 2048;
    private long currentPosition = 0;

    public PaddingHandler(StructureHandler myStructureHandler, ContentHandler myContentHandler) {
        super(myStructureHandler, myContentHandler);
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public void data(DataReference myDataReference)
            throws HandlerException {
        currentPosition += myDataReference.getLength();

        super.data(myDataReference);
    }

    public void endElement()
            throws HandlerException {
        if (currentPosition % blockSize != 0) {
            int paddingLength = blockSize - (int) (currentPosition % blockSize);
            super.data(new ByteArrayDataReference(new byte[paddingLength]));

            currentPosition += paddingLength;
        }

        super.endElement();
    }

}
