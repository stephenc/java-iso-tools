package de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.ContentHandler;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.Element;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.Fixup;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.HandlerException;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.StructureHandler;

public class CopyingStreamHandler extends ChainingStreamHandler {
	private Element targetType = null;
	private DataOutputStream outputStream = null;
	private boolean isCopyingEnabled = false;
	private int elementStack = 0;
	
	public CopyingStreamHandler(File file, Element targetType, StructureHandler chainedStructureHandler, ContentHandler chainedContentHandler) throws FileNotFoundException {
		super(chainedStructureHandler, chainedContentHandler);
		this.targetType = targetType;
		this.outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
	}

	public void startDocument() throws HandlerException {
		// Delegate to super class
		super.startDocument();
	}
	
	public void startElement(Element element) throws HandlerException {
		if (element.getId().equals(this.targetType)) {
			if (!this.isCopyingEnabled) {
				this.isCopyingEnabled = true;
				this.elementStack = 0;
			}
		}
		
		try {
			if (this.isCopyingEnabled) {
				this.elementStack++;
				this.outputStream.writeInt((int)0);
				this.outputStream.writeInt((int)((Long)element.getId()).longValue());
			}
		} catch (IOException e) {
			throw new HandlerException(e);
		}

		// Delegate to super class
		super.startElement(element);
	}

	public void data(DataReference reference) throws HandlerException {
		long lengthToWrite = 0;
		long length = 0;
		
		try {
			if (isCopyingEnabled) {
				InputStream inputStream = null;
				byte[] buffer = null;
				int bytesToRead = 0;
				int bytesHandled = 0;
				int bufferLength = 65535;
				
				buffer = new byte[bufferLength];
				length = reference.getLength();

				System.out.println("Copying "+ length +" bytes to " + this.targetType);
				
				inputStream = reference.createInputStream();
				lengthToWrite = length;
				
				while(lengthToWrite > 0) {
					if (lengthToWrite > bufferLength) {
						bytesToRead = bufferLength;
					} else {
						bytesToRead = (int)lengthToWrite;
					}
					
					bytesHandled = inputStream.read(buffer, 0, bytesToRead);
					this.outputStream.write(buffer, 0, bytesHandled);
					lengthToWrite -= bytesHandled;
				}
				
				inputStream.close();
			}
		} catch (IOException e) {
			throw new HandlerException(e);
		}

		// Delegate to super class
		super.data(reference);
	}
	
	public Fixup fixup(DataReference reference) throws HandlerException {
		Fixup fixup = null;
		
		// FIXME: Think about copying fixups!

		// Delegate to super class
		fixup = super.fixup(reference);
		
		return fixup;
	}

	public void endElement() throws HandlerException {
		if (isCopyingEnabled) {
			elementStack--;
			if (elementStack == 0) {
				isCopyingEnabled = false;
			}
		}

		// Delegate to super class
		super.endElement();
	}

	public void endDocument() throws HandlerException {
		try {
			this.outputStream.flush();
			this.outputStream.close();
		} catch (IOException e) {
			throw new HandlerException(e);
		}
		
		// Delegate to super class
		super.endDocument();
	}
}
