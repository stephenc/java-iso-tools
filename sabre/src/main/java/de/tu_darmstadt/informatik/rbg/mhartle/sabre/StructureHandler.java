package de.tu_darmstadt.informatik.rbg.mhartle.sabre;

public interface StructureHandler {
	public void startDocument() throws HandlerException;
	public void startElement(Element element) throws HandlerException;
	public void endElement() throws HandlerException;
	public void endDocument() throws HandlerException;
}
