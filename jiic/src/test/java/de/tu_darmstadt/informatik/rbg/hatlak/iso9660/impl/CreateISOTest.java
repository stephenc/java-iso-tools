/*
 *  JIIC: Java ISO Image Creator. Copyright (C) 2010, Stephen Connolly <stephenc at apache.org>
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.junit.*;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ISO9660RootDirectory;
import de.tu_darmstadt.informatik.rbg.hatlak.joliet.impl.JolietConfig;
import de.tu_darmstadt.informatik.rbg.hatlak.rockridge.impl.RockRidgeConfig;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.StreamHandler;
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

        // TODO use loopy to check that the iso is empty
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

        // TODO use loopy to check that the iso is empty
        FileSystemManager fsManager = VFS.getManager();
        FileObject isoFile = fsManager.resolveFile("iso:" + outfile.getPath());

        FileObject[] children = isoFile.getChildren();
        assertThat(children.length, is(1));
        assertThat(children[0].getName().getBaseName(), is("readme.txt"));
        assertThat(children[0].getType(), is(FileType.FILE));
        assertThat(IOUtil.toString(children[0].getContent().getInputStream()), is(contentString));
    }
}
