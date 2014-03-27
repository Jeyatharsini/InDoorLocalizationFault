package db2.esper.engine;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

import db2.esper.event.models.LocationEvent;
import db2.esper.event.models.SensorEvent;
import db2.esper.events.EventGenerator;

public class EsperEngine {

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

		/*
		// examples of streams and queries

		final int LOOK_FOR_SPECIFIC_EVENTS = 0;
		final int AGGREGATE_SEVERAL_EVENTS = 1;
		final int LOGICAL_SLIDING_WINDOW = 2;
		final int PHYSICAL_SLIDING_WINDOW = 3;
		final int LOGICAL_TUMBLING_WINDOW = 4;
		final int PHYSICAL_TUMBLING_WINDOW = 5;
		final int LOGICAL_SLIDING_WINDOW_CONTROLLED_REPORTING = 6;
		final int PHYSICAL_SLIDING_WINDOW_CONTROLLED_REPORTING = 7;
		final int JOINING_TWO_STREAMS = 8;
		
		final int PATTERN_FILTER_EXPRESSION = 9;
		final int PATTERN_COMPLEX_EXPRESSION_WITHIN = 10;
		final int INSERT_COMPLEX_EVENT =11;
		
		final int COMBINING_PATTERNS_MATCHING_WITH_EVENT_STREAM_ANALYSIS = 12;
		
		int key = INSERT_COMPLEX_EVENT;
		
		switch (key) {
		case LOOK_FOR_SPECIFIC_EVENTS:
			cepConfig = new Configuration();
			cepConfig.addEventType("TemperatureEventStream", TemperatureSensorEvent.class.getName());
			query = "select * from TemperatureEventStream where temperature> 50";
			break;
		case AGGREGATE_SEVERAL_EVENTS:
			cepConfig = new Configuration();
			cepConfig.addEventType("TemperatureEventStream", TemperatureSensorEvent.class.getName());
			query = "select avg(temperature) from TemperatureEventStream";
			break;
		case LOGICAL_SLIDING_WINDOW:
			cepConfig = new Configuration();
			cepConfig.addEventType("TemperatureEventStream", TemperatureSensorEvent.class.getName());
			query = "select avg(temperature) from TemperatureEventStream.win:time(4 sec)";
			break;
		case PHYSICAL_SLIDING_WINDOW:
			cepConfig = new Configuration();
			cepConfig.addEventType("TemperatureEventStream", TemperatureSensorEvent.class.getName());
			query = "select avg(temperature) from TemperatureEventStream.win:length(5)";
			break;
		case LOGICAL_TUMBLING_WINDOW:
			cepConfig = new Configuration();
			cepConfig.addEventType("TemperatureEventStream", TemperatureSensorEvent.class.getName());
			query = "select avg(temperature) from TemperatureEventStream.win:time_batch(4 sec)";
			break;
		case PHYSICAL_TUMBLING_WINDOW:
			cepConfig = new Configuration();
			cepConfig.addEventType("TemperatureEventStream", TemperatureSensorEvent.class.getName());
			query = "select avg(temperature) from TemperatureEventStream.win:length_batch(5)";
			break;
		case LOGICAL_SLIDING_WINDOW_CONTROLLED_REPORTING:
			cepConfig = new Configuration();
			cepConfig.addEventType("TemperatureEventStream", TemperatureSensorEvent.class.getName());
			query = "select avg(temperature) from TemperatureEventStream.win:time(4 sec) output snapshot every 2 sec";
			break;
		case PHYSICAL_SLIDING_WINDOW_CONTROLLED_REPORTING:
			cepConfig = new Configuration();
			cepConfig.addEventType("TemperatureEventStream", TemperatureSensorEvent.class.getName());
			query = "select avg(temperature) from TemperatureEventStream.win:length(4) output snapshot every 2 events";
			break;
		case JOINING_TWO_STREAMS:
			cepConfig = new Configuration();
			cepConfig.addEventType("TemperatureEventStream", TemperatureSensorEvent.class.getName());
			cepConfig.addEventType("SmokeEventStream", SmokeSensorEvent.class.getName());
			query = "select Tstream.sensor, Tstream.temperature, Sstream.smoke " +
					"from TemperatureEventStream.win:length(4) as Tstream, " +
					"     SmokeEventStream.win:length(4) as Sstream " +
					"where Tstream.sensor = Sstream.sensor and Tstream.temperature>50 and Sstream.smoke=true ";
			break;
		case PATTERN_FILTER_EXPRESSION:
			cepConfig = new Configuration();
			cepConfig.addEventType("TemperatureEventStream", TemperatureSensorEvent.class.getName());
			query = "select count(*) " +
					"from pattern [every TemperatureEventStream(sensor=\"S0\", temperature>50) ]";
			break;
		case PATTERN_COMPLEX_EXPRESSION_WITHIN:
			cepConfig = new Configuration();
			cepConfig.addEventType("TemperatureEventStream", TemperatureSensorEvent.class.getName());
			cepConfig.addEventType("SmokeEventStream", SmokeSensorEvent.class.getName());
			query = "select a.sensor " +
					"from pattern [every ( a = SmokeEventStream(smoke=true) -> TemperatureEventStream(temperature>50, sensor=a.sensor) where timer:within(2 sec) ) ] " +
					"";
			break;
		case INSERT_COMPLEX_EVENT:
			cepConfig = new Configuration();
			cepConfig.addEventType("TemperatureEventStream", TemperatureSensorEvent.class.getName());
			cepConfig.addEventType("SmokeEventStream", SmokeSensorEvent.class.getName());
			cepConfig.addEventType("FireStream", FireComplexEvent.class.getName());
			query = "insert into FireStream " +
					"select a.sensor as sensor, a.smoke as smoke, b.temperature as temperature " +
					"from pattern [every ( a = SmokeEventStream(smoke=true) -> b = TemperatureEventStream(temperature>5, sensor=a.sensor) where timer:within(2 sec) ) ] " +
					"";
			queryDownStream = "select count(*) from FireStream.win:time(10 sec)";
			break;
			
		case COMBINING_PATTERNS_MATCHING_WITH_EVENT_STREAM_ANALYSIS:
			cepConfig = new Configuration();
			cepConfig.addEventType("TemperatureEventStream", TemperatureSensorEvent.class.getName());
			cepConfig.addEventType("SmokeEventStream", SmokeSensorEvent.class.getName());
			cepConfig.addEventType("FireStream", FireComplexEvent.class.getName());
			query = "select count(*) " +
					"from pattern [every ( a = SmokeEventStream(smoke=true) -> b = TemperatureEventStream(temperature>50, sensor=a.sensor) where timer:within(4 sec) ) ].win:time(10 sec) " +
					"";
			break;
			
		}
*/
		
		Configuration cepConfig = null;
		cepConfig = new Configuration();
		
		cepConfig.addEventType("SensorStream", SensorEvent.class.getName());
		cepConfig.addEventType("LocationStream", LocationEvent.class.getName());

//		String query = null;
		
		// The Configuration is meant only as an initialization-time object.

		EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEP",
				cepConfig);
		EPRuntime cepRT = cep.getEPRuntime();
//		EPAdministrator cepAdm = cep.getEPAdministrator();
		
//		EPStatement cepStatement =	cepAdm.createEPL(query);
//		cepStatement.addListener(new CEPListener());
		
		
		EventGenerator evGen = new EventGenerator(cepRT,args);
		
		evGen.start();
		
	}
}
