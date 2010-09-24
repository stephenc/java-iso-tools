package de.tu_darmstadt.informatik.rbg.mhartle.sabre;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataReferenceHelper {	
	public static void transfer(DataReference reference, OutputStream outputStream) throws IOException {
		InputStream inputStream = null;
		byte[] buffer = null;
		int bytesToRead = 0;
		int bytesHandled = 0;
		int bufferLength = 65535;
		long lengthToWrite = 0;
		long length = 0;
		
		buffer = new byte[bufferLength];
		length = reference.getLength();
		lengthToWrite = length;
		inputStream = reference.createInputStream();
		while(lengthToWrite > 0) {
			if (lengthToWrite > bufferLength) {
				bytesToRead = bufferLength;
			} else {
				bytesToRead = (int)lengthToWrite;
			}

			bytesHandled = inputStream.read(buffer, 0, bytesToRead);

			if (bytesHandled == -1) {
				throw new IOException();
			}

			outputStream.write(buffer, 0, bytesHandled);
			lengthToWrite -= bytesHandled;
		}
		outputStream.flush();	
	}
}