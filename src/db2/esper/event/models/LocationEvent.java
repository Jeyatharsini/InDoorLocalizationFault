package db2.esper.event.models;

public class LocationEvent {
	
	protected double x;
	protected double y;
	protected long timestamp;
	protected int radius = 1;
	
	public LocationEvent(double positionX, double positionY, long timestamp) {
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
