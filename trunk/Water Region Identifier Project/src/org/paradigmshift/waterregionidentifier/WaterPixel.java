package org.paradigmshift.waterregionidentifier;

// Immutable
public final class WaterPixel {
	
	private final long category;
	private final int x;
	private final int y;
	
	public WaterPixel( final long category, final int x, final int y ) {
		
		this.category = category;
		this.x = x;
		this.y = y;
	}
	
	public long getCategory() {
		
		return category;
	}
	
	public int getX() {
		
		return x;
	}
	
	public int getY() {
		
		return y;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (category ^ (category >>> 32));
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals( Object obj ) {

		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		WaterPixel other = (WaterPixel) obj;
		if ( category != other.category )
			return false;
		if ( x != other.x )
			return false;
		if ( y != other.y )
			return false;
		return true;
	}
}
