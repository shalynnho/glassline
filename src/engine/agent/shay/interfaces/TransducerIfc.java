package engine.agent.shay.interfaces;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;

public interface TransducerIfc {
	
	public void register(TReceiver toRegister, TChannel channel);
	
	public void fireEvent(TChannel channel, TEvent event, Object[] args);

}
