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

package de.tu_darmstadt.informatik.rbg.hatlak.iso9660;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;

import de.tu_darmstadt.informatik.rbg.hatlak.eltorito.impl.ElToritoConfig;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.CreateISO;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.ISO9660Config;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.ISOImageFileHandler;
import de.tu_darmstadt.informatik.rbg.hatlak.joliet.impl.JolietConfig;
import de.tu_darmstadt.informatik.rbg.hatlak.rockridge.impl.RockRidgeConfig;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.HandlerException;

public class ISOTask extends MatchingTask {
	private File baseDir, destFile, bootImage;
	private Vector filesets;
	private String name, system, publisher, dataPreparer, copyrightFile, bootImageID,
		bootImageEmulation, bootImagePlatformID, movedDirectoriesStoreName;
	private boolean allowASCII, restrictDirDepthTo8, forceDotDelimiter,
		mkisofsCompatibility, forcePortableFilenameCharacterSet,
		enableJoliet, enableRockRidge, hideMovedDirectoriesStore, verbose,
		genBootInfoTable, padEnd;
	private int interchangeLevel, bootImageSectorCount, bootImageLoadSegment;

	public void init() {
		baseDir = destFile = bootImage = null;
		filesets = new Vector();
		name = system = publisher = dataPreparer = bootImageID = "";
		copyrightFile = null;
		movedDirectoriesStoreName = "rr_moved";
		allowASCII = false;
		restrictDirDepthTo8 = true;
		forceDotDelimiter = true;
		interchangeLevel = 1;
		mkisofsCompatibility = true;
		forcePortableFilenameCharacterSet = true;
		enableJoliet = true;
		enableRockRidge = true;
		hideMovedDirectoriesStore = true;
		verbose = false;
		bootImageSectorCount = 1;
		bootImageLoadSegment = ElToritoConfig.LOAD_SEGMENT_7C0;
		bootImageEmulation = bootImagePlatformID = "";
		genBootInfoTable = false;
		padEnd = true;
	}

	public void execute() throws BuildException {
		if (baseDir == null && filesets.size()==0) {
			throw new BuildException("basedir attribute must be set, "
                    + "or at least "
                    + "one fileset must be given!");
        }

		if (destFile == null) {
			throw new BuildException("destfile attribute must be set!");
        }
		
		try {
			// Verbosity
			NamingConventions.VERBOSE = verbose;
			
			// Build directory hierarchy
			ISO9660RootDirectory.MOVED_DIRECTORIES_STORE_NAME = movedDirectoriesStoreName;
			ISO9660RootDirectory root = new ISO9660RootDirectory();
			createHierarchy(root);

			File copyrightFileObj = null;
			if (copyrightFile!=null) {
				copyrightFileObj = new File(copyrightFile);
			}

			// ISO9660 support
			ISO9660Config iso9660Config = new ISO9660Config();
			iso9660Config.setVolumeID(name);
			iso9660Config.setSystemID(system);
			iso9660Config.setPublisher(publisher);
			iso9660Config.setDataPreparer(dataPreparer);
			iso9660Config.allowASCII(allowASCII);
			iso9660Config.restrictDirDepthTo8(restrictDirDepthTo8);
			iso9660Config.forceDotDelimiter(forceDotDelimiter);
			iso9660Config.setInterchangeLevel(interchangeLevel);
			iso9660Config.setPadEnd(padEnd);
			if (copyrightFileObj!=null) {
				iso9660Config.setCopyrightFile(copyrightFileObj);
			}
			
			// Rock Ridge support
			RockRidgeConfig rrConfig = null;
			if (enableRockRidge) {
				log("Rock Ridge support enabled.");
				rrConfig = new RockRidgeConfig();
				rrConfig.setMkisofsCompatibility(mkisofsCompatibility);
				rrConfig.hideMovedDirectoriesStore(hideMovedDirectoriesStore);
				rrConfig.forcePortableFilenameCharacterSet(forcePortableFilenameCharacterSet);
			}
	
			// Joliet support
			JolietConfig jolietConfig = null;
			if (enableJoliet) {
				log("Joliet support enabled.");
				jolietConfig = new JolietConfig();
				jolietConfig.setVolumeID(name);
				jolietConfig.setSystemID(system);
				jolietConfig.setPublisher(publisher);
				jolietConfig.setDataPreparer(dataPreparer);
				jolietConfig.forceDotDelimiter(forceDotDelimiter);
				if (copyrightFileObj!=null) {
					jolietConfig.setCopyrightFile(copyrightFileObj);
				}
			}
	
			// El Torito support
			ElToritoConfig elToritoConfig = null;
			if (bootImage!=null) {
				log("El Torito support enabled.");
				elToritoConfig = new ElToritoConfig(bootImage, getBootEmulation(),
						getBootPlatformID(), bootImageID, bootImageSectorCount,
						bootImageLoadSegment);
				elToritoConfig.setGenBootInfoTable(genBootInfoTable);
			}
	
			CreateISO iso = new CreateISO(new ISOImageFileHandler(destFile), root);
			iso.process(iso9660Config, rrConfig, jolietConfig, elToritoConfig);
			log("Successfully created ISO image " + destFile + ".");
		} catch (ConfigException ce) {
			throw new BuildException(ce);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(e);
		}
	}
	
	private int getBootEmulation() {
		if (bootImageEmulation.matches(".*1.*2.*")) {
			// 1.2 MB diskette
			return ElToritoConfig.BOOT_MEDIA_TYPE_1_2MEG_DISKETTE;
		} // else

		if (bootImageEmulation.matches(".*44.*")) {
			// 1.44 MB diskette
			return ElToritoConfig.BOOT_MEDIA_TYPE_1_44MEG_DISKETTE;
		} // else

		if (bootImageEmulation.matches(".*88.*")) {
			// 2.88 MB diskette
			return ElToritoConfig.BOOT_MEDIA_TYPE_2_88MEG_DISKETTE;
		} // else

		if (bootImageEmulation.matches(".*(hd|hard).*")) {
			// Hard disk
			return ElToritoConfig.BOOT_MEDIA_TYPE_HD;
		} // else
		
		// Default: No Emulation
		return ElToritoConfig.BOOT_MEDIA_TYPE_NO_EMU;
	}

	private int getBootPlatformID() {
		if (bootImagePlatformID.equalsIgnoreCase("mac") || 
			bootImagePlatformID.equalsIgnoreCase("macintosh") ||
			bootImagePlatformID.equalsIgnoreCase("apple")) {
			// Apple Macintosh
			return ElToritoConfig.PLATFORM_ID_MAC;
		} // else

		if (bootImagePlatformID.equalsIgnoreCase("ppc") ||
			bootImagePlatformID.equalsIgnoreCase("powerpc")) {
			// PowerPC
			return ElToritoConfig.PLATFORM_ID_PPC;
		} // else

		if (bootImagePlatformID.equalsIgnoreCase("efi")) {
			// EFI
			return ElToritoConfig.PLATFORM_ID_EFI;
		} // else

		// Default: X86
		return ElToritoConfig.PLATFORM_ID_X86;
	}
	
	private void createHierarchy(ISO9660RootDirectory root) throws HandlerException, IOException {
        if (baseDir != null) {
            FileSet fs = (FileSet) getImplicitFileSet().clone();
            fs.setDir(baseDir);
            filesets.addElement(fs);
        }

		Iterator it = filesets.iterator();
        while (it.hasNext()) {
        	FileSet fs = (FileSet) it.next();
        	
        	String prefix = "";
            if (fs instanceof ISOFileSet) {
            	ISOFileSet ifs = (ISOFileSet) fs;
                prefix = ifs.getPrefix();
            }

        	createHierarchy(root, fs, prefix);
        }
	}

	private void createHierarchy(ISO9660RootDirectory root, FileSet fs, String prefix) throws HandlerException {
		DirectoryScanner ds = fs.getDirectoryScanner(getProject());

		// Add directories
		String[] dirs = ds.getIncludedDirectories();
        for (int i = 0; i < dirs.length; i++) {
        	if (!dirs[i].equals("")) {
        		String path = checkPrefix(dirs[i], prefix);
        		root.addPath(path);
        	}
        }

        // Add files
		String[] files = ds.getIncludedFiles();
        for (int i = 0; i < files.length; i++) {
        	ISO9660Directory dir = root;
        	String path = checkPrefix(files[i], prefix);
        	if (path.indexOf(File.separator) >= 0) {
        		path = path.substring(0, path.lastIndexOf(File.separator));
            	dir = root.addPath(path);
        	}
            dir.addFile(new File(ds.getBasedir(), files[i]));
        }		
	}
	
	private String checkPrefix(String path, String prefix) {
		if (prefix.length() > 0) {
			prefix += "/";
		}
		
		path = prefix + path;
		path = path.replace('\\', File.separatorChar);
		path = path.replace('/', File.separatorChar);
		
		return path;
	}
	
	public void setDestFile(File destFile) {
		this.destFile = destFile;
	}
	
	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	public void addFileset(FileSet set) {
		set.setDir(baseDir);
		filesets.addElement(set);
	}
	
	public void addISOFileset(ISOFileSet set) {
		set.setDir(baseDir);
		filesets.addElement(set);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public void setDataPreparer(String dataPreparer) {
		this.dataPreparer = dataPreparer;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public void setCopyrightFile(String copyrightFile) {
		this.copyrightFile = copyrightFile;
	}	
	
	public void setAllowASCII(boolean allowASCII) {
		this.allowASCII = allowASCII;
	}

	public void setForceDotDelimiter(boolean forceDotDelimiter) {
		this.forceDotDelimiter = forceDotDelimiter;
	}

	public void setRestrictDirDepthTo8(boolean restrictDirDepthTo8) {
		this.restrictDirDepthTo8 = restrictDirDepthTo8;
	}

	public void setInterchangeLevel(int interchangeLevel) {
		this.interchangeLevel = interchangeLevel;
	}

	public void setForcePortableFilenameCharacterSet(
			boolean forcePortableFilenameCharacterSet) {
		this.forcePortableFilenameCharacterSet = forcePortableFilenameCharacterSet;
	}

	public void setMkisofsCompatibility(boolean mkisofsCompatibility) {
		this.mkisofsCompatibility = mkisofsCompatibility;
	}

	public void setEnableJoliet(boolean enableJoliet) {
		this.enableJoliet = enableJoliet;
	}

	public void setEnableRockRidge(boolean enableRockRidge) {
		this.enableRockRidge = enableRockRidge;
	}
	
	public void setHideMovedDirectoriesStore(boolean hideMovedDirectoriesStore) {
		this.hideMovedDirectoriesStore = hideMovedDirectoriesStore;
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setBootImage(File bootImage) {
		this.bootImage = bootImage;
	}

	public void setBootImageSectorCount(int bootImageSectorCount) {
		this.bootImageSectorCount = bootImageSectorCount;
	}

	public void setBootImageID(String bootImageID) {
		this.bootImageID = bootImageID;
	}

	public void setBootImageLoadSegment(int bootImageLoadSegment) {
		this.bootImageLoadSegment = bootImageLoadSegment;
	}

	public void setBootImageEmulation(String bootImageEmulation) {
		this.bootImageEmulation = bootImageEmulation;
	}

	public void setBootImagePlatformID(String bootImagePlatformID) {
		this.bootImagePlatformID = bootImagePlatformID;
	}

	public void setMovedDirectoriesStoreName(String movedDirectoriesStoreName) {
		this.movedDirectoriesStoreName = movedDirectoriesStoreName;
	}

	public void setGenBootInfoTable(boolean genBootInfoTable) {
		this.genBootInfoTable = genBootInfoTable;
	}

	public void setPadEnd(boolean padEnd) {
		this.padEnd = padEnd;
	}
}
