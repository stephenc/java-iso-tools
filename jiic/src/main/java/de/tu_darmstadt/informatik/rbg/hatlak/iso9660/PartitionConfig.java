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

public class PartitionConfig {
	private String systemId, volumePartitionId;

	public PartitionConfig(String bootSystemId, String volumePartitionId) {
		this.systemId = bootSystemId;
		this.volumePartitionId = volumePartitionId;
	}
	
	public PartitionConfig() {
		this("", "");
	}

	/**
	 * System Identifier
	 * 
	 * @param systemId System identifier string
	 */
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}	

	/**
	 * Active System Identifier
	 * 
	 * @return System identifier string
	 */
	public String getSystemId() {
		return systemId;
	}

	/**
	 * Volume Partition Identifier
	 * 
	 * @param volumePartitionId Active Volume Partition identifier string
	 */
	public void setVolumePartitionId(String volumePartitionId) {
		this.volumePartitionId = volumePartitionId;
	}

	/**
	 * Active Volume Partition Identifier
	 * 
	 * @return Active Volume Partition identifier string
	 */
	public String getVolumePartitionId() {
		return volumePartitionId;
	}
}
