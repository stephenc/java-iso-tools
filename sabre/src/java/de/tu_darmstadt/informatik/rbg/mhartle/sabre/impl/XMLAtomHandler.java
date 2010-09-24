package de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.DataReference;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.Element;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.Fixup;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.HandlerException;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.StreamHandler;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.StructureHandler;

public class XMLAtomHandler implements StreamHandler {
	private ContentHandler contentHandler = null;
	private String namespace = null;
	private String prefix = null;
	private Stack elements = null;
	
	public XMLAtomHandler(ContentHandler contentHandler, String namespace, String prefix) {
		this.contentHandler = contentHandler;
		this.namespace = namespace;
		this.prefix = prefix;
		this.elements = new Stack();
	}
	
	public void startDocument() throws HandlerException {
		try {
			this.contentHandler.startElement(this.namespace, "root", this.prefix + ":root", null);
		} catch (SAXException e) {
			throw new HandlerException(e);
		}
	}

	public void startElement(Element element) throws HandlerException {
		try {
			this.elements.push(element);
			this.contentHandler.startElement(this.namespace, element.toString(), this.prefix + ":" + element.toString(), null);
		} catch (SAXException e) {
			throw new HandlerException(e);
		}
	}

	public void data(DataReference reference) throws HandlerException {
		long showLength = 0;
		
		try {
			InputStream inputStream = null;
			byte[] buffer = null;
			String data = null;
			long length = reference.getLength();
			
			if (length > 32) {
				showLength = 32;
			} else {
				showLength = length;
			}
			
			buffer = new byte[(int)showLength];
			inputStream = reference.createInputStream();
			inputStream.read(buffer);
			inputStream.close();
			
			data = new String(buffer);
			data = StringEscapeUtils.escapeJava(data);
			this.contentHandler.characters(data.toCharArray(), 0, data.length());
		} catch (SAXException e) {
			throw new HandlerException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Fixup fixup(DataReference reference) throws HandlerException {
		throw new HandlerException("Cannot handle fixup in SAX.");
	}
	
	public long mark() throws HandlerException {
		throw new HandlerException("Cannot mark position in SAX.");
	}

	public void endElement() throws HandlerException {
		Element element = null;

		try {
			element = (Element)this.elements.pop();
			this.contentHandler.endElement(this.namespace, element.toString(), this.prefix + ":" + element.toString());
		} catch (SAXException e) {
			throw new HandlerException(e);
		}
	}
	
	public void endDocument() throws HandlerException {
		try {
			this.contentHandler.endElement(this.namespace, "root", this.prefix + ":root");
		} catch (SAXException e) {
			throw new HandlerException(e);
		}
	}
}
