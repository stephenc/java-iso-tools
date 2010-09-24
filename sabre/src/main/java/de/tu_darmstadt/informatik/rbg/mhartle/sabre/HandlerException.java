package de.tu_darmstadt.informatik.rbg.mhartle.sabre;

public class HandlerException extends Exception {
	private static final long serialVersionUID = -4027062370589486907L;

	public HandlerException(String message) {
		super(message);
	}
	
	public HandlerException(Exception e) {
		super(e);
	}
}
