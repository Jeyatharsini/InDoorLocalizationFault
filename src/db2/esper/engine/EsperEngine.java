package db2.esper.engine;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

import db2.esper.event.models.DwcEvent;
import db2.esper.event.models.LocationEvent;
import db2.esper.event.models.PircEvent;
import db2.esper.event.models.PirwEvent;
import db2.esper.events.EventGenerator;

/* Rilevamenti:
 * A: senza fault
 * B: senza fault
 * C: senza fault
 * D: senza device di localizzazione
 * E: con un pir non attivo
 */

public class EsperEngine {
	private final static String SENSOR_STATE_DUMP = "stateDump.txt";
	
	public static void main(String[] args) throws InterruptedException {		
		//se in args non è stata passato nessun percorso valido, carica i file di default
		String path = null;
		if(args.length == 0) 
			path = "data/A"; //cambiami per caricare gli altri test case
		else
			path = args[0]; //Zanero NON sarebbe orgoglioso di te...
		
		EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEP", getConfiguration());
		EPRuntime cepRT = cep.getEPRuntime();
		
		Listener mylistener = new Listener();
		String query = null;
		
		//GENERAZIONE DEGLI STREAM PER CATEGORIA DI SENSORE
		query = "INSERT INTO pirwEPL SELECT * FROM PirwEvent ";
		EPStatement pirwEPL= cep.getEPAdministrator().createEPL(query);
		
		query = "INSERT INTO pircEPL SELECT * FROM PircEvent ";
		EPStatement pircEPL = cep.getEPAdministrator().createEPL(query);
		
		query = "INSERT into dwcEPL SELECT * FROM DwcEvent ";
		EPStatement dwcEPL = cep.getEPAdministrator().createEPL(query);
		
		query = "INSERT into locationEPL SELECT * FROM LocationEvent ";
		EPStatement locationEPL = cep.getEPAdministrator().createEPL(query);
		
		//QUERY PER TROVARE LE VARIE ANOMALIE
		query = "SELECT * FROM locationEPL";
		EPStatement onlyTrue = cep.getEPAdministrator().createEPL(query);
		onlyTrue.addListener(mylistener);	//aggiunta del Listener che riceve la notifica di un evento e la stampa!

		//CARICAMENTO DEI FILE
		String[] files = null;
		try {
			files = loadLogFiles(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//AVVIO DEL GENERATORE DI EVENTI
		EventGenerator evGen = new EventGenerator(cepRT,files);
		evGen.start();		
	}
	
	/**
	 * Configure the stream of data, adding the event object
	 * @return Configuration object
	 */
	private static Configuration getConfiguration() {
		Configuration cepConfig = new Configuration();
		
		cepConfig.addEventType("PirwEvent", PirwEvent.class.getName());
		cepConfig.addEventType("PircEvent", PircEvent.class.getName());
		cepConfig.addEventType("DwcEvent", DwcEvent.class.getName());
		cepConfig.addEventType("LocationEvent", LocationEvent.class.getName());
		
		return cepConfig;
	}
	
	/**
	 * Load the log file in the given directory stateDump.txt and LOC[0-9]+.log
	 * @param path String, the path where the log files are
	 * @return String[], with the full path of the two files
	 * @throws FileNotFoundException if one of the two files is not in the directory
	 */
	private static String[] loadLogFiles(String path) throws FileNotFoundException {
		String files[] = new String[2]; //variabile di return
		Pattern filePattern = Pattern.compile("LOC[0-9]+.log"); //pattern del nome per il file loc
		
		//ottengo la lista dei file
		String[] dir = new File(path).list();
		
		//cerco il file delle posizioni nella cartella
		String locFilename = lookForLOCFile(dir, filePattern, 0);
		
		//cerco il dump dello stato dei sensori nella cartella
		if (!(new File(path+"/"+SENSOR_STATE_DUMP).isFile()))
			throw new FileNotFoundException(SENSOR_STATE_DUMP + " file, mancante!");
		
		//preparo i risultati per il return
		files[0] = path + "/" + SENSOR_STATE_DUMP; 
		files[1] = path + "/" + locFilename;
		return files;
	}
	
	/**
	 * Goes trough all the files in the directory looking for the LOC file
	 * @param dir, String[], all the file in the directory
	 * @param filePattern, Pattern, the file name pattern to match
	 * @param i, int, just a counter
	 * @return the file name
	 * @throws FileNotFoundException if the file is not in the directory
	 */
	private static String lookForLOCFile(String[] dir, Pattern filePattern, int i) throws FileNotFoundException {
		if (i<dir.length)
			return ( filePattern.matcher( dir[i] ).find() )?dir[i]:lookForLOCFile(dir, filePattern, i++);
		else
			throw new FileNotFoundException("LOC file mancante!");
	}
}
