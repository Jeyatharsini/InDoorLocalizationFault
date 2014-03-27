package db2.esper.event.models;

import java.sql.Timestamp;

public abstract class SensorEvent extends LocationEvent {
	
	protected int deviceID;
	protected boolean status;

	public SensorEvent(Timestamp timestamp, int deviceID, boolean status, double x, double y) {
		super(x, y, timestamp);

		this.deviceID = deviceID;
		this.status = status;
	}

	public int getDeviceID() {
		return deviceID;
	}

	public boolean isStatus() {
		return status;
	}	

}
