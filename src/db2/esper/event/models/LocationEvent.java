package db2.esper.event.models;

public class LocationEvent {
	
	protected double x;
	protected double y;
	protected long timestamp;
	
	public LocationEvent(double x, double y, long timestamp) {
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
