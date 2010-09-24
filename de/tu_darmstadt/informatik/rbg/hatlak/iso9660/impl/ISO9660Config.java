/*  
 *  JIIC: Java ISO Image Creator. Copyright (C) 2007-2009, Jens Hatlak <hatlak@rbg.informatik.tu-darmstadt.de>
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

import java.lang.Character.UnicodeBlock;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.*;

public class ISO9660Config extends StandardConfig {
	private boolean restrictDirDepthTo8, allowASCII, padEnd;
	
	public ISO9660Config() {
		super();
		this.restrictDirDepthTo8 = true;
		this.allowASCII(false);
	}

	/**
	 * Set Interchange Level<br>
	 * 1: Filenames 8+3, directories 8 characters<br>
	 * 2: Filenames 30, directories 31 characters<br>
	 * 3: multiple File Sections (files > 2 GB)
	 * 
	 * @param level 1, 2 or 3
	 * @throws ConfigException Invalid or unsupported Interchange Level
	 */
	public void setInterchangeLevel(int level) throws ConfigException {
		if (level<1 || level>3) {
			throw new ConfigException(this, "Invalid ISO9660 Interchange Level: " + level);
		}
		if (level==3) {
			throw new ConfigException(this, "Interchange Level 3 (multiple File Sections per file) is not (yet) supported by this implementation.");
		}
		ISO9660NamingConventions.INTERCHANGE_LEVEL = level;
	}

	/**
	 * Force Dot Delimiter
	 * 
	 * @param force Whether files must include the dot character
	 */
	public void forceDotDelimiter(boolean force) {
		ISO9660NamingConventions.FORCE_DOT_DELIMITER = force;
		if (!force) {
			System.out.println("Warning: Not forcing to include the dot in filenames breaks ISO 9660 conformance.");
		}
	}
	
	/**
	 * Allow ASCII for filenames and other strings
	 * 
	 * @param allow Whether to allow the ASCII character set for filenames and other strings
	 */
	public void allowASCII(boolean allow) {
		this.allowASCII = allow;
		ISO9660NamingConventions.FORCE_ISO9660_CHARSET = !allow;
		if (allow) {
			System.out.println("Warning: Allowing the full ASCII character set breaks ISO 9660 conformance.");
		}
	}
	
	/**
	 * Returns whether ASCII is allowed
	 * 
	 * @return Whether the ASCII character set is allowed
	 */
	public boolean allowsASCII() {
		return allowASCII;
	}
	
	/**
	 * Returns whether the directory depth is restricted to eight levels
	 * 
	 * @return Whether the directory hierarchy is limited to eight levels
	 */
	public boolean dirDepthRestrictedTo8() {
		return restrictDirDepthTo8;
	}

	/**
	 * Restrict directory depth to eight levels
	 * 
	 * @param restrictDirDepthTo8 Whether to limit the directory hierarchy to eight levels
	 */
	public void restrictDirDepthTo8(boolean restrictDirDepthTo8) {
		this.restrictDirDepthTo8 = restrictDirDepthTo8;
		if (!restrictDirDepthTo8) {
			System.out.println("Warning: Allowing more than 8 directory levels breaks ISO 9660 conformance.");
		}
	}

	public void setDataPreparer(String dataPreparer) throws ConfigException {
		super.setDataPreparer(checkAString(dataPreparer));
	}

	public void setPublisher(String publisher) throws ConfigException {
		super.setPublisher(checkAString(publisher));
	}

	public void setSystemID(String systemID) throws ConfigException {
		super.setSystemID(checkAString(systemID));
	}

	public void setApp(String app) throws ConfigException {
		super.setApp(checkAString(app));
	}

	public void setVolumeID(String volumeID) throws ConfigException {
		super.setVolumeID(checkDString(volumeID));
	}

	public void setVolumeSetID(String volumeSetID) throws ConfigException {
		super.setVolumeSetID(checkDString(volumeSetID));
	}

	public void setPadEnd(boolean padEnd) {
		this.padEnd = padEnd;
	}

	public boolean getPadEnd() {
		return this.padEnd;
	}

	private String checkAString(String string) {
		if (allowsASCII()) {
			return checkASCIIString(string);
		} // else
		return string.toUpperCase().replaceAll("[^"+ISO9660Constants.ECMA6_A_CHARACTERS+"]", "_");
	}

	private String checkDString(String string) {
		if (allowsASCII()) {
			return checkASCIIString(string);
		} // else
		return string.toUpperCase().replaceAll("[^"+ISO9660Constants.ECMA6_D_CHARACTERS+"]", "_");
	}
	
	private String checkASCIIString(String string) {
		StringBuffer buf = new StringBuffer();
		
		for (int i=0; i<string.length(); i++) {
			UnicodeBlock characterBlock = UnicodeBlock.of(string.charAt(i));
			if (characterBlock==UnicodeBlock.BASIC_LATIN) {
				buf.append(string.charAt(i));
			} else {
				buf.append('_');
			}
		}
		
		return buf.toString();
	}
}
