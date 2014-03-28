package db2.esper.events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.espertech.esper.client.EPRuntime;

import db2.esper.event.models.DwcEvent;
import db2.esper.event.models.PircEvent;
import db2.esper.event.models.PirwEvent;

public class SensorEventGenerator extends EventGenerator {
	
	private long timestamp;
	private int deviceID;
	private boolean status;
	
	public SensorEventGenerator(EPRuntime cepRT, String filePath) {
		super(cepRT, filePath);
	}
	
	@Override
	protected void generateEvent(String line) {
		
		long actualTimestamp = 0;
		
		this.timestamp = 0;
		this.deviceID = 0;
		this.status = false;
		
		//fa il matching della linea con l'espressione regolare
		parseLine(line);
		
		//a seconda del tipo di evento che sto parsando creo un oggetto evento diverso
		if(line.contains("PIRW")) {
			PirwEvent event = new PirwEvent(timestamp, deviceID, status, 10, 10);
			actualTimestamp = event.getTimestamp();
			cepRT.sendEvent(event);
			
			if(verbose) System.out.println(event.toString());
			
		} else if(line.contains("PIRC")) {
			PircEvent event = new PircEvent(timestamp, deviceID, status, 10, 10);
			actualTimestamp = event.getTimestamp();
			cepRT.sendEvent(event);
			
			if(verbose) System.out.println(event.toString());
			
		} else if(line.contains("DOOR")) {
			DwcEvent event = new DwcEvent(timestamp, deviceID, status, 10, 10);
			actualTimestamp = event.getTimestamp();
			cepRT.sendEvent(event);
			
			if(verbose) System.out.println(event.toString());
		}
		
		if(actualTimestamp != 0) {
			//qui il thread va a nanna per il tempo necessario a essere realtime
			try {
				Thread.sleep(getSleepTime(actualTimestamp));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param line
	 */
	protected void parseLine(String line) {
		Pattern pattern = Pattern.compile(
				"(TimeStamp|DeviceID|Status)=(([\\.0-9]+)|(PIRC|PIRW|DOOR|true|false))"
				);

		Matcher matcher = pattern.matcher(line);
		/* dato il file d'ingresso sempre in questo formato, con questa RegEx ho sempre:
		 * primo match: group(1) timestamp, group(2) value
		 * secondo match: group(1) DeviceID, group(2) value
		 * terzo match: group(1) CategoryName, group(2) value
		 * quarto match: group(1) Status, group(2) value
		 * Se si vuol essere zelanti si pu˜ fare un controllo che tutto combaci, 
		 * ma noi siamo per le prestazioni pure quindi non lo facciamo.
		 */
		int i = 0;
		while (matcher.find() && i < 3) {
			
			if (i == 0) {
				if(verbose) System.out.println(matcher.group(2));
				//ho bisogno di avere tutti i tempi in millisecondi, quindi devo fare questo passaggio
				Double tempTimestamp = Double.valueOf(matcher.group(2)); 
				tempTimestamp = tempTimestamp * 1000;
				this.timestamp = tempTimestamp.longValue();
			} else if (i == 1) {
				if(verbose) System.out.println(matcher.group(2));
				this.deviceID = Integer.valueOf(matcher.group(2)).intValue();
			} else if (i == 2) {
				if(verbose) System.out.println(matcher.group(2));
				this.status = Boolean.valueOf(matcher.group(2)).booleanValue();
			}
			
			i++;
		}
	}
}
