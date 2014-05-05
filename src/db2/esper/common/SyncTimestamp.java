package db2.esper.common;

import java.util.Observable;

import db2.esper.engine.EsperEngine;

public class SyncTimestamp extends Observable {

	private boolean verbose = EsperEngine.VERBOSE; //DEBUG
	
	private long timestamp;
	
	public SyncTimestamp() {
		timestamp = 0;
	}
	
	public synchronized long getTimestamp() {
		return timestamp;
	}
	
	public synchronized void setTimestamp(long t) {
		//TODO aggiornami solo se il timestamp nuovo è minore di quello attuale
		if (verbose) {
			System.out.println("Nuovo Timestamp: " + t + " vecchio timestamp: " + this.timestamp + " Osservatori: " + this.countObservers());
		}
		
		if (this.timestamp == 0) {
			this.timestamp = t;
		} else if (t < this.timestamp) {
			this.timestamp = t;
			this.setChanged();
		}

		notifyObservers();
	}
}
