package de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.ContentHandler;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.Element;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.Fixup;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.HandlerException;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.StructureHandler;

public class DebugStreamHandler extends ChainingStreamHandler {
	private long position = 0;
	
	public DebugStreamHandler(StructureHandler chainedStructureHandler, ContentHandler chainedContentHandler) {
		super(chainedStructureHandler, chainedContentHandler);
	}

	public void startDocument() throws HandlerException {
		// Show what is happening
		System.out.println("document starts");
		
		// Delegate to super class
		super.startDocument();
	}
	
	public void startElement(Element element) throws HandlerException {
		// Show what is happening
		System.out.println("node(" + element + ") @" + position);		
		this.position += 8;

		// Delegate to super class
		super.startElement(element);
	}

	public void data(DataReference reference) throws HandlerException {
		long length = 0;
		
		// Show what is happening
		length = reference.getLength();
		System.out.println("data @" + this.position + " for " + length);
		this.position += length;
		
		// Delegate to super class
		super.data(reference);
	}
	
	public Fixup fixup(DataReference reference) throws HandlerException {
		Fixup fixup = null;
		long length = 0;
		
		// Show what is happening
		length = reference.getLength();
		if (length == -1) {
			throw new HandlerException("Cannot fixup unknown length.");
		}
		System.out.println("fixup @" + this.position + " for " + length);

		// Delegate to super class
		fixup = super.fixup(reference);
		
		return fixup;
	}	

	public void endElement() throws HandlerException {
		super.endElement();
	}

	public void endDocument() throws HandlerException {
		super.endDocument();
	}

}
