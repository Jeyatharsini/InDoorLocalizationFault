package db2.esper.events;

import com.espertech.esper.client.EPRuntime;

import db2.esper.event.models.DwcEvent;
import db2.esper.event.models.PircEvent;
import db2.esper.event.models.PirwEvent;
import db2.esper.util.Parse;
import db2.esper.util.SensorParsedData;

public class SensorEventGenerator extends EventGenerator {
	
	/*
	 * TODO Cosa manca qui?
	 * Una maniera efficace per gestire le posizioni dei sensori in base alla loro
	 * deviceID, e il loro raggio in base al tipo di sensore.
	 * 
	 * Per il raggio un modo molto semplice visto che comunque  limitato, si pu˜ definire
	 * nella classe del sensore e siamo a posto.
	 * 
	 * Per le posizioni, se sono date in un file di testo, si pu˜ fare il parsing del file
	 * e creare una mappa con {ID sensore: posizioneX, posizioneY} da qui poi ogni volta
	 * che si crea un nuovo tipo di evento, fare il match tra gli ID e assegnare le posizioni. 
	 * 
	 * 
	 */
	
	//TODO qui mi deve arrivare anche il path del file con posizioni dei sensori
	public SensorEventGenerator(EPRuntime cepRT, String filePath) {
		super(cepRT, filePath);
	}
	
	/**
	 * This method generate the Event, different event objects are instantiated for different events
	 * @param line, String the actual line of the .txt file
	 */
	@Override
	protected void generateEvent(String line) {
		
		//fa il matching della linea con l'espressione regolare e mi ritorna tutti i dati
		SensorParsedData sensorParsedData = Parse.sensorStatusLine(line);
		
		//----> qui devi aggiungere le posizioni ai sensori Parse.qualcosa(sensorParseData, pathFile);
		
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
		
		//qui il thread va a nanna per il tempo necessario a essere realtime
		try {
			Thread.sleep(getSleepTime(sensorParsedData.getTimestamp())); //get the actual Timestamp
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
