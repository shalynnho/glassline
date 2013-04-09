package engine.agent.david.test.mock;

import shared.Glass;
import shared.interfaces.OfflineConveyorFamily;

public class MockConveyorFamily extends MockEntity implements OfflineConveyorFamily {
	public MockConveyorFamily(String name) {
		super(name);
	}

	@Override
	public void msgHereIsGlass(Glass g) {
		log.add(new LoggedEvent("Received msgHereIsGlass"));
	}

	@Override
	public void msgPositionFree() {
		log.add(new LoggedEvent("Received msgPositionFree"));
	}
	
	@Override
	public void msgGlassDone(Glass glass, int machineIndex) {
		log.add(new LoggedEvent("Received msgGlassDone"));
	}
}
