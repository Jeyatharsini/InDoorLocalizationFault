package db2.esper.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import com.espertech.esper.client.EPRuntime;

import db2.esper.common.SyncTimestamp;
import db2.esper.engine.EsperEngine;

public abstract class EventGenerator extends Thread implements Observer {
	
	protected boolean verbose = EsperEngine.VERBOSE;
	
	protected EPRuntime cepRT;
	protected String filePath;
	protected Scanner scanner;
	protected long sleepTime = 0;
	protected long previousTimestamp = 0;
	protected SyncTimestamp syncTimestamp;
	protected long syncDeelay = 0;
	
	
	/**
	 * Constructor
	 * @param cepRT, EPRuntim, the configured Cep run time
	 * @param filePath, String[], the path of the files with the log
	 */
	public EventGenerator(EPRuntime cepRT, String filePath, SyncTimestamp syncTimestamp) {
		super();
		this.cepRT = cepRT;
		this.filePath = filePath;
		
		this.syncTimestamp = syncTimestamp;
		this.syncTimestamp.addObserver(this);
		
		this.syncTimestamp.setTimestamp(getInitialTimeStamp());
		
		try {
			setParsers();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		//TODO qui il thread va in sleep per startingDeelay milliseconds
		
		System.out.println("EventGenerator: " + getClass() + " -> STARTED");
		
		while (this.scanner.hasNext()) {
			generateEvent(this.scanner.nextLine());
		}		
		
		this.scanner.close();
		
		System.out.println("EventGenerator: " + getClass() + " -> END OF DATA");

		//TODO in qualche modo questo thread va fermato quando ho finito gli eventi...
	}
	
	/**
	 * Initialize the scanner
	 * @throws FileNotFoundException, if one files is not found
	 */
	private void setParsers() throws FileNotFoundException{
		this.scanner = new Scanner(new File(filePath));
	}
	
	/**
	 * Calculate the sleep time between two different events
	 * @param actualTimestamp, long, event timestamp
	 * @return long, sleep time in millisec
	 */
	protected long getSleepTime(long actualTimestamp) {
		if (previousTimestamp == 0) {
			previousTimestamp = actualTimestamp;
		} 
		
		this.sleepTime = actualTimestamp - previousTimestamp; 
		previousTimestamp = actualTimestamp;
		
		return this.sleepTime;
	}
	
	private long getInitialTimeStamp() {
		// TODO legge prima riga e prende il time stamp.
		return 0;
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO se il timestamp aggiunto è diverso da quello memorizzato, allora calcola starting deelay
		
	}
	
	protected abstract void generateEvent(String line);
	
}
