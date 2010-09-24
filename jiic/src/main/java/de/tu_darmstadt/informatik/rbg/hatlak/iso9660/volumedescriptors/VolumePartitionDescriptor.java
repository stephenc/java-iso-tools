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

import java.util.HashMap;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.LayoutHelper;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.PartitionConfig;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.*;
import de.tu_darmstadt.informatik.rbg.hatlak.sabre.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class VolumePartitionDescriptor extends ISO9660VolumeDescriptor {
	private String systemId, volumePartitionId;
	
	public VolumePartitionDescriptor(StreamHandler streamHandler, LayoutHelper helper) {
		super(streamHandler, ISO9660Constants.VPD_TYPE, helper);
		this.systemId = this.volumePartitionId = "";
	}
	
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public void setVolumePartitionId(String volumePartitionId) {
		this.volumePartitionId = volumePartitionId;
	}

	public void setMetadata(PartitionConfig config) {
		setSystemId(config.getSystemId());
		setVolumePartitionId(config.getVolumePartitionId());
	}

	public HashMap doVPD() throws HandlerException {
		HashMap memory = new HashMap();
		
		// Volume Descriptor Type: Volume Partition
		streamHandler.data(getType());
		
		// Standard Identifier
		streamHandler.data(getStandardId());

		// Volume Descriptor Version
		streamHandler.data(getVDVersion());
		
		// Unused Field: 1 byte
		streamHandler.data(new ByteDataReference(0));

		// System Identifier: 32 bytes
		streamHandler.data(getSystemId());

		// Volume Partition Identifier: 32 bytes
		streamHandler.data(getVolumePartitionId());

		// Volume Partition Location
		Fixup location = streamHandler.fixup(new BothWordDataReference(0));
		memory.put("volumePartitionLocationFixup", location);

		// Volume Partition Size
		Fixup size = streamHandler.fixup(new BothWordDataReference(0));
		memory.put("volumePartitionSizeFixup", size);

		// System Use: handle externally
		
		return memory;
	}
	
	private ByteArrayDataReference getSystemId() throws HandlerException {
		byte[] bytes = helper.pad(systemId, 32);
		return new ByteArrayDataReference(bytes);
	}
	
	private ByteArrayDataReference getVolumePartitionId() throws HandlerException {
		byte[] bytes = helper.pad(volumePartitionId, 32);
		return new ByteArrayDataReference(bytes);
	}

}
