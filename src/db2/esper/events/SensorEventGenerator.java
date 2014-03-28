package db2.esper.events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.espertech.esper.client.EPRuntime;

import db2.esper.event.models.DwcEvent;
import db2.esper.event.models.LocationEvent;
import db2.esper.event.models.PircEvent;
import db2.esper.event.models.PirwEvent;

public class SensorEventGenerator extends EventGenerator {
	
	private float timestamp = 0;
	private int deviceID = 0;
	private boolean status = false;
	
	public SensorEventGenerator(EPRuntime cepRT, String filePath) {
		super(cepRT, filePath);
	}
	
	
	@Override
	protected LocationEvent parseLine(String line) {
		//fa il matching della linea con l'espressione regolare
		matchLine(line);

		if(line.contains("PIRW")) {
			if(verbose) System.out.println("PIRW");
			event = new PirwEvent(timestamp, deviceID, status, 10, 10);
			if(verbose) System.out.println(event.toString());
		} else if(line.contains("PIRC")) {
			if(verbose) System.out.println("PIRC");
			event = new PircEvent(timestamp, deviceID, status, 10, 10);
			if(verbose) System.out.println(event.toString());
		} else if(line.contains("DOOR")) {
			if(verbose) System.out.println("DOOR");
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
		Pattern pattern = Pattern.compile(
				"(TimeStamp|DeviceID|Status)=(([\\.0-9]+)|(PIRC|PIRW|DOOR|true|false))"
				);
		
		Matcher matcher = pattern.matcher(line);
		
		/* dato il file d'ingresso sempre in questo formato, con questa RegEx ho sempre:
		 * group(1): [1] timestamp, [2] value
		 * group(2): [1] DeviceID, [2] value
		 * group(3): [1] CategoryName, [2] value
		 * group(4): [1] Status, [2] value
		 * Se si vuol essere zelanti si pu˜ fare un controllo che tutto combaci, 
		 * ma noi siamo per le prestazioni pure quindi non lo facciamo.
		 */
		int i = 0;
		verbose = true;
		while (matcher.find()) {
			if(verbose) System.out.println(matcher.group(2));
			if (i == 0)
				this.timestamp = Float.valueOf(matcher.group(2)).floatValue();
			else if (i == 1)
				this.deviceID = Integer.valueOf(matcher.group(2)).intValue();
			else if (i == 2)
				this.status = Boolean.valueOf(matcher.group(2)).booleanValue();
			
			i++;
		}
	}

}
