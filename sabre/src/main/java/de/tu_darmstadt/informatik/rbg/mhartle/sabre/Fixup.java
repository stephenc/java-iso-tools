package de.tu_darmstadt.informatik.rbg.mhartle.sabre;

public interface Fixup extends ContentHandler {
	public void close() throws HandlerException;
	public boolean isClosed();	
}
