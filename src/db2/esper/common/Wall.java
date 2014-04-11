package db2.esper.common;

public class Wall {
	
	private double startX;
	private double startY;
	private double endX;
	private double endY;
	
	public Wall(double string, double string2, double string3, double string4) {
		super();
		this.startX = string;
		this.startY = string2;
		this.endX = string3;
		this.endY = string4;
	}

	public double getStartX() {
		return this.startX;
	}

	public double getStartY() {
		return this.startY;
	}

	public double getEndX() {
		return this.endX;
	}

	public double getEndY() {
		return this.endY;
	}

	@Override
	public String toString() {
		return "Wall [startX=" + startX + ", startY=" + startY + ", endX="
				+ endX + ", endY=" + endY + "]";
	}
	
	
}
