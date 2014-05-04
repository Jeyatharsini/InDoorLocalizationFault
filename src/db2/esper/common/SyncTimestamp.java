package db2.esper.common;

import java.util.Observable;

public class SyncTimestamp extends Observable {

	private long timestamp;
	
	public SyncTimestamp() {
		timestamp = 0;
	}
	
	public synchronized long getTimestamp() {
		return timestamp;
	}
	
	public synchronized void setTimestamp(long t) {
		//TODO aggiornami solo se il timestamp nuovo è minore di quello attuale
		this.timestamp = t;
		notifyObservers();
	}
}
