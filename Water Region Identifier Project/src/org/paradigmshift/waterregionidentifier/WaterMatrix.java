package org.paradigmshift.waterregionidentifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.log4j.Logger;


// Immutable
public class WaterMatrix {
	
	private static final Logger log = Logger.getLogger( WaterMatrix.class );
	
	public static final String COLUMN_SEPARATOR = ",";
	public static final String LINE_SEPARATOR = System.getProperty( "line.separator" );
	
	private WaterPixel [][] matrix;
	
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
				if ( tempColCount > 0 && colCount == 0 ) {
					
					colCount = tempColCount;
				}
				
				if ( tempColCount <= 0 || !tempColCount.equals( colCount ) ) {
					
					log.fatal( "Row " + rowCount + " has an error with number of columns.", new IllegalArgumentException( "colCount: " + colCount + ", tempColCount: " + tempColCount ) );
				}
			}
			if ( rowCount <= 0 ) log.fatal( "File contains no rows.", new IllegalArgumentException( "rowCount: 0" ) );
			if ( log.isDebugEnabled() ) log.debug( "rows: " + rowCount + " cols: " + colCount );
			
			// Create the matrix
			matrix = new WaterPixel[ colCount ][ rowCount ];
			line = null;
			int y = 0;
			
			// Populate the matrix
			reader.close();
			reader = new BufferedReader( new FileReader( inputFile ) );
			while ( ( line = reader.readLine()) != null ) {
				
				String [] numbers = line.split( COLUMN_SEPARATOR );
				for ( int x = 0; x < colCount; x++ ) {
					
					// Insert into the matrix
					long category = Long.parseLong( numbers[ x ] );
					matrix[ x ][ y ] = new WaterPixel( category, x, y );
				}
				
				y++;
			}
			
			if ( log.isInfoEnabled() ) log.info( "Data load complete." );
		}
		catch ( IOException ioe ) {
			
			log.fatal( "Encountered an error when trying to access the file.", new IllegalArgumentException( ioe ) );
		}
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
}
