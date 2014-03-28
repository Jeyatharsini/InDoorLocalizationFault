package db2.esper.event.models;

public abstract class SensorEvent {
	
	protected double x;
	protected double y;
	protected long timestamp;
	protected int radius = 0;
	protected int deviceID;
	protected boolean status;
	protected String categoryID = null;

	public SensorEvent(long timestamp, int deviceID, boolean status, double x, double y) {

		this.x = x;
		this.y = y;
		this.timestamp = timestamp;

		this.deviceID = deviceID;
		this.status = status;
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
	
	public int getDeviceID() {
		return deviceID;
	}

	public boolean isStatus() {
		return status;
	}	
	
	public String getCategoryID() {
		return categoryID;
	}
}
