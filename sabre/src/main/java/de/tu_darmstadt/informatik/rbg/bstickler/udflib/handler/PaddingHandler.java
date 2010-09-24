/*
 *	PaddingHandler.java
 *
 *	2006-07-22
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.handler;

import de.tu_darmstadt.informatik.rbg.mhartle.sabre.*;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.impl.*;

public class PaddingHandler extends ChainingStreamHandler
{
	private int blockSize = 2048;
	private long currentPosition = 0;
	
	public PaddingHandler( 	StructureHandler myStructureHandler, ContentHandler myContentHandler )
	{
		super( myStructureHandler, myContentHandler );
	}
	
	public void setBlockSize( int blockSize )
	{
		this.blockSize = blockSize;
	}
	
	public void data( DataReference myDataReference )
	throws HandlerException
	{
		currentPosition += myDataReference.getLength();
		
		super.data( myDataReference );
	}	

	public void endElement()
	throws HandlerException
	{
		if( currentPosition % blockSize != 0 )
		{
			int paddingLength = blockSize - (int)( currentPosition % blockSize );
			super.data( new ByteArrayDataReference( new byte[paddingLength] ) );
			
			currentPosition += paddingLength;
		}		
		
		super.endElement();
	}

}
