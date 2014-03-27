package db2.esper.event.models;

public class PirwEvent extends SensorEvent {
	
	private final String CATEGORY_ID = "pirw";
	private final int RADIUS = 10;
	
	public PirwEvent(long timestamp, int deviceID, boolean status, double x, double y) {
		super(timestamp, deviceID, status, x, y);
	}

	@Override
	public String toString() {
		return "Pirw [CATEGORY_ID=" + CATEGORY_ID + ", deviceID=" + deviceID
				+ ", status=" + status + ", x=" + x + ", y=" + y
				+ ", timestamp=" + timestamp + "]";
	}
	
}
