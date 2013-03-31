package factory.test.mock;

import shared.Glass;
import shared.interfaces.ConveyorFamily;

public class MockConveyorFamily extends MockEntity implements ConveyorFamily {
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
