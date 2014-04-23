package db2.esper.engine;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.JFrame;

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
import db2.esper.graphic2d.Map;
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
	
	/* dato che avevamo preparato il codice per usare i deviceID, ma nel file c'� il deviceName
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
		JFrame mainWindow; 
		
		//inizializzazione di log4j richiesta da Esper, a noi non serve in realt�...
//		BasicConfigurator.configure(); 

		
		mainWindow = createAndShowGUI();
		Map map = (Map) mainWindow.getContentPane().getComponent(0);

		//se in args non � stata passato nessun percorso valido, carica i file di default
		String path = null;
		if(args.length == 0) {
			path = "data/A"; //cambiami per caricare gli altri test case
		} else {
			path = args[0]; //Zanero NON sarebbe orgoglioso di te...
		}
		
		EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEP", getConfiguration());
		EPRuntime cepRT = cep.getEPRuntime();
		
		Listener myListener = new Listener(map);
		String query = null;
		
		//CARICAMENTO DEI FILE
		HashMap<String, String> files = null;
		try {
			files = loadLogFiles(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//CARICAMENTO MURI
		walls = Parse.wallsPositionFile(files.get("wallsPosition"));
		
		//DRAW WALLS ON MAP
		map.drawWalls(walls);
		
		//AVVIO DEI GENERATORI DI EVENTI
		//siccome i due eventi location e sensor possono essere contemporanei genero due thread separati
		SensorEventGenerator sensorEventGenerator = new SensorEventGenerator(cepRT,files.get("sensorState"), files.get("sensorPosition"));
		LocationEventGenerator locationEventGenerator = new LocationEventGenerator(cepRT, files.get("location")); 
		
		sensorEventGenerator.setName("sensorThread");
		locationEventGenerator.setName("locationThread");
		
		sensorEventGenerator.start();
		locationEventGenerator.start();
		
		//QUERY
//		query = "INSERT INTO pirwEPL SELECT * FROM PirwEvent ";

//		query = "INSERT INTO streamPIRC SELECT * FROM PircEvent(status=true) ";

//		query = "  SELECT * "
//				+ "FROM SensorEvent.win:time(20) as S, LocationEvent.win:time(10) as L "
//				+ "WHERE db2.esper.util.MathAlgorithm.existsWall(S.x, S.y, L.x, L.y) = true";
		
//		query = "SELECT PIRC.deviceID, PIRC.timestamp "
//				+ "FROM pattern[every(PIRC=streamPIRC(status=true))->NOT("
//				+ "LOC=streamLOC WHERE timer:within(10 sec))]";

//		query = "SELECT p.timestamp, p.deviceID "
//				  + "FROM pirwEPL.win:length(3) as p, LocationEvent.win:length(3) as l "
//				  + "WHERE p.timestamp = l.timestamp";

//		query = "SELECT * "
//		  + "FROM pirwEPL.win:time(30sec) "
//		  + "WHERE status IN (SELECT status FROM pircEPL.win:time(30sec))";	

		//by enzo1: non è precisissima ma non trova fault
//		query = "SELECT PIRC.deviceID, PIRC.timestamp, LOC.timestamp "
//				+ "FROM PircEvent(status=true).std:unique(sensorID).win:time(5 sec) as PIRC, LocationEvent.std:unique(timestamp).win:time(5 sec) as LOC "
//				+ "WHERE db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, 4, LOC.x, LOC.y, 4) = false "
//				+ "OR db2.esper.util.MathAlgorithm.existsWall(PIRC.x, PIRC.y, LOC.x, LOC.y) = true ";
		
		//by enzo2: rispetto alla precedente restringe il campo visivo ancora di più, ma non è
		//ai livelli che si otterrebbero usando il "from pattern" secondo me
//		query = "SELECT PIRC.deviceID, PIRC.timestamp, LOC.timestamp "
//				+ "FROM PircEvent(status=true).std:unique(sensorID).win:time(20 sec) as PIRC, LocationEvent.std:unique(timestamp).win:time(20 sec) as LOC "
//				+ "WHERE (PIRC.timestamp BETWEEN LOC.timestamp AND LOC.timestamp + 5000) "
//				+ "AND (db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, 4, LOC.x, LOC.y, 4) = false "
//				+ "OR db2.esper.util.MathAlgorithm.existsWall(PIRC.x, PIRC.y, LOC.x, LOC.y) = true) ";

		// by enzo3: forse migliore
//		 query = "SELECT 'FAULT', PIRC.deviceID, PIRC.timestamp "
//				 + "FROM PircEvent(status=true).std:unique(sensorID).std:lastevent() AS PIRC, LocationEvent.std:unique(timestamp).win:length(10) AS LOC "
//				 + "WHERE db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, 4, LOC.x, LOC.y, 4) = false "
//				 + "OR (db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, 4, LOC.x, LOC.y, 4) = true "
//				 + "AND db2.esper.util.MathAlgorithm.existsWall(PIRC.x, PIRC.y, LOC.x, LOC.y) = true)"
//				 + "OUTPUT first every 5 seconds";
		
		// by enzo4: è un test, non è errata sintatticamente ma non funziona come dovrebbe
//		query= "SELECT PIRC.deviceID, PIRC.timestamp, 'FAULT' "
//				+ "FROM pattern[(every PIRC=PircEvent(status=true)->every (LOC=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, 4, LOC.x, LOC.y, 4)) "
//				+ "WHERE timer:within(2 sec) AND NOT PircEvent(status=true)))].win:time(2 sec) "
//				+ "WHERE db2.esper.util.MathAlgorithm.existsWall(PIRC.x, PIRC.y, LOC.x, LOC.y) = true";
		
//		query= "SELECT PIRW.deviceID, PIRW.timestamp, 'FAULT' "
//				+ "FROM pattern[(every PIRW=PirwEvent(status=true)->every (LOC=LocationEvent AND NOT PirwEvent(status=true)))] "
//				+ "WHERE NOT EXISTS (SELECT LOC.timestamp "
//				+ "FROM LOC WHERE db2.esper.util.MathAlgorithm.doIntersect(PIRW.x, PIRW.y, 4, LOC.x, LOC.y, 4)= true )";
		
		//QUERY 1: attivazione sensore e localizzatore distante
		Listener farAwayListener = new Listener(map);
		query = "SELECT PIRC.x AS pircX, PIRC.y AS pircY, PIRC.radius AS pircRadius, LOC.x AS locX, LOC.y AS locY, LOC.radius AS locRadius "
				+ "FROM PirwEvent.win:time(20 sec) AS PIRC, LocationEvent.win:time(20 sec) AS LOC "
				+ "WHERE db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, LOC.x, LOC.y, LOC.radius) = false";
		EPStatement farAwaySensorActivation = cep.getEPAdministrator().createEPL(query);
		farAwaySensorActivation.addListener(farAwayListener);
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

	/**
	 * Initialize and create the window that is needed to show the map
	 * @return the JFrame window 
	 */
	private static JFrame createAndShowGUI() {
		int width = 800;
		int height = 600;
		JFrame window = new JFrame("Esper In Door Localization Simulator");
       
		//setup the window dimension
		window.setSize(width, height);
		
		//avoid resize (it's not managed, if you want a bigger window change the dimension over here)
		window.setResizable(false);
		
		//add the map panel
		window.getContentPane().add(new Map(width, height), 0);

		//display the window
		window.setVisible(true);
		
		return window;
	}

}
