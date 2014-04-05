package db2.esper.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.espertech.esper.client.EPRuntime;

import db2.esper.engine.EsperEngine;

public abstract class EventGenerator extends Thread {
	
	protected boolean verbose = EsperEngine.VERBOSE;
	
	protected EPRuntime cepRT;
	protected String filePath;
	protected Scanner scanner;
	protected long sleepTime = 0;
	protected long previousTimestamp = 0;
	
	/**
	 * Constructor
	 * @param cepRT, EPRuntim, the configured Cep run time
	 * @param filesPath, String[], the path of the files with the log
	 */
	public EventGenerator(EPRuntime cepRT, String filePath) {
		super();
		this.cepRT = cepRT;
		this.filePath = filePath;
		
		try {
			setParsers();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {	
		while (this.scanner.hasNext()) {
			generateEvent(this.scanner.nextLine());
		}		
		
		this.scanner.close();
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
	
	protected abstract void generateEvent(String line);
	
}
