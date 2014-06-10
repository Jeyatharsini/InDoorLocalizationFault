package db2.esper.engine;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.apache.log4j.BasicConfigurator;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

import db2.esper.common.SyncTimestamp;
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
	public static final float SCALE_FACTOR = 1f;

	// i nomi dei file che servono per far funzionare il tutto, meno il log delle LOC
	private static final String SENSOR_STATE_DUMP = "stateDump.txt";
	private static final String SENSOR_POSITION_FILE = "zwave_pos.txt";
	private static final String WALLS_POSITION_FILE = "walls.txt";
	
	// utilizzata per ottenere il timestamp inferiore per sincronizzare i due thread
	static SyncTimestamp sTimestamp = new SyncTimestamp();
	
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
		
		//inizializzazione di log4j richiesta da Esper, a noi non serve in realtà...
		BasicConfigurator.configure(); 

		mainWindow = createAndShowGUI();
		Map map = (Map) mainWindow.getContentPane().getComponent(0);

		//se in args non � stata passato nessun percorso valido, carica i file di default
		String path = null;
		if(args.length == 0) {
			path = "data/E"; //cambiami per caricare gli altri test case
		} else {
			path = args[0]; //Zanero NON sarebbe orgoglioso di te...
		}
		
		EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEP", getConfiguration());
		EPRuntime cepRT = cep.getEPRuntime();
		
		String query = null;
		String query2 = null;
		
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
		SensorEventGenerator sensorEventGenerator = new SensorEventGenerator(cepRT,files.get("sensorState"), files.get("sensorPosition"), sTimestamp);
		LocationEventGenerator locationEventGenerator = new LocationEventGenerator(cepRT, files.get("location"), sTimestamp); 
		
		sensorEventGenerator.setName("sensorThread");
		locationEventGenerator.setName("locationThread");
		
		//Sincronizzo i due thread
		sensorEventGenerator.sync();
		locationEventGenerator.sync();
		
		//partena dei thread!
		locationEventGenerator.start();
		sensorEventGenerator.start();
		
		//QUERY
//		query = "INSERT INTO pirwEPL SELECT * FROM PirwEvent ";

//		query = "INSERT INTO streamPIRC SELECT * FROM PircEvent(status=true) ";

//		query = "  SELECT * "
//				+ "FROM SensorEvent.win:time(20) as S, LocationEvent.win:time(20) as L ";
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

//by Marco: QUERY 1: attivazione sensore e localizzatore distante
//		Listener farAwayListener = new Listener(map);
//		query = "SELECT PIRC.x AS pircX, PIRC.y AS pircY, PIRC.radius AS pircRadius, LOC.x AS locX, LOC.y AS locY, LOC.radius AS locRadius "
//				+ "FROM PircEvent.win:time(20 sec) AS PIRC, LocationEvent.win:time(20 sec) AS LOC "
//				+ "WHERE db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, LOC.x, LOC.y, LOC.radius) = false";
//		EPStatement farAwaySensorActivation = cep.getEPAdministrator().createEPL(query);
//		farAwaySensorActivation.addListener(farAwayListener);
		
//Nel caso ti serva: merge tra stream1 e stream2 in uno combinato (ogni evento di stream1 ha null values per i dati di stream2 e viceversa):
//		query= "INSERT INTO combinedStream SELECT PIRC.deviceID, LOC.x, PIRC.status, PIRC.timestamp, PIRC.x, PIRC.y, PIRC.radius, LOC.timestamp, LOC.y, LOC.radius FROM pattern[every PIRC=PircEvent or every LOC=LocationEvent].win:keepall()"; 

//		select * pattern[ every EventAT -> every EventAf and not LOC] 
//		query="SELECT 'FAULT', PIRC.deviceID, PIRC.timestamp, PIRC.x, PIRC.y, LOC.x, LOC.y, LOC.timestamp "
//				+ "FROM pattern[every PIRC=PircEvent(status=true)-> every PIRC=PircEvent(status=false) and not "
//				+ "LOC=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, LOC.x, LOC.y, LOC.radius) = true)]";
			
		
//select * from pattern [every EventA -> (timer:interval(10 sec) and not EventB)]  
//"This pattern fires if an event A is not followed by an event B within 10 seconds"  zero fault trovati:
//		query="SELECT 'FAULT', PIRC.deviceID, PIRC.timestamp, PIRC.x, PIRC.y, LOC.x, LOC.y, LOC.timestamp "
//				+ "FROM pattern[every PIRC=PircEvent(status=true)->(timer:interval(10 sec) and not "
//				+ "LOC=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, LOC.x, LOC.y, LOC.radius) = true))]";
		
//select * from pattern [every EventA -> ((timer:interval(10 sec) and not EventB) and not EventA1)] 
//E' una versione più constrained e corretta della precedente, xkè ora la ricerca per ogni sensore attivo si interrompe nonappena questo si disattiva, anche se non sono scaduti i 10 sec; zero fault trovati:
//		query="SELECT 'FAULT', PIRC.deviceID, PIRC.timestamp, PIRC.x, PIRC.y, LOC.x, LOC.y, LOC.timestamp "
//				+ "FROM pattern[every PIRC=PircEvent(status=true)->((timer:interval(10 sec) and not "
//				+ "LOC=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, LOC.x, LOC.y, LOC.radius) = true)) "
//				+ "and not PircEvent(status=false, deviceID=PIRC.deviceID))]";

//select * from pattern [every(EventAf ->(((timer:interval(10sec) and not EventB(conditions)) and not EventAt) -> ((timer:interval(10sec) and not EventB(conditions)) and not EventAf)))]
//Gestisce anche l'intervallo in cui un sensore da false passa a true, e non solo quello in cui da true ripassa a false. Inoltre è comprensiva dei due fault, ma non li distingue. Versione definitiva?:
//		query="SELECT 'FAULT', PIRC.deviceID, PIRC.timestamp, PIRC.x, PIRC.y, LOC.x, LOC.y, LOC.timestamp "
//				+ "FROM pattern[every PIRC=PircEvent(status=false)->(((timer:interval(10 sec) and not "
//				+ "LOC=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, LOC.x, LOC.y, LOC.radius) = true and "
//				+ "db2.esper.util.MathAlgorithm.existsWall(PIRC.x, PIRC.y, LOC.x, LOC.y) = false)) and not "
//				+ "PircEvent(status=true, deviceID=PIRC.deviceID)) -> ((timer:interval(10 sec) and not "
//				+ "Loc=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, Loc.x, Loc.y, Loc.radius) = true and "
//				+ "db2.esper.util.MathAlgorithm.existsWall(PIRC.x, PIRC.y, LOC.x, LOC.y) = false)) "
//				+ "and not PircEvent(status=false, deviceID=PIRC.deviceID)))]";
		
//select * from pattern [every EventA -> (timer:interval(03 sec) and every (EventB and condition))]
//Query che trova un fault se entro 3 sec da un eventosensore a true arriva uno (senza every) o più (con every) eventoLOC con area intersecante e muro in mezzo; 9 fault trovati (se non scrivo sulla mappa, a doppio se scrivo sulla mappa...occore un fix):
//		query2="SELECT PIRC.timestamp, PIRC.deviceID, PIRC.x AS pircX, PIRC.y AS pircY, PIRC.radius AS pircRadius, LOC.x AS locX, LOC.y AS locY, LOC.radius AS locRadius, LOC.timestamp "
//				+ "FROM pattern[every PIRC=PircEvent(status=true)->(timer:interval(03 sec) AND every "
//				+ "(LOC=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, LOC.x, LOC.y, LOC.radius) = true AND "
//				+ "db2.esper.util.MathAlgorithm.existsWall(PIRC.x, PIRC.y, LOC.x, LOC.y) = true)))]";
		
//select * from pattern [every EventA -> ((timer:interval(03 sec) and every (EventB and condition)) and not EventA1)]
//Versione più constrained e corretta(al pari della seconda query); zero fault trovati a differenza della precedente (quindi posso dedurre che occorre dirgli di fermarsi quando il sensore s'è spento):
//		query2="SELECT 'FAULT', PIRC.deviceID, PIRC.timestamp, PIRC.x, PIRC.y, LOC.x, LOC.y, LOC.timestamp "
//				+ "FROM pattern[every PIRC=PircEvent(status=true)->((timer:interval(03 sec) AND every "
//				+ "(LOC=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, LOC.x, LOC.y, LOC.radius) = true AND "
//				+ "db2.esper.util.MathAlgorithm.existsWall(PIRC.x, PIRC.y, LOC.x, LOC.y) = true))) and not "
//				+ "PircEvent(deviceID=PIRC.deviceID, status=false))]";

//select * from pattern [every EventA -> ((every EventB and condition) and not EventA1)]
//Stesso tipo della precedente, ma senza il timer: credo che le due siano equivalenti, xkè qui la ricerca per ogni subexpression muore non appena il sensore della subexpression si spegne; zero fault trovati:
//		query2="SELECT 'FAULT', PIRC.deviceID, PIRC.timestamp, PIRC.x, PIRC.y, LOC.x, LOC.y, LOC.timestamp "
//				+ "FROM pattern[every PIRC=PircEvent(status=true)->((every LOC=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, LOC.x, LOC.y, LOC.radius) = true "
//				+ " AND db2.esper.util.MathAlgorithm.existsWall(PIRC.x, PIRC.y, LOC.x, LOC.y) = true)) and not "
//				+ "PircEvent(deviceID=PIRC.deviceID, status=false))]";
		
//query che esplicitamente rileva se nella sequenza: sensorIDx=false->sensorIDx=true->sensorIDx=false ci sono 
//SOLO [LOC events con aree intersecanti e muri in mezzo], e non [LOC events con aree intersecanti senza muri in mezzo]. Versione definitiva?:
//		query2="SELECT 'FAULT', PIRC.deviceID, PIRC.timestamp, PIRC.x, PIRC.y, LOC.x, LOC.y, LOC.timestamp "
//				+ "FROM pattern[every (PIRC=PircEvent(status=false)->(((timer:interval(10 sec) and not "
//				+ "LOC=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, LOC.x, LOC.y, LOC.radius) = true and "
//				+ "db2.esper.util.MathAlgorithm.existsWall(PIRC.x, PIRC.y, LOC.x, LOC.y) = false)) and not "
//				+ "PircEvent(status=true, deviceID=PIRC.deviceID)) AND "
//				+ "(Loc=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, Loc.x, Loc.y, Loc.radius) = true and "
//				+ "db2.esper.util.MathAlgorithm.existsWall(PIRC.x, PIRC.y, Loc.x, Loc.y) = true) and not PircEvent(status=true, deviceID=PIRC.deviceID))) -> "
//				+ "(((timer:interval(10 sec) and not "
//				+ "LOC2=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, LOC2.x, LOC2.y, LOC2.radius) = true and "
//				+ "db2.esper.util.MathAlgorithm.existsWall(PIRC.x, PIRC.y, LOC2.x, LOC2.y) = false)) and not "
//				+ "PircEvent(status=false, deviceID=PIRC.deviceID)) AND "
//				+ "(Loc2=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(PIRC.x, PIRC.y, PIRC.radius, Loc2.x, Loc2.y, Loc2.radius) = true and "
//				+ "db2.esper.util.MathAlgorithm.existsWall(PIRC.x, PIRC.y, Loc2.x, Loc2.y) = true) and not PircEvent(status=false, deviceID=PIRC.deviceID))))]";
			
//		query = "INSERT INTO lastActive "
//				+ "SELECT * FROM PircEvent.win:time_batch(10 sec) AS Pirc "
//				+ "WHERE Pirc.deviceID IN (SELECT PircT.deviceID "
//				+ "FROM PircEvent(status=true).win:time_batch(10 sec) PircT "
//				+ "WHERE NOT EXISTS (SELECT * FROM PircEvent(status=false).win:time_batch(10) AS PircF "
//				+ "WHERE PircF.deviceID=PircT.deviceID AND PircF.timestamp>PircT.timestamp))";
		
	
//		String query3 = "SELECT last(deviceID, 0) AS deviceID0, last(deviceID, 1) as deviceID1, last(deviceID,2) as deviceID2, status " 
//						+ "FROM SensorEvent.win:length(10) AS TS "
//						+ "WHERE "
//						+ "deviceID NOT IN ("
//							+ "SELECT deviceID "
//							+ "FROM SensorEvent(status=false).win:time(5 sec) AS FS "
//						+ ") AND "
//						+ "timestamp > ALL ("
//							+ "SELECT timestamp "
//							+ "FROM SensorEvent(status=false).win:time(5 sec) AS FS "
//						+ ")"
//						;

//		String join = "SELECT last(myWin1.deviceID,0) from SensorEvent.win:length(5) as myWin1, SensorEvent.win:length(5) as myWin2 "
//						 + "WHERE myWin1.deviceID=myWin2.deviceID AND (( "
//						 + "myWin1.status=true AND myWin2.status=true AND myWin1.timestamp=myWin2.timestamp) "
//						 + "OR (myWin2.status=true AND myWin1.status=false AND myWin2.timestamp >= myWin1.timestamp)) "
//				;
		
//		query = "SELECT last(se1.deviceID, 0) "
//				+ "FROM SensorEvent.win:time_length_batch(5 sec, 4, \"FORCE_UPDATE\") AS se1 "
//				+ "WHERE se1.status = true AND "
//				+ "se1.timestamp > 1395246600080 AND "
//				+ "NOT EXISTS ( "
//					+ "SELECT deviceID "
//					+ "FROM SensorEvent.win:time_length_batch(5 sec, 4, \"FORCE_UPDATE\") AS se2 "
//					+ "WHERE se2.deviceID = se1.deviceID AND "
//					+ "se2.status = false AND "
//					+ "se2.timestamp > se1.timestamp AND "
//					+ "se2.timestamp > 1395246600080 "
//				+ ")";
		
//		String falseSensor = "INSERT INTO lastTrueSensor "
//				+ "SELECT deviceID, timestamp "
//				+ "FROM SensorEvent(status=true).std:lastEvent() AS TS ";
//
//		String combinedStream = "INSERT INTO combinedStream "
//				+ "SELECT TS.deviceID AS trueSensor, TS.timestamp AS trueTimestamp, FS.deviceID AS falseSensor, FS.timestamp AS falseTimestamp "
//				+ "FROM pattern[every TS=SensorEvent(status = true) or every FS=SensorEvent(status = false)].win:keepall()"; 

//		combinedStream = "SELECT MAX(TS.deviceID) "
//				+ "FROM SensorEvent.win:length(10) as TS, SensorEvent.win:length(10) as FS "
//				+ "WHERE "
//				+ "TS.deviceID = FS.deviceID AND "
//				+ "TS.status = true AND "
//				+ "FS.status = true "
//				;
		
//		combinedStream = "INSERT INTO combinedStream "
//						+ "SELECT TS.deviceID AS trueDeviceID, FS.deviceID AS falseDeviceID "
//						+ "FROM SensorEvent(status=true) AS TS, SensorEvent(status=false) AS FS "
//						+ "WHERE TS.deviceID = FS.deviceID AND TS.timestamp < FS.timestamp "
//						;

//		String query3 = "SELECT last(event.trueSensor, 0) AS deviceID0 "
//						+ "FROM combinedStream.win:length(10) AS event "
//						+ "WHERE event.trueSensor NOT IN ("
//								+ "SELECT falseSensor "
//								+ "FROM combinedStream.win:time(10 sec) "
//							+ ") AND "
//							+ "event.trueTimestamp > ALL ("
//								+ "SELECT falseSensor "
//								+ "FROM combinedStream.win:time(10 sec) "
//							+ ")"
//						;
		
//		query3 = "SELECT last(deviceID, 0) AS deviceID0, last(deviceID, 1) as deviceID1, last(deviceID,2) as deviceID2, status " 
//		+ "FROM SensorEvent.win:length(10) AS TS "
//		+ "WHERE "
//		+ "timestamp > ALL ("
//			+ "SELECT timestamp "
//			+ "FROM SensorEvent(status=false).win:length(10) AS FS "
//		+ ")"
//		;
		
//		String query3 = "SELECT pirw.x AS sensX, pirw.y AS sensY, pirw.radius AS sensRadius, pirw.timestamp "
//				+ "FROM PirwEvent as pirw "
//				+ "WHERE pirw.deviceID = 2 AND pirw.status = true AND pirw.timestamp > 1395246168154" ;
//		query = "SELECT PIRC.x AS pircX, PIRC.y AS pircY, PIRC.radius AS pircRadius, PIRC.timestamp, PIRC.deviceID "
//				+ "FROM SensorEvent as PIRC "
//				+ "WHERE PIRC.status = true";
//				
//		query = "SELECT PIRC.x AS pircX, PIRC.y AS pircY, PIRC.radius AS pircRadius, LOC.x AS locX, LOC.y AS locY, LOC.radius AS locRadius "
//		+ "FROM PircEvent.win:time(20 sec) AS PIRC, LocationEvent.win:time(20 sec) AS LOC ";

//		query2 = "SELECT * "
//				+ "FROM PATTERN [every SensA1 = SensorEvent(status=true) -> LOC=LocationEvent("
//					+ "db2.esper.util.MathAlgorithm.doIntersect(SensA1.x, SensA1.y, SensA1.radius, LOC.x, LOC.y, LOC.radius) = false OR "
//						+ "db2.esper.util.MathAlgorithm.existsWall(SensA1.x, SensA1.y, LOC.x, LOC.y) = true"
//					+ ")"
//				+ "]";
//		
		
//		le tre query che danno insieme il risultato migliore fin'ora:
//		String eventiStream = "INSERT INTO eventiStream "
//				+ "SELECT win.deviceID AS deviceID, win.timestamp AS timestamp, win.categoryID AS category, win.x AS posX, win.y AS posY, win.radius AS rad  "
//				+ "FROM SensorEvent(status=true).win:time(10 sec) as win "
//				;
//		
//		query = "INSERT INTO lastSensorActive SELECT irstream deviceID, timestamp, category, posX, posY, rad "
//				+ "FROM eventiStream.ext:rank(deviceID, 1, timestamp DESC) "
//				;
//		query2 = "SELECT Sens1.deviceID, 'blindSensorafter', Sens1.timestamp, Sens1.posX AS sensX, Sens1.posY AS sensY, Sens1.rad AS sensRadius "
//				+ "FROM PATTERN[every Sens1=lastSensorActive -> LOC=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(Sens1.posX, Sens1.posY, Sens1.rad, LOC.x, LOC.y, LOC.radius) = false) "
//				+ "and not Sens2=lastSensorActive]";
//		
//con le seguenti n query cerco la sequenza dell'ultimo sensore attivo, ipotizzando che non piu di 3 sensori siano attivi allo stesso tempo, e considerando tutti i casi possibili:
//		query = "INSERT INTO lastSensorActive (deviceID, sensX, sensY, sensRadius, timestamp) "
//				+ "Select st1.deviceID, st1.x, st1.y, st1.radius, st1.timestamp "
//				+ "from PATTERN[every st1=SensorEvent(status=true) -> sf1=SensorEvent(status=false and deviceID=st1.deviceID) "
//										+ "and not (SensorEvent(status=true) or SensorEvent(status=false and deviceID!=st1.deviceID))]";
//		
//		String query1 ="INSERT INTO lastSensorActive (deviceID, sensX, sensY, sensRadius, timestamp) "
//				+ "Select st1.deviceID, st1.x, st1.y, st1.radius, st1.timestamp "
//				+ "from PATTERN[every st1=SensorEvent(status=true) -> st2=SensorEvent(status=true and deviceID!=st1.deviceID) "
//				+ "and not SensorEvent(status=false and deviceID=st1.deviceID)]";
//		
//		query2 = "INSERT INTO lastSensorActive (deviceID, sensX, sensY, sensRadius, timestamp) "
//				+ "Select st3.deviceID, st3.x, st3.y, st3.radius, st3.timestamp "
//				+ "from PATTERN[every st1=SensorEvent(status=true) ->( st2=SensorEvent(status=true and deviceID!=st1.deviceID) "
//				+ "and not SensorEvent(status=false) )-> st3=SensorEvent(status=true) and not "
//				+ "(SensorEvent(status=false and deviceID=st1.deviceID) or SensorEvent(status=false and deviceID=st2.deviceID))]";
//
//		String query3 = "INSERT INTO lastSensorActive (deviceID, sensX, sensY, sensRadius, timestamp) "
//				+ "Select st1.deviceID, st1.x, st1.y, st1.radius, st1.timestamp "
//				+ "from PATTERN[every st1=SensorEvent(status=true) ->( st2=SensorEvent(status=true and deviceID!=st1.deviceID) "
//				+ "and not SensorEvent(status=false and deviceID=st1.deviceID) )-> SensorEvent(status=false and deviceID=st2.deviceID) "
//				+ "and not SensorEvent(status=true)]";
//		
//		String query4= "INSERT INTO lastSensorActive (deviceID, sensX, sensY, sensRadius, timestamp) "
//				+ "Select st3.deviceID, st3.x, st3.y, st3.radius, st3.timestamp "
//				+ "from PATTERN[every st1=SensorEvent(status=false and timestamp>1395246600080) -> st2=SensorEvent(status=false) and not "
//				+ "SensorEvent(status=true) -> st3=SensorEvent(status=false) and not SensorEvent(status=true)]";
//		
//		String query5= "INSERT INTO lastSensorActive (deviceID, sensX, sensY, sensRadius, timestamp) "
//				+ "Select st1.deviceID, st1.x, st1.y, st1.radius, st1.timestamp "
//				+ "from PATTERN[every st1=SensorEvent(status=true) ->( st2=SensorEvent(status=false and deviceID!=st1.deviceID) and not "
//				+ "(SensorEvent(status=false and deviceID=st1.deviceID) or SensorEvent(status=true)) )-> "
//				+ "SensorEvent(status=false and deviceID=st1.deviceID) and not SensorEvent(status=true)]";
//		
//		String query6= "INSERT INTO lastSensorActive (deviceID, sensX, sensY, sensRadius, timestamp) "
//				+ "Select st1.deviceID, st1.x, st1.y, st1.radius, st1.timestamp "
//				+ "from PATTERN[every st1=SensorEvent(status=true) ->( st2=SensorEvent(status=true and deviceID!=st1.deviceID) "
//				+ "and not SensorEvent )->( st3=SensorEvent(status=false and deviceID=st2.deviceID) "
//				+ "and not SensorEvent)->( st4=SensorEvent(status=true) and not SensorEvent(status=false and deviceID=st1.deviceID) )->( "
//				+ "SensorEvent(status=false and deviceID=st4.deviceID) and not SensorEvent)]";
//		
//per controllare che la sequenza dei sensori sia corretta:	
//		String query7= "Select * from lastSensorActive.std:lastevent()";
		
//		la query che tira fuori i fault per il caso E:
//		String query7 = "SELECT Sens1.deviceID, 'blindSensorafter', Sens1.sensX AS sensX, Sens1.sensY AS sensY, Sens1.sensRadius AS sensRadius "
//		+ "FROM PATTERN[every Sens1=lastSensorActive -> LOC=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(Sens1.sensX, Sens1.sensY, Sens1.sensRadius, LOC.x, LOC.y, LOC.radius) = false) "
//		+ "and not Sens2=lastSensorActive]";
//		
//		la seguente per vedere se uso come pattern ogni sensore che si attiva e successivo: zero fault trovati...ciò dimostrerebbe che è necessario trovare la sequenza dell'ultimo sensore attivo: 
//		String query7 = "SELECT Sens1.deviceID, 'blindSensorafter', Sens1.timestamp, Sens1.x AS sensX, Sens1.y AS sensY, Sens1.radius AS sensRadius "
//						+ "FROM PATTERN[Sens1=SensorEvent(status=true) -> LOC=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(Sens1.x, Sens1.y, Sens1.radius, LOC.x, LOC.y, LOC.radius) = false)"
//						+ "and not Sens2=SensorEvent(status=true)]";
		
//		stampa tutti gli eventi attivazione sensore:
//		String query3= "SELECT Sens1.deviceID, Sens1.timestamp, Sens1.x AS sensX, Sens1.y AS sensY, Sens1.radius AS sensRadius "
//						+ "FROM SensorEvent(status=true).std:lastevent() AS Sens1";
		
//		stampa su mappa tutti gli eventi localizzatore in sequenza
//		String query4 = "SELECT LOC.x AS locX, LOC.y AS locY, LOC.radius AS locRadius, LOC.timestamp "
//				+ "FROM LocationEvent.std:lastevent() as LOC";
		

//		String query7 = "SELECT Sens1.deviceID, 'blindSensorafter', Sens1.sensX AS sensX, Sens1.sensY AS sensY, Sens1.sensRadius AS sensRadius "
//					  + "FROM PATTERN[every Sens1=veryLastEvent -> LOC=LocationEvent("
//					  		+ "db2.esper.util.MathAlgorithm.doIntersect(Sens1.sensX, Sens1.sensY, Sens1.sensRadius, LOC.x, LOC.y, LOC.radius) = false "
//					  		+ "AND db2.esper.util.MathAlgorithm.existsWall(Sens1.sensX, Sens1.sensY, LOC.x, LOC.y) = false "
//					  	+ ")  "
//					  	+ "and not Sens2=veryLastEvent("
//					  		+ "db2.esper.util.MathAlgorithm.doIntersect(Sens2.sensX, Sens2.sensY, Sens2.sensRadius, LOC.x, LOC.y, LOC.radius) = true "
//					  		+ "AND db2.esper.util.MathAlgorithm.existsWall(Sens2.sensX, Sens2.sensY, LOC.x, LOC.y) = false "
//					  	+ ")"
//					  	+ "]"
//					  	;
//		
//		String query7 = ""
//				+ "SELECT 'faultLocation', Sens.sensX AS sensX, Sens.sensY AS sensY, Sens.sensRadius AS sensRadius "
//				+ "FROM PATTERN[every Sens=veryLastEvent -> "
//					+ "LOC=LocationEvent("
//						+ "db2.esper.util.MathAlgorithm.doIntersect(Sens.sensX, Sens.sensY, Sens.sensRadius, LOC.x, LOC.y, LOC.radius) = false "
//						+ "AND db2.esper.util.MathAlgorithm.existsWall(Sens.sensX, Sens.sensY, LOC.x, LOC.y) = false "
//					+ ") and not veryLastEvent "
//				  + "]"
//				  	;
//		
//		String query7 = ""
//				+ "SELECT 'faultLocation', LOC.x AS locX, LOC.y AS locY, LOC.radius AS locRadius "
//				+ "FROM PATTERN[ every LOC=LocationEvent -> Sens=veryLastEvent("
//					+ "db2.esper.util.MathAlgorithm.doIntersect(Sens.sensX, Sens.sensY, Sens.sensRadius, LOC.x, LOC.y, LOC.radius) = false OR "
//					+ "db2.esper.util.MathAlgorithm.existsWall(Sens.sensX, Sens.sensY, LOC.x, LOC.y) = true "
//				+ ") AND NOT veryLastEvent "
//				+ "]"
//				;

		// seleziono tutti gli event location, seguiti da un evento sensore con cui non si intersecano e non esiste un muro
//		String query7 = ""
//				+ "SELECT loc.x AS locX, loc.y AS locY, loc.radius AS locRadius "
//				+ "FROM PATTERN[ "
//					+ "every sens=veryLastEvent -> "
//					+ "loc=LocationEvent("
//						+ "db2.esper.util.MathAlgorithm.doIntersect(sens.sensX, sens.sensY, sens.sensRadius, loc.x, loc.y, loc.radius) = true AND "
//						+ "db2.esper.util.MathAlgorithm.existsWall(sens.sensX, sens.sensY, loc.x, loc.y) = false "
//					+ ") AND NOT veryLastEvent -> sens1=veryLastEvent( "
//						+ "db2.esper.util.MathAlgorithm.doIntersect(sens1.sensX, sens1.sensY, sens1.sensRadius, loc.x, loc.y, loc.radius) = true OR "
//						+ "db2.esper.util.MathAlgorithm.existsWall(sens.sensX, sens.sensY, loc.x, loc.y) = true "
//					+ ") AND NOT veryLastEvent "
//				+ "]"
//				;
			
		//INVIATA A VERONESE
//		String query7 = ""
//				+ "SELECT loc.x AS locX, loc.y AS locY, loc.radius AS locRadius "
//				+ "FROM PATTERN[ "
//					+ "every sens=veryLastEvent -> "
//					+ "loc=LocationEvent("
//						+ "db2.esper.util.MathAlgorithm.doIntersect(sens.sensX, sens.sensY, sens.sensRadius, loc.x, loc.y, loc.radius) = true AND "
//						+ "db2.esper.util.MathAlgorithm.existsWall(sens.sensX, sens.sensY, loc.x, loc.y) = false "
//					+ ") AND NOT veryLastEvent -> sens2=veryLastEvent( "
//						+ "db2.esper.util.MathAlgorithm.existsWall(sens2.sensX, sens2.sensY, loc.x, loc.y) = true "
//					+ ") AND NOT veryLastEvent "
//				+ "]"
//				;

//		String query7 = ""
//		+ "SELECT loc.x AS locX, loc.y AS locY, loc.radius AS locRadius "
//		+ "FROM PATTERN[ "
//			+ "every loc=LocationEvent -> "
//			+ "sens=veryLastEvent("
//				+ "db2.esper.util.MathAlgorithm.doIntersect(sens.sensX, sens.sensY, sens.sensRadius, loc.x, loc.y, loc.radius) = true AND "
//				+ "db2.esper.util.MathAlgorithm.existsWall(sens.sensX, sens.sensY, loc.x, loc.y) = false "
//			+ ") -> sens2=veryLastEvent( "
//				+ "db2.esper.util.MathAlgorithm.existsWall(sens2.sensX, sens2.sensY, loc.x, loc.y) = true "
//			+ ") "
//		+ "]"
//		;
		

//		String query7 = ""
//				+ "SELECT loc.x AS locX, loc.y AS locY, loc.radius AS locRadius "
//				+ "FROM PATTERN[ "
//					+ "every sens=veryLastEvent -> "
//					+ "loc=LocationEvent("
//						+ "db2.esper.util.MathAlgorithm.doIntersect(sens.sensX, sens.sensY, sens.sensRadius, loc.x, loc.y, loc.radius) = false AND "
//						+ "db2.esper.util.MathAlgorithm.existsWall(sens.sensX, sens.sensY, loc.x, loc.y) = false "
//					+ ") AND NOT veryLastEvent  "
//				+ "]"
//				;
//		

				
		
///		QUERY FINALE -- Copre i casi A,B,C,D
String query4 = "SELECT 'FAULT', SensA1.x AS sensX, SensA1.y AS sensY, SensA1.radius AS sensRadius, SensA1.timestamp, SensA1.deviceID "
		+ "FROM PATTERN [every SensA1=SensorEvent(status=true) -> SensA2=SensorEvent(status=false and deviceID=SensA1.deviceID) "
		+ "and not LOC=LocationEvent(db2.esper.util.MathAlgorithm.doIntersect(SensA1.x, SensA1.y, SensA1.radius, LOC.x, LOC.y, LOC.radius) = true AND "
		+ "db2.esper.util.MathAlgorithm.existsWall(SensA1.x, SensA1.y, LOC.x, LOC.y) = false)]"
		;

//Query1: controlla solo l'ultimo evento sensore attivo nello stream
String query1 = "INSERT INTO lastActive (deviceID, sensX, sensY, sensRadius, timestamp) "
		+ "SELECT deviceID, x, y, radius, timestamp "
		+ "FROM SensorEvent(status=true).std:lastevent()"
		;

query2 = "INSERT INTO lastActive (deviceID, sensX, sensY, sensRadius, timestamp) "
		+ "SELECT a1.deviceID, a1.x, a1.y, a1.radius, a1.timestamp "
		+ "FROM pattern [ every a1=SensorEvent(status=true) -> "
		+ "(a2=SensorEvent(status=true) and not b1=SensorEvent(status=false and deviceID=a1.deviceID)) -> "
		+ "b2=SensorEvent(status=false AND deviceID=a2.deviceID) and not SensorEvent(status=true) ] "
		+ "ORDER BY a1.timestamp "
		+ "LIMIT 1"
		;

//query2 = "INSERT INTO lastActive (deviceID, sensX, sensY, sensRadius, timestamp) "
//		+ "SELECT a1.deviceID, a1.x, a1.y, a1.radius, a1.timestamp "
//		+ "FROM pattern [ "
//			+ "every a1 = SensorEvent(status=true) -> "
//			+ "every((a2 = SensorEvent(status=true) and not b1=SensorEvent(status=false and deviceID=a1.deviceID)) -> "
//			+ "b2=SensorEvent(status=false AND deviceID=a2.deviceID) and not SensorEvent(status=false AND deviceID=a1.deviceID)) ->"
//			+ "SensorEvent(status=false AND deviceID=a1.deviceID) "
//		+ "] "
//		+ "ORDER BY a1.timestamp "
//		+ "LIMIT 1"
//		;

String query3 = "INSERT INTO lastActive (deviceID, sensX, sensY, sensRadius, timestamp) "
	+ "Select st1.deviceID, st1.x, st1.y, st1.radius, st1.timestamp "
	+ "from PATTERN[every st1=SensorEvent(status=true) ->( st2=SensorEvent(status=true and deviceID!=st1.deviceID) "
	+ "and not SensorEvent )->( st3=SensorEvent(status=false and deviceID=st2.deviceID) "
	+ "and not SensorEvent)->( st4=SensorEvent(status=true) and not SensorEvent(status=false and deviceID=st1.deviceID) )->( "
	+ "SensorEvent(status=false and deviceID=st4.deviceID) and not SensorEvent)]";

// QUERY 1 PER SENSORE CIECO
String query5 = ""
	+ "SELECT loc.x AS locX, loc.y AS locY, loc.radius AS locRadius, sens.deviceID "
	+ "FROM lastActive.std:lastevent() as sens, LocationEvent.std:lastevent() as loc "
	+ "WHERE db2.esper.util.MathAlgorithm.doIntersect(sens.sensX, sens.sensY, sens.sensRadius, loc.x, loc.y, loc.radius) = false AND "
			+ "db2.esper.util.MathAlgorithm.existsWall(sens.sensX, sens.sensY, loc.x, loc.y) = false "
	;

// QUERY 1 IMPROVED
String query6 = ""
		+ "SELECT 'blindSensorFault', loc.x AS locX, loc.y AS locY, loc.radius AS locRadius, loc.timestamp, sens.deviceID "
		+ "FROM lastActive.std:lastevent() AS sens, LocationEvent.std:lastevent() AS loc "
		+ "WHERE (db2.esper.util.MathAlgorithm.doIntersect(sens.sensX, sens.sensY, sens.sensRadius, loc.x, loc.y, loc.radius) = false "
					+ "AND db2.esper.util.MathAlgorithm.existsWall(sens.sensX, sens.sensY, loc.x, loc.y) = false) "
		+ "OR (db2.esper.util.MathAlgorithm.doIntersect(sens.sensX, sens.sensY, sens.sensRadius, loc.x, loc.y, loc.radius) = true "
				+ "AND db2.esper.util.MathAlgorithm.existsWall(sens.sensX, sens.sensY, loc.x, loc.y) = true)"
		;

		Listener farAwayListener = new Listener(map);
		cep.getEPAdministrator().createEPL(query1);
		cep.getEPAdministrator().createEPL(query2);
		cep.getEPAdministrator().createEPL(query3);
//		cep.getEPAdministrator().createEPL("SELECT * FROM lastActive.std:lastevent()").addListener(farAwayListener);
//		cep.getEPAdministrator().createEPL(query4).addListener(farAwayListener); //Query finale copre A,B,C,D
//		cep.getEPAdministrator().createEPL(query5).addListener(farAwayListener); //Query sensore cieco versione basic
		cep.getEPAdministrator().createEPL(query6).addListener(farAwayListener); //Query sensore cieco versione pro		
		
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
		int position = i;
		if (position < dir.length)
			return ( filePattern.matcher( dir[position] ).find() ) ? dir[position] : lookForLOCFile(dir, filePattern, position + 1);
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
