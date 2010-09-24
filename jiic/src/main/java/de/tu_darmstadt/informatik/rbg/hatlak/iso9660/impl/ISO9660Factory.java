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

import java.util.*;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.*;
import de.tu_darmstadt.informatik.rbg.hatlak.sabre.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class ISO9660Factory {
	StreamHandler streamHandler;
	StandardConfig config;
	LayoutHelper helper;
	ISO9660RootDirectory root;
	HashMap volumeFixups, typeLPTFixups, typeMPTFixups, dirFixups, fileFixups, locationFixups;
	Vector emptyFileFixups;
	
	public ISO9660Factory(StreamHandler streamHandler, StandardConfig config, LayoutHelper helper, ISO9660RootDirectory root, HashMap volumeFixups) {
		this.streamHandler = streamHandler;
		this.config = config;
		this.helper = helper;
		this.root = root;
		this.volumeFixups = volumeFixups;

		int dirCount = root.deepDirCount() + 1;
		this.typeLPTFixups = new HashMap(dirCount);
		this.typeMPTFixups = new HashMap(dirCount);
		this.dirFixups = new HashMap(dirCount);
		
		int fileCount = root.deepFileCount() + 1;
		this.fileFixups = new HashMap(fileCount);
		this.locationFixups = new HashMap(fileCount);

		this.emptyFileFixups = new Vector();
	}
	
	public void applyNamingConventions() throws HandlerException {
		NamingConventions namingConventions = helper.getNamingConventions();
		namingConventions.processDirectory(root);
		
		Iterator dit = root.unsortedIterator();
		while (dit.hasNext()) {
			ISO9660Directory dir = (ISO9660Directory) dit.next();
			namingConventions.processDirectory(dir);
		}
	}
	
	public void relocateDirectories() {
		if (root.deepLevelCount()>=8) {
			root.setMovedDirectoryStore();
			relocateDirectories(root);	
		}
	}
	
	private void relocateDirectories(ISO9660Directory dir) {
		Iterator it = dir.sortedIterator();
		while (it.hasNext()) {
			ISO9660Directory subdir = (ISO9660Directory) it.next();
			if (subdir.getLevel()==9) {
				relocate(subdir);
			}
		}
	}
	
	ISO9660Directory relocate(ISO9660Directory dir) {
		return dir.relocate();
	}
	
	public void doPT(Object type) throws HandlerException {
		streamHandler.startElement(new LogicalSectorElement("PT"));
		
		// Write and close Path Table Location Fixup
		HashMap ptFixups;
		long position = streamHandler.mark();
		int location = helper.getCurrentLocation();
		if (type==ISO9660Constants.TYPE_L_PT) {
			ptFixups = typeLPTFixups;
			Fixup ptLocationFixup = (Fixup) volumeFixups.get("typeLPTLocationFixup");
			ptLocationFixup.data(new LSBFWordDataReference(location));
			ptLocationFixup.close();
			volumeFixups.remove("typeLPTLocationFixup");
		} else
		if (type==ISO9660Constants.TYPE_M_PT) {
			ptFixups = typeMPTFixups;
			Fixup ptLocationFixup = (Fixup) volumeFixups.get("typeMPTLocationFixup");
			ptLocationFixup.data(new WordDataReference(location));
			ptLocationFixup.close();
			volumeFixups.remove("typeMPTLocationFixup");
		} else {
			throw new HandlerException("Unknown Path Table Type: " + type);
		}
		
		HashMap parentMapper = new HashMap();
		ISO9660Directory dir = root;
		int dirNumber = 1;
		
		// Root Directory
		ISO9660PathTableRecord rptr = new ISO9660PathTableRecord(streamHandler, type, ISO9660Constants.FI_ROOT, 1);
		ptFixups.put(root, rptr.doPTR());
		parentMapper.put(dir, new Integer(dirNumber));
		
		// Subdirectories
		Iterator it = root.sortedIterator();
		while (it.hasNext()) {
			dirNumber++;
			dir = (ISO9660Directory) it.next();

			// Retrieve parent directory number and reset filename clash detection if appropriate
			int parent = ((Integer) parentMapper.get(dir.getParentDirectory())).intValue();
			
			DataReference ref = helper.getFilenameDataReference(dir);
			ISO9660PathTableRecord ptr = new ISO9660PathTableRecord(streamHandler, type, ref, parent);
			ptFixups.put(dir, ptr.doPTR());
			parentMapper.put(dir, new Integer(dirNumber));
		}
		
		if (volumeFixups.containsKey("ptSizeFixup")) {
			int ptSize = helper.getDifferenceTo(position);
			Fixup ptSizeFixup = (Fixup) volumeFixups.get("ptSizeFixup");
			ptSizeFixup.data(new BothWordDataReference(ptSize));
			ptSizeFixup.close();
			volumeFixups.remove("ptSizeFixup");
		}
		
		streamHandler.endElement();
	}
	
	public void doDRA() throws HandlerException {
		HashMap parentMapper = new HashMap();
		
		// Root Directory
		doDir(root, parentMapper);
		doRootDirFixups(parentMapper);
		
		// Subdirectories
		ISO9660Directory dir = root;
		Iterator it = root.sortedIterator();
		while (it.hasNext()) {
			dir = (ISO9660Directory) it.next();
			doDir(dir, parentMapper);
		}
	}
	
	private void doRootDirFixups(HashMap parentMapper) throws HandlerException {
		ParentInfo parentInfo = (ParentInfo) parentMapper.get(root);
		
		// Write and close Root Directory Location Fixup
		Fixup rootDirLocationFixup = (Fixup) volumeFixups.get("rootDirLocationFixup");
		rootDirLocationFixup.data(new BothWordDataReference(parentInfo.location));
		rootDirLocationFixup.close();
		volumeFixups.remove("rootDirLocationFixup");

		// Write and close Root Directory Length Fixup
		Fixup rootDirLengthFixup = (Fixup) volumeFixups.get("rootDirLengthFixup");
		rootDirLengthFixup.data(new BothWordDataReference(parentInfo.length));
		rootDirLengthFixup.close();
		volumeFixups.remove("rootDirLengthFixup");		
	}
	
	void doDir(ISO9660Directory dir, HashMap parentMapper) throws HandlerException {
		streamHandler.startElement(new LogicalSectorElement("DIR"));
		long position = streamHandler.mark();
		int location = helper.getCurrentLocation();
		
		// "dot": Current Directory
		HashMap dotMemory = doDRLengthFixup(doDotDR(dir));
		Fixup dotLocationFixup = (Fixup) dotMemory.get("drLocationFixup");		
		Fixup dotLengthFixup = (Fixup) dotMemory.get("drDataLengthFixup");
			
		// "dotdot": Parent Directory
		HashMap dotdotMemory = doDRLengthFixup(doDotDotDR(dir));
		Fixup dotdotLocationFixup = (Fixup) dotdotMemory.get("drLocationFixup");
		Fixup dotdotLengthFixup = (Fixup) dotdotMemory.get("drDataLengthFixup");

		// Prepare files and directories to be processed in sorted order
		Vector contents = new Vector();
		contents.addAll(dir.getDirectories());
		contents.addAll(dir.getFiles());
		Collections.sort(contents);
			
		Iterator it = contents.iterator();
		while (it.hasNext()) {
			doBlockCheck(position);
			Object object = it.next();
			if (object instanceof ISO9660Directory) {
				ISO9660Directory subdir = (ISO9660Directory) object;
				if (subdir.isMoved() && dir!=root.getMovedDirectoriesStore()) {
					doDRLengthFixup(doFakeDR(subdir));
				} else {
					doDRLengthFixup(doDR(subdir));
				}
			} else
			if (object instanceof ISO9660File) {
				ISO9660File file = (ISO9660File) object;
				doDRLengthFixup(doDR(file));
			} else {
				throw new HandlerException("Neither file nor directory: " + object);
			}
		}

		streamHandler.endElement();

		// Compute sector-padded length for Directory Data Length Fixups
		int length = helper.getCurrentLocation() - location;
		length *= ISO9660Constants.LOGICAL_BLOCK_SIZE;

		// Save Location and Length to parentMapper
		ParentInfo dotInfo = new ParentInfo();
		dotInfo.location = location;
		dotInfo.length = length;
		parentMapper.put(dir, dotInfo);
		
		// Write and close "dot" Fixups
		dotLocationFixup.data(new BothWordDataReference(location));
		dotLocationFixup.close();
		dotLengthFixup.data(new BothWordDataReference(length));
		dotLengthFixup.close();

		// Retrieve Parent Location and Length from parentMapper
		ParentInfo dotdotInfo = (ParentInfo) parentMapper.get(dir.getParentDirectory());
		
		// Write and close "dotdot" Fixups
		dotdotLocationFixup.data(new BothWordDataReference(dotdotInfo.location));
		dotdotLocationFixup.close();
		dotdotLengthFixup.data(new BothWordDataReference(dotdotInfo.length));
		dotdotLengthFixup.close();
		

		// Write and close Fixups of linking Directory Records
		DirFixupPair fixups = (DirFixupPair) dirFixups.get(dir);
		if (fixups!=null) {	
			// Write and close Location Fixup
			fixups.location.data(new BothWordDataReference(location));
			fixups.location.close();
			
			// Write and close Length Fixup
			fixups.length.data(new BothWordDataReference(length));
			fixups.length.close();
		}
		
		// Write and close Type L Path Table Fixup
		Fixup typeLPTDirLocation = (Fixup) typeLPTFixups.get(dir);
		typeLPTDirLocation.data(new LSBFWordDataReference(location));
		typeLPTDirLocation.close();
		
		// Write and close Type M Path Table Fixup
		Fixup typeMPTDirLocation = (Fixup) typeMPTFixups.get(dir);
		typeMPTDirLocation.data(new WordDataReference(location));
		typeMPTDirLocation.close();
	}
	
	private HashMap doDRLengthFixup(HashMap memory) throws HandlerException {
		int drLength = ((Integer) memory.get("drLength")).intValue();
		memory.remove("drLength");
		
		if (drLength%2==1) {
			// DR length must be an even number, see ISO 9660 section 9.1.13
			streamHandler.data(new ByteDataReference(0));
			drLength++;
		}
		
		if (drLength > 0xFF) {
			throw new HandlerException("Invalid Directory Record Length: " + drLength);
		}
		
		// Write and close Directory Record Length Fixup
		Fixup drLengthFixup = (Fixup) memory.get("drLengthFixup");	 
		drLengthFixup.data(new ByteDataReference(drLength));
		drLengthFixup.close();
		memory.remove("drLengthFixup");

		return memory;
	}

	HashMap doFakeDR(ISO9660Directory dir) throws HandlerException {
		ISO9660File file = new ISO9660File(dir.getName());
		file.setIsMovedDirectory();
		ISO9660DirectoryRecord dr = new ISO9660DirectoryRecord(streamHandler, file, helper);
		HashMap memory = dr.doDR();
		
		// Remember Location Fixup
		Fixup locationFixup = (Fixup) memory.get("drLocationFixup");
		emptyFileFixups.add(locationFixup);
		
		// Write and close Length Fixup
		Fixup dataLengthFixup = (Fixup) memory.get("drDataLengthFixup");
		dataLengthFixup.data(new BothWordDataReference(0));
		dataLengthFixup.close();
		
		return memory;
	}

	HashMap doDR(ISO9660File file) throws HandlerException {
		ISO9660DirectoryRecord dr = new ISO9660DirectoryRecord(streamHandler, file, helper);
		HashMap memory = dr.doDR();
		
		// Remember Location Fixup
		Fixup locationFixup = (Fixup) memory.get("drLocationFixup");
		if (fileFixups.containsKey(file.getID())) {
			throw new RuntimeException("Duplicate file encountered: " + file.getISOPath());
		}
		fileFixups.put(file.getID(), locationFixup);
		
		// Write and close Length Fixup
		Fixup dataLengthFixup = (Fixup) memory.get("drDataLengthFixup");
		dataLengthFixup.data(new BothWordDataReference(file.length()));
		dataLengthFixup.close();
		
		return memory;
	}
	
	HashMap doDR(ISO9660Directory dir) throws HandlerException {
		ISO9660DirectoryRecord dr = new ISO9660DirectoryRecord(streamHandler, dir, helper);
		HashMap memory = dr.doDR();
		
		// Remember Location and Length Fixups
		DirFixupPair dirFixupPair = new DirFixupPair();
		dirFixupPair.location = (Fixup) memory.get("drLocationFixup");
		dirFixupPair.length = (Fixup) memory.get("drDataLengthFixup");
		dirFixups.put(dir, dirFixupPair);
		
		return memory;
	}
	
	HashMap doDotDR(ISO9660Directory dir) throws HandlerException {
		Object dot;
		if (dir==root) {
			dot = ISO9660Constants.FI_ROOT;
		} else {
			dot = ISO9660Constants.FI_DOT;
		}
		
		ISO9660DirectoryRecord dr = new ISO9660DirectoryRecord(streamHandler, dot, dir, helper);
		return dr.doDR();		
	}

	HashMap doDotDotDR(ISO9660Directory dir) throws HandlerException {
		ISO9660Directory parentDir = dir.getParentDirectory();
		Object dotdot = ISO9660Constants.FI_DOTDOT;
		ISO9660DirectoryRecord dr = new ISO9660DirectoryRecord(streamHandler, dotdot, parentDir, helper);
		return dr.doDR();
	}
	
	private void doBlockCheck(long position) throws HandlerException {
		int length = (int) ((streamHandler.mark() - position) % ISO9660Constants.LOGICAL_BLOCK_SIZE);
		int rest = ISO9660Constants.LOGICAL_BLOCK_SIZE - length;
		if (rest < 0xFF) {
			// Maybe not enough space to store another DR in this block
			// -> pad to end of block (see ISO9660:7.8.1.1)
			streamHandler.data(new EmptyByteArrayDataReference(rest));
		}
	}

	public void doFileFixup(ISO9660File file) throws HandlerException {
		if (!fileFixups.containsKey(file.getID())) {
			throw new RuntimeException("File " + file.getID() + " missing: " + file.getISOPath());
		}		
		Fixup locationFixup = (Fixup) fileFixups.get(file.getID());

		int location = helper.getCurrentLocation();
		
		// Hardlink support for ISO9660Files that have the same underlying File
		if (locationFixups.containsKey(file.getContentID())) {
			location = ((Integer) locationFixups.get(file.getContentID())).intValue();
		} else {
			locationFixups.put(file.getContentID(), new Integer(location));
		}
				
		// Write and close File Fixup
		locationFixup.data(new BothWordDataReference(location));
		locationFixup.close();
	}
	
	public void doEmptyFileFixups() throws HandlerException {
		Iterator it = emptyFileFixups.iterator();
		while (it.hasNext()) {			
			streamHandler.startElement(new LogicalSectorElement("DUMMY"));
			
			// Write and close Empty File Fixup
			int location = helper.getCurrentLocation();
			Fixup locationFixup = (Fixup) it.next();
			locationFixup.data(new BothWordDataReference(location));
			locationFixup.close();
			
			streamHandler.endElement();
		}
	}
	
	class DirFixupPair {
		Fixup location, length;
	}
	
	class ParentInfo {
		int location, length;
	}
}