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

import de.tu_darmstadt.informatik.rbg.hatlak.sabre.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class ISO9660PathTableRecord {
	private StreamHandler streamHandler;
	private Object type;
	private DataReference filename;
	private int parent, extAttrRecordLength;
	
	public ISO9660PathTableRecord(StreamHandler streamHandler, Object type, Object object, int parent) throws HandlerException {
		this.streamHandler = streamHandler;
		this.extAttrRecordLength = 0;
		
		if (object==ISO9660Constants.FI_ROOT || object==ISO9660Constants.FI_DOT) {
			// "root" or "dot", i.e. the directory itself
			this.filename = new ByteDataReference(0);
		} else
		if (object==ISO9660Constants.FI_DOTDOT) {
			// "dotdot", i.e. the parent directory
			this.filename = new ByteDataReference(1);			
		} else {
			throw new HandlerException("Unknown special directory type, must be one of ROOT, DOT, DOTDOT: " + type);
		}

		setType(type);
		setParent(parent);
	}

	public ISO9660PathTableRecord(StreamHandler streamHandler, Object type, DataReference ref, int parent) throws HandlerException {
		this.streamHandler = streamHandler;
		this.extAttrRecordLength = 0;		
		this.filename = ref;

		setType(type);
		setParent(parent);
	}

	private void setType(Object type) throws HandlerException {
		if (!type.equals(ISO9660Constants.TYPE_L_PT) &&
			!type.equals(ISO9660Constants.TYPE_M_PT)) {
			throw new HandlerException("Unknown Path Table type: " + type);
		}
		this.type = type;
	}
	
	private void setParent(int parent) throws HandlerException {
		if (parent < 0) {
			throw new HandlerException("Invalid parent directory number.");
		}
		this.parent = parent;		
	}
	
	public int hasExtAddrRecordLength() {
		return extAttrRecordLength;
	}

	public void setExtAttrRecordLength(int extAttrRecordLength) {
		this.extAttrRecordLength = extAttrRecordLength;
	}
	
	public Fixup doPTR() throws HandlerException {
		// Length of Directory Identifier
		streamHandler.data(new ByteDataReference(filename.getLength()));
		
		// Extended Attribute Record Length
		streamHandler.data(new ByteDataReference(extAttrRecordLength));
		
		// Location of Extent
		DataReference location_dr = null;
		if (type.equals(ISO9660Constants.TYPE_L_PT)) {
			location_dr = new LSBFWordDataReference(0);
		} else if (type.equals(ISO9660Constants.TYPE_M_PT)) {
			location_dr = new WordDataReference(0);			
		}
		Fixup locationFixup = streamHandler.fixup(location_dr);
		
		// Parent Directory Number
		DataReference parent_dn = null;
		if (type.equals(ISO9660Constants.TYPE_L_PT)) {
			parent_dn = new LSBFShortDataReference(parent);
		} else if (type.equals(ISO9660Constants.TYPE_M_PT)) {
			parent_dn = new ShortDataReference(parent);			
		}
		streamHandler.data(parent_dn);
		
		// Directory Identifier
		streamHandler.data(filename);
		
		// Padding Field
		if (filename.getLength()%2==1) {
			streamHandler.data(new ByteDataReference(0));
		}
		
		return locationFixup;
	}
}
