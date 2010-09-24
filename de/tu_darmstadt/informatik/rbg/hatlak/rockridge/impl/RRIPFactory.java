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

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.*;
import de.tu_darmstadt.informatik.rbg.hatlak.sabre.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class RRIPFactory extends SUSPFactory {
	public static boolean MKISOFS_COMPATIBILITY = true;
	public static final int CR_CONTINUES = 1;
	public static final int CR_CURRENT = 2;
	public static final int CR_PARENT = 4;
	public static final int CR_ROOT = 8;
	public static final int NM_CONTINUES = 1;
	public static final int NM_CURRENT = 2;
	public static final int NM_PARENT = 4;
	public static final int TF_CREATION = 1;
	public static final int TF_MODIFY = 2;
	public static final int TF_ACCESS = 4;
	public static final int TF_ATTRIBUTES = 8;
	public static final int TF_BACKUP = 16;
	public static final int TF_EXPIRATION = 32;
	public static final int TF_EFFECTIVE = 64;
	public static final int TF_LONG_FORM = 128;
	public static final int RR_PX_RECORDED = 1;
	public static final int RR_PN_RECORDED = 2;
	public static final int RR_SL_RECORDED = 4;
	public static final int RR_NM_RECORDED = 8;
	public static final int RR_CL_RECORDED = 16;
	public static final int RR_PL_RECORDED = 32;
	public static final int RR_RE_RECORDED = 64;
	public static final int RR_TF_RECORDED = 128;
	public static final int NM_ENTRY_LENGTH = 5;
	
	public RRIPFactory(StreamHandler streamHandler) {
		super(streamHandler);
	}

	public void doPXEntry(int fileModes, int fileLinks, int uid, int gid, long serialNumber) throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("PX", 1));
		streamHandler.data(new BothWordDataReference(fileModes));
		streamHandler.data(new BothWordDataReference(fileLinks));
		streamHandler.data(new BothWordDataReference(uid));
		streamHandler.data(new BothWordDataReference(gid));
		
		if (!MKISOFS_COMPATIBILITY) {
			// RRIP 1.12 includes the Serial Number field, RRIP 1.09 does not
			streamHandler.data(new BothWordDataReference(serialNumber));
		}
		
		streamHandler.endElement();
	}
	
	public void doPNEntry(int deviceNumberHigh, int deviceNumberLow) throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("PN", 1));
		streamHandler.data(new BothWordDataReference(deviceNumberHigh));
		streamHandler.data(new BothWordDataReference(deviceNumberLow));
		streamHandler.endElement();
	}
	
	public void startSLEntry(boolean continues) throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("SL", 1));
		streamHandler.data(new ByteDataReference(continues ? 1 : 0));
	}
	
	public void doComponentRecord(int flags) throws HandlerException {
		if (flags != CR_CONTINUES && flags != CR_CURRENT && flags != CR_PARENT && flags != CR_ROOT) {
			throw new HandlerException("Invalid Rock Ridge Component Record flags combination: " + flags);
		}
		streamHandler.data(new ByteDataReference(flags));		
		streamHandler.data(new ByteDataReference(0));
	}

	public void doComponentRecord(DataReference name) throws HandlerException {
		streamHandler.data(new ByteDataReference(0));		
		streamHandler.data(new ByteDataReference(name.getLength()));
		streamHandler.data(name);
	}

	public void endSLEntry() throws HandlerException {
		streamHandler.endElement();
	}

	public void doNMEntry(int flags, DataReference name) throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("NM", 1));
		
		if (flags != 0 && flags != NM_CONTINUES && flags != NM_CURRENT && flags != NM_PARENT) {
			throw new HandlerException("Invalid Rock Ridge directory flags combination: " + flags);
		}
		streamHandler.data(new ByteDataReference(flags));
		
		streamHandler.data(name);
		streamHandler.endElement();
	}

	public Fixup doCLEntry() throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("CL", 1));
		Fixup childLocationFixup = streamHandler.fixup(new BothWordDataReference(0));
		streamHandler.endElement();
		
		return childLocationFixup;
	}

	public Fixup doPLEntry() throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("PL", 1));
		Fixup parentLocationFixup = streamHandler.fixup(new BothWordDataReference(0));
		streamHandler.endElement();

		return parentLocationFixup;
	}

	public void doREEntry() throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("RE", 1));
		streamHandler.endElement();
	}

	public void doTFEntry(int type, ISO9660DateDataReference date) throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("TF", 1));
		
		checkTFType(type);
		streamHandler.data(new ByteDataReference(type | TF_LONG_FORM));
		
		streamHandler.data(date);
		streamHandler.endElement();
	}
	
	public void doTFEntry(int type, ISO9660ShortDateDataReference date) throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("TF", 1));
		
		checkTFType(type);
		streamHandler.data(new ByteDataReference(type));
		
		streamHandler.data(date);
		streamHandler.endElement();
	}

	private void checkTFType(int type) throws HandlerException {
		if (type != TF_CREATION && type != TF_MODIFY && type !=	TF_ACCESS && type != TF_ATTRIBUTES
			&& type != TF_BACKUP && type != TF_EXPIRATION && type != TF_EFFECTIVE) {
			throw new HandlerException("Invalid Rock Ridge Timestamp type: " + type);
		}
	}
	
	public void doSFEntry(long virtualFileSizeHigh, long virtualFileSizeLow, int tableDepth) throws HandlerException {
		streamHandler.startElement(new SystemUseEntryElement("SF", 1));
		streamHandler.data(new BothWordDataReference(virtualFileSizeHigh));
		streamHandler.data(new BothWordDataReference(virtualFileSizeLow));
		streamHandler.data(new ByteDataReference(tableDepth));
		streamHandler.endElement();
	}
	
	public void doEREntry() throws HandlerException {
		String id, descriptor, source;
		if (RRIPFactory.MKISOFS_COMPATIBILITY) {
			id = "RRIP_1991A";
			descriptor = "THE ROCK RIDGE INTERCHANGE PROTOCOL PROVIDES SUPPORT FOR POSIX FILE SYSTEM SEMANTICS";
			source = "PLEASE CONTACT DISC PUBLISHER FOR SPECIFICATION SOURCE.  SEE PUBLISHER IDENTIFIER IN PRIMARY VOLUME DESCRIPTOR FOR CONTACT INFORMATION";
		} else {
			id = "IEEE 1282";
			descriptor = "THE IEEE 1282 PROTOCOL PROVIDES SUPPORT FOR POSIX FILE SYSTEM SEMANTICS.";
			source = "PLEASE CONTACT THE IEEE STANDARDS DEPARTMENT, PISCATAWAY, NJ, USA FOR THE 1282 SPECIFICATION.";
		}
		
		ByteArrayDataReference idRef = new ByteArrayDataReference(id.getBytes());
		ByteArrayDataReference descriptorRef = new ByteArrayDataReference(descriptor.getBytes());
		ByteArrayDataReference sourceRef = new ByteArrayDataReference(source.getBytes());
		
		doEREntry(idRef, descriptorRef, sourceRef, 1);
	}
	
	public void doRREntry(int flags) throws HandlerException {
		if (MKISOFS_COMPATIBILITY) {			
			if (flags<0 || flags>255) {
				throw new HandlerException("Invalid RR flags: " + flags);
			}
			
			streamHandler.startElement(new SystemUseEntryElement("RR", 1));
			streamHandler.data(new ByteDataReference(flags));
			streamHandler.endElement();
		}
		// Else: Do nothing (RRIP 1.12 does not include the RR Entry type)
	}
}
