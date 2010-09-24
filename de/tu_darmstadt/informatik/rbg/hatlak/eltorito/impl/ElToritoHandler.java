/*
 *  JIIC: Java ISO Image Creator. Copyright (C) 2007-2009, Jens Hatlak <hatlak@rbg.informatik.tu-darmstadt.de>, Gilles Duboscq <gilwooden@gmail.com>
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

package de.tu_darmstadt.informatik.rbg.hatlak.eltorito.impl;

import java.io.*;
import java.util.Arrays;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ISO9660File;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.LayoutHelper;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.*;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.volumedescriptors.*;
import de.tu_darmstadt.informatik.rbg.hatlak.sabre.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class ElToritoHandler extends ChainingStreamHandler {
	private ElToritoConfig config;
	private Fixup bootCatalogLocation, bootImageLocation;

	public ElToritoHandler(StreamHandler streamHandler, ElToritoConfig config) {
		super(streamHandler, streamHandler);
		this.config = config;
	}

	public void startElement(Element element) throws HandlerException {
		if (element instanceof ISO9660Element) {
			String id = (String) element.getId();
			process(id);
		}
		super.startElement(element);
	}

	private void process(String id) throws HandlerException {
		if (id.equals("VDS")) {
			doBVD();
		} else
		if (id.equals("BIA")) {
			doCatalog();
		}
		if (id.equals("BDA")) {
			doImage();
		}
	}

	private void doBVD() throws HandlerException {
		super.startElement(new LogicalSectorElement("BR"));

		LayoutHelper helper = new ElToritoLayoutHelper(this);
		BootRecord br = new BootRecord(this, helper);
		br.setMetadata(config);
		br.doBR();

		// Remember Boot System Use (absolute pointer to first sector of Boot Catalog)
		bootCatalogLocation = fixup(new LSBFWordDataReference(0));

		super.endElement();
	}

	private void doCatalog() throws HandlerException {
		super.startElement(new LogicalSectorElement("BCAT"));

		// Write and close Boot Catalog Location Fixup
		long position = mark();
		int location = (int) (position / ISO9660Constants.LOGICAL_BLOCK_SIZE);
		bootCatalogLocation.data(new LSBFWordDataReference(location));
		bootCatalogLocation.close();

		ElToritoFactory etf = new ElToritoFactory(this);

		// Validation Entry
		int platformID = config.getPlatformID();
		String idString = config.getIDString();
		etf.doValidationEntry(platformID, idString);

		// Initial/Default Entry
		boolean bootable = config.getBootable();
		int bootMediaType = config.getBootMediaType();
		int loadSegment = config.getLoadSegment();
		int systemType = config.getSystemType();
		int sectorCount = config.getSectorCount();
		bootImageLocation = etf.doDefaultEntry(bootable, bootMediaType, loadSegment, systemType, sectorCount);

		super.endElement();
	}

	private void doImage() throws HandlerException {
		super.startElement(new LogicalSectorElement("BIMG"));

		// Write and close Boot Image Location Fixup
		long position = mark();
		int location = (int) (position / ISO9660Constants.LOGICAL_BLOCK_SIZE);
		bootImageLocation.data(new LSBFWordDataReference(location));
		bootImageLocation.close();

		if (config.getGenBootInfoTable()) {
			this.genBootInfoTable(location);
		}

		// Write Boot Image
		FileDataReference fdr = new FileDataReference(config.getBootImage());
		data(fdr);

		super.endElement();
	}

	private void genBootInfoTable(int lba) throws HandlerException {
		// Patch the Boot Image: write 56 byte boot information table
		// (cf. man mkisofs, section EL TORITO BOOT INFORMATION TABLE)
		try {
			String orgName = config.getBootImage().getAbsolutePath();
			File orgFile = new File(orgName);

			// Compute the checksum over all 32-bit words starting at byte offset 64
			FileInputStream fis = new FileInputStream(orgFile);
			fis.skip(64);
			long checksum = 0;
			byte[] buffer = new byte[0x2000];
			while (fis.available() > 0) {
				int len = fis.read(buffer);
				for (int i = 0; i < len;) {
					long temp = buffer[i++]&0xFF;
					temp |= (buffer[i++]<<8)&0xFF00;
					temp |= (buffer[i++]<<16)&0xFF0000;
					temp |= (buffer[i++]<<24)&0xFF000000l;
					checksum += temp;
				}
			}
			fis.close();

			// Create the patched file
			fis = new FileInputStream(orgFile);
			File patchedFile = new File(orgName + ".mod");
			FileOutputStream fos = new FileOutputStream(patchedFile);

			// Copy first 8 bytes
			buffer = new byte[8];
			fis.read(buffer);
			fos.write(buffer);

			// Read 56 bytes and init the buffer with as many 0 bytes
			buffer = new byte[56];
			fis.read(buffer);
			Arrays.fill(buffer, (byte)0);

			// Write boot info tables fields
			int i = 0;
			// PVD LBA (always 16), 7.3.1 format
			int pvd = 16;
			buffer[i++] = (byte) (pvd&0xFF);
			buffer[i++] = (byte) ((pvd>>8)&0xFF);
			buffer[i++] = (byte) ((pvd>>16)&0xFF);
			buffer[i++] = (byte) ((pvd>>24)&0xFF);
			// Boot file LBA, 7.3.1 format
			buffer[i++] = (byte) (lba&0xFF);
			buffer[i++] = (byte) ((lba>>8)&0xFF);
			buffer[i++] = (byte) ((lba>>16)&0xFF);
			buffer[i++] = (byte) ((lba>>24)&0xFF);
			// Boot file length in bytes, 7.3.1 format
			int len = (int) config.getBootImage().getAbsoluteFile().length();
			buffer[i++] = (byte) (len&0xFF);
			buffer[i++] = (byte) ((len>>8)&0xFF);
			buffer[i++] = (byte) ((len>>16)&0xFF);
			buffer[i++] = (byte) ((len>>24)&0xFF);
			// 32-bit checksum, 7.3.1 format
			buffer[i++] = (byte) (checksum&0xFF);
			buffer[i++] = (byte) ((checksum>>8)&0xFF);
			buffer[i++] = (byte) ((checksum>>16)&0xFF);
			buffer[i++] = (byte) ((checksum>>24)&0xFF);
			// Write 38 byte buffer
			fos.write(buffer);

			// Write the rest
			buffer = new byte[0x2000];
			while (fis.available() > 0) {
				len = fis.read(buffer);
				fos.write(buffer, 0, len);
			}
			fis.close();
			fos.close();

			// Replace original file by patched one
			orgFile.delete();
			orgFile = new File(orgName);
			patchedFile.renameTo(orgFile);
			config.setBootImage(new ISO9660File(orgFile));

			System.out.println("Patched boot image at " + orgFile.getPath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
