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

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ISO9660Directory;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ISO9660File;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.LayoutHelper;
import de.tu_darmstadt.informatik.rbg.hatlak.sabre.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class ISO9660DirectoryRecord {
	private StreamHandler streamHandler;
	private int volSeqNo;
	private DataReference filenameDataReference;
	public boolean hide, isDirectory;
	
	/**
	 * File
	 */
	public ISO9660DirectoryRecord(StreamHandler streamHandler, ISO9660File file, LayoutHelper helper) throws HandlerException {
		init(streamHandler, helper);
		this.filenameDataReference = helper.getFilenameDataReference(file);
		this.isDirectory = false;
	}

	/**
	 * Directory
	 */
	public ISO9660DirectoryRecord(StreamHandler streamHandler, ISO9660Directory dir, LayoutHelper helper) throws HandlerException {
		init(streamHandler, helper);		
		this.filenameDataReference = helper.getFilenameDataReference(dir);
	}

	/**
	 * Special Directory
	 */
	public ISO9660DirectoryRecord(StreamHandler streamHandler, Object object, ISO9660Directory dir, LayoutHelper helper) throws HandlerException {
		init(streamHandler, helper);

		if (object==ISO9660Constants.FI_ROOT) {
			// The root directory
			this.filenameDataReference = new ByteDataReference(0);
		} else
		if (object==ISO9660Constants.FI_DOT) {
			// "dot", i.e. the directory itself
			this.filenameDataReference = new ByteDataReference(0);
		} else
		if (object==ISO9660Constants.FI_DOTDOT) {
			// "dotdot", i.e. the parent directory
			this.filenameDataReference = new ByteDataReference(1);			
		} else {
			throw new HandlerException("Unknown special directory type, neither ROOT nor DOT nor DOTDOT: " + object);
		}
	}

	private void init(StreamHandler streamHandler, LayoutHelper helper) {
		this.streamHandler = streamHandler;
		this.volSeqNo = 1;
		this.hide = false;
		this.isDirectory = true;
	}
	
	public void setVolSeqNo(int value) {
		this.volSeqNo = value;
	}
	
	public void hide() {
		this.hide = true;
	}

	public HashMap doDR() throws HandlerException {
		HashMap memory = new HashMap();
		int length = 0;
		
		// Length of Directory Record (including System Use Area)
		Fixup drLength = streamHandler.fixup(new ByteDataReference(0));
		memory.put("drLengthFixup", drLength);
		length += 1;

		// Extended Attribute Record Length
		Fixup extAttrRecordLengthFixup = streamHandler.fixup(new ByteDataReference(0));
		memory.put("drExtAttrRecordLengthFixup", extAttrRecordLengthFixup);
		length += 1;

		// Location of Extent
		Fixup locationFixup = streamHandler.fixup(new BothWordDataReference(0));
		memory.put("drLocationFixup", locationFixup);
		length += 8;

		// Data Length
		Fixup dataLengthFixup = streamHandler.fixup(new BothWordDataReference(0));
		memory.put("drDataLengthFixup", dataLengthFixup);
		length += 8;
		
		// Recording Date and Time
		Date now = new Date();
		ISO9660ShortDateDataReference date = new ISO9660ShortDateDataReference(now);
		streamHandler.data(date);
		length += date.getLength();
		
		// File Flags
		byte fileFlags = getFileFlags();
		streamHandler.data(new ByteDataReference(fileFlags));
		length += 1;
		
		// File Unit Size: 0 (no interleaving)
		streamHandler.data(new ByteDataReference(0));
		length += 1;
		
		// Interleave Gap Size: 0 (no interleaving)
		streamHandler.data(new ByteDataReference(0));
		length += 1;
		
		// Volume Sequence Number
		streamHandler.data(new BothShortDataReference(volSeqNo));
		length += 4;

		// Length of File Identifier
		streamHandler.data(new ByteDataReference(filenameDataReference.getLength()));
		length += 1;
		
		// File Identifier
		streamHandler.data(filenameDataReference);
		length += filenameDataReference.getLength();
		
		// Padding Field
		if (filenameDataReference.getLength()%2==0) {
			streamHandler.data(new ByteDataReference(0));
			length += 1;
		}
		
		memory.put("drLength", new Integer(length));
		
		return memory;
	}

	private byte getFileFlags() {
		byte flags = 0;
		
		if (hide) {
			flags |= 1;
		}
		if (isDirectory) {
			flags |= 2;
		}
		// ignore other cases for now:
		// - bit 2: Associated File
		// - bit 3: Record Format specified in XAR
		// - bit 4: Owner, Group and Permissions specified in XAR
		// - bit 7: not the final DR for this file (-> multiple File Sections)
		
		return flags;
	}
}
