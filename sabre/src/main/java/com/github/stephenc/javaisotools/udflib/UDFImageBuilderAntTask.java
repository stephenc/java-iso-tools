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
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class UDFImageBuilderAntTask extends Task {

    public static class FileLocation {

        private String location;

        private boolean childfilesonly = false;

        public void setLocation(String location) {
            this.location = location;
        }

        public void setChildfilesonly(boolean childfilesonly) {
            this.childfilesonly = childfilesonly;
        }
    }

    private String imageIdentifier;

    private String imageOutputFile;

    private String udfRevision = "2.60";

    private List<FileLocation> fileLocations = new ArrayList<FileLocation>();

    public void setImageIdentifier(String imageIdentifier) {
        this.imageIdentifier = imageIdentifier;
    }

    public void setImageOutputFile(String imageOutputFile) {
        this.imageOutputFile = imageOutputFile;
    }

    public void setUdfRevision(String udfRevision) {
        this.udfRevision = udfRevision;
    }

    public void addFileLocation(FileLocation myFileLocation) {
        fileLocations.add(myFileLocation);
    }

    public void execute() throws BuildException {
        try {
            log("Creating UDF image " + imageOutputFile);
            UDFImageBuilder myUDFImageBuilder = new UDFImageBuilder();

            myUDFImageBuilder.setImageIdentifier(imageIdentifier);

            for (int i = 0; i < fileLocations.size(); ++i) {
                FileLocation myFileLocation = fileLocations.get(i);

                File myFile = new File(myFileLocation.location);

                if (myFileLocation.childfilesonly && myFile.isDirectory()) {
                    File[] childFiles = myFile.listFiles();
                    for (int j = 0; j < childFiles.length; ++j) {
                        myUDFImageBuilder.addFileToRootDirectory(childFiles[j]);
                    }
                } else {
                    myUDFImageBuilder.addFileToRootDirectory(myFile);
                }
            }

            UDFRevision myUDFRevision = UDFRevision.Revision201;

            if (udfRevision.equals("1.02")) {
                myUDFRevision = UDFRevision.Revision102;
            } else if (udfRevision.equals("2.01")) {
                myUDFRevision = UDFRevision.Revision201;
            } else if (udfRevision.equals("2.60")) {
                myUDFRevision = UDFRevision.Revision260;
            } else {
                throw new BuildException("Unkown UDF-Revision [" + udfRevision + "]");
            }

            myUDFImageBuilder.writeImage(imageOutputFile, myUDFRevision);

        } catch (Exception ex) {
            throw new BuildException(ex.toString());

        }
    }
}
