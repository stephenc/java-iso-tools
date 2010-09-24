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

import java.util.HashMap;

import de.tu_darmstadt.informatik.rbg.hatlak.sabre.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class SUSPFactory {
	public static final int CE_ENTRY_LENGTH = 28;
	StreamHandler streamHandler;
	
	public SUSPFactory(StreamHandler streamHandler) {
		this.streamHandler = new SystemUseEntryHandler(streamHandler, streamHandler);
	}
	
	public HashMap doCEEntry() throws HandlerException {
		HashMap memory = new HashMap();
		streamHandler.startElement(new SystemUseEntryElement("CE", 1));
		
		Fixup ceLocationFixup = streamHandler.fixup(new BothWordDataReference(0));
		memory.put("ceLocationFixup", ceLocationFixup);
		
		Fixup ceOffsetFixup = streamHandler.fixup(new BothWordDataReference(0));
		memory.put("ceOffsetFixup", ceOffsetFixup);
		
		Fixup ceLengthFixup = streamHandler.fixup(new BothWordDataReference(0));
		memory.put("ceLengthFixup", ceLengthFixup);
		
		streamHandler.endElement();
		return memory;
	}
	
	public void doPDEntry(int paddingLength) throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("PD", 1));
		streamHandler.data(new EmptyByteArrayDataReference(paddingLength));
		streamHandler.endElement();
	}
	
	public void doSPEntry(int skipBytes) throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("SP", 1));
		streamHandler.data(new ByteDataReference(0xBE));
		streamHandler.data(new ByteDataReference(0xEF));
		streamHandler.data(new ByteDataReference(skipBytes));
		streamHandler.endElement();
	}
	
	public void doSTEntry() throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("ST", 1));
		streamHandler.endElement();
	}
	
	public void doEREntry(DataReference id, DataReference descriptor, DataReference source, int version) throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("ER", 1));
		streamHandler.data(new ByteDataReference(id.getLength()));
		streamHandler.data(new ByteDataReference(descriptor.getLength()));
		streamHandler.data(new ByteDataReference(source.getLength()));
		streamHandler.data(new ByteDataReference(version));
		streamHandler.data(id);
		if (descriptor.getLength() > 0) {
			streamHandler.data(descriptor);
		}
		streamHandler.data(source);
		streamHandler.endElement();
	}
	
	public void doEREntry(DataReference id, DataReference source, int version) throws HandlerException {
		doEREntry(id, new EmptyByteArrayDataReference(0), source, version);
	}
	
	public void doESEntry(int sequenceNumber) throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("ES", 1));
		streamHandler.data(new ByteDataReference(sequenceNumber));
		streamHandler.endElement();
	}
}
