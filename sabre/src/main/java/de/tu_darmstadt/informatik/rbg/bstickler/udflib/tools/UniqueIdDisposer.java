/*
 *	UniqueIdDisposer.java
 *
 *	2006-06-14
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

package de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools;


public class UniqueIdDisposer
{
	private long currentUniqueId;
	
	public UniqueIdDisposer()
	{
		currentUniqueId = 15;
	}
	
	public long getNextUniqueId()
	{
		currentUniqueId++;
		
		if( (currentUniqueId & 0xFFFFFFFF) == 0 )
		{
			currentUniqueId |= 0x00000010;			
		}
		
		return currentUniqueId;
	}

}
