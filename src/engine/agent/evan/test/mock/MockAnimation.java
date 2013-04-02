package engine.agent.evan.test.mock;

import transducer.*;

public class MockAnimation extends MockAgent implements TReceiver {
	
	public MockAnimation(String name, Transducer t, TChannel chs[]) {
		super(name);
		for (TChannel ch : chs) // register to specified channels
			t.register(this, ch);
	}
	
	//log all transducer events
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		log.add(new LoggedEvent(
				"Channel: " + channel.toString() + ", Event: " + event.toString() + ", Arguments: " + (Integer)args[0] + "."));
		//args is always 1 Integer during testing
	}
}
