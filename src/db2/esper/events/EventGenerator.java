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
//	protected boolean verbose = false;
	
	protected EPRuntime cepRT;
	protected String filePath;
	protected Scanner scanner;
	protected long sleepTime = 0;
	protected long previousTimestamp = 0;
	protected SyncTimestamp syncTimestamp;
	protected long syncDelay = 0;
	
	
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
		
		try {
			setParsers();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void run() {
		
		try {
			Thread.sleep(syncDelay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("EventGenerator: " + getClass() + " -> STARTED with delay: " + syncDelay);
		
		while (this.scanner.hasNext()) {
			generateEvent(this.scanner.nextLine());
		}		
		
		this.scanner.close();
		
		System.out.println("EventGenerator: " + getClass() + " -> END OF DATA");

		//TODO in qualche modo questo thread va fermato quando ho finito gli eventi...
	}
	
	public void sync() {
		try {
			this.syncTimestamp.setTimestamp(getInitialTimeStamp());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
	
	/**
	 * Each time a thread being created update the syncTimestamp object each thread needs to
	 * recompute the starting delay, if the new syncTimestamp value is less than thread one
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		long initialTimeStamp = 0;

		//DEBUG
		if (verbose) {
			System.out.println("Update method called for: " + getClass());
		}
		
		try {
			initialTimeStamp = getInitialTimeStamp();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// se il timestamp in syncTimestamp è minore dello starting timestamp per questo thread
		// vuol dire che questo thread dovrà partire dopo l'altro, ritardato di syncDelay millisec.
		if (syncTimestamp.getTimestamp() < initialTimeStamp ) {
			this.syncDelay = initialTimeStamp - syncTimestamp.getTimestamp();
		}
	}
	
	/**
	 * Generates the event object that is fired on the stram
	 * @param line, String, the first line in the data file
	 */
	protected abstract void generateEvent(String line);

	/**
	 * Read the first line of the file to get the timestamp
	 * @return timestamp, long, first event timestamp
	 * @throws FileNotFoundException
	 */
	protected abstract long getInitialTimeStamp() throws FileNotFoundException;
	
	
}
