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

public class ISO9660Constants {
	/**
	 * Sector size: 2048 [bytes] 
	 */
	public static final int LOGICAL_SECTOR_SIZE = 2048;

	/**
	 * Logical Block size: same as or fraction of Logical Block Size
	 */
	public static final int LOGICAL_BLOCK_SIZE = LOGICAL_SECTOR_SIZE;

	/**
	 * Boot Record Type
	 */
	public static final int BR_TYPE = 0;

	/**
	 * Primary Volume Descriptor Type
	 */
	public static final int PVD_TYPE = 1;

	/**
	 * Supplementary Volume Descriptor Type
	 */
	public static final int SVD_TYPE = 2;

	/**
	 * Volume Partition Descriptor Type
	 */
	public static final int VPD_TYPE = 3;

	/**
	 * Volume Descriptor Set Terminator Type
	 */
	public static final int VDST_TYPE = 255;
	
	/**
	 * Standard Identifier
	 */
	public static final String STD_ID = "CD001";
	
	/**
	 * Volume Descriptor Version
	 */
	public static final int VDV = 1;
	
	/**
	 * File Structure Version
	 */
	public static final int FSV = 1;
	
	/**
	 * Max value for File Version Number
	 */
	public static final int MAX_FILE_VERSION = 32767;
	
	/**
	 * "dot" File Identifier
	 */
	public static final Object FI_DOT = new Object();

	/**
	 * Root File Identifier
	 */
	public static final Object FI_ROOT = FI_DOT;

	/**
	 * "dotdot" File Identifier
	 */
	public static final Object FI_DOTDOT = new Object();
	
	/**
	 * Type L Path Table
	 */
	public static final Object TYPE_L_PT = new Object();

	/**
	 * Type M Path Table
	 */
	public static final Object TYPE_M_PT = new Object();
	
	/**
	 * d-characters (ECMA-6), ready for use in RegEx character class with negation
	 */
	public static final String ECMA6_D_CHARACTERS = "0-9A-Z_";
	
	/**
	 * a-characters (ECMA-6), ready for use in RegEx character class with negation
	 */
	public static final String ECMA6_A_CHARACTERS = "- !\"%&'()*+,./:;<=>?" + ECMA6_D_CHARACTERS;
}
