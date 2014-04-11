package db2.esper.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import db2.esper.common.SensorParsedData;
import db2.esper.common.Wall;
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
		 * quarto match: group(1) CategoryName, group(2) value
		 * quinto match: group(1) Status, group(2) value
		 * Se si vuol essere zelanti si può fare un controllo che tutto combaci, 
		 * ma noi siamo per le prestazioni pure quindi non lo facciamo.
		 */
		int i = 0;
		while (matcher.find() && i <= 4) {
			
			if (i == 0) {
				if(verbose) System.out.println(matcher.group(2));
				//ho bisogno di avere tutti i tempi in millisecondi, quindi devo fare questo passaggio
				Double tempTimestamp = Double.valueOf(matcher.group(2)); 
				tempTimestamp = tempTimestamp * 1000;
				sensorParsedData.setTimestamp(tempTimestamp.longValue());
			} else if (i == 1) {
				if(verbose) System.out.println(matcher.group(2));
				sensorParsedData.setDeviceID(Integer.valueOf(matcher.group(2)).intValue());
			} else if (i == 2) {
				if(verbose) System.out.println(matcher.group(2));
				sensorParsedData.setCategoryName(String.valueOf(matcher.group(2)));
			} else if (i == 3) {
				if(verbose) System.out.println(matcher.group(2));
				sensorParsedData.setStatus(Boolean.valueOf(matcher.group(2)).booleanValue());
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
			// qui, so per certo che questa è una riga di location quindi estrapoto timestamp e coordinate
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
	
	/**
	 * Parser to load the sensor location coordinate directly from the given file
	 * @param path, String, the location with the name of the text file containing the coordinate
	 * @return Map object <String, double[]> the first is the description of the sensor, the second is
	 * an object containing the coordinates X,Y
	 * @throws FileNotFoundException, if the file is not found in the given path
	 */
	public static Map<String, double[]> sensorPositionFile(String path) throws FileNotFoundException {
		boolean verbose = EsperEngine.VERBOSE;
		Map<String, double[]> sensorsPosition = new HashMap<String, double[]>();
		String line = null;	//rappresenta l'attuale riga che sto leggendo dal file
		Matcher match = null;
		/*
		 * Estrae nell'ordine:
		 * group(1): device Name
		 * group(2): coordinata x
		 * group(3): coordinata y
		 */
		Pattern pattern = Pattern.compile("\\'(.+)\\'\\t([0-9]+\\.[0-9]+)?\\t([0-9]+\\.[0-9]+)?");
		Scanner scanner = new Scanner( new File( path ) );	// apro il file
		
		do {
			line = scanner.nextLine();	// estrae la prossima linea
			match = pattern.matcher(line);	// fa il match dell'espressione regolare
			double[] coordinates = new double[2];
			
			if ( match.find() ) {
				coordinates[0] = new Double(match.group(2).toString());
				coordinates[1] = new Double(match.group(3).toString());
				
				if(verbose) System.out.println(coordinates[0]);
				
				sensorsPosition.put(match.group(1), coordinates);
			}
		} while(scanner.hasNext());
		
		return sensorsPosition;
	}
	
	
	public static ArrayList<Wall> wallsPositionFile(String path) throws FileNotFoundException {
		boolean verbose = EsperEngine.VERBOSE;
		ArrayList<Wall> walls = new ArrayList<Wall>();
		String line = null;
		//il file presenta numeri separati da spazi questo il perché del \s+
		Pattern pattern = Pattern.compile(
				"([0-9]+\\.[0-9]+)\\s+([0-9]+\\.[0-9]+)\\s+([0-9]+\\.[0-9]+)\\s+([0-9]+\\.[0-9]+)"
				);
		Scanner scanner = new Scanner(new File( path ));
		Matcher match = null;
		
		do {
			line = scanner.nextLine();
			match = pattern.matcher(line);
			double[] coordinates = new double[2];
			
			walls.add(new Wall(new Double(match.group(1)), 
					new Double(match.group(2)), 
					new Double(match.group(3)), 
					new Double(match.group(4)))
			);
		} while (scanner.hasNext());
		
		return walls;
		
	}
}
