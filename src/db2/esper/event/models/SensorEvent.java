package db2.esper.event.models;

public abstract class SensorEvent extends LocationEvent {
	
	protected int deviceID;
	protected boolean status;

	public SensorEvent(long timestamp, int deviceID, boolean status, double x, double y) {
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
