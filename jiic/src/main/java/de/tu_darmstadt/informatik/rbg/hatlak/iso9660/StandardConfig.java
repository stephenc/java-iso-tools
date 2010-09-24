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

import java.io.File;
import java.util.*;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.HandlerException;

public abstract class StandardConfig {
	String systemID, volumeID, volumeSetID;
	Object publisher, dataPreparer, app;
	Date createDate, modDate, expireDate, effectiveDate;
	ISO9660File abstractFile, bibFile, copyrightFile;
	int volSeqNo, volSetSize;

	public StandardConfig() {
		this.systemID = "";
		this.volumeID = "";
		this.volumeSetID = "";
		this.publisher = null;
		this.dataPreparer = null;
		this.app = null;
		this.createDate = new Date();
		this.modDate = createDate;
		this.expireDate = null;
		this.effectiveDate = createDate;
		this.volSeqNo = 1;
		this.volSetSize = 1;
	}
	
	/**
	 * Returns active metadata files<br>
	 * Possibly including Publisher, Data Preparer and Application Identifier
	 * as well as Abstract, Bibliographic and Copyright File
	 * 
	 * @return Vector of all active metadata files
	 */
	public Vector getFiles() {
		Vector files = new Vector();
		
		if (publisher instanceof ISO9660File) {
			files.add(publisher);
		}
		if (dataPreparer instanceof ISO9660File) {
			files.add(dataPreparer);
		}
		if (app instanceof ISO9660File) {
			files.add(app);
		}		
		
		files.add(abstractFile);
		files.add(bibFile);
		files.add(copyrightFile);
		
		return files;
	}
	
	/**
	 * Returns active Abstract file
	 * 
	 * @return Active Abstract file
	 */
	public ISO9660File getAbstractFile() {
		return abstractFile;
	}

	/**
	 * Set Abstract file
	 * 
	 * @param abstractFile File containing Abstract information
	 * (to be recorded in root directory)
	 * @throws HandlerException Problems converting to ISO9660File
	 */
	public void setAbstractFile(File abstractFile) throws HandlerException {
		this.abstractFile = new ISO9660File(abstractFile);
	}

	/**
	 * Returns active Bibliographic file
	 * 
	 * @return Active Bibliographic file
	 */
	public ISO9660File getBibFile() {
		return bibFile;
	}

	/**
	 * Set Bibliographic file
	 * 
	 * @param bibFile File containing Bibliographic information
	 * (to be recorded in root directory)
	 * @throws HandlerException Problems converting to ISO9660File
	 */
	public void setBibFile(File bibFile) throws HandlerException {
		this.bibFile = new ISO9660File(bibFile);
	}

	/**
	 * Returns active Copyright file
	 * 
	 * @return Active Copyright file
	 */
	public ISO9660File getCopyrightFile() {
		return copyrightFile;
	}

	/**
	 * Set Copyright file
	 * 
	 * @param copyrightFile File containing Copyright information
	 * (to be recorded in root directory)
	 * @throws HandlerException Problems converting to ISO9660File
	 */
	public void setCopyrightFile(File copyrightFile) throws HandlerException {
		this.copyrightFile = new ISO9660File(copyrightFile);
	}

	/**
	 * Returns active Volume Sequence Number
	 * 
	 * @return Active Volume Sequence Number
	 */
	public int getVolumeSequenceNumber() {
		return volSeqNo;
	}

	/**
	 * Set Volume Sequence Number
	 * 
	 * @param volSeqNo Volume Sequence Number
	 * @throws ConfigException Invalid number
	 */
	public void setVolumeSequenceNumber(int volSeqNo) throws ConfigException {
		if (volSeqNo < 1 || volSeqNo > 65535) {
			throw new ConfigException(this, "The Volume Sequence Number must be a positive integer.");
		}
		this.volSeqNo = volSeqNo;
	}

	/**
	 * Returns active Volume Set Identifier
	 * 
	 * @return Active Volume Set Identifier
	 */
	public String getVolumeSetID() {
		return volumeSetID;
	}

	/**
	 * Set Volume Set Identifier
	 * 
	 * @param volumeSetID Volume Set Identifier
	 * @throws ConfigException String too long
	 */
	public void setVolumeSetID(String volumeSetID) throws ConfigException {
		if (volumeSetID.length() > 128) {
			throw new ConfigException(this, "The Volume Set ID may be no longer than 128 characters.");
		}
		this.volumeSetID = volumeSetID;
	}

	/**
	 * Returns active Volume Set Size
	 * 
	 * @return Active Volume Set Size
	 */
	public int getVolumeSetSize() {
		return volSetSize;
	}

	/**
	 * Set Volume Set Size
	 * 
	 * @param volSetSize Volume Set Size
	 * @throws ConfigException Invalid number
	 */
	public void setVolumeSetSize(int volSetSize) throws ConfigException {
		if (volSetSize < 1 || volSetSize > 65535) {
			throw new ConfigException(this, "The Volume Set Size must be a positive integer.");
		}
		this.volSetSize = volSetSize;
	}

	/**
	 * Returns active Application Identifier
	 * 
	 * @return Active Application Identifier (String or ISO9660File)
	 */
	public Object getApp() {
		return app;
	}

	/**
	 * Set Application Identifier
	 * 
	 * @param app Application Identifier
	 * @throws ConfigException String too long
	 */
	public void setApp(String app) throws ConfigException {
		if (app.length() > 128) {
			throw new ConfigException(this, "The Application Identifier may be no longer than 128 characters.");
		}
		this.app = app;
	}

	/**
	 * Set Application Identifier
	 * 
	 * @param app File containing information on the Application
	 * @throws HandlerException Problems converting to ISO9660File
	 */
	public void setApp(File app) throws HandlerException {
		this.app = new ISO9660File(app);
	}

	/**
	 * Returns active Creation Date
	 * 
	 * @return Active Creation Date
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * Set Creation Date
	 * 
	 * @param createDate Date when volume was created
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	/**
	 * Returns active Data Preparer
	 * 
	 * @return Active Data Preparer (String or ISO9660File)
	 */
	public Object getDataPreparer() {
		return dataPreparer;
	}

	/**
	 * Set Data Preparer
	 * 
	 * @param dataPreparer Data Preparer of the volume
	 * @throws ConfigException String too long
	 */
	public void setDataPreparer(String dataPreparer) throws ConfigException {
		if (dataPreparer.length() > 128) {
			throw new ConfigException(this, "The Data Preparer Identifier may be no longer than 128 characters.");
		}
		this.dataPreparer = dataPreparer;
	}

	/**
	 * Set Data Preparer
	 * 
	 * @param dataPreparer File containting information on the volume data preparer
	 * @throws HandlerException Problems converting to ISO9660File
	 */
	public void setDataPreparer(File dataPreparer) throws HandlerException {
		this.dataPreparer = new ISO9660File(dataPreparer);
	}

	/**
	 * Returns active Effective Date
	 * 
	 * @return Active Effective Date
	 */
	public Date getEffectiveDate() {
		return effectiveDate;
	}

	/**
	 * Set Effective Date
	 * 
	 * @param effectiveDate Date when volume data is effective
	 */
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	/**
	 * Returns active Expiration Date
	 * 
	 * @return Active Expiration Date
	 */
	public Date getExpireDate() {
		return expireDate;
	}

	/**
	 * Set Expiration Date
	 * 
	 * @param expireDate Date when volume data expires
	 */
	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

	/**
	 * Returns active Modification Date
	 * 
	 * @return Active Modification Date
	 */
	public Date getModDate() {
		return modDate;
	}

	/**
	 * Set Modification Date
	 * 
	 * @param modDate Date when volume data was last modified
	 */
	public void setModDate(Date modDate) {
		this.modDate = modDate;
	}

	/**
	 * Returns active Publisher
	 * 
	 * @return Publisher
	 */
	public Object getPublisher() {
		return publisher;
	}

	/**
	 * Set Publisher
	 * 
	 * @param publisher Publisher of the volume
	 * @throws ConfigException String too long
	 */
	public void setPublisher(String publisher) throws ConfigException {
		if (publisher.length() > 128) {
			throw new ConfigException(this, "The Publisher Identifier may be no longer than 128 characters.");
		}
		this.publisher = publisher;
	}

	/**
	 * Set Publisher
	 * 
	 * @param publisher File containing information on the volume publisher
	 * @throws HandlerException Problems converting to ISO9660File
	 */
	public void setPublisher(File publisher) throws HandlerException {
		this.publisher = new ISO9660File(publisher);
	}

	/**
	 * Returns active System Identifier
	 * 
	 * @return Active System Identifer
	 */
	public String getSystemID() {
		return systemID;
	}

	/**
	 * Set System Identifier
	 * 
	 * @param systemID Identifier for the system that can act upon the System Area
	 * @throws ConfigException String too long
	 */
	public void setSystemID(String systemID) throws ConfigException {
		if (systemID.length() > 32) {
			throw new ConfigException(this, "The System Identifier may be no longer than 32 characters.");
		}
		this.systemID = systemID;
	}

	/**
	 * Returns active Volume Identifier
	 * 
	 * @return Active Volume Identifier
	 */
	public String getVolumeID() {
		return volumeID;
	}

	/**
	 * Set Volume Identifier
	 * 
	 * @param volumeID Identifier for the volume (disc name)
	 * @throws ConfigException String too long
	 */
	public void setVolumeID(String volumeID) throws ConfigException {
		if (volumeID.length() > 32) {
			throw new ConfigException(this, "The Volume Identifier may be no longer than 32 characters.");
		}
		this.volumeID = volumeID;
	}
}
