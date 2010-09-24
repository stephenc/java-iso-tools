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

package de.tu_darmstadt.informatik.rbg.hatlak.eltorito.impl;

import de.tu_darmstadt.informatik.rbg.hatlak.sabre.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class ElToritoFactory {
	private StreamHandler streamHandler;
	
	public ElToritoFactory(StreamHandler streamHandler) {
		this.streamHandler = streamHandler;
	}

	public void doValidationEntry(int platformID, String idString) throws HandlerException {
		int sum = 0;
		
		// Header ID (0x01)
		streamHandler.data(new ByteDataReference(1));
		sum++;
		
		// Platform ID
		streamHandler.data(new ByteDataReference(platformID));
		sum += platformID << 8;
		
		// Reserved (2 bytes)
		streamHandler.data(new EmptyByteArrayDataReference(2));
		
		// ID string (24 bytes)
		byte[] idStringData = pad(idString, 24);
		streamHandler.data(new ByteArrayDataReference(idStringData));
		sum += stringWordSum(idStringData);
		
		// Checksum word
		sum += 0xAA55; // Key bytes
		int checksum = 0x10000 - sum;
		streamHandler.data(new LSBFShortDataReference(checksum));
		
		// Key byte (0x55)
		streamHandler.data(new ByteDataReference(0x55));
		
		// Key byte (0xAA)
		streamHandler.data(new ByteDataReference(0xAA));
	}
	
	public Fixup doDefaultEntry(boolean bootable, int bootMediaType, int loadSegment, int systemType, int sectorCount) throws HandlerException {
		// Boot Indicator
		if (bootable) {
			streamHandler.data(new ByteDataReference(0x88));
		} else {
			streamHandler.data(new ByteDataReference(0));			
		}
		
		// Boot media type
		streamHandler.data(getBootMediaType(bootMediaType));
		
		// Load Segment: (0: use traditional segment of 0x7C0)
		streamHandler.data(new LSBFShortDataReference(loadSegment));
		
		// System Type
		streamHandler.data(new ByteDataReference(systemType));
		
		// Unused byte
		streamHandler.data(new ByteDataReference(0));
		
		// Sector Count
		streamHandler.data(new LSBFShortDataReference(sectorCount));
		
		// Load RBA
		Fixup loadRBA = streamHandler.fixup(new LSBFWordDataReference(0));
		
		// Unused (19 bytes)
		streamHandler.data(new EmptyByteArrayDataReference(19));
		
		return loadRBA;
	}
	
	public void doSectionHeader(boolean moreHeaders, int platformID, int sectionEntriesCount, String idString) throws HandlerException {
		// Header Indicator
		if (moreHeaders) {
			streamHandler.data(new ByteDataReference(0x90));
		} else {
			streamHandler.data(new ByteDataReference(0x91));
		}
		
		// Platform ID
		streamHandler.data(new ByteDataReference(platformID));

		// Number of section entries following this header
		streamHandler.data(new LSBFShortDataReference(sectionEntriesCount));

		// ID string identifying the section
		byte[] idStringData = pad(idString, 28);
		streamHandler.data(new ByteArrayDataReference(idStringData));
	}
	
	public Fixup doSectionEntry(boolean bootable, int bootMediaType, int loadSegment, int systemType, int sectorCount, boolean entryExtFollows, boolean containsATAPIDriver, boolean containsSCSIDriver, int selectionCriteriaType) throws HandlerException {
		// Boot Indicator
		if (bootable) {
			streamHandler.data(new ByteDataReference(0x88));
		} else {
			streamHandler.data(new ByteDataReference(0));			
		}
		
		// Boot media type
		streamHandler.data(getExtendedBootMediaType(bootMediaType, entryExtFollows, containsATAPIDriver, containsSCSIDriver));
		
		// Load Segment: (0: use traditional segment of 0x7C0)
		streamHandler.data(new LSBFShortDataReference(loadSegment));
		
		// System Type
		streamHandler.data(new ByteDataReference(systemType));
		
		// Unused byte
		streamHandler.data(new ByteDataReference(0));
		
		// Sector Count
		streamHandler.data(new LSBFShortDataReference(sectorCount));
		
		// Load RBA
		Fixup loadRBA = streamHandler.fixup(new LSBFWordDataReference(0));
		
		// Selection criteria type
		streamHandler.data(new ByteDataReference(selectionCriteriaType));
		
		// Vendor unique selection criteria (18 bytes): handle externally
		return loadRBA;
	}

	public void doSectionEntryExtension(boolean moreExtRecords) throws HandlerException {
		// Extension Identificator (0x44)
		streamHandler.data(new ByteDataReference(0x44));
		
		// Whether Extension Records follows or not (final Extension)
		if (moreExtRecords) {
			// Set bit 5
			streamHandler.data(new ByteDataReference(1 << 4));
		} else {
			streamHandler.data(new ByteDataReference(0));
		}
		
		// Vendor unique selection criteria (30 bytes): handle externally
	}

	private byte[] pad(String string, int pad) {		
		if (string.length() == pad) {
			return string.getBytes();
		}

		if (string.length() > pad) {
			return string.substring(0, pad).getBytes();
		}
		
		byte[] bytes = new byte[pad];		
		byte[] original = string.getBytes();
		for (int i = 0; i < original.length; i++) {
			bytes[i] = original[i];
		}
		for (int i = original.length; i < bytes.length; i++) {
			bytes[i] = 0;
		}
		
		return bytes;
	}
	
	private int stringWordSum(byte[] stringData) throws HandlerException {
		if (stringData.length%2 != 0) {
			throw new HandlerException("stringWordSum expects even string length.");
		}
		
		int sum = 0;
		for (int i=0; i<stringData.length; i+=2) {
			sum += stringData[i];
			sum += stringData[i+1] << 8;
		}
		return sum;
	}
	
	private DataReference getBootMediaType(int bootMediaType) throws HandlerException {
		if (bootMediaType>=0 && bootMediaType<=4) {
			return new ByteDataReference(bootMediaType);
		}
		throw new HandlerException("Invalid Boot Media Type: " + bootMediaType);
	}
	
	private DataReference getExtendedBootMediaType(int bootMediaType, boolean entryExtFollows, boolean containsATAPIDriver, boolean containsSCSIDriver) throws HandlerException {
		int bits = 0;
		if (bootMediaType>=0 && bootMediaType<=4) {
			bits = bootMediaType;
		} else {
			throw new HandlerException("Invalid Boot Media Type: " + bootMediaType);
		}
		
		if (entryExtFollows) {
			// Set bit 5
			bits &= 1 << 4;
		}
		if (containsATAPIDriver) {
			// Set bit 6
			bits &= 1 << 5;
		}
		if (containsSCSIDriver) {
			// Set bit 7
			bits &= 1 << 6;
		}

		return new ByteDataReference(bits);
	}
}
