package db2.esper.event.models;

import db2.esper.common.SensorParsedData;

public abstract class SensorEvent {
	
	protected double x;
	protected double y;
	protected long timestamp;
	protected float radius = 0;
	protected int deviceID;
	protected boolean status;
	protected String categoryID = null;

	public SensorEvent(SensorParsedData sensorParsedData) {

		this.x = sensorParsedData.getX();
		this.y = sensorParsedData.getY();
		this.timestamp = sensorParsedData.getTimestamp();

		this.deviceID = sensorParsedData.getDeviceID();
		this.status = sensorParsedData.isStatus();
		
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

	public float getRadius() {
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
