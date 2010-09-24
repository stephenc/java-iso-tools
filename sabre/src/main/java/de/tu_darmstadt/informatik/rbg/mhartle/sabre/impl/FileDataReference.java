package de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.io.LimitingInputStream;

public class FileDataReference implements DataReference {
	private File file = null;
	private long position = 0;
	private long length = 0;
	
	public FileDataReference(File file) {
		this.file = file;
		this.position = 0;
		this.length = -1;
	}
	
	public FileDataReference(File file, int position, int length) {
		this.file = file;
		this.position = position;
		this.length = length;
	}
	

	public long getLength() {
		long length = 0;
		
		if (this.length == -1) {
			length = this.file.length();
		} else {
			length = this.length;
		}
		
		return length;
	}

	public InputStream createInputStream() throws IOException {
		InputStream fileInputStream = null;
		
		fileInputStream = new FileInputStream(file);
		
		// Reposition input stream, if necessary
		if (this.position > 0) {
			long skipped = 0;
			// System.out.print("Skipping to " + position + "/" + this.file.length() + " ");
			while(skipped != this.position) {
				skipped = fileInputStream.skip(this.position - skipped);
			}
		}
		
		// Limit input stream, if necessary
		if (this.length != -1) {
			// System.out.println("Limiting to " + this.length + " ");
			fileInputStream = new LimitingInputStream(fileInputStream, (int)this.length);
		}
		
		// System.out.println();
		
		// Buffer input stream 
		fileInputStream = new BufferedInputStream(fileInputStream);
		
		return fileInputStream;
	}

}
