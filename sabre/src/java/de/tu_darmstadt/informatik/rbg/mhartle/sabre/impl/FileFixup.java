package de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.Fixup;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.FixupListener;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.HandlerException;

public class FileFixup implements Fixup {
	// private File file = null;
	private RandomAccessFile randomAccessFile = null;
	private long position = 0;
	private long available = 0;
	private boolean closed = false;
	
	public FileFixup(RandomAccessFile file, long position, long available) {
		this.randomAccessFile = file;
		this.position = position;
		this.available = available;
	}
	
	public void data(DataReference reference) throws HandlerException {
		InputStream inputStream = null;
		byte[] buffer = null;
		int bytesRead = 0;
		
		// Test if fixup is still open
		if (!closed) {
			if (reference.getLength() > available) {
				throw new HandlerException("Fixup larger than available space.");
			}
			
			// Write fixup into file
			try {				
				// Move to position in file
				randomAccessFile.seek(this.position);
				
				// Copy data reference to position
				buffer = new byte[1024];
				inputStream = reference.createInputStream();
				while((bytesRead = inputStream.read(buffer, 0, 1024)) != -1) {
					randomAccessFile.write(buffer, 0, bytesRead);
				}
				
				// Handle position and available placeholder bytes
				position += reference.getLength();
				available -= reference.getLength();
				
				// Close the fixup
				// randomAccessFile.close();
			} catch (FileNotFoundException e) {
				throw new HandlerException(e);
			} catch (IOException e) {
				throw new HandlerException(e);
			}
		}
	}
	
	public Fixup fixup(DataReference reference) throws HandlerException {
		throw new RuntimeException("Cannot yet handle fixup in fixup.");
	}
	
	public long mark() throws HandlerException {
		return this.position;
	}

	public void close() throws HandlerException {
		try {
			this.closed = true;
			// this.randomAccessFile.close();
			
		} /*catch (IOException e) {
			throw new HandlerException(e);
		} */finally {
			
		}
	}

	public boolean isClosed() {
		return this.closed;
	}

	public void addFixupListener(FixupListener listener) throws HandlerException {
		// TODO Auto-generated method stub
	}

	public void removeFixupListener(FixupListener listener) throws HandlerException {
		// TODO Auto-generated method stub
	}
	
	public RandomAccessFile getFile() {
		return this.randomAccessFile;
	}
	
	public long getPosition() {
		return this.position;
	}
	
	public long getAvailable() {
		return this.available;
	}
}
