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

public abstract class BootConfig {
	private String bootSystemId, bootId;

	public BootConfig(String bootSystemId, String bootId) {
		this.bootSystemId = bootSystemId;
		this.bootId = bootId;
	}
	
	public BootConfig() {
		this("", "");
	}
	
	/**
	 * Boot System Identifier
	 * 
	 * @param bootSystemId Identifier for the system that can act upon the Boot System Use field
	 */
	public void setBootSystemId(String bootSystemId) {
		this.bootSystemId = bootSystemId;
	}	

	/**
	 * Active Boot System Identifier
	 * 
	 * @return Active Boot System identifier
	 */
	public String getBootSystemId() {
		return bootSystemId;
	}

	/**
	 * Boot Identifier
	 * 
	 * @param bootId Boot identifier
	 */
	public void setBootId(String bootId) {
		this.bootId = bootId;
	}

	/**
	 * Active Boot Identifier
	 * 
	 * @return Active Boot identifier
	 */
	public String getBootId() {
		return bootId;
	}
}
