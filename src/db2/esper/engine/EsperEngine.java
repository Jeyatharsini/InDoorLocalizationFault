package db2.esper.engine;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

import db2.esper.common.Wall;
import db2.esper.event.models.DwcEvent;
import db2.esper.event.models.LocationEvent;
import db2.esper.event.models.PircEvent;
import db2.esper.event.models.PirwEvent;
import db2.esper.event.models.SensorEvent;
import db2.esper.events.LocationEventGenerator;
import db2.esper.events.SensorEventGenerator;
import db2.esper.util.Parse;

/* Rilevamenti:
 * A: senza fault
 * B: senza fault
 * C: senza fault
 * D: senza device di localizzazione
 * E: con un pir non attivo
 */

public class EsperEngine {
	
	//DEBUG FLAG
	public static final boolean VERBOSE = false;

	// i nomi dei file che servono per far funzionare il tutto, meno il log delle LOC
	private static final String SENSOR_STATE_DUMP = "stateDump.txt";
	private static final String SENSOR_POSITION_FILE = "zwave_pos.txt";
	private static final String WALLS_POSITION_FILE = "walls.txt";
	
	/* dato che avevamo preparato il codice per usare i deviceID, ma nel file c'è il deviceName
	 * per non riscrivere l'espressione regolare abbiamo fatto questa Map
	 * che fa corrispondere a ciascun deviceName il suo deviceID
	 */
	public static final String[] sensorIdToName = {
		null,	// per far tornare i conti mi serve riempire lo zero :-) 
		"PIRC 2.12", 		// 1
		"PIRW corridor", 	// 2
		"PIRW Salice", 		// 3
		"PIRW wc", 			// 4
		"Door Salice", 		// 5
		"DOOR wc",			// 6
		"Door 2.12"			// 7
		};
	
	public static ArrayList<Wall> walls = null;
	
	//TODO sistemare questi throws che qui non servono a molto...
	public static void main(String[] args) throws InterruptedException, FileNotFoundException {
		//inizializzazione di log4j richiesta da Esper, a noi non serve in realtà...
		BasicConfigurator.configure(); 
		
		//se in args non è stata passato nessun percorso valido, carica i file di default
		String path = null;
		if(args.length == 0) {
			path = "data/A"; //cambiami per caricare gli altri test case
		} else {
			path = args[0]; //Zanero NON sarebbe orgoglioso di te...
		}
		
		EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEP", getConfiguration());
		EPRuntime cepRT = cep.getEPRuntime();
		
		Listener myListener = new Listener();
		String query = null;
		
		/*
		 * PROBLEMA: 
		 * Se prendo una query che recupera solo gli eventi di un certo tipo, e ci attacco il listener
		 * a consolle viene stampato un solo evento per tipo come è giusto che sia.
		 * Se attivo tutte e 4 le query contemporaneamente e ad ogni query attacco lo stesso listener ottengo
		 * un sacco di eventi duplicati.
		 */
		
		//Qualche query per testare che tutto funzioni...
		//query = "INSERT INTO pirwEPL SELECT * FROM PirwEvent ";
		query = "  SELECT * "
				+ "FROM PircEvent";
		EPStatement pirwEPL= cep.getEPAdministrator().createEPL(query);
		//if(verbose) pirwEPL.addListener(myListener);
		pirwEPL.addListener(myListener);

//		query = "INSERT INTO pircEPL SELECT * FROM PircEvent ";
//		EPStatement pircEPL = cep.getEPAdministrator().createEPL(query);
		//if(verbose) pircEPL.addListener(myListener);

//		query = "INSERT into dwcEPL SELECT * FROM DwcEvent ";
//		EPStatement dwcEPL = cep.getEPAdministrator().createEPL(query);
		//if(verbose) dwcEPL.addListener(myListener);

//		query = "INSERT into locationEPL SELECT * FROM LocationEvent ";
//		EPStatement locationEPL = cep.getEPAdministrator().createEPL(query);
		//if(verbose) locationEPL.addListener(myListener);
		
		//query = "SELECT p.timestamp, p.deviceID "
		//	  + "FROM pirwEPL.win:length(3) as p, LocationEvent.win:length(3) as l "
		//	  + "WHERE p.timestamp = l.timestamp";
//		query = "SELECT * "
//			  + "FROM pirwEPL.win:time(30sec) "
//			  + "WHERE status IN (SELECT status FROM pircEPL.win:time(30sec))";
//		EPStatement onlyTrue = cep.getEPAdministrator().createEPL(query);
		//onlyTrue.addListener(myListener);	//aggiunta del Listener che riceve la notifica di un evento e la stampa!

		//CARICAMENTO DEI FILE
		HashMap<String, String> files = null;
		try {
			files = loadLogFiles(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//CARICAMENTO MURI
		walls = Parse.wallsPositionFile(files.get("wallsPosition"));

		//AVVIO DEI GENERATORI DI EVENTI
		//siccome i due eventi location e sensor possono essere contemporanei genero due thread separati
		SensorEventGenerator sensorEventGenerator = new SensorEventGenerator(cepRT,files.get("sensorState"), files.get("sensorPosition"));
		LocationEventGenerator locationEventGenerator = new LocationEventGenerator(cepRT, files.get("location")); 
		
		sensorEventGenerator.setName("sensorThread");
		locationEventGenerator.setName("locationThread");
		
		sensorEventGenerator.start();
		locationEventGenerator.start();
		
		//QUERY
//		query = "  SELECT * "
//				+ "FROM SensorEvent.win:time(20) as S, LocationEvent.win:time(10) as L "
//				+ "WHERE db2.esper.util.MathAlgorithm.existsWall(S.x, S.y, L.x, L.y) = true";
//		EPStatement pirwEPL= cep.getEPAdministrator().createEPL(query);
//		//if(verbose) pirwEPL.addListener(myListener);
//		pirwEPL.addListener(myListener);
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
		cepConfig.addEventType("SensorEvent", SensorEvent.class.getName());
		cepConfig.addEventType("LocationEvent", LocationEvent.class.getName());
		
		return cepConfig;
	}
	
	/**
	 * Load the log file in the given directory stateDump.txt and LOC[0-9]+.log
	 * @param path String, the path where the log files are
	 * @return String[], full path of the files [0]: stateDump file, [1]: location log file
	 * @throws FileNotFoundException if one of the two files is not in the directory
	 */
	private static HashMap<String, String> loadLogFiles(String path) throws FileNotFoundException {
		HashMap<String, String> files = new HashMap<String, String>(); //variabile di return
		
		//pattern del nome per il file loc
		Pattern filePattern = Pattern.compile("LOC[0-9]+.log"); 
		
		//ottengo la lista dei file
		String[] dir = new File(path).list();
		
		//cerco il file delle posizioni nella cartella
		String locFilename = lookForLOCFile(dir, filePattern, 0);
		
		//cerco il dump dello stato dei sensori nella cartella
		if (!(new File(path + "/" + SENSOR_STATE_DUMP).isFile()))
			throw new FileNotFoundException(SENSOR_STATE_DUMP + " file, mancante!");
		
		//cerco il dump dello stato dei sensori nella cartella
		if (!(new File("data/" + SENSOR_POSITION_FILE).isFile()))
			throw new FileNotFoundException(SENSOR_POSITION_FILE + " file, mancante!");
		
		//cerco il dump dello stato dei sensori nella cartella
		if (!(new File("data/" + WALLS_POSITION_FILE).isFile()))
			throw new FileNotFoundException(WALLS_POSITION_FILE + " file, mancante!");
		
		//preparo i risultati per il return
		files.put("sensorState", path + "/" + SENSOR_STATE_DUMP); 
		files.put("location", path + "/" + locFilename);
		files.put("sensorPosition", "data/" + SENSOR_POSITION_FILE);
		files.put("wallsPosition", "data/" + WALLS_POSITION_FILE);

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
