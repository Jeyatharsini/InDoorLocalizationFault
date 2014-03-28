package db2.esper.event.models;

import java.text.DecimalFormat;

public class LocationEvent {
	
	protected double x;
	protected double y;
	protected float timestamp;
	protected int radius = 0;
	
	public LocationEvent(double positionX, double positionY, float timestamp) {
		this.x = positionX;
		this.y = positionY;
		this.timestamp = timestamp;
	}
	
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}

	public float getTimestamp() {
		return timestamp;
	}

	public int getRadius() {
		return radius;
	}

	@Override
	public String toString() {
		DecimalFormat decimalFormat = new DecimalFormat("##########.##");
		
		return "LocationEvent [Position x= " + x + ", Position y= " + y + ", timestamp= " + decimalFormat.format(timestamp) + "]";
	}

}
