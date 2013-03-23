package factory.test.mock;

import factory.interfaces.Sensor;

public class MockSensor extends MockEntity implements Sensor {

	public MockSensor(String name) {
		super(name);
	}

	@Override
	public void msgHereIsGlass() {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgPositionFree() {
		// TODO Auto-generated method stub

	}

	@Override
	public void actPassOnGlass() {
		// TODO Auto-generated method stub

	}

}
