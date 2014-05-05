package db2.esper.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.espertech.esper.client.EPRuntime;

import db2.esper.common.LocationParsedData;
import db2.esper.common.SyncTimestamp;
import db2.esper.event.models.LocationEvent;
import db2.esper.util.Parse;

public class LocationEventGenerator extends EventGenerator {

	public LocationEventGenerator(EPRuntime cepRT, String filePath, SyncTimestamp syncTimestamp) {
		super(cepRT, filePath, syncTimestamp);
	}
	
	@Override
	protected void generateEvent(String line) {
		
		LocationParsedData locationParsedData = Parse.locationLine(line);
		
		// palliativo al fatto che a volte ritorna dei valori nulli
		//TODO fai in modo che il parsing non ritorni mai valori nulli, probabilmente RegExp da sistemare
		if (locationParsedData != null) {
			//qui il thread va a nanna per il tempo necessario a essere realtime
			try {
				Thread.sleep(getSleepTime(locationParsedData.getTimestamp()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			cepRT.sendEvent(new LocationEvent(locationParsedData));

		}
	}

	@Override
	protected long getInitialTimeStamp() throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(filePath));		
		LocationParsedData locationParsedData = null;
		
		//TODO sistema parser ritorna valori null!!
		do {
			locationParsedData = Parse.locationLine(scanner.nextLine());
		} while (locationParsedData == null);
		
		scanner.close();
		
		//DEBUG
		if (verbose) {
			System.out.println("Initial timestamp location: " + locationParsedData.getTimestamp());
		}
				
		return locationParsedData.getTimestamp();
	}
}
