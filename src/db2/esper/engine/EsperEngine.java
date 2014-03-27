package db2.esper.engine;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

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

//		String query = null;
		
		// The Configuration is meant only as an initialization-time object.

		EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEP",
				cepConfig);
		EPRuntime cepRT = cep.getEPRuntime();
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
