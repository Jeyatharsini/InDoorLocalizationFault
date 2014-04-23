package db2.esper.engine;

import java.awt.Color;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import db2.esper.graphic2d.Map;

public class Listener implements UpdateListener {
	Map map;
	
	public Listener(Map map) {
		this.map = map;
	}

	@Override
	public void update(EventBean[] arg0, EventBean[] arg1) {
		for (EventBean e : arg0) {
			System.out.println("Event received:" + e.getUnderlying());
			map.drawInPosition(
					(Double.valueOf(e.get("locX").toString()).doubleValue()), 
					(Double.valueOf(e.get("locY").toString()).doubleValue()), 
					(Double.valueOf(e.get("locRadius").toString()).doubleValue()),
					Color.RED);
			map.drawInPosition(
					(Double.valueOf(e.get("pircX").toString()).doubleValue()), 
					(Double.valueOf(e.get("pircY").toString()).doubleValue()), 
					(Double.valueOf(e.get("pircRadius").toString()).doubleValue()),
					Color.GREEN);
		}
		
   	}
}
