package factory.test.mock;

import java.util.List;

import shared.Glass;
import transducer.Transducer;
import factory.agents.SensorAgent.SensorState;
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
		log.add(new LoggedEvent("Received msgHereIsGlass"));
	}

	@Override
	public void msgPositionFree() {
		log.add(new LoggedEvent("Received msgPositionFree"));
	}

	@Override
	public void actPassOnGlass(Glass g) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actTellPrevFamilyPositionFree() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean pickAndExecuteAnAction() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public List<Glass> getGlasses() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SensorState getState() {
		// TODO Auto-generated method stub
		return null;
	}

}
