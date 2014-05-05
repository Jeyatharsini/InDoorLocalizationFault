package db2.esper.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.espertech.esper.client.EPRuntime;

import db2.esper.common.SensorParsedData;
import db2.esper.common.SyncTimestamp;
import db2.esper.engine.EsperEngine;
import db2.esper.event.models.DwcEvent;
import db2.esper.event.models.PircEvent;
import db2.esper.event.models.PirwEvent;
import db2.esper.util.Parse;

public class SensorEventGenerator extends EventGenerator {
		
	// questa Map mappa ciscun sensore tramite il suo deviceID alla sua posizione
	protected Map<String, double[]> sensorsPosition = new HashMap<String, double[]>();
	
	protected String sensorPositionFilePath = null;
	
	public SensorEventGenerator(EPRuntime cepRT, String sensorStateFilePath, String sensorPositionFilePath, SyncTimestamp syncTimestamp) throws FileNotFoundException {
		super(cepRT, sensorStateFilePath, syncTimestamp);
		
		//carico la HashMap con le posizioni dei sensori, durante tutta la simulazione non cambieranno 
		sensorsPosition = Parse.sensorPositionFile(sensorPositionFilePath);
	}
	
	/**
	 * This method generate the Event, different event objects are instantiated for different events
	 * @param line, String the actual line of the .txt file
	 */
	@Override
	protected void generateEvent(String line) {
		
		//fa il matching della linea con l'espressione regolare e mi ritorna tutti i dati
		SensorParsedData sensorParsedData = Parse.sensorStatusLine(line);
		
		//aggiunta, ai dati presenti nel file degli stati dei sensori, delle posizioni dei sensori
		sensorParsedData = addSensorPosition(sensorParsedData);
		
		if(verbose) System.out.println(sensorParsedData.toString());
		
		//qui il thread va a nanna per il tempo necessario a essere realtime
		try {
			Thread.sleep(getSleepTime(sensorParsedData.getTimestamp())); //get the actual Timestamp
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//a seconda del tipo di evento che ho parsando creo un oggetto evento diverso
		if(sensorParsedData.getCategoryName().equalsIgnoreCase("PIRW")) {
			PirwEvent pirwEvent = new PirwEvent(sensorParsedData);
			cepRT.sendEvent(pirwEvent);
			
			if(verbose) System.out.println(pirwEvent.toString());
			
		} else if(sensorParsedData.getCategoryName().equalsIgnoreCase("PIRC")) {
			PircEvent pircEvent = new PircEvent(sensorParsedData);
			cepRT.sendEvent(pircEvent);
			
			if(verbose) System.out.println(pircEvent.toString());
			
		} else if(sensorParsedData.getCategoryName().equalsIgnoreCase("DOOR")) {
			DwcEvent dwcEvent = new DwcEvent(sensorParsedData);
			cepRT.sendEvent(dwcEvent);
			
			if(verbose) System.out.println(dwcEvent.toString());
		}
		
	}
	
	/**
	 * This method is only to load (in the object obtained from the parse of the sensor state file)
	 * the sensor position parsed from a specific file
	 * @param sensorParsedData
	 * @return 
	 */
	private SensorParsedData addSensorPosition(SensorParsedData sensorParsedData) {
		// si suppone che tutti i nomi dei sensori siano mappati nella Map dichiarata nella classe EsperEngine
		//TODO se vuoi generalizzarmi aggiungi un controllo, ma per questi file non c'� bisogno.
		
		double[] thisSensorPosition = sensorsPosition.get( 
				EsperEngine.sensorIdToName[ sensorParsedData.getDeviceID() ] );
		
		sensorParsedData.setX(thisSensorPosition[0]);
		sensorParsedData.setY(thisSensorPosition[1]);
		
		return sensorParsedData;
	}

	@Override
	protected long getInitialTimeStamp() throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(filePath));		
		SensorParsedData sensorParsedData = Parse.sensorStatusLine(scanner.nextLine());
		scanner.close();
		
		//DEBUG
		if (verbose) {
			System.out.println("Initial timestamp sensor: " + sensorParsedData.getTimestamp());
		}
		
		return sensorParsedData.getTimestamp();
	}


}
