package engine.agent;

import java.util.ArrayList;
import java.util.List;

import shared.Glass;
import transducer.TChannel;
import transducer.TEvent;


public class BinRobotAgent extends Agent {
	// *** DATA ***
	private List<Glass> glasses = new ArrayList<Glass>();

	
	// *** MESSAGES ***
	public void msgPositionFree() { // from first conveyor family
		
	}
	
	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// TODO Auto-generated method stub

	}

	
	// *** ACTIONS ***
	
	// *** ANIMATION ACTIONS ***
	
	// *** EXTRA ***
	// Seeds list of glasses with the given list
	public void seedGlasses(List<Glass> glasses) {
		this.glasses = glasses;
	}
}
