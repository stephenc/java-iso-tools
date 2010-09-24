package de.tu_darmstadt.informatik.rbg.mhartle.sabre;

public interface ContentHandler {
	public void data(DataReference reference) throws HandlerException;
	public Fixup fixup(DataReference reference) throws HandlerException;
	public long mark() throws HandlerException;
}
