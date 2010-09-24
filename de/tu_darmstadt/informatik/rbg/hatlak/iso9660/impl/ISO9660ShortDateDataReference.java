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

import java.io.*;
import java.util.*;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;

public class ISO9660ShortDateDataReference implements DataReference {
	private Date date = null;
	
	public ISO9660ShortDateDataReference(Date date) {
		this.date = date;
	}

	public ISO9660ShortDateDataReference(long date) {
		this(new Date(date));
	}

	public ISO9660ShortDateDataReference() {
		this(new Date());
	}

	public long getLength() {
		return 7;
	}
	
	public InputStream createInputStream() throws IOException {
		byte[] buffer;
		if (date==null) {
			buffer = getEmptyDate();
		} else {
			buffer = getDate();
		}
		
		return new ByteArrayInputStream(buffer);
	}

	private byte[] getEmptyDate() {
		byte[] buffer = {0,0,0,0,0,0,0};		
		return buffer;
	}
	
	private byte[] getDate() {
		byte[] buffer = new byte[7];
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		
		// Parse date
		int year = cal.get(Calendar.YEAR) - 1900;
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		int gmt_offset = cal.get(Calendar.ZONE_OFFSET) / (15 * 60 * 1000);

		// Create ISO9660 date
		buffer[0] = (byte) year;
		buffer[1] = (byte) month;
		buffer[2] = (byte) day;
		buffer[3] = (byte) hour;
		buffer[4] = (byte) minute;
		buffer[5] = (byte) second;
		buffer[6] = (byte) gmt_offset;
		
		return buffer;
	}
}
