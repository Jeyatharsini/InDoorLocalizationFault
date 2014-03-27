package db2.esper.event.models;

import java.sql.Timestamp;

public class LocationEvent {
	
	protected Double x;
	protected Double y;
	protected Timestamp timestamp;
	
	public LocationEvent(Double positionX, Double positionY, Timestamp timestamp) {
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

	@Override
	public String toString() {
		return "GenericEvent [Position x= " + x + ", Position y= " + y + ", timestamp= " + timestamp + "]";
	}

}
