package factory.test.mock;

import shared.Glass;
import factory.interfaces.Sensor;

public class MockSensor extends MockEntity implements Sensor {

	public MockSensor(String name) {
		super(name);
	}

	@Override
	public void msgHereIsGlass(Glass g) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgPositionFree() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actPassOnGlass(Glass g) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actTellPrevFamilyPositionFree() {
		// TODO Auto-generated method stub
		
	}

}
