/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (c) 2006. Björn Stickler <bjoern@stickler.de>.
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
