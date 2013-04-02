package engine.agent.evan.test.mock;

import shared.Glass;
import engine.agent.evan.interfaces.Popup;

public class MockPopup extends MockAgent implements Popup {

	public MockPopup(String name) {
		super(name);
	}
	
	//log messages
	public void msgNextGlass(Glass g) {
		log.add(new LoggedEvent(
				"Received message msgNextGlass for " + g.toString() + "."));
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
