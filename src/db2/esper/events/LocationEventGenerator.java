package db2.esper.events;

import com.espertech.esper.client.EPRuntime;

import db2.esper.common.SyncTimestamp;
import db2.esper.event.models.LocationEvent;
import db2.esper.util.Parse;

public class LocationEventGenerator extends EventGenerator {

	public LocationEventGenerator(EPRuntime cepRT, String filePath, SyncTimestamp syncTimestamp) {
		super(cepRT, filePath, syncTimestamp);
	}
	
	@Override
	protected void generateEvent(String line) {
		
		LocationEvent locationEvent = Parse.locationLine(line);
		
		if (locationEvent != null) {
			
			//qui il thread va a nanna per il tempo necessario a essere realtime
			try {
				Thread.sleep(getSleepTime(locationEvent.getTimestamp()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			cepRT.sendEvent(locationEvent);

		}
	}
}
