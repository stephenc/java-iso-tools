/*  
 *  JIIC: Java ISO Image Creator. Copyright (C) 2007-2009, Jens Hatlak <hatlak@rbg.informatik.tu-darmstadt.de>
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

package de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl;

import java.util.Stack;

import de.tu_darmstadt.informatik.rbg.hatlak.sabre.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class LogicalSectorPaddingHandler extends ChainingStreamHandler {
	private long bytesWritten;
	private Stack elements;
	private boolean padEnd;

	public LogicalSectorPaddingHandler(StructureHandler chainingStructureHandler, ContentHandler chainingContentHandler) {
		super(chainingStructureHandler, chainingContentHandler);
		bytesWritten = 0;
		elements = new Stack();
	}

	public void setPadEnd(boolean padEnd) {
		this.padEnd = padEnd;
	}

	public void startElement(Element element) throws HandlerException {
		if (element instanceof LogicalSectorElement || isSAElement(element)) {
			// Reset byte counter
			bytesWritten = 0;
		}
		elements.push(element);
		super.startElement(element);
	}

	private boolean isSAElement(Object element) {
		if (element instanceof ISO9660Element) {
			String id = (String) ((ISO9660Element) element).getId();
			if (id.equals("SA")) {
				return true;
			}
		}
		return false;
	}

	public void data(DataReference reference) throws HandlerException {
		bytesWritten += reference.getLength();
		super.data(reference);
	}

	public Fixup fixup(DataReference reference) throws HandlerException {
		bytesWritten += reference.getLength();
		return super.fixup(reference);
	}

	public void endElement() throws HandlerException {
		Object element = elements.pop();
		if (element instanceof LogicalSectorElement) {
			// Pad to one logical block
			int pad = (int) (ISO9660Constants.LOGICAL_SECTOR_SIZE - bytesWritten % ISO9660Constants.LOGICAL_SECTOR_SIZE);
			super.data(new EmptyByteArrayDataReference(pad));
		} else
		if (isSAElement(element)) {
			// Pad to 16 sectors
			int pad = (int) (16*ISO9660Constants.LOGICAL_SECTOR_SIZE - bytesWritten % 16*ISO9660Constants.LOGICAL_SECTOR_SIZE);
			super.data(new EmptyByteArrayDataReference(pad));				
		}
		super.endElement();
	}

	public void endDocument() throws HandlerException {
		if (padEnd) {
			// Pad to 150 sectors (like mkisofs -pad does)
			int pad = (int) (150*ISO9660Constants.LOGICAL_SECTOR_SIZE - bytesWritten % 16*ISO9660Constants.LOGICAL_SECTOR_SIZE);
			super.data(new EmptyByteArrayDataReference(pad));
		}
	}
}