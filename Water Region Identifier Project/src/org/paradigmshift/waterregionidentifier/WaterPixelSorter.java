package org.paradigmshift.waterregionidentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;


public final class WaterPixelSorter implements Callable<Collection<Region>> {
	
	private static final Logger log = Logger.getLogger( WaterPixelSorter.class );
	
	private final WaterMatrix matrix;
	private final long category;
	private final Collection<WaterPixel> pixels = new ArrayList<WaterPixel>();
	private final Collection<Region> regions = new ArrayList<Region>();
	
	public WaterPixelSorter( final WaterMatrix matrix, long category ) {
		
		this.matrix = matrix;
		this.category = category;
	}
	
	public void add( WaterPixel pixel ) {
		
		pixels.add( pixel );
	}
	
	@Override
	public Collection<Region> call() throws Exception {
		
		if ( log.isDebugEnabled() ) log.debug( "Processing category: " + category );
		
		for ( WaterPixel currentPixel : pixels ) {
			
			// Get the pixel's location
			int x = currentPixel.getX();
			int y = currentPixel.getY();
			Region currentRegion = currentPixel.getRegion();
			
			// Calculate the locations of the neighboring pixels to compare against
			WaterPixel [] neighbors = new WaterPixel[ 8 ];
			neighbors[0] = matrix.getWaterPixel( x - 1, y + 1 );	// North West
			neighbors[1] = matrix.getWaterPixel( x, y + 1 );		// North
			neighbors[2] = matrix.getWaterPixel( x + 1, y + 1 );	// North East
			neighbors[3] = matrix.getWaterPixel( x - 1, y );		// West
			neighbors[4] = matrix.getWaterPixel( x + 1, y );		// East
			neighbors[5] = matrix.getWaterPixel( x - 1, y - 1 );	// South West
			neighbors[6] = matrix.getWaterPixel( x, y - 1 );		// South
			neighbors[7] = matrix.getWaterPixel( x + 1, y - 1 );	// South East
			
			for ( WaterPixel neighbor : neighbors ) {
				
				if ( neighbor != null && neighbor.getCategory() == category ) {
					
					currentRegion = currentPixel.getRegion();
					Region neighborRegion = neighbor.getRegion();
					
					if ( neighborRegion != null ) {
						
						if ( currentRegion == null ) {
							
							// Insert the current pixel into the neighbor region
							neighborRegion.add( currentPixel );
						}
						else if ( !currentRegion.equals( neighborRegion ) ) {
							
							// Do a merge
							if ( log.isDebugEnabled() ) log.debug( "Merging regions " + currentRegion.toString() + " and " + neighborRegion.toString() + " for category " + category );
							neighborRegion.merge( currentRegion );
							regions.remove( currentRegion );
							
							// Insert the current pixel into the neighbor region
							neighborRegion.add( currentPixel );
						}
					}
				}
			}
			
			if ( currentRegion == null ) {
				
				// Means all of or a combination of neighbors and/or their regions were null
				if ( log.isDebugEnabled() ) log.debug( "Creating a new region for category " + category );
				Region region = new Region();
				region.add( currentPixel );
				regions.add( region );
			}
		}
		
		if ( log.isDebugEnabled() ) log.debug( "Completed category: " + category );
		
		return regions;
	}
}
