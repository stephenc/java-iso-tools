package de.tu_darmstadt.informatik.rbg.mhartle.sabre;

import java.io.IOException;
import java.io.InputStream;

public interface DataReference {
	public long getLength();
	public InputStream createInputStream() throws IOException;
}
