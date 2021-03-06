package engine.agent.david.test.mock;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

public class MockTransducer extends Transducer { // not used
	public EventLog log = new EventLog();
	
	@Override
	public void fireEvent(TChannel channel, TEvent event, Object[] args) {
		log.add(new LoggedEvent("Fired event "+event.toString()+" on channel "+channel.toString()+" with args "+args));
	}
	
}
