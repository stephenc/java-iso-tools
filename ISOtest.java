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

import java.io.*;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.*;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.*;
import de.tu_darmstadt.informatik.rbg.hatlak.eltorito.impl.ElToritoConfig;
import de.tu_darmstadt.informatik.rbg.hatlak.joliet.impl.JolietConfig;
import de.tu_darmstadt.informatik.rbg.hatlak.rockridge.impl.RockRidgeConfig;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;

public class ISOtest {
	private static boolean enableJoliet = true;
	private static boolean enableRockRidge = true;
	private static boolean enableElTorito = true;

	private static void handleOption(String option) {
	  if (option.equals("disable-joliet")) {
		  enableJoliet = false;
	  } else
	  if (option.equals("disable-rockridge")) {
		  enableRockRidge = false;
	  } else
	  if (option.equals("disable-eltorito")) {
		  enableElTorito = false;
	  }
	}

	public static void main(String[] args) throws Exception {
		// Output file
		File outfile = new File(args.length>0 ? args[0] : "ISOTest.iso");

		// Directory hierarchy, starting from the root
		ISO9660RootDirectory.MOVED_DIRECTORIES_STORE_NAME = "rr_moved";
		ISO9660RootDirectory root = new ISO9660RootDirectory();

		if (args.length>1) {
			// Record specified files and directories

			for (int i=1; i<args.length; i++) {
				if (args[i].startsWith("--")) {
					handleOption(args[i].substring(2, args[i].length()));
				} else {
					// Add file or directory contents recursively
					File file = new File(args[i]);
					if (file.exists()) {
						if (file.isDirectory()) {
							root.addContentsRecursively(file);
						} else {
							root.addFile(file);
						}
					}
				}
			}
		} else {
			// Record test cases

			// Very long filename: a...z
			root.addDirectory("a1234567890b1234567890c1234567890d1234567890e1234567890f1234567890g1234567890h1234567890i1234567890j1234567890k1234567890l1234567890m1234567890n1234567890o1234567890p1234567890q1234567890r1234567890s1234567890t1234567890u1234567890v1234567890w1234567890x1234567890y1234567890z");
			// German Umlauts
			root.addDirectory("äöüÄÖÜß");

			// Filenames that will have to be renamed (count test)
			ISO9660Directory dir_1 = root.addDirectory("1");
			dir_1.addDirectory("1");
			dir_1.addDirectory("1");

			ISO9660Directory dir_a = root.addDirectory("a");
			dir_a.addDirectory("a");
			dir_a.addDirectory("a");

			ISO9660Directory dir_abcdefg = root.addDirectory("abcdefg");
			dir_abcdefg.addDirectory("abcdefg");
			dir_abcdefg.addDirectory("abcdefg");

			ISO9660Directory dir_abcdefgh = root.addDirectory("abcdefgh");
			dir_abcdefgh.addDirectory("abcdefgh");
			dir_abcdefgh.addDirectory("abcdefgh");

			ISO9660Directory dir_abcde321 = root.addDirectory("abcde321");
			dir_abcde321.addDirectory("abcde321");
			dir_abcde321.addDirectory("abcde321");

			// Additional test cases
			// (file without extension, tar.gz, deeply nested directory;
			// sort order tests, renaming tests: filename + extension,
			// directory with many files: sector end test)
			root.addRecursively(new File("test"));

			// Dirs to appear in order A, B, Aeins, Azwei, Cubase, Beins, Bzwei
			ISO9660Directory subdirA = root.addDirectory("A");
			subdirA.addDirectory("Aeins");
			subdirA.addDirectory("Azwei");
			ISO9660Directory subdirB = root.addDirectory("B");
			subdirB.addDirectory("Bzwei");
			subdirB.addDirectory("Beins");

			// Files with different versions
			// (to appear in descending order, pointing to same LSN)
			ISO9660File file1 = new ISO9660File("test/tux.gif", 1);
			root.addFile(file1);
			ISO9660File file10 = new ISO9660File("test/tux.gif", 10);
			root.addFile(file10);
			ISO9660File file12 = new ISO9660File("test/tux.gif", 12);
			root.addFile(file12);
		}

		// ISO9660 support
		ISO9660Config iso9660Config = new ISO9660Config();
		iso9660Config.allowASCII(false);
		iso9660Config.setInterchangeLevel(1);
		iso9660Config.restrictDirDepthTo8(true);
		iso9660Config.setPublisher("Jens Hatlak");
		iso9660Config.setVolumeID("ISO Test");
		iso9660Config.setDataPreparer("Jens Hatlak");
		iso9660Config.setCopyrightFile(new File("Copyright.txt"));
		iso9660Config.forceDotDelimiter(true);

		RockRidgeConfig rrConfig = null;
		if (enableRockRidge) {
			// Rock Ridge support
			rrConfig = new RockRidgeConfig();
			rrConfig.setMkisofsCompatibility(false);
			rrConfig.hideMovedDirectoriesStore(true);
			rrConfig.forcePortableFilenameCharacterSet(true);
		}

		JolietConfig jolietConfig = null;
		if (enableJoliet) {
			// Joliet support
			jolietConfig = new JolietConfig();
			jolietConfig.setPublisher("Jens Hatlak");
			jolietConfig.setVolumeID("Joliet Test");
			jolietConfig.setDataPreparer("Jens Hatlak");
			jolietConfig.setCopyrightFile(new File("Copyright.txt"));
			jolietConfig.forceDotDelimiter(true);
		}

		ElToritoConfig elToritoConfig = null;
		if (enableElTorito) {
			// El Torito support
			elToritoConfig = new ElToritoConfig(
				new File("tomsrtbt-2.0.103.ElTorito.288.img"),
				ElToritoConfig.BOOT_MEDIA_TYPE_2_88MEG_DISKETTE,
				ElToritoConfig.PLATFORM_ID_X86, "isoTest", 4,
				ElToritoConfig.LOAD_SEGMENT_7C0);
		}

		// Create ISO
		StreamHandler streamHandler = new ISOImageFileHandler(outfile);
		CreateISO iso = new CreateISO(streamHandler, root);
		iso.process(iso9660Config, rrConfig, jolietConfig, elToritoConfig);
		System.out.println("Done. File is: " + outfile);
	}
}
