package db2.esper.event.models;

import java.sql.Timestamp;

public class LocationEvent {
	
	protected double x;
	protected double y;
	protected Timestamp timestamp;
	
	public LocationEvent(double x, double y, Timestamp timestamp) {
		this.x = x;
		this.y = y;
		this.timestamp = timestamp;
	}
	
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}

	@Override
	public String toString() {
		return "GenericEvent [Position x= " + x + ", Position y= " + y + ", timestamp= " + timestamp + "]";
	}

}
