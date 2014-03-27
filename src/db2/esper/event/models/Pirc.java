package db2.esper.event.models;

public class Pirc extends SensorEvent {

	private final String CATEGORY_ID = "pirc";
	private final int RADIUS = 5;
	
	public Pirc(long timestamp, int deviceID, boolean status, double x, double y) {
		super(timestamp, deviceID, status, x, y);
	}

	@Override
	public String toString() {
		return "Pirc [CATEGORY_ID=" + CATEGORY_ID + ", RADIUS=" + RADIUS
				+ ", deviceID=" + deviceID + ", status=" + status + ", x=" + x
				+ ", y=" + y + ", timestamp=" + timestamp + "]";
	}

	
}
