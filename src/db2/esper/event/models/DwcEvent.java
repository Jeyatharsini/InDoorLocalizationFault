package db2.esper.event.models;

import db2.esper.common.SensorParsedData;

public class DwcEvent extends SensorEvent {

	private final int RADIUS = 3;	// da capire se � da mettere o meno...
	
	public DwcEvent(SensorParsedData sensorParsedData) {
		super(sensorParsedData);
		
		this.radius = RADIUS;
		this.categoryID = sensorParsedData.getCategoryName();
	}

	@Override
	public String toString() {
		return "Dwc [Category=" + categoryID + ", Radius=" + radius
				+ ", deviceID=" + deviceID + ", status=" + status + ", x=" + x
				+ ", y=" + y + ", timestamp=" + timestamp + "]";
	}	
}
