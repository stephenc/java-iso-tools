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

import java.io.*;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;

public abstract class FilenameDataReference implements DataReference {
	private String name;
	
	public FilenameDataReference(ISO9660Directory dir) throws HandlerException {
		setName(dir.getName());
	}

	public FilenameDataReference(ISO9660File file) throws HandlerException {
		setName(file.getFullName());
	}

	public FilenameDataReference(String name) {
		setName(name);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract long getLength();

	public abstract InputStream createInputStream() throws IOException;
}
