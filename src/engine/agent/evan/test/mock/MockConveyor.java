package engine.agent.evan.test.mock;

import shared.Glass;
import engine.agent.evan.interfaces.Conveyor;

public class MockConveyor extends MockAgent implements Conveyor {

	public MockConveyor(String name) {
		super(name);
	}
	
	//log messages
	public void msgHereIsGlass(Glass g) {
		log.add(new LoggedEvent(
				"Received message msgHereIsGlass for " + g.toString() + "."));
	}
	
	public void msgPositionFree() {
		log.add(new LoggedEvent(
				"Received message msgPositionFree."));
	}
}
