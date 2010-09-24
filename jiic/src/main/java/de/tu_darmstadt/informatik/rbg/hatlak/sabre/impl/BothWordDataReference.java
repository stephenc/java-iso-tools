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

package de.tu_darmstadt.informatik.rbg.hatlak.sabre.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;

public class BothWordDataReference implements DataReference {
	private long value = 0;

	public BothWordDataReference(long value) {
		this.value = value;
	}
	
	public long getLength() {
		return 8;
	}

	public InputStream createInputStream() throws IOException {
		byte[] buffer = new byte[8];
		
		// MSB first (Big Endian)
		buffer[4] = (byte)((this.value & 0xFF000000) >> 24);
		buffer[5] = (byte)((this.value & 0x00FF0000) >> 16);
		buffer[6] = (byte)((this.value & 0x0000FF00) >> 8);
		buffer[7] = (byte)(this.value & 0x000000FF);

		// LSB first (Little Endian)
		buffer[3] = buffer[4];
		buffer[2] = buffer[5];
		buffer[1] = buffer[6];
		buffer[0] = buffer[7];

		return new ByteArrayInputStream(buffer);
	}
}
