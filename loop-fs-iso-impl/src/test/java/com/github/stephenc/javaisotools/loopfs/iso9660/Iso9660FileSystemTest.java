/*
Copyright (C) 2010 Stephen Connolly

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.github.stephenc.javaisotools.loopfs.iso9660;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.junit.*;

import com.github.stephenc.javaisotools.loopfs.api.FileEntry;
import com.github.stephenc.javaisotools.loopfs.api.FileSystem;
import org.codehaus.plexus.util.IOUtil;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Tests the Iso9660 implementation.
 *
 * @author Stephen Connolly
 * @since Oct 5, 2010 1:06:36 PM
 */
public class Iso9660FileSystemTest {

    private static Properties testProperties;

    @BeforeClass
    public static void loadConfiguration() throws Exception {
        testProperties = new Properties();
        InputStream is = null;
        try {
            is = Iso9660FileSystemTest.class.getResourceAsStream("/test.properties");
            testProperties.load(is);
        } finally {
            IOUtil.close(is);
        }
    }

    @Test
    public void smokes() throws Exception {
        Iso9660FileSystem image = new Iso9660FileSystem(new File(testProperties.getProperty("source-image")), true);
        File source = new File(testProperties.getProperty("source-root"));
        for (Iso9660FileEntry entry : image) {
            File sourceFile = new File(source, entry.getPath());
            assertThat(sourceFile.isDirectory(), is(entry.isDirectory()));
            if (!sourceFile.isDirectory()) {
                assertThat(sourceFile.length(), is(entry.getSize() * 1L));
                assertThat("contents are equal",
                        IOUtil.contentEquals(image.getInputStream(entry), new FileInputStream(sourceFile)), is(true));
            }
        }
    }
}
