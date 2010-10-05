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

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import com.github.stephenc.javaisotools.sabre.ContentHandler;
import com.github.stephenc.javaisotools.sabre.Element;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.impl.ByteArrayDataReference;
import com.github.stephenc.javaisotools.sabre.impl.ChainingStreamHandler;
import com.github.stephenc.javaisotools.udflib.structures.Tag;
import com.github.stephenc.javaisotools.udflib.tools.BinaryTools;
import com.github.stephenc.javaisotools.udflib.tools.Checksum;
import com.github.stephenc.javaisotools.udflib.SabreUDFElement.UDFElementType;
import com.github.stephenc.javaisotools.sabre.DataReference;
import com.github.stephenc.javaisotools.sabre.StructureHandler;

public class DescriptorTagHandler extends ChainingStreamHandler {

    protected Stack<Element> elementStack;
    protected Stack<DataReference> dataReferenceStack;

    public DescriptorTagHandler(StructureHandler myStructureHandler, ContentHandler myContentHandler) {
        super(myStructureHandler, myContentHandler);

        elementStack = new Stack<Element>();
        dataReferenceStack = new Stack<DataReference>();
    }

    public void startElement(Element myElement)
            throws HandlerException {
        elementStack.push(myElement);
        super.startElement(myElement);
    }

    public void endElement()
            throws HandlerException {
        Element myElement = elementStack.pop();

        if (myElement.getId() == UDFElementType.DescriptorTag) {
            createAndPassDescriptorTag();
        }

        super.endElement();
    }

    private void createAndPassDescriptorTag()
            throws HandlerException {
        InputStream myInputStream = null;
        Tag descriptorTag = new Tag();
        byte[] payload = new byte[0];

        try {
            DataReference myDataReference = dataReferenceStack.pop();
            myInputStream = myDataReference.createInputStream();
            payload = BinaryTools.readByteArray(myInputStream, (int) myDataReference.getLength());
            myInputStream.close();
            myInputStream = null;

            myInputStream = dataReferenceStack.pop().createInputStream();
            descriptorTag.DescriptorVersion = (int) BinaryTools.readUInt32AsLong(myInputStream);
            myInputStream.close();
            myInputStream = null;

            myInputStream = dataReferenceStack.pop().createInputStream();
            descriptorTag.TagSerialNumber = (int) BinaryTools.readUInt32AsLong(myInputStream);
            myInputStream.close();
            myInputStream = null;

            myInputStream = dataReferenceStack.pop().createInputStream();
            descriptorTag.TagLocation = BinaryTools.readUInt32AsLong(myInputStream);
            myInputStream.close();
            myInputStream = null;

            myInputStream = dataReferenceStack.pop().createInputStream();
            descriptorTag.TagIdentifier = (int) BinaryTools.readUInt32AsLong(myInputStream);
            myInputStream.close();
            myInputStream = null;

        }
        catch (IOException myIOException) {
            throw new HandlerException(myIOException);
        }
        finally {
            if (myInputStream != null) {
                try {
                    myInputStream.close();
                }
                catch (IOException myIOException) {
                }
            }
        }

        descriptorTag.DescriptorCRCLength = payload.length;
        descriptorTag.DescriptorCRC = Checksum.cksum(payload);

        super.data(new ByteArrayDataReference(descriptorTag.getBytes()));
        super.data(new ByteArrayDataReference(payload));
    }

    public void data(DataReference myDataReference)
            throws HandlerException {
        if ((elementStack.size() != 0) && elementStack.peek().getId() == UDFElementType.DescriptorTag) {
            dataReferenceStack.push(myDataReference);
        } else {
            super.data(myDataReference);
        }
    }

}
