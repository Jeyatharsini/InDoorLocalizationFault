package db2.esper.event.models;

import db2.esper.common.SensorParsedData;

public class PircEvent extends SensorEvent {

	private final int RADIUS = 5;
	
	public PircEvent(SensorParsedData sensorParsedData) {
		super(sensorParsedData);
		
		this.radius = RADIUS;
		this.categoryID = sensorParsedData.getCategoryName();
	}

	@Override
	public String toString() {
		return "Pirc [Category=" + categoryID + ", Radius=" + radius
				+ ", deviceID=" + deviceID + ", status=" + status + ", x=" + x
				+ ", y=" + y + ", timestamp=" + timestamp + "]";
	}

	
}
