package de.tu_darmstadt.informatik.rbg.mhartle.sabre.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.Fixup;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.FileFixup;

public class SingleFileFixupList {
	private static int fixupLength = 16;
	private File originalIndexFile = null;
	private RandomAccessFile indexFile = null;
	private RandomAccessFile fixedFile = null;
	private int fixupCount = 0;
	
	public SingleFileFixupList(RandomAccessFile fixedFile, File indexFile) throws FileNotFoundException {
		this.fixedFile = fixedFile;
		this.originalIndexFile = indexFile;
		this.indexFile = new RandomAccessFile(indexFile, "rw");
	}
	
	public synchronized void addLast(Fixup fixup) {
		FileFixup fileFixup = null;
		
		if (fixup instanceof FileFixup) {
			fileFixup = (FileFixup)fixup;
			if (this.fixedFile != null) {
				if (this.fixedFile != fileFixup.getFile()) {
					throw new RuntimeException();
				}
			} else {
				// The first fixup file is used if undefined
				this.fixedFile = fileFixup.getFile();
			}
			
			try {
				this.indexFile.seek(this.fixupCount * fixupLength);
				this.indexFile.writeLong(fileFixup.getPosition());
				this.indexFile.writeLong(fileFixup.getAvailable());
				this.fixupCount++;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public synchronized Fixup get(int index) {
		Fixup result = null;
		long position = 0;
		long available = 0;

		try {
			if (index < this.fixupCount) {
				this.indexFile.seek(index * fixupLength);
				position = this.indexFile.readLong();
				available = this.indexFile.readLong();
				result = new FileFixup(this.fixedFile, position, available);
			} else {
				System.out.println("Autsch");
				// throw new IndexOutOfBoundsException();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
		
		return result;
	}
	
	public void delete() throws IOException {
		this.indexFile.close();
		this.originalIndexFile.delete();
	}
}
