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

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.LayoutHelper;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;

public class VolumeDescriptorSetTerminator extends ISO9660VolumeDescriptor {
	public VolumeDescriptorSetTerminator(StreamHandler streamHandler, LayoutHelper helper) {
		super(streamHandler, ISO9660Constants.VDST_TYPE, helper);
	}
	
	public void doVDST() throws HandlerException {
		// Volume Descriptor Type: Primary
		streamHandler.data(getType());

		// Standard Identifier
		streamHandler.data(getStandardId());

		// Volume Descriptor Version
		streamHandler.data(getVDVersion());
	}
}
