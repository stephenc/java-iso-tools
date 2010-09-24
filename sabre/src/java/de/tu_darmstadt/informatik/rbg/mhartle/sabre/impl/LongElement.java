package de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.Element;

public class LongElement extends Element {
	private long id = 0;
	
	public LongElement(long id) {
		this.id = id;
	}

	public Object getId() {
		return new Long(id);
	}
	
	public String toString() {
		String result = null;
		
		result = new String();
		result += (char)((id & 0xFF000000) >> 24);
		result += (char)((id & 0x00FF0000) >> 16);
		result += (char)((id & 0x0000FF00) >> 8);
		result += (char)((id & 0x000000FF));
		
		return result;
	}
}
