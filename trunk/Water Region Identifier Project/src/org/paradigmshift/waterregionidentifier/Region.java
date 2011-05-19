package org.paradigmshift.waterregionidentifier;

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


public final class Region {
	
	private static final Logger log = Logger.getLogger( Region.class );
	
	private static int maxLength = 0; 
	
	private long id;
	private Collection<WaterPixel> pixels;
	
	public Region() {}
	
	public static void identifyRegions( WaterMatrix matrix ) throws InterruptedException, ExecutionException, NumberFormatException, IOException {
		
		if ( log.isInfoEnabled() ) {
			
			log.info( "Processing the matrix..." );
		}
		
		Map<Long, WaterPixelSorter> sorters = new HashMap<Long, WaterPixelSorter>();
		Collection<Region> regions = new ArrayList<Region>();
		
		// Sort all the pixels by category - divide and conquer
		for ( int y = 0; y < matrix.getNumRows(); y++ ) {
			
			for ( int x = 0; x < matrix.getNumCols(); x++ ) {
				
				WaterPixel pixel = matrix.getWaterPixel( x, y );
				
				// Add pixel to existing category sorter or create a new sorter
				Long category = pixel.getCategory();
				WaterPixelSorter sorter = sorters.get( category );
				if ( sorter == null ) {
					
					sorter = new WaterPixelSorter( matrix, category );
					sorters.put( category, sorter );
				}
				sorter.add( pixel );
			}
		}
		
		// Start sorting and wait for all tasks to complete
		ExecutorService threadPool = Executors.newFixedThreadPool( 10 );
		Collection<WaterPixelSorter> tasks = sorters.values();
		for ( Future<Collection<Region>> f : threadPool.invokeAll( tasks ) ) {
			
			Collection<Region> collection = f.get();
			if ( collection != null ) regions.addAll( collection );
		}
		threadPool.shutdown();
		
		// Set the ids on the regions
		long regionId = 0;
		for ( Region region : regions ) region.setId( regionId++ );
		Region.maxLength = String.valueOf( regionId - 1 ).length();
		if ( log.isInfoEnabled() ) log.info( "Processing complete: regions identified." );
	}
	
	public static int getMaxLength() {
		
		return Region.maxLength;
	}
	
	public synchronized long getId() {
		
		return this.id;
	}
	
	public synchronized void setId( long id ) {
		
		this.id = id;
	}
	
	public synchronized void add( WaterPixel pixel ) {
		
		if ( pixels == null ) pixels = new ArrayList<WaterPixel>();
		pixels.add( pixel );
		pixel.setRegion( this );
	}
	
	public synchronized void merge( Region regionToMerge ) {
		
		// Obtain a lock on the other region to prevent access to it till the pixels are updated
		synchronized ( regionToMerge ) {
			
			Collection<WaterPixel> pixelsToMerge = regionToMerge.getPixels();
			pixels.addAll( pixelsToMerge );
			regionToMerge.removeAllPixels();
			
			for ( WaterPixel pixel : pixelsToMerge ) {
				
				pixel.setRegion( this );
			}
		}
	}
	
	public synchronized Collection<WaterPixel> getPixels() {
		
		return pixels;
	}
	
	public synchronized boolean contains( WaterPixel pixel ) {
		
		return pixels.contains( pixel );
	}
	
	public synchronized void removeAllPixels() {
		
		pixels = null;
	}
}
