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
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.ByteDataReference;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.ThreeByteDataReference;

public class PrimaryVolumeDescriptor extends StandardVolumeDescriptor {
	public PrimaryVolumeDescriptor(StreamHandler streamHandler, LayoutHelper helper) {
		super(streamHandler, ISO9660Constants.PVD_TYPE, helper);
	}
	
	public HashMap doPVD() throws HandlerException {
		HashMap memory = doStandardVD();
		
		// Set Volume Flags to 0 (Unused Field)
		Fixup volumeFlags = (Fixup) memory.get("volumeFlagsFixup");
		volumeFlags.data(new ByteDataReference(0));
		volumeFlags.close();
		memory.remove("volumeFlagsFixup");
		
		// Set Escape Sequences to all 0 (Unused Field)
		Fixup escapeSequences = (Fixup) memory.get("escapeSequencesFixup");
		escapeSequences.data(new ThreeByteDataReference(0));
		escapeSequences.close();
		memory.remove("escapeSequencesFixup");
		
		return memory;
	}
}
