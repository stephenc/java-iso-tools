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

import java.io.UnsupportedEncodingException;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;

public class ISO9660LayoutHelper extends LayoutHelper {
	public ISO9660LayoutHelper(StreamHandler streamHandler, ISO9660RootDirectory root) {
		super(streamHandler, root, new ISO9660NamingConventions());
	}

	public FilenameDataReference getFilenameDataReference(ISO9660Directory dir) throws HandlerException {
		return new ISO9660FilenameDataReference(dir);
	}

	public FilenameDataReference getFilenameDataReference(ISO9660File file) throws HandlerException {
		return new ISO9660FilenameDataReference(file);
	}

	public byte[] pad(String string, int targetByteLength) throws HandlerException {
		byte[] bytes = new byte[targetByteLength];
		byte[] original = null;
		int length = 0;
		
		try {
			if (string!=null) {
				original = string.getBytes("ISO-8859-1"); // ISO Latin 1
				length = original.length;
			}
			for (int i = 0; i < length; i++) {
				bytes[i] = original[i];
			}
			for (int i = length; i < bytes.length; i++) {
				bytes[i] = 0x20;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return bytes;
	}
}
