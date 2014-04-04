package db2.esper.util;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import db2.esper.engine.EsperEngine;
import db2.esper.event.models.LocationEvent;


public class Parse {
	
	/**
	 * Parser of the actual line of the txt file, it take the line and matches it
	 * against a RegEx to get the value of TimeStamp, DeviceID e Status.
	 * @param line String, the actual string
	 */
	public static SensorParsedData sensorStatusLine(String line) {
		
		boolean verbose = EsperEngine.VERBOSE;
		SensorParsedData sensorParsedData = new SensorParsedData();
		
		Pattern pattern = Pattern.compile(
				"(TimeStamp|DeviceID|CategoryName|ZoneName|Status)=(([\\.0-9]+)|(PIRC|PIRW|DOOR|true|false))"
				);

		Matcher matcher = pattern.matcher(line);
		/* dato il file d'ingresso sempre in questo formato, con questa RegEx ho sempre:
		 * primo match: group(1) timestamp, group(2) value
		 * secondo match: group(1) DeviceID, group(2) value
		 * terzo match: group(1) ZoneName, group(2) value
		 * quarto match: group(1) CategoryName, group(2) value
		 * quinto match: group(1) Status, group(2) value
		 * Se si vuol essere zelanti si pu˜ fare un controllo che tutto combaci, 
		 * ma noi siamo per le prestazioni pure quindi non lo facciamo.
		 */
		int i = 0;
		while (matcher.find() && i <= 4) {
			
			if (i == 0) {
				if(verbose) System.out.println(matcher.group(2));
				//ho bisogno di avere tutti i tempi in millisecondi, quindi devo fare questo passaggio
				Double tempTimestamp = Double.valueOf(matcher.group(2)); 
				tempTimestamp = tempTimestamp * 1000;
				sensorParsedData.timestamp= tempTimestamp.longValue();
			} else if (i == 1) {
				if(verbose) System.out.println(matcher.group(2));
				sensorParsedData.deviceID = Integer.valueOf(matcher.group(2)).intValue();
			} else if (i == 2) {
				if(verbose) System.out.println(matcher.group(2));
				sensorParsedData.zoneName = String.valueOf(matcher.group(2));
			} else if (i == 3) {
				if(verbose) System.out.println(matcher.group(2));
				sensorParsedData.categoryName = String.valueOf(matcher.group(2));
			} else if (i == 4) {
				if(verbose) System.out.println(matcher.group(2));
				sensorParsedData.status = Boolean.valueOf(matcher.group(2)).booleanValue();
			}
			
			i++;
		}
		
		return sensorParsedData;
	}
	
	/**
	 * Parser to read the location line in the LOC[0-9]+.log file
	 * @param line, String the actual line of the file
	 * @return LoccationEvent with the specific position
	 */
	public static LocationEvent locationLine (String line) {
		
		boolean verbose = EsperEngine.VERBOSE;
		LocationEvent locationEvent = null;		
		
		// qui controllo che la riga sia di mio interesse e non contenga le lettere
		Matcher matcher = Pattern.compile("[a-zA-Z\\_]+").matcher(line);
		
		if ( !matcher.find() ) {
			// qui, so per certo che questa  una riga di location quindi estrapoto timestamp e coordinate
			matcher = Pattern.compile( "([0-9]+) ([0-9]+\\.[0-9]+)? ([0-9]+\\.[0-9]+)?" ).matcher(line);
			matcher.find();

			long timestamp = Long.valueOf(matcher.group(1)).longValue();
			double positionX = new Double(matcher.group(2).toString());
			double positionY = new Double(matcher.group(3).toString());
			
			locationEvent = new LocationEvent(positionX, positionY, timestamp);
			
			if(verbose) System.out.println(locationEvent.toString());
		}

		return locationEvent;
		
	}

	public static HashMap<String, double[]> sensorPositionFile(String path) {
		return null;
		
	}
}
