package db2.esper.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.espertech.esper.client.EPRuntime;

import db2.esper.event.models.LocationEvent;
import db2.esper.event.models.PirwEvent;

public class EventGenerator extends Thread {
	
	protected boolean verbose = false; //TRUE se vuoi debuggarmi
	
	protected EPRuntime cepRT;
	protected String[] filesPath;
	protected Scanner scannerLocationFile;
	protected Scanner scannerStateDumpFile;
	
	/**
	 * Constructor
	 * @param cepRT, EPRuntim, the configured Cep run time
	 * @param filesPath, String[], the path of the files with the log
	 */
	public EventGenerator(EPRuntime cepRT, String[] filesPath) {
		super();
		this.cepRT = cepRT;
		this.filesPath = filesPath;		
		
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
		scannerStateDumpFile = new Scanner(new File(filesPath[0]));
		scannerLocationFile = new Scanner(new File(filesPath[1]));
	}
	
	@Override
	public void run(){
		int second=0;
		
		LocationEvent event = null;
		Timestamp timestamp = null;
		Double positionX = null;
		Double positionY = null;
		
		while(scanFile.hasNextLine()){
			
			String line = scanFile.nextLine();
			
			if(line.contains("PIRW")) {
				if(verbose) System.out.println("PIRW");
				Scanner scanner = new Scanner(line);
				scanner.useDelimiter(",");
				
				// timestamp
				String token = scanner.next();
				Pattern pattern = Pattern.compile("[0-9]+(\\.[0-9][0-9]?)?");
				
				Matcher matcher = pattern.matcher(token);
				if (matcher.find()) {
					timestamp = new Timestamp((long) (Double.valueOf( matcher.group(0) ) * 1000));
					if(verbose) System.out.println(timestamp);
				}
				
				
				// deviceID
				token = scanner.next();
				pattern = Pattern.compile("[1-7]");
				matcher = pattern.matcher(token);
				int deviceID = 0;
				if (matcher.find()) {
					deviceID = Integer.valueOf( matcher.group(0) );
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
					status = false;
					if(verbose) System.out.println(status);
				} else if (token.contains("true")) {
					status = true;
					if(verbose) System.out.println(status);
				}
				
				event = new PirwEvent(timestamp, deviceID, status, 10, 10);
				if(verbose) System.out.println(event.toString());

				scanner.close();

			} else if(line.contains("PIRC")) {
				if(verbose) System.out.println("PIRC");
			} else if(line.contains("DOOR")) {
				if(verbose) System.out.println("DOOR");
			} else {
				
				Pattern pattern = Pattern.compile("[a-zA-Z\\_]+");
				Matcher matcher = pattern.matcher(line);
				
				if ( !matcher.find() ) {
					pattern = Pattern.compile( "([0-9]+) ([0-9]+\\.[0-9]+)? ([0-9]+\\.[0-9]+)?" );
					matcher = pattern.matcher(line);
					matcher.find();

					timestamp = new Timestamp((Long.valueOf( matcher.group(1) ).longValue()));
					positionX = new Double(matcher.group(2).toString());
					positionY = new Double(matcher.group(3).toString());
					
					event = new LocationEvent(positionX, positionY, timestamp);
					
					if(verbose) System.out.println(event.toString());
					
				}
				
			}
			
			//TODO temporizzazione degli eventi
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cepRT.sendEvent(event);

		}
		
		//TODO SPEGNI IL THREAD! PIRLA!
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
