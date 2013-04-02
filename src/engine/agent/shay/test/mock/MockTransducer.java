package engine.agent.shay.test.mock;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import engine.agent.shay.interfaces.TransducerIfc;

public class MockTransducer implements TransducerIfc  {
	
	public EventLog log = new EventLog();

	@Override
	public void register(TReceiver toRegister, TChannel channel) {
		log.add(new LoggedEvent(
				"Transducer received call to register TReciever: " + toRegister + " on TChannel: " +
				channel + "."));
	}	

	@Override
	public void fireEvent(TChannel channel, TEvent event, Object[] args) {
		log.add(new LoggedEvent(
				"Transducer received call to fireEvent on TChannel: " +
				channel + " for TEvent: " + event + ", passing args: " + args));
	}

}
