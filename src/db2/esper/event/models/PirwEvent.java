package db2.esper.event.models;

import java.sql.Timestamp;

public class PirwEvent extends SensorEvent {
	
	private final String CATEGORY_ID = "pirw";
	private final int RADIUS = 10;
	
	public PirwEvent(Timestamp timestamp, int deviceID, boolean status, double x, double y) {
		super(timestamp, deviceID, status, x, y);
		
		this.radius = RADIUS;
		this.categoryID = CATEGORY_ID;
	}

	@Override
	public String toString() {
		return "Pirw [Category=" + categoryID + ", deviceID=" + deviceID
				+ ", status=" + status + ", x=" + x + ", y=" + y
				+ ", timestamp=" + timestamp + "]";
	}
	
}
