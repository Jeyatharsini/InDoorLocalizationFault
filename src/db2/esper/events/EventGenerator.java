package db2.esper.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.espertech.esper.client.EPRuntime;

import db2.esper.event.models.LocationEvent;

public abstract class EventGenerator extends Thread {
	
	protected boolean verbose = false; //TRUE se vuoi debuggarmi
	
	protected EPRuntime cepRT;
	protected String filePath;
	protected Scanner scanner;
	protected LocationEvent event;
	
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
	
	/**
	 * Initialize two scanners, one for each file to scan
	 * @throws FileNotFoundException, if one files is not found
	 */
	private void setParsers() throws FileNotFoundException{
		scanner = new Scanner(new File(filePath));
	}

	@Override
	public void run() {
		
		while (scanner.hasNext()) {
			event = parseLine(scanner.nextLine());

			//TODO temporizzazione degli eventi
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (event != null)
				cepRT.sendEvent(event);
		
		}
		
		//scanner.close();
		//TODO in qualche modo questo thread va fermato...
	}
	
	public float waitTime(LocationEvent firstEvent, LocationEvent secondEvent) {
		float timeDifference = 1000;
		//TODO calcolami in maniera efficente!
		return timeDifference;
	}
	
	protected LocationEvent parseLine(String line) {
		//TODO override me!
		return null;
	}
	
}
