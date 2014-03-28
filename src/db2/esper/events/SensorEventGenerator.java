package db2.esper.events;

import java.sql.Timestamp;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.espertech.esper.client.EPRuntime;

import db2.esper.event.models.LocationEvent;
import db2.esper.event.models.PirwEvent;

public class SensorEventGenerator extends EventGenerator {
		
	public SensorEventGenerator(EPRuntime cepRT, String filePath) {
		super(cepRT, filePath);
	}
	
	@Override
	public void run(){
		int second=0;
		
		LocationEvent event = null;
		Timestamp timestamp = null;
		Double positionX = null;
		Double positionY = null;
		
		while(scanner.hasNextLine()){
			
			String line = scanner.nextLine();
			
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
