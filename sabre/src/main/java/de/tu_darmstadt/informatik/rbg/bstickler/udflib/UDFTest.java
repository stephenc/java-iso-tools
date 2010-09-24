package de.tu_darmstadt.informatik.rbg.bstickler.udflib;
/*
 *	UDFTest.java
 *
 *	2006-06-01
 *
 *	Björn Stickler <bjoern@stickler.de>
 */

import java.io.*;
import java.util.*;

public class UDFTest
{
	
	private void testUDFImageBuilder()
	{
		try
		{
			UDFImageBuilder myUDFImageBuilder = new UDFImageBuilder();
			
			File testFile = new File( "C:\\Program Files (x86)\\Microsoft Visual Studio 8" );			
			File testChildFiles[] = testFile.listFiles();
			for( int i = 0; i < testChildFiles.length; ++i )
			{
				myUDFImageBuilder.addFileToRootDirectory( testChildFiles[ i ] );
			}
									
			myUDFImageBuilder.setImageIdentifier( "Test-Disc" );
			
			myUDFImageBuilder.writeImage( "c:\\temp\\test-disc.iso", UDFRevision.Revision201 );
		}
		catch( Exception myException )
		{
			System.out.println( myException.toString() );
			myException.printStackTrace();
		}
	}
	
	public void testSabreUDFImageBuilder()
	{
		try
		{
			SabreUDFImageBuilder mySabreUDF = new SabreUDFImageBuilder();
			
			File testFile = new File( "C:\\Program Files (x86)\\Microsoft Visual Studio 8" );			
			File testChildFiles[] = testFile.listFiles();
			for( int i = 0; i < testChildFiles.length; ++i )
			{
				mySabreUDF.addFileToRootDirectory( testChildFiles[ i ] );
			}
									
			mySabreUDF.setImageIdentifier( "Test-Disc" );
			
			mySabreUDF.writeImage(  "c:\\temp\\test-disc.iso", UDFRevision.Revision201 );
		}
		catch( Exception myException )
		{
			System.out.println( myException.toString() );
			myException.printStackTrace();
		}
	}
		
	public UDFTest()
	{
		System.out.println( "UDFTest\n" );
		
		long startTime= Calendar.getInstance().getTimeInMillis();

		//testUDFImageBuilder();
		testSabreUDFImageBuilder();	
		
		System.out.println( "Run-Time: " + ( Calendar.getInstance().getTimeInMillis() - startTime ) + " Milliseconds" );
	}

	public static void main( String[] args )
	{		
		new UDFTest();
	}

}
