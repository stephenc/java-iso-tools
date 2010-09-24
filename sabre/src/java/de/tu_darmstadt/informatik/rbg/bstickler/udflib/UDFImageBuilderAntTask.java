/*
 *	UDFImageBuilderAntTask.java
 *
 *	2006-06-29
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib;

import java.io.*;
import java.util.*;
import org.apache.tools.ant.*;

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
