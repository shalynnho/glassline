package engine.agent.evan.test.mock;

import shared.Glass;
import shared.interfaces.OfflineConveyorFamily;

public class MockConveyorFamily extends MockAgent implements OfflineConveyorFamily {
	
	public MockConveyorFamily(String name) {
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
	
	public void msgGlassDone(Glass g, int index) {
		log.add(new LoggedEvent(
				"Received message msgGlassDone for " + g.toString() + " and index " + index + "."));
	}
}
