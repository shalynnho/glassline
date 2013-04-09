package engine.agent;

import java.util.ArrayList;
import java.util.List;

import shared.Glass;
import shared.interfaces.LineComponent;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

public class BinRobotAgent extends Agent implements LineComponent {
	// *** Constructor(s) ***
	// Make sure to do setNextConveyorFamily upon creation
	public BinRobotAgent(String name, Transducer trans) {
		super(name, trans);
	}

	// *** DATA ***
	private List<Glass> glasses = new ArrayList<Glass>();
	private boolean posFree = true;
	private LineComponent next;
	
	// *** MESSAGES ***
	public void msgPositionFree() { // from first conveyor family
		print("Received msgPositionFree");
		posFree = true;
		stateChanged();
	}

	public void msgHereIsGlass(Glass g) {
		// dummy method, not used: just need this so we can implement LineComponent
	}
	
	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		if (posFree && !glasses.isEmpty()) {
			actPassOnGlass();
			return true;
		}
		return false;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// Optional: listen for BIN_PART_CREATED and *then* send msgHereIsGlass here
	}
	
	// *** ACTIONS ***
	private void actPassOnGlass() {
		print("Doing passOnGlass");
		doPassOnGlass();
		next.msgHereIsGlass(glasses.remove(0));
		posFree = false;
	}
	
	// *** ANIMATION ACTIONS ***
	private void doPassOnGlass() {
		transducer.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		// assumes this makes the part go to the next conveyor
	}

	// *** EXTRA ***
	public void setNextLineComponent(LineComponent l) {
		next = l;
	}

	// Seeds list of glasses with the given list
	public void seedGlasses(List<Glass> glasses) {
		this.glasses = glasses;
	}
}
