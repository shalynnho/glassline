package factory.test.mock;

import shared.Glass;
import factory.interfaces.Conveyor;

public class MockConveyor extends MockEntity implements Conveyor {
	public EventLog log = new EventLog();
	
	public MockConveyor(String name) {
		super(name);
	}

	@Override
	public void msgHereIsGlass(Glass g) {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgTakingGlass() {
		// TODO Auto-generated method stub

	}

	@Override
	public void actTellPopupGlassOnConveyor(Glass g) {
		
	}
	
	@Override
	public void actTellSensorPositionFree() {
		
	}

}
