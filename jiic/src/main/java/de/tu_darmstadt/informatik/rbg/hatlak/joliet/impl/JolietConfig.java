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

package de.tu_darmstadt.informatik.rbg.hatlak.joliet.impl;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ConfigException;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.StandardConfig;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.ByteArrayDataReference;

public class JolietConfig extends StandardConfig {
	private static byte[][] UCS2_LEVEL_ESCAPE_SEQUENCES = {
		{0x25, 0x2F, 0x40},
		{0x25, 0x2F, 0x43},
		{0x25, 0x2F, 0x45}};
	int ucs2_level;
	
	public JolietConfig() {
		super();
		this.ucs2_level = 3;
	}

	/**
	 * Force Dot Delimiter
	 * 
	 * @param force Whether files must include the dot character
	 */
	public void forceDotDelimiter(boolean force) {
		JolietNamingConventions.FORCE_DOT_DELIMITER = force;
		if (!force) {
			System.out.println("Warning: Not forcing to include the dot in filenames breaks Joliet conformance.");
		}
	}

	/**
	 * Set UCS-2 Level
	 * 
	 * @see http://www.nada.kth.se/i18n/ucs/unicode-iso10646-oview.html
	 * @param level 1, 2 or 3
	 * @throws ConfigException Invalid UCS-2 level
	 */
	public void setUCS2Level(int level) throws ConfigException {
		if (level!=1 && level!=2 && level!=3) {
			throw new ConfigException(this, "Invalid UCS-2 level: " + level);
		}
		this.ucs2_level = level; 
	}
	
	/**
	 * Returns active UCS-2 Level
	 * 
	 * @return Active UCS-2 level
	 */
	public int getUCS2Level() {
		return ucs2_level;
	}
	
	/**
	 * Returns UCS-2 level Escape Sequences
	 * 
	 * @return Escape Sequences matching the active UCS-2 level
	 */
	public ByteArrayDataReference getUCS2LevelEscapeSequence() {
		return new ByteArrayDataReference(UCS2_LEVEL_ESCAPE_SEQUENCES[ucs2_level-1]);
	}
	
	public void setVolumeSetID(String volumeSetID) throws ConfigException {
		if (volumeSetID.length() > 64) {
			throw new ConfigException(this, "The Volume Set ID may be no longer than 64 characters.");
		}
		super.setVolumeSetID(volumeSetID);
	}
	
	public void setApp(String app) throws ConfigException {
		if (app.length() > 64) {
			throw new ConfigException(this, "The Application Identifier may be no longer than 64 characters.");
		}
		super.setApp(app);
	}
	
	public void setDataPreparer(String dataPreparer) throws ConfigException {
		if (dataPreparer.length() > 64) {
			throw new ConfigException(this, "The Data Preparer Identifier may be no longer than 64 characters.");
		}
		super.setDataPreparer(dataPreparer);
	}
	
	public void setPublisher(String publisher) throws ConfigException {
		if (publisher.length() > 64) {
			throw new ConfigException(this, "The Publisher Identifier may be no longer than 64 characters.");
		}
		super.setPublisher(publisher);
	}
	
	public void setSystemID(String systemID) throws ConfigException {
		if (systemID.length() > 16) {
			throw new ConfigException(this, "The System Identifier may be no longer than 16 characters.");
		}
		super.setSystemID(systemID);
	}

	public void setVolumeID(String volumeID) throws ConfigException {
		if (volumeID.length() > 16) {
			throw new ConfigException(this, "The Volume Identifier may be no longer than 16 characters.");
		}
		super.setVolumeID(volumeID);
	}
}
