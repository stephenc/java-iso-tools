/*
 *	DescriptorTagHandler.java
 *
 *	2006-07-22
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.handler;

import java.io.*;
import java.util.*;

import de.tu_darmstadt.informatik.rbg.bstickler.udflib.SabreUDFElement.UDFElementType;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.structures.*;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.BinaryTools;
import de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools.Checksum;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;


public class DescriptorTagHandler extends ChainingStreamHandler
{
	protected Stack<Element> elementStack;
	protected Stack<DataReference> dataReferenceStack;
	
	public DescriptorTagHandler( StructureHandler myStructureHandler, ContentHandler myContentHandler )
	{
		super( myStructureHandler, myContentHandler );
		
		elementStack = new Stack<Element>();
		dataReferenceStack = new Stack<DataReference>();
	}
	
	public void startElement( Element myElement )
	throws HandlerException
	{
		elementStack.push( myElement );
		super.startElement( myElement );
	}

	public void endElement()
	throws HandlerException
	{
		Element myElement = elementStack.pop();

		if( myElement.getId() == UDFElementType.DescriptorTag )
		{
			createAndPassDescriptorTag();
		}
		
		super.endElement();
	}
	
	private void createAndPassDescriptorTag()
	throws HandlerException
	{
		InputStream myInputStream = null;
		Tag descriptorTag = new Tag();
		byte[] payload = new byte[0];

		try
		{
			DataReference myDataReference = dataReferenceStack.pop();
			myInputStream = myDataReference.createInputStream();
			payload = BinaryTools.readByteArray( myInputStream, (int)myDataReference.getLength() );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			descriptorTag.DescriptorVersion = (int)BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			descriptorTag.TagSerialNumber = (int)BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
			myInputStream = dataReferenceStack.pop().createInputStream();
			descriptorTag.TagLocation = BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;

			myInputStream = dataReferenceStack.pop().createInputStream();
			descriptorTag.TagIdentifier = (int)BinaryTools.readUInt32AsLong( myInputStream );
			myInputStream.close();
			myInputStream = null;
			
		}
		catch( IOException myIOException )
		{
			throw new HandlerException( myIOException );
		}
		finally
		{
			if( myInputStream != null )
			{
				try
				{
					myInputStream.close();
				}
				catch( IOException myIOException ) {}
			}
		}		
		
	    descriptorTag.DescriptorCRCLength = payload.length;
	    descriptorTag.DescriptorCRC = Checksum.cksum( payload );
	    
	    super.data( new ByteArrayDataReference( descriptorTag.getBytes() ) );
	    super.data( new ByteArrayDataReference( payload ) );
	}
	
	public void data( DataReference myDataReference )
	throws HandlerException
	{
		if( ( elementStack.size() != 0 ) && elementStack.peek().getId() == UDFElementType.DescriptorTag )
		{
			dataReferenceStack.push( myDataReference );
		}
		else
		{
			super.data( myDataReference );
		}
	}	

}
