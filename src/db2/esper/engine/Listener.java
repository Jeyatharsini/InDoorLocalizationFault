package db2.esper.engine;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import db2.esper.event.models.SensorEvent;
import db2.esper.graphic2d.Map;

public class Listener implements UpdateListener {
	Map map;
	
	public Listener(Map map) {
		this.map = map;
	}

	@Override
	public void update(EventBean[] arg0, EventBean[] arg1) {
		SensorEvent event = null;
		for (EventBean e : arg0) {
			System.out.println("Event received:" + e.getUnderlying());
			map.drawInPosition(
					(Double.valueOf(e.get("x").toString()).doubleValue()), 
					(Double.valueOf(e.get("y").toString()).doubleValue()), 
					(Double.valueOf(e.get("radius").toString()).doubleValue())
					);
		}
		
   	}
}
