package org.paradigmshift.waterregionidentifier;

import java.util.Collection;
import java.util.HashSet;

public final class Region {
	
	private long id;
	private final Collection<WaterPixel> pixels = new HashSet<WaterPixel>();
	
	public Region() {}
	
	public long getId() {
		
		return this.id;
	}
	
	public void setId( long id ) {
		
		this.id = id;
	}
	
	public void add( WaterPixel pixel ) {
		
		pixels.add( pixel );
	}
	
	public void addAll( Collection<WaterPixel> pixels ) {
		
		this.pixels.addAll( pixels );
	}
	
	public Collection<WaterPixel> getPixels() {
		
		return pixels;
	}
	
	public boolean contains( WaterPixel pixel ) {
		
		return pixels.contains( pixel );
	}
}
