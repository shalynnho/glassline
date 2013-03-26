package factory.test.mock;

import shared.Glass;
import shared.interfaces.ConveyorFamily;

public class MockConveyorFamily implements ConveyorFamily {
	public EventLog log = new EventLog();
	
	@Override
	public void msgHereIsGlass(Glass g) {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgPositionFree() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void msgGlassDone(Glass glass, int machineIndex) {
		
	}
}
