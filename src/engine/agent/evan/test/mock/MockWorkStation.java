package engine.agent.evan.test.mock;

import engine.agent.evan.interfaces.*;
import shared.Glass;

public class MockWorkStation extends MockAgent implements WorkStation {
	
	
	public MockWorkStation(String name) {
		super(name);
	}
	
	//log messages
	public void msgHereIsGlass(Glass g) {
		log.add(new LoggedEvent(
				"Received message msgHereIsGlass for " + g.toString() + "."));
	}
}
