package engine.agent;

import java.util.ArrayList;
import java.util.List;

import shared.Glass;
import shared.interfaces.ConveyorFamily;
import transducer.TChannel;
import transducer.TEvent;

public class BinRobotAgent extends Agent {
	// *** Constructor(s) ***
	// Make sure to do setNextConveyorFamily upon creation
	public BinRobotAgent() {}

	// *** DATA ***
	private List<Glass> glasses = new ArrayList<Glass>();
	private boolean posFree = false;
	private ConveyorFamily next;

	
	// *** MESSAGES ***
	public void msgPositionFree() { // from first conveyor family
		posFree = true;
		stateChanged();
	}
	
	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		if (posFree && !glasses.isEmpty()) {
			passOnGlass();
			return true;
		}
		return false;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// Optional: listen for BIN_PART_CREATED and *then* send msgHereIsGlass here
	}
	
	// *** ACTIONS ***
	private void passOnGlass() {
		doPassOnGlass();
		next.msgHereIsGlass(glasses.remove(0));
		posFree = false;
	}
	
	// *** ANIMATION ACTIONS ***
	private void doPassOnGlass() {
		transducer.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
	}

	// *** EXTRA ***
	public void setNextConveyorFamily(ConveyorFamily f) {
		next = f;
	}

	// Seeds list of glasses with the given list
	public void seedGlasses(List<Glass> glasses) {
		this.glasses = glasses;
	}
}
