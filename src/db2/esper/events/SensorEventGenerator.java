package db2.esper.events;

import java.sql.Timestamp;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.espertech.esper.client.EPRuntime;

import db2.esper.event.models.DwcEvent;
import db2.esper.event.models.LocationEvent;
import db2.esper.event.models.PircEvent;
import db2.esper.event.models.PirwEvent;

public class SensorEventGenerator extends EventGenerator {
	
	private Timestamp timestamp = null;
	private int deviceID = 0;
	private boolean status = false;
	
	public SensorEventGenerator(EPRuntime cepRT, String filePath) {
		super(cepRT, filePath);
	}
	
	
	@Override
	protected LocationEvent parseLine(String line) {
		if(line.contains("PIRW")) {
			if(verbose) System.out.println("PIRW");
			
			matchLine(line);
			
			event = new PirwEvent(timestamp, deviceID, status, 10, 10);
			if(verbose) System.out.println(event.toString());

		} else if(line.contains("PIRC")) {
			if(verbose) System.out.println("PIRC");
			
			matchLine(line);			
			
			event = new PircEvent(timestamp, deviceID, status, 10, 10);
			if(verbose) System.out.println(event.toString());

		} else if(line.contains("DOOR")) {
			if(verbose) System.out.println("DOOR");
			
			matchLine(line);
	
			event = new DwcEvent(timestamp, deviceID, status, 10, 10);
			if(verbose) System.out.println(event.toString());

		}
		
		return event;
	}
	
	/**
	 * 
	 * @param line
	 */
	private void matchLine(String line) {
		Scanner scanner = new Scanner(line);
		scanner.useDelimiter(",");
		
		// timestamp
		String token = scanner.next();
		Pattern pattern = Pattern.compile("[0-9]+(\\.[0-9][0-9]?)?");
		
		Matcher matcher = pattern.matcher(token);
		Timestamp timestamp = null;
		if (matcher.find()) {
			this.timestamp = new Timestamp((long) (Double.valueOf( matcher.group(0) ) * 1000));
			if(verbose) System.out.println(timestamp);
		}
		
		// deviceID
		token = scanner.next();
		pattern = Pattern.compile("[1-7]");
		matcher = pattern.matcher(token);
		int deviceID = 0;
		if (matcher.find()) {
			this.deviceID = Integer.valueOf( matcher.group(0) );
			if(verbose) System.out.println(deviceID);
		}
			
		token = scanner.next();
		token = scanner.next();
		token = scanner.next();
		token = scanner.next();

		//status
		token = scanner.next();
		Boolean status = null;
		if (token.contains("false")) {
			this.status = false;
			if(verbose) System.out.println(status);
		} else if (token.contains("true")) {
			this.status = true;
			if(verbose) System.out.println(status);
		}
		
		scanner.close();
		
	}
	
}
