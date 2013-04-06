package engine.agent.evan.test.mock;

import engine.agent.evan.interfaces.*;
import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.Workstation;
import transducer.TChannel;

public class MockWorkstation extends MockAgent implements Workstation {
	
	public MockWorkstation(String name) {
		super(name);
	}
	
	//log messages
	public void msgHereIsGlass(Glass g) {
		log.add(new LoggedEvent(
				"Received message msgHereIsGlass for " + g.toString() + "."));
	}

	/* Don't need getters for testing. */
	public MachineType getType() { return null; }
	public TChannel getChannel() { return null; }
	public int getIndex() { return 0; }
}
