package engine.agent.david.test.mock;

import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.OfflineWorkstation;
import transducer.TChannel;


public class MockWorkstation extends MockEntity implements OfflineWorkstation {
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
