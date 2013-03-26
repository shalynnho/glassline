package factory.test.mock;

import shared.Glass;
import shared.enums.MachineType;
import transducer.TChannel;
import factory.interfaces.Workstation;


public class MockWorkstation extends MockEntity implements Workstation {
	public EventLog log = new EventLog();

	public MockWorkstation(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public void msgHereIsGlass(Glass g) {
	}

	@Override
	public MachineType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TChannel getChannel() {
		// TODO Auto-generated method stub
		return null;
	}
}
