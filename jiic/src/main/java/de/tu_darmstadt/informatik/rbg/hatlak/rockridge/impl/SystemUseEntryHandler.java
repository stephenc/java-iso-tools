/*  
 *  JIIC: Java ISO Image Creator. Copyright (C) 2007, Jens Hatlak <hatlak@rbg.informatik.tu-darmstadt.de>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package de.tu_darmstadt.informatik.rbg.hatlak.rockridge.impl;

import java.util.Stack;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class SystemUseEntryHandler extends ChainingStreamHandler {
	private Stack elements;
	private int length = 0;
	private Fixup lengthFixup;

	public SystemUseEntryHandler(StructureHandler chainingStructureHandler, ContentHandler chainingContentHandler) {
		super(chainingStructureHandler, chainingContentHandler);
		this.elements = new Stack();
	}

	public void startElement(Element element) throws HandlerException {
		elements.push(element);
		if (element instanceof SystemUseEntryElement) {
			SystemUseEntryElement sue = (SystemUseEntryElement) element;

			// Reset byte counter
			length = 0;

			// Signature Word
			data(new ByteArrayDataReference(sue.getSignatureWord()));

			// Length (including Signature Word, Length, Version and Data)
			lengthFixup = fixup(new ByteDataReference(0));

			// Version
			data(new ByteDataReference(sue.getVersion()));
		}

		super.startElement(element);
	}

	public void data(DataReference reference) throws HandlerException {
		length += reference.getLength();
		super.data(reference);
	}

	public Fixup fixup(DataReference reference) throws HandlerException {
		length += reference.getLength();
		return super.fixup(reference);
	}

	public void endElement() throws HandlerException {
		Element element = (Element) elements.pop();
		if (element instanceof SystemUseEntryElement) {
			// Write and close Entry Length Fixup
			if (length > 255) {
				throw new RuntimeException("Invalid System Use Entry length: " + length);
			}
			lengthFixup.data(new ByteDataReference(length));
			lengthFixup.close();
		}
		super.endElement();
	}
}
