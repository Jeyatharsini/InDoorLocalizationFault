package db2.esper.engine;
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

public class EsperEngine {

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		
		Configuration cepConfig = null;
		cepConfig = new Configuration();
		
		cepConfig.addEventType("PirwEvent", PirwEvent.class.getName());
		cepConfig.addEventType("PircEvent", PircEvent.class.getName());
		cepConfig.addEventType("DwcEvent", DwcEvent.class.getName());
		cepConfig.addEventType("LocationEvent", LocationEvent.class.getName());

		EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEP",
				cepConfig);
		EPRuntime cepRT = cep.getEPRuntime();
		
		//GENERO GLI STREAM PER CATEGORIA DI SENSORE
		Listener mylistener = new Listener();
		String query = null;
		query = "INSERT INTO pirwEPL SELECT * FROM PirwEvent ";
		EPStatement pirwEPL= cep.getEPAdministrator().createEPL(query);
		//pirwEPL.addListener(mylistener);
		
		query = "INSERT INTO pircEPL SELECT * FROM PircEvent ";
		EPStatement pircEPL = cep.getEPAdministrator().createEPL(query);
		//pircEPL.addListener(mylistener);
		
		query = "INSERT into dwcEPL SELECT * FROM DwcEvent ";
		EPStatement dwcEPL = cep.getEPAdministrator().createEPL(query);
		//dwcEPL.addListener(mylistener);
		
		query = "INSERT into locationEPL SELECT * FROM LocationEvent ";
		EPStatement locationEPL = cep.getEPAdministrator().createEPL(query);
		
		
		//QUI CI VANNO LE QUERY PER TROVARE LE ANOMALIE
		query = "SELECT * FROM locationEPL";
		EPStatement onlyTrue = cep.getEPAdministrator().createEPL(query);
		onlyTrue.addListener(mylistener);
		
		// The Configuration is meant only as an initialization-time object.

		
//		EPAdministrator cepAdm = cep.getEPAdministrator();
		
//		EPStatement cepStatement =	cepAdm.createEPL(query);
//		cepStatement.addListener(new CEPListener());
		
		String file[] = new String[1];
		//file[0] = "data/A/stateDump.txt";
		file[0] = "data/A/LOC1395236845939.log";

		EventGenerator evGen = new EventGenerator(cepRT,file);
		
		evGen.start();
		
	}
}
