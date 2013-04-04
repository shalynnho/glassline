package engine.agent.david.test.mock;

import shared.Glass;
import shared.enums.MachineType;
import transducer.TChannel;
import engine.agent.david.interfaces.Workstation;


public class MockWorkstation extends MockEntity implements Workstation {
	public MockWorkstation(String name) {
		super(name);
	}

	public void msgHereIsGlass(Glass g) {
		log.add(new LoggedEvent("Received msgHereIsGlass"));
	}

	public MachineType getType() {
		return MachineType.DRILL;
	}

	public TChannel getChannel() {
		return getType().getChannel();
	}

	public int getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}
}
