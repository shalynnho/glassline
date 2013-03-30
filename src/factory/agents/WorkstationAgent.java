package factory.agents;

import shared.Glass;
import shared.enums.MachineType;
import transducer.TChannel;
import factory.interfaces.Workstation;

/**
 * Temporarily stubbed class that would contain the RobotAgent and MachineAgent concepts.
 */
public class WorkstationAgent implements Workstation {
	// *** Constructor(s) ***
	public WorkstationAgent(MachineType type, int index) {
		this.type = type;
		this.index = index;
	}

	// *** DATA ***
	private MachineType type;
	private int index;

	// *** MESSAGES ***
	public void msgHereIsGlass(Glass g) {
		// later
	}

	// *** SCHEDULER ***

	// *** ACTIONS ***

	// *** TRANSDUCER / ANIMATION CALLS ***
	
	// *** EXTRA ***
	public TChannel getChannel() {
		return type.getChannel();
	}
	
	public MachineType getType() {
		return type;
	}

	public int getIndex() {
		return index;
	}
}
