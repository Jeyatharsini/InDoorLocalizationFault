package db2.esper.event.models;

import db2.esper.common.LocationParsedData;

public class LocationEvent {
	
	protected double x;
	protected double y;
	protected long timestamp;
	protected int radius = 1;
	
	public LocationEvent(LocationParsedData locationParsedData) {
		this.x = locationParsedData.getX();
		this.y = locationParsedData.getY();
		this.timestamp = locationParsedData.getTimestamp();
	}
	
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getRadius() {
		return radius;
	}

	@Override
	public String toString() {
		return "LocationEvent [Position x= " + x + ", Position y= " + y + ", timestamp= " + timestamp + "]";
	}

}
