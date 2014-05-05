package db2.esper.common;

public class LocationParsedData {

	private long timestamp = 0;
	private double x = 0;
	private double y = 0;
	
	public LocationParsedData(long timestamp, double x, double y) {
		super();
		this.timestamp = timestamp;
		this.x = x;
		this.y = y;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public String toString() {
		return "LocationParsedData [timestamp= " + timestamp + ", x= " + x
				+ ", y= " + y + "]";
	}
	
	
	
}
