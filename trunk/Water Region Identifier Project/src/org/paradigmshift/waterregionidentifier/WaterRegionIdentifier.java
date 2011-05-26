package org.paradigmshift.waterregionidentifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;


public class WaterRegionIdentifier {
	
	private static final Logger log = Logger.getLogger( WaterRegionIdentifier.class );
	
	private static final long IGNORED_VALUE = -1L; 
	
	private static final String LOG_CONFIG = "log4j-config.xml";
	private static final String DEFAULT_OUTPUT_FILENAME = "results.csv";
	private static final char OPTION_ARG_SEPARATOR = ',';
	
	public static String processFile( File srcFile, List<Long> ignoreValues ) {
		
		String resultString = null;
		WaterMatrix matrix = new WaterMatrix( srcFile );
		try {
			
			matrix.identifyRegions( ignoreValues );
			
			// Prepare the results
			StringBuilder outputSb = new StringBuilder();
			for ( int y = 0; y < matrix.getNumRows(); y++ ) {
				
				for ( int x = 0; x < matrix.getNumCols(); x++ ) {
					
					WaterPixel pixel = matrix.getWaterPixel( x, y );
					Region region = pixel.getRegion();
					long regionId = (region != null ) ? region.getId() : IGNORED_VALUE;
					outputSb.append( regionId );
					if ( x < matrix.getNumCols() - 1 ) outputSb.append( WaterMatrix.COLUMN_SEPARATOR );
				}
				outputSb.append( WaterMatrix.LINE_SEPARATOR );
			}
			resultString = outputSb.toString();
		}
		catch ( NumberFormatException nfe ) {
			log.fatal( "Invalid number in input file.", nfe );
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
		
		return resultString;
	}
	
	public static void printResults( String resultString, File resultFile ) {
		
		try {
			
			if ( log.isDebugEnabled() ) log.debug( "Writting results to: " + resultFile.getAbsolutePath() );
			
			BufferedWriter writer = new BufferedWriter( new FileWriter( resultFile ) );
			
			try {
				writer.write( resultString );
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
	
	public static void main( String[] args ) {
		
		Long start = System.currentTimeMillis();
		
		// Setup Log4j
		DOMConfigurator.configure( LOG_CONFIG );
		
		// Define synonyms
		List<String> ignoreSynonyms = new ArrayList<String>();
		ignoreSynonyms.add( "i" );
		ignoreSynonyms.add( "ignore" );
		
		List<String> srcSynonyms = new ArrayList<String>();
		srcSynonyms.add( "s" );
		srcSynonyms.add( "source" );
		
		List<String> resultSynonyms = new ArrayList<String>();
		resultSynonyms.add( "r" );
		resultSynonyms.add( "result" );
		
		// Define options
		File result = new File( DEFAULT_OUTPUT_FILENAME );
		OptionParser parser = new OptionParser();
		OptionSpec<Long> ignoreSpec = parser.acceptsAll( ignoreSynonyms, "Set ignore categories. Ex: -i 1,2,3" ).withOptionalArg().ofType( Long.class ).withValuesSeparatedBy( OPTION_ARG_SEPARATOR ).defaultsTo( -1L );
		OptionSpec<File> srcFileSpec = parser.acceptsAll( srcSynonyms, "Sets the source file." ).withRequiredArg().ofType( File.class ).required();
		OptionSpec<File> resultFileSpec = parser.acceptsAll( resultSynonyms, "Sets the result file." ).withRequiredArg().ofType( File.class ).defaultsTo( result );
		
		// Parse options
		OptionSet options = parser.parse( args );
			
		// Ignored categories
		List<Long> ignoreValues = null;
		if ( options.has( ignoreSpec ) && !options.hasArgument( ignoreSpec ) ) {
			
			ignoreValues = null;
		}
		else {
			
			ignoreValues = options.valuesOf( ignoreSpec );
		}
		
		// Process the source file
		if ( !options.has( srcFileSpec ) || !options.hasArgument( srcFileSpec ) ) {
			
			IllegalArgumentException iae = new IllegalArgumentException();
			log.fatal( "Must specify source file.", iae );
			throw iae;
		}
		else {
			
			File srcFile = options.valueOf( srcFileSpec );
			String resultString = processFile( srcFile, ignoreValues );
			
			// Write the results to file
			if ( resultString != null ) printResults( resultString, options.valueOf( resultFileSpec ) );
		}
		
		// Calculate running time
		Long elapsed = System.currentTimeMillis() - start;
		SimpleDateFormat formatter = new SimpleDateFormat( "HH:mm:ss:SSS" );
		formatter.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
		if ( log.isInfoEnabled() ) log.info( "Completed! Elapsed time: " + formatter.format( elapsed ) );
	}
}
