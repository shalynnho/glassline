package factory.agents;

import transducer.TChannel;
import transducer.TEvent;
import engine.agent.Agent;
import factory.interfaces.ConveyorFamily;

public class ConveyorFamilyAgent extends Agent implements ConveyorFamily {
	// *** Constructor(s) ***
	public ConveyorFamilyAgent() {
	}

	// *** DATA ***

	// *** MESSAGES ***
	@Override
	public void msgHereIsGlass() {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgPositionFree() {
		// TODO Auto-generated method stub
	}

	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		return false;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// TODO Auto-generated method stub
	}

	// *** ACTIONS ***

	// *** EXTRA ***
}
