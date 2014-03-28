package db2.esper.event.models;

import java.sql.Timestamp;

public class PircEvent extends SensorEvent {

	private final String CATEGORY_ID = "pirc";
	private final int RADIUS = 5;
	
	public PircEvent(Timestamp timestamp, int deviceID, boolean status, double x, double y) {
		super(timestamp, deviceID, status, x, y);
		
		this.radius = RADIUS;
		this.categoryID = CATEGORY_ID;
	}

	@Override
	public String toString() {
		return "Pirc [Category=" + categoryID + ", Radius=" + radius
				+ ", deviceID=" + deviceID + ", status=" + status + ", x=" + x
				+ ", y=" + y + ", timestamp=" + timestamp + "]";
	}

	
}
