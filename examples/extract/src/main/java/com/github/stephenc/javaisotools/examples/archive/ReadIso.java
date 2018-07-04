package com.github.stephenc.javaisotools.examples.archive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileEntry;
import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileSystem;

/***
 * This is a adaptation of https://github.com/danveloper/provisioning-gradle-plugin/blob/master/src/main/groovy/
 * gradle/plugins/provisioning/tasks/image/ImageAssemblyTask.groovy
 */

public class ReadIso {
    Iso9660FileSystem discFs;

    public ReadIso(File isoToRead, File saveLocation) {

        try {
            //Give the file and mention if this is treated as a read only file.
            discFs = new Iso9660FileSystem(isoToRead, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Make our saving folder if it does not exist
        if (!saveLocation.exists()) {
            saveLocation.mkdirs();
        }

        //Go through each file on the disc and save it.
        for (Iso9660FileEntry singleFile : discFs) {
            if (singleFile.isDirectory()) {
                new File(saveLocation, singleFile.getPath()).mkdirs();
            } else {
                File tempFile = new File(saveLocation, singleFile.getPath());
                try {
                    //This is java 7, sorry if that is too new for some people
                    Files.copy(discFs.getInputStream(singleFile), tempFile.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
