package db2.esper.engine;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class Listener implements UpdateListener {

	@Override
	public void update(EventBean[] arg0, EventBean[] arg1) {
		for (EventBean e : arg0){
			System.out.println("Event received:" + e.getUnderlying());
			}
	   	}
}
