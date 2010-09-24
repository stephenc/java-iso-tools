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

package de.tu_darmstadt.informatik.rbg.hatlak.eltorito.impl;

import java.io.File;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.HandlerException;

public class ElToritoConfig extends BootConfig {
	private boolean bootable, genBootInfoTable;
	private int platformID, bootMediaType, loadSegment, systemType, sectorCount;
	private String idString;
	private ISO9660File bootImage;
	public static int PLATFORM_ID_X86 = 0;
	public static int PLATFORM_ID_PPC = 1;
	public static int PLATFORM_ID_MAC = 2;
	public static int PLATFORM_ID_EFI = 0xEF;
	public static int BOOT_MEDIA_TYPE_NO_EMU = 0;
	public static int BOOT_MEDIA_TYPE_1_2MEG_DISKETTE = 1;
	public static int BOOT_MEDIA_TYPE_1_44MEG_DISKETTE = 2;
	public static int BOOT_MEDIA_TYPE_2_88MEG_DISKETTE = 3;
	public static int BOOT_MEDIA_TYPE_HD = 4;
	public static int LOAD_SEGMENT_7C0 = 0;

	/**
	 * El Torito Boot Image configuration
	 * 
	 * @param bootImage Boot Image file
	 * @param emulation Emulation mode: one of BOOT_MEDIA_TYPE_*
	 * @param platformID Platform Identifier: one of PLATFORM_ID_*
	 * @param idString Identifier String
	 * @param sectorCount Boot Image Sector Count
	 * @param loadSegment Boot Image Load Segment
	 * @throws HandlerException Problems converting to ISO9660File
	 * @throws ConfigException String too long or invalid platform/emulation mode
	 */
	public ElToritoConfig(File bootImage, int emulation, int platformID, String idString, int sectorCount, int loadSegment) throws HandlerException, ConfigException {
		super("EL TORITO SPECIFICATION", "");
		this.bootable = true;
		this.loadSegment = loadSegment;
		this.bootImage = new ISO9660File(bootImage);
		setIDString(idString);
		this.systemType = 0;
		this.sectorCount = sectorCount;
		setPlatformID(platformID);
		setEmulation(emulation);
		genBootInfoTable = false;
	}

	/**
	 * Returns active Boot Image
	 * 
	 * @return Active Boot Image
	 */
	public ISO9660File getBootImage() {
		return bootImage;
	}

	/**
	 * Set Boot Image
	 * 
	 * @param bootImage Boot Image
	 */
	public void setBootImage(ISO9660File bootImage) {
		this.bootImage = bootImage;
	}

	/**
	 * Returns whether the Boot Image is bootable
	 * 
	 * @return Whether the Boot Image is bootable
	 */
	public boolean getBootable() {
		return bootable;
	}

	/**
	 * Make bootable
	 * 
	 * @param bootable Whether the Boot Image should be bootable
	 */
	public void setBootable(boolean bootable) {
		this.bootable = bootable;
	}

	/**
	 * Set Boot Info Table (only allowed for no-emulation images)
	 * 
	 * @param genBootInfoTable Whether to generate a boot info table
	 */
	public void setGenBootInfoTable(boolean genBootInfoTable) throws ConfigException {
		if (!genBootInfoTable || this.bootMediaType == ElToritoConfig.BOOT_MEDIA_TYPE_NO_EMU) {
			this.genBootInfoTable = genBootInfoTable;
		} else {
			throw new ConfigException(this, "Boot info table generation requires no-emulation image.");
		}
	}

	/**
	 * Generate Boot Info Table
	 * 
	 * @return Whether a boot info table is to be generated
	 */
	public boolean getGenBootInfoTable() {
		return this.genBootInfoTable;
	}

	/**
	 * Set Platform Identifier
	 * 
	 * @param platformID Platform Identifier: one of PLATFORM_ID_*
	 * @throws ConfigException Invalid Platform Identifier
	 */
	public void setPlatformID(int platformID) throws ConfigException {
		if (platformID>=0 && platformID<=2) {
			this.platformID = platformID;
		} else {
			throw new ConfigException(this, "Invalid Platform ID: " + platformID);
		}
	}

	/**
	 * Returns active Platform Identifier
	 * 
	 * @return Platform Identifier
	 */
	public int getPlatformID() {
		return platformID;
	}

	/**
	 * Set Identifier String
	 * 
	 * @param idString Identifier String
	 * @throws ConfigException String too long
	 */
	public void setIDString(String idString) throws ConfigException {
		if (idString.length() > 24) {
			throw new ConfigException(this, "The ID string may be no longer than 24 characters.");
		}
		this.idString = idString;
	}

	/**
	 * Returns active Identifier String
	 * 
	 * @return Active Identifier String
	 */
	public String getIDString() {
		return idString;
	}

	/**
	 * Returns active Boot Media Type (Emulation Mode)
	 * 
	 * @return Active Boot Media Type
	 */
	public int getBootMediaType() {
		return bootMediaType;
	}

	/**
	 * Set Emulation Mode
	 * 
	 * @param bootMediaType Boot Media Type: one of BOOT_MEDIA_TYPE_*
	 * @throws ConfigException Invalid Boot Media Type
	 */
	public void setEmulation(int bootMediaType) throws ConfigException {
		if (bootMediaType>=0 && bootMediaType<=4) {
			this.bootMediaType = bootMediaType;
		} else {
			throw new ConfigException(this, "Invalid Boot Media Type: " + bootMediaType);
		}
	}

	/**
	 * Returns active Load Segment
	 * 
	 * @return Active Load Segment
	 */
	public int getLoadSegment() {
		return loadSegment;
	}

	/**
	 * Set Load Segment
	 * 
	 * @param loadSegment Load Segment
	 */
	public void setLoadSegment(int loadSegment) {
		this.loadSegment = loadSegment;
	}

	/**
	 * Returns active Sector Count
	 * 
	 * @return Active Sector Count
	 */
	public int getSectorCount() {
		return sectorCount;
	}

	/**
	 * Set Sector Count
	 * 
	 * @param sectorCount Sector Count
	 */
	public void setSectorCount(int sectorCount) {
		this.sectorCount = sectorCount;
	}

	/**
	 * Returns active System Type
	 * 
	 * @return Active System Type
	 */
	public int getSystemType() {
		return systemType;
	}

	/**
	 * Set System Type
	 * 
	 * @param systemType System Type
	 */
	public void setSystemType(int systemType) {
		this.systemType = systemType;
	}
}
