package com.github.stephenc.javaisotools.examples.archive;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        //Create a new root of our disc
        IsoCreation sampleCreation = new IsoCreation();
        //Add our files, you have to give a directory to use the recusively adding function
        sampleCreation.insertFile(new File("test directory"));
        //This is the base iso9660 standard file system
        sampleCreation.setIsoConfig("test");
        //To quote wikipedia "Rock Ridge is an extension to the ISO 9660 volume format, commonly used on CD-ROM and
        // DVD media, which adds POSIX file system semantics."
        sampleCreation.setRockConfig();
        //This is another extension to the standard after Windows 95 to add support for longer filenames
        sampleCreation.setJolietConfig("test");
        //El Torito is boot information for the disc
        sampleCreation.setElToritoConfig();
        //Finalize and save our ISO
        sampleCreation.finalizeDisc(new File("test.iso"));
    }
}
