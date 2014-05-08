package db2.esper.engine;

import java.awt.Color;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
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
			
			double pircX = 0;
			double pircY = 0;
			double pircRadius = 0;
			try {
				pircX = (Double.valueOf(e.get("sensX").toString()).doubleValue());
				pircY = (Double.valueOf(e.get("sensY").toString()).doubleValue());
				pircRadius = (Double.valueOf(e.get("sensRadius").toString()).doubleValue());				
			} catch (PropertyAccessException e2) {
				pircX = 0;
				pircY = 0;
				pircRadius = 0;
			}
			
			if (pircX > 0) {
			map.drawInPosition(
					pircX, 
					pircY,
					pircRadius,
					Color.BLUE);
			}
			
			double locX = 0;
			double locY = 0;
			double locRadius = 0;
			try {
				locX = (Double.valueOf(e.get("locX").toString()).doubleValue());
				locY = (Double.valueOf(e.get("locY").toString()).doubleValue());
				locRadius = (Double.valueOf(e.get("locRadius").toString()).doubleValue());				
			} catch (PropertyAccessException e2) {
				locX = 0;
				locY = 0;
				locRadius = 0;
			}
			if (locX > 0) {
				map.drawInPosition(
						locX, 
						locY, 
						locRadius,
						Color.RED);
			}
		}
		
   	}
}
