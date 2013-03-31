package factory.test.mock;

import shared.Glass;
import shared.enums.MachineType;
import transducer.TChannel;
import factory.interfaces.Workstation;


public class MockWorkstation extends MockEntity implements Workstation {
	public EventLog log = new EventLog();

	public MockWorkstation(String name) {
		super(name);
	}

	public void msgHereIsGlass(Glass g) {
		log.add(new LoggedEvent("Received msgHereIsGlass"));
	}

	@Override
	public MachineType getType() {
		return MachineType.DRILL;
	}

	@Override
	public TChannel getChannel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}
}
