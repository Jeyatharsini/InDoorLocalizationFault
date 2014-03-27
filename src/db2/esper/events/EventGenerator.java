package db2.esper.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.espertech.esper.client.EPRuntime;

import db2.esper.event.models.LocationEvent;
import db2.esper.event.models.PirwEvent;
import db2.esper.event.models.SensorEvent;

/* Rilevamenti:
 * A: senza fault
 * B: senza fault
 * C: senza fault
 * D: senza device di localizzazione
 * E: con un pir non attivo
 * 
 */

public class EventGenerator extends Thread {
	
	protected int periodMS=1000;
	protected boolean verbose=true;
	
	protected EPRuntime cepRT;
	protected String filePath;
	protected int[] defaultSensorState={};
	protected Scanner scanFile; 
	protected String lastLine="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
	
	public EventGenerator(EPRuntime cepRT, String[] args) {
		super();
		this.cepRT = cepRT;
		this.filePath = args[0];		
		
		try {
			setParser();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void setParser() throws FileNotFoundException{
		scanFile= new Scanner(new File(filePath));
	}
	
	@Override
	public void run(){
		int second=0;
		LocationEvent event = null;
		
		while(scanFile.hasNextLine()){
			
			String line = scanFile.nextLine();
			
			if(line.contains("PIRW")) {
				System.out.println("PIRW");
				Scanner scanner = new Scanner(line);
				scanner.useDelimiter(",");
				
				// timestamp
				String token = scanner.next();
				Pattern pattern = Pattern.compile("[0-9]+(\\.[0-9][0-9]?)?");
				
				Matcher matcher = pattern.matcher(token);
				Timestamp timestamp = null;
				if (matcher.find()) {
					timestamp = new Timestamp((long) (Double.valueOf( matcher.group(0) ) * 1000));
					System.out.println(timestamp);
				}
				
				// deviceID
				token = scanner.next();
				pattern = Pattern.compile("[1-7]");
				matcher = pattern.matcher(token);
				int deviceID = 0;
				if (matcher.find()) {
					deviceID = Integer.valueOf( matcher.group(0) );
					System.out.println(deviceID);
				}
				
				token = scanner.next();
				token = scanner.next();
				token = scanner.next();
				token = scanner.next();

				//status
				token = scanner.next();
				Boolean status = null;
				if (token.contains("false")) {
					status = false;
					System.out.println(status);
				} else if (token.contains("true")) {
					status = true;
					System.out.println(status);
				}
				
				event = new PirwEvent(timestamp, deviceID, status, 10, 10);
				System.out.println(event.toString());
				
			} else if(line.contains("PIRC")) {
				System.out.println("PIRC");
			} else if(line.contains("DOOR")) {
				System.out.println("DOOR");
			}
			

		}
		
		cepRT.sendEvent(event);
	};
	

	
	/*
	 * Generiamo le classi Event -> tuple
	 * sistemiamo il parser per popolare le nostre tuple
	 * buttiamo tutto sugli stream
	 * 		- stream uno per sensore o uno per categoria di sensore?
	 * 		- Tutti i sensori usano lo stesso evento SensorEvent
	 */
//	private void genEvent(String inLine, int second){
//		Scanner scan= new Scanner(inLine);
//		scan.useDelimiter(" ");
//		Scanner lastScan= new Scanner(lastLine);
//		scan.useDelimiter(" ");
//		byte sensId=0;
//		while(scan.hasNext()){
//			sensId++;
//			String newTok=scan.next();
//			String oldTok=lastScan.next();
//			if(!newTok.contentEquals(oldTok)){
//					if (verbose)
//						System.out.println(ev.toString());
//					cepRT.sendEvent(ev);
//				} else {
//					SensorEvent ev = new SensorEvent(sensId,new Byte(newTok),second);
//					if (verbose)
//						System.out.println(ev.toString());
//					cepRT.sendEvent(ev);
//				}
//			}
//		}
//		scan.close();
//		lastScan.close();
//	};
	
}
