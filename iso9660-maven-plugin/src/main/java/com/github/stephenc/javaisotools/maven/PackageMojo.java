/*
 * Copyright (c) 2010. Stephen Connolly.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.github.stephenc.javaisotools.maven;

import java.io.File;
import java.io.FileNotFoundException;

import com.github.stephenc.javaisotools.eltorito.impl.ElToritoConfig;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.iso9660.StandardConfig;
import com.github.stephenc.javaisotools.iso9660.impl.ISOImageFileHandler;
import com.github.stephenc.javaisotools.rockridge.impl.RockRidgeConfig;
import com.github.stephenc.javaisotools.sabre.StreamHandler;
import com.github.stephenc.javaisotools.iso9660.ConfigException;
import com.github.stephenc.javaisotools.iso9660.impl.CreateISO;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Config;
import com.github.stephenc.javaisotools.joliet.impl.JolietConfig;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * Creates an iso9660 image.
 *
 * @goal iso
 * @phase package
 */
public class PackageMojo extends AbstractMojo {

    /**
     * The directory to place the iso9660 image.
     *
     * @parameter default-value="${project.build.directory}"
     */
    private File outputDirectory;

    /**
     * The directory to capture the content from.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     */
    private File inputDirectory;

    /**
     * The name of the file to create.
     *
     * @parameter default-value="${project.build.finalName}.${project.packaging}"
     */
    private String finalName;

    /**
     * The system id.
     *
     * @parameter
     */
    private String systemId;

    /**
     * The volume id.
     *
     * @parameter default-value="${project.artifactId}"
     */
    private String volumeId;

    /**
     * The volume set id.
     *
     * @parameter
     */
    private String volumeSetId;

    /**
     * The publisher.
     *
     * @parameter default-value="${project.organization.name}"
     */
    private String publisher;

    /**
     * The preparer.
     *
     * @parameter default-value="${project.organization.name}"
     */
    private String preparer;

    /**
     * The application.
     *
     * @parameter default-value="iso9660-maven-plugin"
     */
    private String application;

    /**
     * Moved Directories Store Name.
     *
     * @parameter default-value="rr_moved"
     */
    private String movedDirectoriesStoreName;

    /**
     * The volume sequence number.
     *
     * @parameter
     */
    private Integer volumeSequenceNumber;

    /**
     * The volume set size.
     */
    private Integer volumeSetSize;
    
    /**
	* The maven project.  This is injected by Maven.
	* 
	* @parameter expression="${project}"
	* @required
	* @readonly
	*/
	private MavenProject project;

    /**
     * enable RockRidge.
     *
     * @parameter default-value="true"
     */
    private Boolean enableRockRidge;

    /**
     * enable Joliet.
     *
     * @parameter default-value="true"
     */
    private Boolean enableJoliet;

    /**
     * Allow Ascii.
     *
     * @parameter default-value="false"
     */
    private Boolean allowASCII;

    /**
     * The Interchange Level.
     *
     * @parameter default-value="1"
     */
    private Integer interchangeLevel;

    /**
     * Pad End.
     *
     * @parameter default-value="true"
     */
    private Boolean padEnd;

    /**
     * Restric Directory Depth to 8.
     *
     * @parameter default-value="true"
     */
    private Boolean restrictDirDepthTo8;

    /**
     * Force Dot Delimiter.
     *
     * @parameter default-value="true"
     */
    private Boolean forceDotDelimiter;

    /**
     * Mkisofs Compatibility.
     *
     * @parameter default-value="false"
     */
    private Boolean mkisofsCompatibility;

    /**
     * Hide Moved Directories Store.
     *
     * @parameter default-value="true"
     */
    private Boolean hideMovedDirectoriesStore;

    /**
     * Force Portable Filename CharacterSet
     *
     * @parameter default-value="true"
     */
    private Boolean forcePortableFilenameCharacterSet;

    /**
     * Boot Image ID
     *
     * @parameter default-value=""
     */
    private String bootImagePlatformID;

    /**
     * Boot Image Emulation
     *
     * @parameter default-value=""
     */
    private String bootImageEmulation;

    /**
     * The boot Image.
     *
     * @parameter
     */
    private File bootImage;

    /**
     * Boot Image ID
     *
     * @parameter default-value=""
     */
    private String bootImageID;

    /**
     * Boot Image SectorCount
     *
     * @parameter default-value="1"
     */
    private Integer bootImageSectorCount;

    /**
     * Boot Image SectorCount
     *
     * @parameter default-value="0"
     */
    private Integer bootImageLoadSegment;

    /**
     * Generate Boot Info Table
     *
     * @parameter default-value="false"
     */
    private boolean genBootInfoTable;

    /**
     * @parameter default-value="64"
     */
	private Integer maxJolietFilenameLength = 64;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (outputDirectory.isFile()) {
            throw new MojoExecutionException("Output directory: " + outputDirectory + " is a file");
        }
        outputDirectory.mkdirs();
        if (!outputDirectory.isDirectory()) {
            throw new MojoExecutionException("Could not create output directory: " + outputDirectory);
        }
        
        // Directory hierarchy, starting from the root
        ISO9660RootDirectory.MOVED_DIRECTORIES_STORE_NAME = movedDirectoriesStoreName;
        ISO9660RootDirectory root = new ISO9660RootDirectory();

        File isoFile = new File(outputDirectory, finalName);
        
        try {
            if (inputDirectory.isDirectory()) {
                root.addContentsRecursively(inputDirectory);
            }

            StreamHandler streamHandler = new ISOImageFileHandler(isoFile);
            CreateISO iso = new CreateISO(streamHandler, root);
            ISO9660Config iso9660Config = new ISO9660Config();
            iso9660Config.allowASCII(allowASCII.booleanValue());
            iso9660Config.setInterchangeLevel(interchangeLevel.intValue());
            iso9660Config.restrictDirDepthTo8(restrictDirDepthTo8.booleanValue());
            iso9660Config.forceDotDelimiter(forceDotDelimiter.booleanValue());
            iso9660Config.setInterchangeLevel(interchangeLevel.intValue());
            iso9660Config.setPadEnd(padEnd.booleanValue());
            applyConfig(iso9660Config);

            RockRidgeConfig rrConfig = null;

            if (enableRockRidge.booleanValue()) {
	            rrConfig = new RockRidgeConfig();
	            rrConfig.setMkisofsCompatibility(mkisofsCompatibility.booleanValue());
	            rrConfig.hideMovedDirectoriesStore(hideMovedDirectoriesStore.booleanValue());
	            rrConfig.forcePortableFilenameCharacterSet(forcePortableFilenameCharacterSet.booleanValue());
            }

            JolietConfig jolietConfig = null;

            if (enableJoliet.booleanValue()) {
	            jolietConfig = new JolietConfig();
	            jolietConfig.forceDotDelimiter(forceDotDelimiter.booleanValue());
	            jolietConfig.setMaxCharsInFilename(maxJolietFilenameLength);
	            applyConfig(jolietConfig);
            }

            // El Torito support
            ElToritoConfig elToritoConfig = null;
            if (bootImage != null) {
                this.getLog().info("El Torito support enabled.");
                elToritoConfig = new ElToritoConfig(bootImage, getBootEmulation(),
                        getBootPlatformID(), bootImageID, bootImageSectorCount,
                        bootImageLoadSegment);
                elToritoConfig.setGenBootInfoTable(genBootInfoTable);
            }

            iso.process(iso9660Config, rrConfig, jolietConfig, elToritoConfig);
        } catch (HandlerException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ConfigException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        
        project.getArtifact().setFile(isoFile);
    }

    private void applyConfig(StandardConfig config) throws ConfigException {
        if (StringUtils.isNotEmpty(systemId)) {
            config.setSystemID(systemId);
        }
        if (StringUtils.isNotEmpty(volumeId)) {
            config.setVolumeID(volumeId);
        }
        if (StringUtils.isNotEmpty(volumeSetId)) {
            config.setVolumeSetID(volumeSetId);
        }
        if (StringUtils.isNotEmpty(publisher)) {
            config.setPublisher(publisher);
        }
        if (StringUtils.isNotEmpty(preparer)) {
            config.setDataPreparer(preparer);
        }
        if (StringUtils.isNotEmpty(application)) {
            config.setApp(application);
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
}
