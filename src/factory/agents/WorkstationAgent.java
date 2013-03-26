package factory.agents;

import shared.Glass;
import shared.enums.MachineType;
import transducer.TChannel;
import factory.interfaces.Workstation;

/**
 * Temporarily stubbed class (not really an agent, but might be) that would contain RobotAgent and MachineAgent concepts.
 * May be moved to misc later if these internal agents become needed.
 */
public class WorkstationAgent implements Workstation {
	// *** Constructor(s) ***

	// *** DATA ***
	private MachineType type;

	// *** MESSAGES ***
	public void msgHereIsGlass(Glass g) {
		// later
	}

	// *** SCHEDULER ***

	// *** ACTIONS ***

	// *** EXTRA ***
	public TChannel getChannel() {
		return type.getChannel();
	}
	
	public MachineType getType() {
		return type;
	}
}
