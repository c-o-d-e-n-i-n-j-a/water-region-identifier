package org.paradigmshift.waterregionidentifier;

import java.util.ArrayList;
import java.util.Collection;


public final class Region {
	
	private long id;
	private Collection<WaterPixel> pixels;
	
	public Region() {}
	
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
