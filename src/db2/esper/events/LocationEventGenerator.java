package db2.esper.events;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.espertech.esper.client.EPRuntime;

import db2.esper.event.models.LocationEvent;

public class LocationEventGenerator extends EventGenerator {

	public LocationEventGenerator(EPRuntime cepRT, String filePath) {
		super(cepRT, filePath);
	}


	
	public void parseLog() {
		
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
