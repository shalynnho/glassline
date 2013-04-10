package engine.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import shared.Glass;
import shared.interfaces.LineComponent;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

public class TruckAgent extends Agent implements LineComponent {
	// *** Constructor(s) ***
	// Make sure to do setNextConveyorFamily upon creation
	public TruckAgent(String name, Transducer trans) {
		super(name, trans);
		animSem = new Semaphore[2]; // [0] -> load, [1] -> empty
	}
	
	// *** DATA ***
	private List<Glass> glasses = new ArrayList<Glass>();
	private LineComponent prev;
	private boolean alreadyTold = false; // true if already told previous family that position is free (to prevent repeated messaging)
	private Semaphore animSem[];
	
	// *** MESSAGES ***
	public void msgPositionFree() {
		// dummy method, not used: just need this so we can implement LineComponent
	}
	
	public void msgHereIsGlass(Glass g) {
		alreadyTold = false;
		glasses.add(g);
		stateChanged();
	}
	
	// *** SCHEDULER ***
	public boolean pickAndExecuteAnAction() {
		if (!glasses.isEmpty()) {
			actLoadAndEmptyTruck();
			return true;
		} else if (!alreadyTold && glasses.isEmpty()) {
			actTellPrevPosFree();
			return true;
		}
		return false;
	}
	
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (channel == TChannel.TRUCK) {
			if (event == TEvent.TRUCK_GUI_LOAD_FINISHED) {
				animSem[0].release();
			} else if (event == TEvent.TRUCK_GUI_EMPTY_FINISHED) {
				animSem[1].release();
			}
		}
	}
	
	// *** ACTIONS ***
	private void actTellPrevPosFree() {
		alreadyTold = true;
		prev.msgPositionFree();
	}

	private void actLoadAndEmptyTruck() {
		doLoadGlass();
		doEmptyTruck();
		glasses.remove(0);
	}
	
	// *** ANIMATION ACTIONS ***
	private void doLoadGlass() {
		transducer.fireEvent(TChannel.TRUCK, TEvent.TRUCK_DO_LOAD_GLASS, null);
		doWaitAnimation(0);
	}

	private void doEmptyTruck() {
		transducer.fireEvent(TChannel.TRUCK, TEvent.TRUCK_DO_EMPTY, null);
		doWaitAnimation(1);
	}

	// Wait on an animation action using a semaphore acquire
	private void doWaitAnimation(int i) {
		try {
			animSem[i].acquire(); // wait for animation action to finish
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// *** EXTRA ***
	public void setPrevLineComponent(LineComponent l) {
		prev = l;
	}


}