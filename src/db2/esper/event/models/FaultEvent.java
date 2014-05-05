package db2.esper.event.models;

public class FaultEvent {
	
	private int sensorID;
	private boolean rise;
	protected double x;
	protected double y;
	protected long timestamp;
	
	public FaultEvent(double x, double y, long timestamp, int sensorID,	boolean rise) {
		this.x = x;
		this.y = y;
		this.timestamp = timestamp;
		this.sensorID = sensorID;
		this.rise = rise;
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

	public int getSensorID() {
		return sensorID;
	}

	public boolean isRise() {
		return rise;
	}

	@Override
	public String toString() {
		return "FaultEvent [sensorID=" + sensorID + ", rise=" + rise + ", Position x="
				+ x + ", Position y=" + y + ", timestamp=" + timestamp + "]";
	}
	
	

}
