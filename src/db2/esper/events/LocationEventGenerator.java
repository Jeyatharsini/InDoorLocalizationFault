package db2.esper.events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.espertech.esper.client.EPRuntime;

import db2.esper.event.models.LocationEvent;

public class LocationEventGenerator extends EventGenerator {

	LocationEvent event;
	
	public LocationEventGenerator(EPRuntime cepRT, String filePath) {
		super(cepRT, filePath);
	}
	
	@Override
	protected void generateEvent(String line) {
		parseLine(line);
		
		if (event != null) {
			
			cepRT.sendEvent(event);

			//qui il thread va a nanna per il tempo necessario a essere realtime
			try {
				Thread.sleep(getSleepTime(event.getTimestamp()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void parseLine(String line) {
		this.event = null;		
		
		Matcher matcher = Pattern.compile("[a-zA-Z\\_]+").matcher(line);
		
		if ( !matcher.find() ) {
			matcher = Pattern.compile( "([0-9]+) ([0-9]+\\.[0-9]+)? ([0-9]+\\.[0-9]+)?" ).matcher(line);
			matcher.find();

			long timestamp = Long.valueOf(matcher.group(1)).longValue();
			double positionX = new Double(matcher.group(2).toString());
			double positionY = new Double(matcher.group(3).toString());
			
			event = new LocationEvent(positionX, positionY, timestamp);
			
			if(verbose) System.out.println(event.toString());
		}

	}

}
