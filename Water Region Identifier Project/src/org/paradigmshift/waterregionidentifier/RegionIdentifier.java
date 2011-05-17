package org.paradigmshift.waterregionidentifier;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;


public class RegionIdentifier {
	
	private static final Logger log = Logger.getLogger( RegionIdentifier.class );
	
	private final WaterMatrix matrix;
	
	// TODO: make collection to hold onto sorters
	private Map<Long, CategorySorter> sorters = new HashMap<Long, CategorySorter>();
	
	// TODO: make hashset to eventually contain all regions - used to create result file
	private Collection<Region> regions = new LinkedList<Region>();
	
	public RegionIdentifier( final WaterMatrix matrix ) {
		
		this.matrix = matrix;
	}
	
	public String identifyRegions() throws InterruptedException, ExecutionException, NumberFormatException, IOException {
		
		if ( log.isInfoEnabled() ) {
			
			log.info( "Processing the matrix..." );
		}
		
		// Sort all the pixels by category - divide and conquer
		for ( int y = 0; y < matrix.getNumRows(); y++ ) {
			
			for ( int x = 0; x < matrix.getNumCols(); x++ ) {
				
				WaterPixel pixel = matrix.getWaterPixel( x, y );
				
				// Add pixel to existing category sorter or create a new sorter
				Long category = pixel.getCategory();
				CategorySorter sorter = sorters.get( category );
				if ( sorter == null ) {
					
					sorter = new CategorySorter( matrix, category );
					sorters.put( category, sorter );
				}
				sorter.add( pixel );
			}
		}
		
		// Start sorting and wait for all tasks to complete
		ExecutorService threadPool = Executors.newFixedThreadPool( 10 );
		Collection<CategorySorter> tasks = sorters.values();
		for ( Future<Collection<Region>> f : threadPool.invokeAll( tasks ) ) {
			
			Collection<Region> collection = f.get();
			if ( collection != null ) regions.addAll( collection );
		}
		threadPool.shutdown();
		
		// Set the ids on the regions
		long regionId = 0;
		for ( Region region : regions ) region.setId( regionId++ );
		if ( log.isInfoEnabled() ) log.info( "Regions identified." );
			
		if ( log.isDebugEnabled() ) log.debug( "Preparing results..." );
		
		// Construct results string
		StringBuilder outputSb = new StringBuilder();
		for ( int y = 0; y < matrix.getNumRows(); y++ ) {
			
			for ( int x = 0; x < matrix.getNumCols(); x++ ) {
				
				WaterPixel pixel = matrix.getWaterPixel( x, y );
				for ( Region region : regions ) {
					
					if ( region.contains( pixel ) ) {
						
						outputSb.append( region.getId() );
						if ( x < matrix.getNumCols() - 1 ) outputSb.append( WaterMatrix.COLUMN_SEPARATOR );
						break;
					}
				}
			}
			outputSb.append( WaterMatrix.LINE_SEPARATOR );
		}
		
		log.info( "Processing complete." );
		return outputSb.toString();
	}
}
