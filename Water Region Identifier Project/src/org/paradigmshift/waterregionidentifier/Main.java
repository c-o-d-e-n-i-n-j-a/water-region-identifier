package org.paradigmshift.waterregionidentifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;


public class Main {
	
	private static final Logger log = Logger.getLogger( Main.class );
	
	private static final String LOG_CONFIG = "log4j-config.xml";
	private static final String DEFAULT_OUTPUT_FILENAME = "results";
	private static final String FILE_EXTENSION = ".csv";
	
	public static void main( String[] args ) {
		
		// Setup Log4j
		DOMConfigurator.configure( LOG_CONFIG );
		
		Long start = System.currentTimeMillis();
		
		args = new String[1];
		args[ 0 ] = "test.csv";
		// Check args for input file
		if ( args != null && args.length >= 1 ) {
		
			// Load input file
			File inputFile = new File( args[0] );
			if ( inputFile.exists() && !inputFile.isDirectory() ) {
				
				try {
					
					// Process the input data
					WaterMatrix matrix = new WaterMatrix( inputFile );
					RegionIdentifier regionIdentifier = new RegionIdentifier( matrix );
					String results = regionIdentifier.identifyRegions();
					
					// Get the output file
					if ( log.isDebugEnabled() ) log.debug( "Writing results to file..." );
					File outputFile;
					try {
						
						// Check args for output dir name or file name
						StringBuilder filenameSb = new StringBuilder();
						String extension = FILE_EXTENSION;
						if ( args.length >= 2 ) {
							
							outputFile = new File( args[ 1 ] );
							if ( outputFile.isDirectory() ) {
								
								// Given a directory name
								if ( !outputFile.exists() ) {
									
									outputFile.mkdirs();
								}
								filenameSb.append( args[ 1 ] );
								filenameSb.append( DEFAULT_OUTPUT_FILENAME );
							}
							else {
								
								// Given a filename
								String name = outputFile.getName();
								int index = name.lastIndexOf( "." );
								filenameSb.append( name.substring( 0, index ) );
								extension = name.substring( index );
							}
						}
						else {
							
							// No dir or file specified
							filenameSb.append( DEFAULT_OUTPUT_FILENAME );
						}
						
						// Try to create the output file
						int fileNumber = 1;
						StringBuilder firstNameSb = new StringBuilder( filenameSb );
						firstNameSb.append( extension );
						outputFile = new File( firstNameSb.toString() );
						
						while ( !outputFile.createNewFile() ) {
							
							StringBuilder nextNameSb = new StringBuilder( filenameSb );
							nextNameSb.append( " (" );
							nextNameSb.append( fileNumber );
							nextNameSb.append( ")" );
							nextNameSb.append( extension );
							
							outputFile = new File( nextNameSb.toString() );
							fileNumber++;
						}
						
						log.info( "Writting results to: " + outputFile.getAbsolutePath() );
						
						// Write the output
						BufferedWriter writer;
						try {
							
							writer = new BufferedWriter( new FileWriter( outputFile ) );
							
							try {
								
								writer.write( results );
							}
							catch ( IOException ioe ) {
								
								log.error( "Failed to write results to output file.", ioe );
							}
							finally {
								
								try {
									
									writer.flush();
								}
								catch ( IOException ioe ) {}
								
								try {
									
									writer.close();
								}
								catch ( IOException ioe ) {
									
									log.error( "Failed to close output file.", ioe );
								}
							}
						}
						catch ( IOException ioe ) {
							
							log.fatal( "Failed to open output file for writting.", ioe );
						}
					}
					catch ( IOException ioe ) {
						
						log.fatal( "Failed to create output file.", ioe );
					}
				}
				catch ( NumberFormatException nfe ) {
					
					log.fatal( "Invalid data in input file.", nfe );
				}
				catch ( InterruptedException ie ) {
					
					log.fatal( "Process could not complete.", ie );
				}
				catch ( ExecutionException ee ) {
					
					log.fatal( "Process could not complete.", ee );
				}
				catch ( IOException ioe ) {
					
					log.fatal( "Failed to read input file.", ioe );
				}
			}
			else {
				
				// Must specify a file
				log.fatal( "Must specify an input csv file." );
			}
		}
		else {
			
			// No input file specified
			log.fatal( "Must specify an input csv file." );
		}
		
		Long finish = System.currentTimeMillis();
		Long elapsed = finish - start;
		SimpleDateFormat formatter = new SimpleDateFormat( "HH:mm:ss:SSS" );
		formatter.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
		
		StringBuilder timeSb = new StringBuilder( "Completed! Total time: " );
		timeSb.append( formatter.format( elapsed ) );
		
		if ( log.isInfoEnabled() ) log.info( timeSb.toString() );
	}
}
