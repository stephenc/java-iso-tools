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

package de.tu_darmstadt.informatik.rbg.hatlak.joliet.impl;

import java.util.*;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.*;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.*;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.volumedescriptors.*;
import de.tu_darmstadt.informatik.rbg.hatlak.sabre.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class JolietHandler extends StandardHandler {
	private JolietConfig config;
	private LayoutHelper helper;
	private HashMap volumeFixups;
	private ISO9660Factory factory;
	
	public JolietHandler(StreamHandler streamHandler, ISO9660RootDirectory root, JolietConfig config) throws HandlerException {
		super(streamHandler, root, config);
		this.config = config;
		this.volumeFixups = new HashMap();

		checkMetadataFiles();

		// Use a copy of the original root for Joliet
		ISO9660RootDirectory jolietRoot = (ISO9660RootDirectory) root.clone();
		this.helper = new JolietLayoutHelper(this, jolietRoot);
		this.factory = new ISO9660Factory(this, config, helper, jolietRoot, volumeFixups);
		
		factory.applyNamingConventions();
	}
	
	public void startElement(Element element) throws HandlerException {
		if (element instanceof ISO9660Element) {
			String id = (String) element.getId();
			process(id);
		} else
		if (element instanceof FileElement) {
			FileElement fileElement = (FileElement) element;
			factory.doFileFixup(fileElement.getFile());
		}
		super.startElement(element);
	}
	
	private void process(String id) throws HandlerException {
		if (id.equals("VDS")) {
			doSVD();
		} else
		if (id.equals("PTA")) {
			factory.doPT(ISO9660Constants.TYPE_L_PT);
			factory.doPT(ISO9660Constants.TYPE_M_PT);
		} else
		if (id.equals("DRA")) {
			factory.doDRA();
		}
}
	
	private void doSVD() throws HandlerException {
		super.startElement(new LogicalSectorElement("SVD"));

		SupplementaryVolumeDescriptor svd = new SupplementaryVolumeDescriptor(this, helper);
		svd.setMetadata(config);
		volumeFixups.putAll(svd.doSVD());
		
		// Set Volume Flags to 0 (Unused Field)
		Fixup volumeFlags = (Fixup) volumeFixups.get("volumeFlagsFixup");
		volumeFlags.data(new ByteDataReference(0));
		volumeFlags.close();
		volumeFixups.remove("volumeFlagsFixup");
		
		// Set Escape Sequences for UCS-2 level
		Fixup escapeSequences = (Fixup) volumeFixups.get("escapeSequencesFixup");
		escapeSequences.data(config.getUCS2LevelEscapeSequence());
		escapeSequences.close();
		volumeFixups.remove("escapeSequencesFixup");
		
		super.endElement();
	}
	
	public void endDocument() throws HandlerException {
		// Write and close Volume Space Size Fixup
		Fixup volumeSpaceSizeFixup = (Fixup) volumeFixups.get("volumeSpaceSizeFixup");
		volumeSpaceSizeFixup.data(new BothWordDataReference(helper.getCurrentLocation()));
		volumeSpaceSizeFixup.close();
		volumeFixups.remove("volumeSpaceSizeFixup");
		
		super.endDocument();
	}
}
