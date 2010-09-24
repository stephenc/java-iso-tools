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

package de.tu_darmstadt.informatik.rbg.hatlak.iso9660;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.ISO9660Constants;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;

public abstract class LayoutHelper {
	private StreamHandler streamHandler;
	private ISO9660RootDirectory root;
	private NamingConventions namingConventions;
	
	public LayoutHelper(StreamHandler streamHandler, ISO9660RootDirectory root, NamingConventions namingConventions) {
		this.streamHandler = streamHandler;
		this.root = root;
		this.namingConventions = namingConventions;
	}
	
	public ISO9660RootDirectory getRoot() {
		return root;
	}
	
	public int getCurrentLocation() throws HandlerException {
		long position = streamHandler.mark();
		int location = (int) (position / ISO9660Constants.LOGICAL_BLOCK_SIZE);
		return location;
	}

	public int getDifferenceTo(long position) throws HandlerException {
		return (int) ((streamHandler.mark() - position));
	}
	
	public NamingConventions getNamingConventions() {
		return namingConventions;
	}
	
	public abstract FilenameDataReference getFilenameDataReference(ISO9660Directory dir) throws HandlerException;

	public abstract FilenameDataReference getFilenameDataReference(ISO9660File file) throws HandlerException;

	public abstract byte[] pad(String string, int targetByteLength) throws HandlerException;
}
