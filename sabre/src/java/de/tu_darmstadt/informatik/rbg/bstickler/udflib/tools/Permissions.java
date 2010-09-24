/*
 *	Permissions.java
 *
 *	2006-06-23
 *
 *	Björn Stickler <bjoern@stickler.de>
 */ 


package de.tu_darmstadt.informatik.rbg.bstickler.udflib.tools;

public class Permissions
{
	public static int OTHER_Execute	=	0x00000001;
	public static int OTHER_Write	=	0x00000002;
	public static int OTHER_Read	=	0x00000004;
	public static int OTHER_ChAttr	=	0x00000008;
	public static int OTHER_Delete	=	0x00000010;
	
	public static int GROUP_Execute	=	0x00000020;
	public static int GROUP_Write	=	0x00000040;
	public static int GROUP_Read	=	0x00000080;
	public static int GROUP_ChAttr	=	0x00000100;
	public static int GROUP_Delete	=	0x00000200;
	
	public static int OWNER_Execute	=	0x00000400;
	public static int OWNER_Write	=	0x00000800;
	public static int OWNER_Read	=	0x00001000;
	public static int OWNER_ChAttr	=	0x00002000;
	public static int OWNER_Delete	=	0x00004000;
	

}
