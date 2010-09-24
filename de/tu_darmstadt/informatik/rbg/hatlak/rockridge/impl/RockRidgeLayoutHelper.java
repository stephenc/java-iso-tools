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

package de.tu_darmstadt.informatik.rbg.hatlak.rockridge.impl;

import java.util.Iterator;
import java.util.HashMap;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;

public class RockRidgeLayoutHelper extends LayoutHelper {
	private ISO9660RootDirectory rripRoot;
	private HashMap directoryMapper, fileMapper;
	
	public RockRidgeLayoutHelper(StreamHandler streamHandler, ISO9660RootDirectory isoRoot, ISO9660RootDirectory rripRoot) {
		super(streamHandler, isoRoot, new RockRidgeNamingConventions());
		this.rripRoot = rripRoot;
		setup(isoRoot);
	}
	
	private void setup(ISO9660RootDirectory isoRoot) {
		// Lookup tables mapping files and directories between hierarchies
		// (ISO 9660 -> Rock Ridge)
		int dirCount = isoRoot.deepDirCount() + 1;
		directoryMapper = new HashMap(dirCount);
		int fileCount = isoRoot.deepFileCount() + 1;
		fileMapper = new HashMap(fileCount);
		
		// Root files (root itself does not have to be mapped)
		Iterator isoFit = isoRoot.getFiles().iterator();
		Iterator rripFit = rripRoot.getFiles().iterator();
		while (isoFit.hasNext()) {
			ISO9660File isoFile = (ISO9660File) isoFit.next();
			ISO9660File rripFile = (ISO9660File) rripFit.next();
			fileMapper.put(isoFile, rripFile);
		}		
		
		// Subdirectories:
		// Since rripRoot and isoRoot are just a deep copy of the same
		// root at this point, simultaneous iteration can be applied here
		Iterator isoIt = isoRoot.unsortedIterator();
		Iterator rripIt = rripRoot.unsortedIterator();
		while (isoIt.hasNext()) {
			ISO9660Directory isoDir = (ISO9660Directory) isoIt.next();
			ISO9660Directory rripDir = (ISO9660Directory) rripIt.next();
			directoryMapper.put(isoDir.getID(), rripDir);
			
			isoFit = isoDir.getFiles().iterator();
			rripFit = rripDir.getFiles().iterator();
			while (isoFit.hasNext()) {
				ISO9660File isoFile = (ISO9660File) isoFit.next();
				ISO9660File rripFile = (ISO9660File) rripFit.next();
				fileMapper.put(isoFile.getID(), rripFile);
			}
		}
	}

	public FilenameDataReference getFilenameDataReference(ISO9660Directory dir) throws HandlerException {
		return new RockRidgeFilenameDataReference(matchDirectory(dir));
	}

	public FilenameDataReference getFilenameDataReference(ISO9660File file) throws HandlerException {
		return new RockRidgeFilenameDataReference(matchFile(file));
	}

	public FilenameDataReference getFilenameDataReference(String name) throws HandlerException {
		return new RockRidgeFilenameDataReference(name);
	}

	public ISO9660Directory matchDirectory(ISO9660Directory dir) {
		if (dir==dir.getRoot()) {
			return rripRoot;
		}
		
		if (dir==dir.getRoot().getMovedDirectoriesStore()) {
			return rripRoot.getMovedDirectoriesStore();
		}
		
		ISO9660Directory rripDir = (ISO9660Directory) directoryMapper.get(dir.getID());
		if (rripDir!=null) {
			return rripDir;
		}
		
		throw new RuntimeException("No matching directory found for " + dir.getISOPath());
	}

	public ISO9660File matchFile(ISO9660File file) {		
		ISO9660File rripFile = (ISO9660File) fileMapper.get(file.getID());
		if (rripFile!=null) {
			return rripFile;
		}
		
		throw new RuntimeException("No matching file found for " + file.getISOPath());
	}

	public byte[] pad(String string, int targetByteLength)
			throws HandlerException {
		// Unused
		return null;
	}
}
