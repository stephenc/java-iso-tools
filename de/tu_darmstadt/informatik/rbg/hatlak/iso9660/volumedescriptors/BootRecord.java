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

package de.tu_darmstadt.informatik.rbg.hatlak.iso9660.volumedescriptors;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.BootConfig;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.LayoutHelper;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class BootRecord extends ISO9660VolumeDescriptor {
	private String bootSystemId, bootId;

	public BootRecord(StreamHandler streamHandler, LayoutHelper helper) {
		super(streamHandler, ISO9660Constants.BR_TYPE, helper);
		this.bootSystemId = this.bootId = "";
	}

	public void setBootId(String bootId) {
		this.bootId = bootId;
	}

	public void setBootSystemId(String bootSystemId) {
		this.bootSystemId = bootSystemId;
	}

	public void setMetadata(BootConfig config) {
		setBootSystemId(config.getBootSystemId());
		setBootId(config.getBootId());
	}

	public void doBR() throws HandlerException {
		// Volume Descriptor Type: Boot
		streamHandler.data(getType());
		
		// Standard Identifier
		streamHandler.data(getStandardId());

		// Volume Descriptor Version
		streamHandler.data(getVDVersion());

		// Boot System Identifier: 32 bytes
		streamHandler.data(getBootSystemId());

		// Boot Identifier: 32 bytes
		streamHandler.data(getBootId());
		
		// Boot System Use: handle externally
	}
	
	private ByteArrayDataReference getBootSystemId() throws HandlerException {
		byte[] bytes = helper.pad(bootSystemId, 32);
		return new ByteArrayDataReference(bytes);
	}

	private ByteArrayDataReference getBootId() throws HandlerException {
		byte[] bytes = helper.pad(bootId, 32);
		return new ByteArrayDataReference(bytes);
	}
}
