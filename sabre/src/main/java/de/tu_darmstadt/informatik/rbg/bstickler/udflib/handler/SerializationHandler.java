/*
 *	SerializationHandler.java
 *
 *	2006-07-06
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.handler;

import java.io.*;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class SerializationHandler implements StreamHandler
{	
	private File myOutputFile;
	private DataOutputStream myDataOutputStream;
	private long position;
	
	public SerializationHandler( File outputFile )
	throws HandlerException
	{
		myOutputFile = outputFile;
		position = 0;
	}
	
	public void startDocument()
	throws HandlerException
	{
		position = 0;
		
		try
		{
			myDataOutputStream = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( myOutputFile ) ) ) ;
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
		}
	}
	
	public void endDocument()
	throws HandlerException
	{
		try
		{
			myDataOutputStream.close();
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
		}	
	}
	
	public void startElement( Element myElement )
	throws HandlerException
	{
		
	}

	public void endElement()
	throws HandlerException
	{
	
	}	
	
	public void data( DataReference myDataReference )
	throws HandlerException
	{
		InputStream myInputStream = null;
		
		try
		{
			int bufferLength = 32768;
			byte[] buffer = new byte[ bufferLength ];			
			long length = myDataReference.getLength();
			long lengthToWrite = length;
			int bytesToRead = 0;
			int bytesHandled = 0;
			
			myInputStream = myDataReference.createInputStream();
			
			while( lengthToWrite > 0 )
			{
				if( lengthToWrite > bufferLength )
				{
					bytesToRead = bufferLength;
				}
				else
				{
					bytesToRead = (int)lengthToWrite;
				}

				bytesHandled = myInputStream.read( buffer, 0, bytesToRead );
				
				if( bytesHandled == -1 ) 
				{				
					throw new HandlerException("Cannot read all data from reference.");
				}

				myDataOutputStream.write( buffer, 0, bytesHandled );
				lengthToWrite -= bytesHandled;
				this.position += bytesHandled;
			}
			
			myDataOutputStream.flush();
			
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
		}
		finally
		{
			try
			{
				if( myInputStream != null )
				{
					myInputStream.close();
					myInputStream = null;
				}
			}
			catch( IOException myIOException ){}
		}
	}
	
	public Fixup fixup( DataReference myDataReference ) throws HandlerException {
		try {
			Fixup fixup = new FileFixup( new RandomAccessFile(myOutputFile, "rw"), position, myDataReference.getLength() );
			data( myDataReference );
			return fixup;
		} catch (FileNotFoundException e) {
			throw new HandlerException(e);
		}
	}	

	public long mark()
	throws HandlerException
	{
		return position;
	}
	
}
