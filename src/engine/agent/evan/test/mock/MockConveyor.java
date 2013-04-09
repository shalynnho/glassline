package engine.agent.evan.test.mock;

import shared.Glass;
import shared.interfaces.LineComponent;

public class MockConveyor extends MockAgent implements LineComponent {

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
