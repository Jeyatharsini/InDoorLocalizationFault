package db2.esper.event.models;

import db2.esper.common.SensorParsedData;

public class PirwEvent extends SensorEvent {
	
	private final int RADIUS = 10;
	
	public PirwEvent(SensorParsedData sensorParsedData) {
		super(sensorParsedData);
		
		this.radius = RADIUS;
		this.categoryID = sensorParsedData.getCategoryName();
	}

	@Override
	public String toString() {
		return "Pirw [Category=" + categoryID + ", deviceID=" + deviceID
				+ ", status=" + status + ", x=" + x + ", y=" + y
				+ ", timestamp=" + timestamp + "]";
	}
	
}
