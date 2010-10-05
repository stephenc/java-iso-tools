/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (c) 2006. Björn Stickler <bjoern@stickler.de>.
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

package com.github.stephenc.javaisotools.udflib;

import java.io.File;
import java.util.Calendar;

public class UDFTest {

    private void testUDFImageBuilder() {
        try {
            UDFImageBuilder myUDFImageBuilder = new UDFImageBuilder();

            File testFile = new File("C:\\Program Files (x86)\\Microsoft Visual Studio 8");
            File testChildFiles[] = testFile.listFiles();
            for (int i = 0; i < testChildFiles.length; ++i) {
                myUDFImageBuilder.addFileToRootDirectory(testChildFiles[i]);
            }

            myUDFImageBuilder.setImageIdentifier("Test-Disc");

            myUDFImageBuilder.writeImage("c:\\temp\\test-disc.iso", UDFRevision.Revision201);
        }
        catch (Exception myException) {
            System.out.println(myException.toString());
            myException.printStackTrace();
        }
    }

    public void testSabreUDFImageBuilder() {
        try {
            SabreUDFImageBuilder mySabreUDF = new SabreUDFImageBuilder();

            File testFile = new File("C:\\Program Files (x86)\\Microsoft Visual Studio 8");
            File testChildFiles[] = testFile.listFiles();
            for (int i = 0; i < testChildFiles.length; ++i) {
                mySabreUDF.addFileToRootDirectory(testChildFiles[i]);
            }

            mySabreUDF.setImageIdentifier("Test-Disc");

            mySabreUDF.writeImage("c:\\temp\\test-disc.iso", UDFRevision.Revision201);
        }
        catch (Exception myException) {
            System.out.println(myException.toString());
            myException.printStackTrace();
        }
    }

    public UDFTest() {
        System.out.println("UDFTest\n");

        long startTime = Calendar.getInstance().getTimeInMillis();

        //testUDFImageBuilder();
        testSabreUDFImageBuilder();

        System.out.println("Run-Time: " + (Calendar.getInstance().getTimeInMillis() - startTime) + " Milliseconds");
    }

    public static void main(String[] args) {
        new UDFTest();
    }

}
