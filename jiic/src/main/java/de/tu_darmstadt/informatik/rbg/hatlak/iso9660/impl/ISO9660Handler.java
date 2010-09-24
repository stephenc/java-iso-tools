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

package de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl;

import java.util.*;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.*;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.volumedescriptors.*;
import de.tu_darmstadt.informatik.rbg.hatlak.rockridge.impl.*;
import de.tu_darmstadt.informatik.rbg.hatlak.sabre.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;

public class ISO9660Handler extends StandardHandler {
	private Stack elements;
	private ISO9660Config config;
	private LayoutHelper helper;
	private HashMap volumeFixups;
	private ISO9660Factory factory;
	
	public ISO9660Handler(StreamHandler streamHandler, ISO9660RootDirectory root, ISO9660Config config, RockRidgeConfig rrConfig) throws HandlerException {
		super(streamHandler, root, config);
		this.elements = new Stack();
		this.config = config;
		this.volumeFixups = new HashMap();

		checkMetadataFiles();

		// Use a copy of the original root for ISO 9660
		ISO9660RootDirectory isoRoot = (ISO9660RootDirectory) root.clone();
		this.helper = new ISO9660LayoutHelper(this, isoRoot);
		
		if (rrConfig!=null) {
			this.factory = new ISO9660RockRidgeFactory(this, config, helper, root, isoRoot, volumeFixups);
		} else {
			this.factory = new ISO9660Factory(this, config, helper, isoRoot, volumeFixups);
		}

		if (config.dirDepthRestrictedTo8()) {
			factory.relocateDirectories();
		}
		
		factory.applyNamingConventions();
	}
	
	public void startElement(Element element) throws HandlerException {
		elements.push(element);
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
			doPVD();
		} else
		if (id.equals("PTA")) {
			factory.doPT(ISO9660Constants.TYPE_L_PT);
			factory.doPT(ISO9660Constants.TYPE_M_PT);
		} else
		if (id.equals("DRA")) {
			factory.doDRA();
		}
	}
	
	private void doPVD() throws HandlerException {
		super.startElement(new LogicalSectorElement("PVD"));

		PrimaryVolumeDescriptor pvd = new PrimaryVolumeDescriptor(this, helper);
		pvd.setMetadata(config);
		volumeFixups.putAll(pvd.doPVD());
		
		super.endElement();
	}
	
	public void endElement() throws HandlerException {
		Element element = (Element) elements.pop();
		if (element instanceof ISO9660Element) {
			String id = (String) element.getId();
			if (id.equals("VDS")) {
				// Add VDST
				doVDST();
			}
		}
		super.endElement();
	}
	
	private void doVDST() throws HandlerException {
		super.startElement(new LogicalSectorElement("VDST"));
		VolumeDescriptorSetTerminator vdst = new VolumeDescriptorSetTerminator(this, helper);
		vdst.doVDST();
		super.endElement();
	}
	
	public void endDocument() throws HandlerException {
		// Write and close Empty File Fixups
		factory.doEmptyFileFixups();
		
		// Write and close Volume Space Size Fixup
		Fixup volumeSpaceSizeFixup = (Fixup) volumeFixups.get("volumeSpaceSizeFixup");
		volumeSpaceSizeFixup.data(new BothWordDataReference(helper.getCurrentLocation()));
		volumeSpaceSizeFixup.close();
		volumeFixups.remove("volumeSpaceSizeFixup");
		
		super.endDocument();
	}
}
