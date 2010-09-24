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

import java.io.FileNotFoundException;

import de.tu_darmstadt.informatik.rbg.hatlak.eltorito.impl.*;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.*;
import de.tu_darmstadt.informatik.rbg.hatlak.joliet.impl.*;
import de.tu_darmstadt.informatik.rbg.hatlak.rockridge.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;

public class CreateISO {
	private ISO9660RootDirectory root;
	private StreamHandler streamHandler;

	public CreateISO(StreamHandler streamHandler, ISO9660RootDirectory root) throws FileNotFoundException {
		this.streamHandler = new LogicalSectorPaddingHandler(streamHandler, streamHandler);
		this.root = root;
	}

	public void process(ISO9660Config iso9660Config, RockRidgeConfig rrConfig, JolietConfig jolietConfig, ElToritoConfig elToritoConfig) throws HandlerException {
		if (iso9660Config==null) {
			throw new NullPointerException("Cannot create ISO without ISO9660Config.");
		}
		((LogicalSectorPaddingHandler) streamHandler).setPadEnd(iso9660Config.getPadEnd());

		// Last handler added processes data first
		if (jolietConfig!=null) {
			streamHandler = new JolietHandler(streamHandler, root, jolietConfig);
		}
		if (elToritoConfig!=null) {
			streamHandler = new ElToritoHandler(streamHandler, elToritoConfig);
		}
		streamHandler = new ISO9660Handler(streamHandler, root, iso9660Config, rrConfig);
		streamHandler = new FileHandler(streamHandler, root);

		streamHandler.startDocument();

		// System Area
		streamHandler.startElement(new ISO9660Element("SA"));
		streamHandler.endElement();

		// Volume Descriptor Set
		streamHandler.startElement(new ISO9660Element("VDS"));
		streamHandler.endElement();

		// Boot Info Area
		streamHandler.startElement(new ISO9660Element("BIA"));
		streamHandler.endElement();

		// Path Table Area
		streamHandler.startElement(new ISO9660Element("PTA"));
		streamHandler.endElement();

		// Directory Records Area
		streamHandler.startElement(new ISO9660Element("DRA"));
		streamHandler.endElement();

		// Boot Data Area
		streamHandler.startElement(new ISO9660Element("BDA"));
		streamHandler.endElement();

		// File Contents Area
		streamHandler.startElement(new ISO9660Element("FCA"));
		streamHandler.endElement();

		streamHandler.endDocument();
	}
}