package org.paradigmshift.waterregionidentifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;


// Immutable
public final class WaterMatrix {
	
	private static final Logger log = Logger.getLogger( WaterMatrix.class );
	
	public static final String COLUMN_SEPARATOR = ",";
	public static final String LINE_SEPARATOR = System.getProperty( "line.separator" );
	
	private WaterPixel [][] matrix;
	private Map<Long, WaterPixelSorter> sorters = new HashMap<Long, WaterPixelSorter>();
	private int maxRegionDigits = 0; 
	
	public WaterMatrix( File inputFile ) {
		
		if ( inputFile == null ) log.fatal( "Must specify a valid input file.", new IllegalArgumentException( "inputFile is null." ) );
		
		try {
			
			if ( log.isInfoEnabled() ) log.info( "Loading matrix data..." );
			
			BufferedReader reader = new BufferedReader( new FileReader( inputFile ) );
			String line;
			
			// Get number of rows and columns
			int rowCount = 0;
			int colCount = 0;
			if ( log.isDebugEnabled() ) log.debug( "Discovering number of rows and columns in the file..." );
			while ( (line = reader.readLine()) != null ) {
				
				rowCount++;
				Integer tempColCount = line.split( COLUMN_SEPARATOR ).length;
				if ( tempColCount > 0 && colCount == 0 ) colCount = tempColCount;
				if ( tempColCount <= 0 || !tempColCount.equals( colCount ) )
					log.fatal( "Row " + rowCount + " has an error with number of columns.", new IllegalArgumentException( "colCount: " + colCount + ", tempColCount: " + tempColCount ) );
			}
			if ( rowCount <= 0 ) log.fatal( "File contains no rows.", new IllegalArgumentException( "rowCount: 0" ) );
			if ( log.isDebugEnabled() ) log.debug( "rows: " + rowCount + " cols: " + colCount );
			
			// Create the matrix
			matrix = new WaterPixel[ colCount ][ rowCount ];
			line = null;
			int y = 0;
			
			// Populate the matrix and sort all the pixels by category (divide and conquer)
			reader.close();
			reader = new BufferedReader( new FileReader( inputFile ) );
			while ( (line = reader.readLine()) != null ) {
				
				String [] numbers = line.split( COLUMN_SEPARATOR );
				for ( int x = 0; x < colCount; x++ ) {
					
					// Insert into the matrix
					long category = Long.parseLong( numbers[ x ] );
					matrix[ x ][ y ] = new WaterPixel( category, x, y );
					
					// Add pixel to existing category sorter or create a new sorter
					WaterPixelSorter sorter = sorters.get( category );
					if ( sorter == null ) {
						
						sorter = new WaterPixelSorter( this, category );
						sorters.put( category, sorter );
					}
					sorter.add( matrix[ x ][ y ] );
				}
				
				y++;
			}
			
			if ( log.isInfoEnabled() ) log.info( "Data load complete." );
		}
		catch ( IOException ioe ) {
			
			log.fatal( "Encountered an error when trying to access the file.", new IllegalArgumentException( ioe ) );
		}
	}
	
	public void identifyRegions() throws InterruptedException, ExecutionException, NumberFormatException, IOException {
		
		if ( log.isInfoEnabled() ) log.info( "Processing the matrix..." );
		
		Collection<Region> regions = new ArrayList<Region>();
		
		// Start sorting and wait for all tasks to complete
		ExecutorService threadPool = Executors.newFixedThreadPool( 50 );
		Collection<WaterPixelSorter> tasks = sorters.values();
		for ( Future<Collection<Region>> f : threadPool.invokeAll( tasks ) ) {
			
			Collection<Region> collection = f.get();
			if ( collection != null ) regions.addAll( collection );
		}
		threadPool.shutdown();
		
		// Set the ids on the regions
		long regionId = 0;
		for ( Region region : regions ) region.setId( regionId++ );
		maxRegionDigits = String.valueOf( regionId - 1 ).length();
		if ( log.isInfoEnabled() ) log.info( "Processing complete: regions identified." );
	}
	
	public int getNumRows() {
		
		return matrix[ 0 ].length;
	}
	
	public int getNumCols() {
		
		return matrix.length;
	}
	
	public WaterPixel getWaterPixel( int x, int y ) {
		
		if ( x < 0 || x >= this.getNumCols() || y < 0 || y >= this.getNumRows() )
			return null;
		return matrix[ x ][ y ];
	}
	
	public int getMaxRegionDigits() {
		
		return maxRegionDigits;
	}
}
