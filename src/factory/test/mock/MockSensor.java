package factory.test.mock;

import shared.Glass;
import transducer.Transducer;
import factory.interfaces.Sensor;
import factory.misc.ConveyorFamilyEntity;

public class MockSensor extends MockEntity implements Sensor {
	public MockSensor(String name) {
		super(name);
	}
	public MockSensor(String name, ConveyorFamilyEntity f, Transducer transducer) {
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
