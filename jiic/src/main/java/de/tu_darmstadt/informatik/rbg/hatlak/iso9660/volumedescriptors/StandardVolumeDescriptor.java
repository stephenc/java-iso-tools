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

package de.tu_darmstadt.informatik.rbg.hatlak.iso9660.volumedescriptors;

import java.nio.ByteBuffer;
import java.util.*;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ISO9660File;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.LayoutHelper;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.StandardConfig;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.*;
import de.tu_darmstadt.informatik.rbg.hatlak.sabre.impl.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public abstract class StandardVolumeDescriptor extends ISO9660VolumeDescriptor {
	String systemId, volumeId, volSetId;
	int volSetSize, volSeqNo;
	Object publisher, dataPreparer, app;
	ISO9660File copyrightFile, abstractFile, bibFile;
	Date createDate, modDate, expireDate, effectiveDate;
	
	public StandardVolumeDescriptor(StreamHandler streamHandler, int type, LayoutHelper helper) {
		super(streamHandler, type, helper);
		this.systemId = this.volumeId = this.volSetId = "";
		this.volSetSize = this.volSeqNo = 1;
		this.publisher = this.dataPreparer = this.app = null;
		this.copyrightFile = this.abstractFile = this.bibFile = null;
		this.createDate = this.modDate = this.expireDate = this.effectiveDate = null;
	}
	
	public void setMetadata(StandardConfig config) {
		setSystemId(config.getSystemID());
		setVolumeId(config.getVolumeID());
		setPublisher(config.getPublisher());
		setDataPreparer(config.getDataPreparer());
		setApp(config.getApp());
		setCreateDate(config.getCreateDate());
		setModDate(config.getModDate());
		setExpireDate(config.getExpireDate());
		setEffectiveDate(config.getEffectiveDate());
		setAbstractFile(config.getAbstractFile());
		setBibFile(config.getBibFile());
		setCopyrightFile(config.getCopyrightFile());
		setVolSeqNo(config.getVolumeSequenceNumber());
		setVolSetId(config.getVolumeSetID());
		setVolSetSize(config.getVolumeSetSize());
	}
	
	public void setAbstractFile(ISO9660File abstractFile) {
		this.abstractFile = abstractFile;
	}

	public void setApp(Object app) {
		this.app = app;
	}

	public void setBibFile(ISO9660File bibFile) {
		this.bibFile = bibFile;
	}

	public void setCopyrightFile(ISO9660File copyrightFile) {
		this.copyrightFile = copyrightFile;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setDataPreparer(Object dataPreparer) {
		this.dataPreparer = dataPreparer;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

	public void setModDate(Date modDate) {
		this.modDate = modDate;
	}

	public void setPublisher(Object publisher) {
		this.publisher = publisher;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public void setVolSeqNo(int volSeqNo) {
		this.volSeqNo = volSeqNo;
	}

	public void setVolSetId(String volSetId) {
		this.volSetId = volSetId;
	}

	public void setVolSetSize(int volSetSize) {
		this.volSetSize = volSetSize;
	}

	public void setVolumeId(String volumeId) {
		this.volumeId = volumeId;
	}

	HashMap doStandardVD() throws HandlerException {
		HashMap memory = new HashMap();

		// Volume Descriptor Type: Primary
		streamHandler.data(getType());

		// Standard Identifier
		streamHandler.data(getStandardId());

		// Volume Descriptor Version
		streamHandler.data(getVDVersion());

		// Unused Field / Volume Flags (SVD): 1 byte
		Fixup vf = streamHandler.fixup(new ByteDataReference(0));
		memory.put("volumeFlagsFixup", vf);

		// System Identifier: 32 bytes
		streamHandler.data(getSystemId());

		// Volume Identifier: 32 bytes
		streamHandler.data(getVolumeId());

		// Unused Field: 8 bytes
		streamHandler.data(new EmptyByteArrayDataReference(8));

		// Volume Space Size
		Fixup vss = streamHandler.fixup(new BothWordDataReference(0));
		memory.put("volumeSpaceSizeFixup", vss);

		// Unused Field / Escape Sequences (SVD): 32 bytes
		Fixup es = streamHandler.fixup(new EmptyByteArrayDataReference(32));
		memory.put("escapeSequencesFixup", es);

		// Volume Set Size
		streamHandler.data(getVolumeSetSize());

		// Volume Sequence Number
		streamHandler.data(getVolumeSeqNo());

		// Logical Block Size
		streamHandler.data(getLogicalBlockSize());

		// Path Table Size
		Fixup pts = streamHandler.fixup(new BothWordDataReference(0));
		memory.put("ptSizeFixup", pts);

		// Type L Path Table Location
		Fixup tlpt = streamHandler.fixup(new LSBFWordDataReference(0));
		memory.put("typeLPTLocationFixup", tlpt);
		// Optional Type L Path Table Location: none
		streamHandler.data(new LSBFWordDataReference(0));

		// Type M Path Table Location
		Fixup tmpt = streamHandler.fixup(new WordDataReference(0));
		memory.put("typeMPTLocationFixup", tmpt);
		// Optional Type M Path Table Location: none
		streamHandler.data(new WordDataReference(0));

		// Directory Record for Root Directory: 34 bytes
		doRootDR(memory);

		// Volume Set Identifier: 128 bytes
		streamHandler.data(getVolumeSetId());

		// Publisher Identifier: 128 bytes
		streamHandler.data(getIdOrFile(publisher));
		// Data Preparer Identifier: 128 bytes
		streamHandler.data(getIdOrFile(dataPreparer));
		// Application Identifier: 128 bytes
		streamHandler.data(getIdOrFile(app));

		// Copyright File Identifier: 37 bytes
		streamHandler.data(getFile(copyrightFile));
		// Abstract File Identifier: 37 bytes
		streamHandler.data(getFile(abstractFile));
		// Bibliographic File Identifier: 37 bytes
		streamHandler.data(getFile(bibFile));

		// Volume Creation Date and Time: 17 bytes
		streamHandler.data(new ISO9660DateDataReference(createDate));
		// Volume Modification Date and Time: 17 bytes
		streamHandler.data(new ISO9660DateDataReference(modDate));
		// Volume Expiration Date and Time: 17 bytes
		streamHandler.data(new ISO9660DateDataReference(expireDate));
		// Volume Effective Date and Time: 17 bytes
		streamHandler.data(new ISO9660DateDataReference(effectiveDate));

		// File Structure Version
		streamHandler.data(getFileStructureVersion());

		// Reserved Field
		streamHandler.data(new ByteDataReference(0));

		// Application Use and reserved bytes: handle externally

		return memory;
	}
	
	private void doRootDR(HashMap memory) throws HandlerException {
		ISO9660DirectoryRecord rddr = new ISO9660DirectoryRecord(streamHandler, ISO9660Constants.FI_ROOT, helper.getRoot(), helper);
		HashMap drMemory = rddr.doDR();
		
		// Length of Directory Record
		Fixup drLengthFixup = (Fixup) drMemory.get("drLengthFixup");
		int drLength = ((Integer) drMemory.get("drLength")).intValue(); 
		drLengthFixup.data(new ByteDataReference(drLength));
		drLengthFixup.close();
		
		// Root Directory Location
		Fixup rootDirLocation = (Fixup) drMemory.get("drLocationFixup");
		memory.put("rootDirLocationFixup", rootDirLocation);

		// Root Directory Length
		Fixup rootDirLength = (Fixup) drMemory.get("drDataLengthFixup");
		memory.put("rootDirLengthFixup", rootDirLength);
	}

	private ByteArrayDataReference getSystemId()
			throws HandlerException {
		byte[] bytes = helper.pad(systemId, 32);
		return new ByteArrayDataReference(bytes);
	}

	private ByteArrayDataReference getVolumeId()
			throws HandlerException {
		byte[] bytes = helper.pad(volumeId, 32);
		return new ByteArrayDataReference(bytes);
	}

	private BothShortDataReference getVolumeSetSize() {
		return new BothShortDataReference(volSetSize);
	}

	private BothShortDataReference getVolumeSeqNo() {
		return new BothShortDataReference(volSeqNo);
	}
	
	private BothShortDataReference getLogicalBlockSize() {
		return new BothShortDataReference(ISO9660Constants.LOGICAL_BLOCK_SIZE);
	}

	private ByteDataReference getFileStructureVersion() {
		return new ByteDataReference(ISO9660Constants.FSV);
	}
	
	private DataReference getVolumeSetId() throws HandlerException {
		byte[] bytes = helper.pad(volSetId, 128);
		return new ByteArrayDataReference(bytes);
	}
	
	DataReference getIdOrFile(Object object) throws HandlerException {
		String id;
		byte[] bytes;

		if (object == null) {
			bytes = helper.pad("", 128);
		} else if (object instanceof String) {
			id = (String) object;
			bytes = helper.pad(id, 128);
		} else if (object instanceof ISO9660File) {
			ByteBuffer buf = ByteBuffer.allocate(128);
			buf.put((byte) 0x5F);
			ISO9660File file = (ISO9660File) object;
			file.enforce8plus3(true);
			id = helper.getFilenameDataReference(file).getName();
			buf.put(helper.pad(id, 127));
			bytes = buf.array();
		} else {
			throw new HandlerException(
					"String or ISO9660File expected in getIdOrFile, got "
							+ object.getClass());
		}

		return new ByteArrayDataReference(bytes);
	}

	DataReference getFile(ISO9660File file) throws HandlerException {
		String id;
		byte[] bytes;

		if (file == null) {
			id = "";
			bytes = helper.pad(id, 37);
		} else {
			file.enforce8plus3(true);
			id = helper.getFilenameDataReference(file).getName();
			bytes = helper.pad(id, 37);
		}

		return new ByteArrayDataReference(bytes);
	}
}
