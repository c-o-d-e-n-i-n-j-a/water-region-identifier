package org.paradigmshift.waterregionidentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;


public final class CategorySorter implements Callable<Collection<Region>> {
	
	private static final Logger log = Logger.getLogger( CategorySorter.class );
	
	private final WaterMatrix matrix;
	private final long category;
	private final Collection<WaterPixel> pixels = new LinkedList<WaterPixel>();
	private final Collection<Region> regions = new ArrayList<Region>();
	
	public CategorySorter( final WaterMatrix matrix, long category ) {
		
		this.matrix = matrix;
		this.category = category;
	}
	
	public void add( WaterPixel pixel ) {
		
		pixels.add( pixel );
	}
	
	@Override
	public Collection<Region> call() throws Exception {
		
		if ( log.isInfoEnabled() ) log.info( "Processing category: " + category );
		
		for ( WaterPixel pixel : pixels ) {
			
			// Get the pixel's location
			int x = pixel.getX();
			int y = pixel.getY();
			
			// Calculate the locations of the neighboring pixels to compare against
			WaterPixel [] neighbors = new WaterPixel[ 8 ];
			neighbors[0] = matrix.getWaterPixel( x - 1, y + 1 );
			neighbors[1] = matrix.getWaterPixel( x, y + 1 );
			neighbors[2] = matrix.getWaterPixel( x + 1, y + 1 );
			neighbors[3] = matrix.getWaterPixel( x - 1, y );
			neighbors[4] = matrix.getWaterPixel( x + 1, y );
			neighbors[5] = matrix.getWaterPixel( x - 1, y - 1 );
			neighbors[6] = matrix.getWaterPixel( x, y - 1 );
			neighbors[7] = matrix.getWaterPixel( x + 1, y - 1 );
			
			List<WaterPixel> sameCategory = new LinkedList<WaterPixel>();
			List<Region> regionsContainingACurrPixel = new ArrayList<Region>();
			
			for ( Region region : regions ) {
				
				if ( region.contains( pixel ) ) regionsContainingACurrPixel.add( region );
			}

			for ( WaterPixel neighbor : neighbors ) {
				
				// Compare the neighbor's and the current pixel's categories and check if neighbor is in a region
				if ( neighbor != null && neighbor.getCategory() == category ) {
					
					sameCategory.add( neighbor );
					
					for ( Region region : regions ) {
						
						if ( region.contains( neighbor ) ) regionsContainingACurrPixel.add( region );
					}
				}
			}
			
			// Create a new region if there are no regions.  Add all the current pixels to it.
			int numRegions = regionsContainingACurrPixel.size();
			if ( numRegions <= 0 ) {
				
				// Create a new region and all the current pixels
				Region region = new Region();
				region.add( pixel );
				for ( WaterPixel same : sameCategory ) {
					
					region.add( same );
				}
				
				regions.add( region );
			}
			// If there is one region make sure all current pixels are in it
			else if ( numRegions == 1 ) {
				
				// Make sure all the current pixels are in the one region
				Region region = regionsContainingACurrPixel.get( 0 );
				if ( !region.contains( pixel ) ) region.add( pixel );
				for ( WaterPixel same : sameCategory ) {
					
					if ( !region.contains( same ) ) region.add( same );
				}
			}
			// If there were more 2 or more regions, merge all regions and make sure all current pixels are in final region
			else {
				
				// Merge all the current regions into one
				Region region = new Region();
				for ( Region tempRegion : regionsContainingACurrPixel ) {
					
					region.addAll( tempRegion.getPixels() );
					regions.remove( tempRegion );
				}
				
				// Make sure the new region contains all the current pixels
				if ( !region.contains( pixel ) ) region.add( pixel );
				for ( WaterPixel same : sameCategory ) {
					
					if ( !region.contains( same ) ) region.add( same );
				}
				
				// Add the new final region to the list
				regions.add( region );
			}
		}
		
		if ( log.isDebugEnabled() ) log.debug( "Completed category: " + category );
		
		return regions;
	}
}
