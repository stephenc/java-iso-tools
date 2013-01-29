/*
 * Copyright (c) 2010. Stephen Connolly
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.github.stephenc.javaisotools.iso9660.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Random;

import org.junit.*;
import org.hamcrest.*;

import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.joliet.impl.JolietConfig;
import com.github.stephenc.javaisotools.rockridge.impl.RockRidgeConfig;
import com.github.stephenc.javaisotools.sabre.StreamHandler;
import com.github.stephenc.javaisotools.iso9660.ISO9660Directory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.codehaus.plexus.util.IOUtil;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Some simple ISO file system tests.
 *
 * @author connollys
 * @since Sep 24, 2010 3:27:44 PM
 */
public class CreateISOTest {

    private static File workDir;
    
    private Random entropy = new Random();

    @BeforeClass
    public static void loadConfiguration() throws Exception {
        Properties props = new Properties();
        InputStream is = null;
        try {
            is = CreateISOTest.class.getResourceAsStream("/test.properties");
            props.load(is);
        } finally {
            IOUtil.close(is);
        }
        workDir = new File(props.getProperty("work-directory"));
        assertThat("We can create our work directory", workDir.mkdirs() || workDir.isDirectory(), is(true));
    }

    @Test
    public void canCreateAnEmptyIso() throws Exception {
        // Output file
        File outfile = new File(workDir, "empty.iso");

        // Directory hierarchy, starting from the root
        ISO9660RootDirectory.MOVED_DIRECTORIES_STORE_NAME = "rr_moved";
        ISO9660RootDirectory root = new ISO9660RootDirectory();
        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);
        iso.process(new ISO9660Config(), null, null, null);

        assertThat(outfile.isFile(), is(true));
        assertThat(outfile.length(), not(is(0L)));

        // TODO use loop-fs to check that the iso is empty
    }

    @Test
    public void canCreateAnIsoWithOneFile() throws Exception {
        final String contentString = "This is a test file";
        // Output file
        File outfile = new File(workDir, "one-file.iso");
        File contents = new File(workDir, "readme.txt");
        OutputStream os = new FileOutputStream(contents);
        IOUtil.copy(contentString, os);
        IOUtil.close(os);

        // Directory hierarchy, starting from the root
        ISO9660RootDirectory.MOVED_DIRECTORIES_STORE_NAME = "rr_moved";
        ISO9660RootDirectory root = new ISO9660RootDirectory();

        root.addFile(contents);

        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);
        ISO9660Config iso9660Config = new ISO9660Config();
        iso9660Config.allowASCII(false);
        iso9660Config.setInterchangeLevel(1);
        iso9660Config.restrictDirDepthTo8(true);
        iso9660Config.setVolumeID("ISO Test");
        iso9660Config.forceDotDelimiter(true);
        RockRidgeConfig rrConfig = new RockRidgeConfig();
        rrConfig.setMkisofsCompatibility(false);
        rrConfig.hideMovedDirectoriesStore(true);
        rrConfig.forcePortableFilenameCharacterSet(true);

        JolietConfig jolietConfig = new JolietConfig();
        jolietConfig.setVolumeID("Joliet Test");
        jolietConfig.forceDotDelimiter(true);

        iso.process(iso9660Config, rrConfig, jolietConfig, null);

        assertThat(outfile.isFile(), is(true));
        assertThat(outfile.length(), not(is(0L)));

        FileSystemManager fsManager = VFS.getManager();
        // TODO figure out why we can't just do
        // FileObject isoFile = fsManager.resolveFile("iso:" + outfile.getPath() + "!/");
        // smells like a bug between loop-fs and commons-vfs
        FileObject isoFile = fsManager.resolveFile("iso:" + outfile.getPath() + "!/readme.txt").getParent();
        assertThat(isoFile.getType(), is(FileType.FOLDER));

        FileObject[] children = isoFile.getChildren();
        assertThat(children.length, is(1));
        assertThat(children[0].getName().getBaseName(), is("readme.txt"));
        assertThat(children[0].getType(), is(FileType.FILE));
        assertThat(IOUtil.toString(children[0].getContent().getInputStream()), is(contentString));
    }

    @Test
    public void canCreateAnIsoWithSomeFiles() throws Exception {
        // Output file
        File outfile = new File(workDir, "test.iso");
        File contentsA = new File(workDir, "a.txt");
        OutputStream os = new FileOutputStream(contentsA);
        IOUtil.copy("Hello", os);
        IOUtil.close(os);
        File contentsB = new File(workDir, "b.txt");
        os = new FileOutputStream(contentsB);
        IOUtil.copy("Goodbye", os);
        IOUtil.close(os);

        // Directory hierarchy, starting from the root
        ISO9660RootDirectory.MOVED_DIRECTORIES_STORE_NAME = "rr_moved";
        ISO9660RootDirectory root = new ISO9660RootDirectory();

        ISO9660Directory dir = root.addDirectory("root");
        dir.addFile(contentsA);
        dir.addFile(contentsB);

        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);
        ISO9660Config iso9660Config = new ISO9660Config();
        iso9660Config.allowASCII(false);
        iso9660Config.setInterchangeLevel(2);
        iso9660Config.restrictDirDepthTo8(true);
        iso9660Config.setVolumeID("ISO Test");
        iso9660Config.forceDotDelimiter(true);
        RockRidgeConfig rrConfig = new RockRidgeConfig();
        rrConfig.setMkisofsCompatibility(true);
        rrConfig.hideMovedDirectoriesStore(true);
        rrConfig.forcePortableFilenameCharacterSet(true);

        JolietConfig jolietConfig = new JolietConfig();
        jolietConfig.setVolumeID("Joliet Test");
        jolietConfig.forceDotDelimiter(true);

        iso.process(iso9660Config, rrConfig, jolietConfig, null);

        assertThat(outfile.isFile(), is(true));
        assertThat(outfile.length(), not(is(0L)));

        FileSystemManager fsManager = VFS.getManager();
        FileObject isoFile = fsManager.resolveFile("iso:" + outfile.getPath() + "!/root");

        FileObject t = isoFile.getChild("a.txt");
        assertThat(t, CoreMatchers.<Object>notNullValue());
        assertThat(t.getType(), is(FileType.FILE));
        assertThat(t.getContent().getSize(), is(5L));
        assertThat(IOUtil.toString(t.getContent().getInputStream()), is("Hello"));
        t = isoFile.getChild("b.txt");
        assertThat(t, CoreMatchers.<Object>notNullValue());
        assertThat(t.getType(), is(FileType.FILE));
        assertThat(t.getContent().getSize(), is(7L));
        assertThat(IOUtil.toString(t.getContent().getInputStream()), is("Goodbye"));
    }

    @Test
    public void canCreateAnIsoWithLoadsOfFiles() throws Exception {
        final int numFiles = entropy.nextInt(50) + 50;
        // Output file
        File outfile = new File(workDir, "big.iso");
        File rootDir = new File(workDir, "big");
        assertThat(rootDir.isDirectory() || rootDir.mkdirs(), is(true));

        // Directory hierarchy, starting from the root
        ISO9660RootDirectory.MOVED_DIRECTORIES_STORE_NAME = "rr_moved";
        ISO9660RootDirectory root = new ISO9660RootDirectory();
        for (int i = 0; i < numFiles; i++) {
            File content = new File(rootDir, Integer.toString(i) + ".bin");
            int length = entropy.nextInt(1024 * 10 + 1);
            byte[] contents = new byte[length];
            entropy.nextBytes(contents);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(content);
                 fos.write(contents);
            } finally {
                IOUtil.close(fos);
            }
            root.addFile(content);
        }

        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);
        ISO9660Config iso9660Config = new ISO9660Config();
        iso9660Config.allowASCII(false);
        iso9660Config.setInterchangeLevel(2);
        iso9660Config.restrictDirDepthTo8(true);
        iso9660Config.setVolumeID("ISO Test");
        iso9660Config.forceDotDelimiter(true);
        RockRidgeConfig rrConfig = new RockRidgeConfig();
        rrConfig.setMkisofsCompatibility(true);
        rrConfig.hideMovedDirectoriesStore(true);
        rrConfig.forcePortableFilenameCharacterSet(true);

        JolietConfig jolietConfig = new JolietConfig();
        jolietConfig.setVolumeID("Joliet Test");
        jolietConfig.forceDotDelimiter(true);

        iso.process(iso9660Config, rrConfig, jolietConfig, null);

        assertThat(outfile.isFile(), is(true));
        assertThat(outfile.length(), not(is(0L)));

        FileSystemManager fsManager = VFS.getManager();
        for (int i = 0; i < numFiles; i++) {
            File content = new File(rootDir, Integer.toString(i) + ".bin");
            FileObject t = fsManager.resolveFile("iso:" + outfile.getPath() + "!/" + Integer.toString(i) + ".bin");
            assertThat(t, CoreMatchers.<Object>notNullValue());
            assertThat(t.getType(), is(FileType.FILE));
            assertThat(t.getContent().getSize(), is(content.length()));
            assertThat(IOUtil.toByteArray(t.getContent().getInputStream()), is(IOUtil.toByteArray(new FileInputStream(content))));
        }
    }

    @Test
    public void canCreateAnIsoTopDownHierarchy() throws Exception {
		// Output file
        File outfile = new File(workDir, "test.iso");
        File contentsA = new File(workDir, "a.txt");
        OutputStream os = new FileOutputStream(contentsA);
        IOUtil.copy("Hello", os);
        IOUtil.close(os);
        File contentsB = new File(workDir, "b.txt");
        os = new FileOutputStream(contentsB);
        IOUtil.copy("Goodbye", os);
        IOUtil.close(os);

        // Top down
        ISO9660RootDirectory root = new ISO9660RootDirectory();
        ISO9660Directory n1 = root.addDirectory("D1");
        ISO9660Directory n2 = n1.addDirectory("D2");
        ISO9660Directory n3 = n2.addDirectory("D3");
        n3.addFile(contentsA);
        n3.addFile(contentsB);

        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);
        ISO9660Config iso9660Config = new ISO9660Config();
        iso9660Config.allowASCII(false);
        iso9660Config.setInterchangeLevel(2);
        iso9660Config.restrictDirDepthTo8(true);
        iso9660Config.setVolumeID("ISO Test");
        iso9660Config.forceDotDelimiter(true);
        RockRidgeConfig rrConfig = new RockRidgeConfig();
        rrConfig.setMkisofsCompatibility(true);
        rrConfig.hideMovedDirectoriesStore(true);
        rrConfig.forcePortableFilenameCharacterSet(true);

        JolietConfig jolietConfig = new JolietConfig();
        jolietConfig.setVolumeID("Joliet Test");
        jolietConfig.forceDotDelimiter(true);

        iso.process(iso9660Config, rrConfig, jolietConfig, null);

        assertThat(outfile.isFile(), is(true));
        assertThat(outfile.length(), not(is(0L)));
    }

    @Test
    public void canCreateAnIsoBottomUpHierarchy() throws Exception {
		// Output file
        File outfile = new File(workDir, "test.iso");
        File contentsA = new File(workDir, "a.txt");
        OutputStream os = new FileOutputStream(contentsA);
        IOUtil.copy("Hello", os);
        IOUtil.close(os);
        File contentsB = new File(workDir, "b.txt");
        os = new FileOutputStream(contentsB);
        IOUtil.copy("Goodbye", os);
        IOUtil.close(os);

        // Bottom up
        ISO9660Directory n3 = new ISO9660Directory("D3");
        n3.addFile(contentsA);
        n3.addFile(contentsB);
        ISO9660Directory n2 = new ISO9660Directory("D2");
        n2.addDirectory(n3);
        ISO9660Directory n1 = new ISO9660Directory("D1");
        n1.addDirectory(n2);
        ISO9660RootDirectory root = new ISO9660RootDirectory();
        root.addDirectory(n1);

        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);
        ISO9660Config iso9660Config = new ISO9660Config();
        iso9660Config.allowASCII(false);
        iso9660Config.setInterchangeLevel(2);
        iso9660Config.restrictDirDepthTo8(true);
        iso9660Config.setVolumeID("ISO Test");
        iso9660Config.forceDotDelimiter(true);
        RockRidgeConfig rrConfig = new RockRidgeConfig();
        rrConfig.setMkisofsCompatibility(true);
        rrConfig.hideMovedDirectoriesStore(true);
        rrConfig.forcePortableFilenameCharacterSet(true);

        JolietConfig jolietConfig = new JolietConfig();
        jolietConfig.setVolumeID("Joliet Test");
        jolietConfig.forceDotDelimiter(true);

        iso.process(iso9660Config, rrConfig, jolietConfig, null);

        assertThat(outfile.isFile(), is(true));
        assertThat(outfile.length(), not(is(0L)));
    }

    @Test
    public void canOpenFakeIso() throws Exception {
    	final String contentString = "This is a text file, not an iso";
        // Output file
        File fakeIso = new File(workDir, "fake.iso");
        OutputStream os = new FileOutputStream(fakeIso);
        IOUtil.copy(contentString, os);
        IOUtil.close(os);

        // Trying to open a fake iso
        FileSystemManager fsManager = VFS.getManager();
        FileObject fo = fsManager.resolveFile("iso:" + fakeIso.getPath() + "!/");
        assertFalse("The file '" + fakeIso.getName() + "' is not a valid iso file", fo.exists());
    }
}
