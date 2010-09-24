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

public interface ISO9660HierarchyObject extends Cloneable, Comparable {
	/**
	 * Returns the name of the hierarchy object
	 * 
	 * @return Name
	 */
	public String getName();

	/**
	 * Returns the root of the directory hierarchy
	 * 
	 * @return Root
	 */
	public ISO9660RootDirectory getRoot();

	/**
	 * Returns whether the hierarchy object is a directory
	 * 
	 * @return Whether this is a directory
	 */
	public boolean isDirectory();

	/**
	 * Set the name of the hierarchy object
	 * 
	 * @param name Name
	 */
	public void setName(String name);

	/**
	 * Returns the parent directory of this hierarchy object
	 * 
	 * @return Parent directory
	 */
	public ISO9660Directory getParentDirectory();

	/**
	 * Returns the path from the root to this hierarchy object
	 * with each path component separated by File.separator so
	 * that its length represents the ISO 9660 path length 
	 * 
	 * @return Path
	 */
	public String getISOPath();

	/**
	 * Returns an Object identifying this hierarchy object
	 * 
	 * @return Identifying Object
	 */
	public Object getID();
}
