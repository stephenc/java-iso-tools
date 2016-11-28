package com.github.stephenc.javaisotools.examples.archive;

import com.github.stephenc.javaisotools.eltorito.impl.ElToritoConfig;
import com.github.stephenc.javaisotools.iso9660.ConfigException;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.iso9660.impl.CreateISO;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Config;
import com.github.stephenc.javaisotools.iso9660.impl.ISOImageFileHandler;
import com.github.stephenc.javaisotools.joliet.impl.JolietConfig;
import com.github.stephenc.javaisotools.rockridge.impl.RockRidgeConfig;
import com.github.stephenc.javaisotools.sabre.HandlerException;

import java.io.File;
import java.io.FileNotFoundException;

public class IsoCreation {
    ISO9660RootDirectory root;
    ISO9660Config iso9660config;
    RockRidgeConfig rockConfig;
    JolietConfig jolietConfig;
    ElToritoConfig elToritoConfig;

    // This is a conversion of https://github.com/danveloper/provisioning-gradle-plugin/blob/
    // master/src/main/groovy/gradle/plugins/provisioning/tasks/image/ImageAssemblyTask.groovy
    public IsoCreation() {
        root = new ISO9660RootDirectory();
    }

    public void insertFile(File passedInsert) {
        try {
            root.addContentsRecursively(passedInsert);
        } catch (HandlerException e) {
            e.printStackTrace();
        }
    }

    public ISO9660Config setIsoConfig(String volumeId) {
        //Let's make a new configuration for the basic file system
        iso9660config = new ISO9660Config();
        iso9660config.allowASCII(false);
        //The standard says you are not allowed more than 8 levels, unless you enable RockRidge support
        iso9660config.restrictDirDepthTo8(true);
        //Set the volume ID
        try {
            iso9660config.setVolumeID(volumeId.substring(0, Math.min(volumeId.length(), 15)));
        } catch (ConfigException e) {
            e.printStackTrace();
        }
        iso9660config.forceDotDelimiter(false);
        try {
            iso9660config.setInterchangeLevel(3);
        } catch (ConfigException e) {
            e.printStackTrace();
        }

        return iso9660config;
    }

    public RockRidgeConfig setRockConfig() {
        rockConfig = new RockRidgeConfig();
        rockConfig.setMkisofsCompatibility(false);
        rockConfig.hideMovedDirectoriesStore(true);
        rockConfig.forcePortableFilenameCharacterSet(true);
        return rockConfig;
    }

    public JolietConfig setJolietConfig(String volumeId) {
        jolietConfig = new JolietConfig();
        try {
            //Not sure if there is a limit here, but it seems most names are fine
            jolietConfig.setVolumeID(volumeId);
        } catch (ConfigException e) {
            e.printStackTrace();
        }
        jolietConfig.forceDotDelimiter(false);
        return jolietConfig;
    }

    /**
     * This is for boot information on the iso
     *
     * @return The finalized config.
     */
    public ElToritoConfig setElToritoConfig(){
        elToritoConfig = null;
        return elToritoConfig;
    }

    /**
     * Close out the disc.
     * @param isoPath
     */
    public void finalizeDisc(File isoPath) {
        ISOImageFileHandler handler = null;
        try {
            handler = new ISOImageFileHandler(isoPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            CreateISO iso = new CreateISO(handler, root);
            try {
                iso.process(iso9660config, rockConfig, jolietConfig, elToritoConfig);

            } catch (HandlerException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
