package db2.esper.events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.espertech.esper.client.EPRuntime;

import db2.esper.event.models.LocationEvent;

public class LocationEventGenerator extends EventGenerator {

	public LocationEventGenerator(EPRuntime cepRT, String filePath) {
		super(cepRT, filePath);
	}
	
	@Override
	protected LocationEvent parseLine(String line) {
		Pattern pattern = Pattern.compile("[a-zA-Z\\_]+");
		Matcher matcher = pattern.matcher(line);
		
		if ( !matcher.find() ) {
			pattern = Pattern.compile( "([0-9]+) ([0-9]+\\.[0-9]+)? ([0-9]+\\.[0-9]+)?" );
			matcher = pattern.matcher(line);
			matcher.find();

			float timestamp = Float.valueOf(matcher.group(1)).floatValue();
			double positionX = new Double(matcher.group(2).toString());
			double positionY = new Double(matcher.group(3).toString());
			
			event = new LocationEvent(positionX, positionY, timestamp);
			
			if(verbose) System.out.println(event.toString());
		}

		return event;
		
		//TODO dammi un'occhiata ritorno degli eventi nulli...
	}

}
