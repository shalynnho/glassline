package engine.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import shared.Glass;
import shared.interfaces.LineComponent;
import shared.interfaces.NonnormBreakInteraction;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

public class TruckAgent extends Agent implements LineComponent, NonnormBreakInteraction {
	// *** Constructor(s) ***
	// Make sure to do setNextConveyorFamily upon creation
	public TruckAgent(String name, Transducer trans) {
		super(name, trans);
		animSem = new Semaphore[2]; // [0] -> load, [1] -> empty
		for (int i = 0; i < 2; ++i)
			animSem[i] = new Semaphore(0);
		
		transducer.register(this, TChannel.TRUCK);
		loadFinished = alreadyTold = false;
		glasses = new ArrayList<Glass>();
	}
	
	// *** DATA ***
	private List<Glass> glasses;
	private GeneralConveyorAgent prev;
	private boolean alreadyTold; // true if already told previous family that position is free (to prevent repeated messaging)
	private boolean loadFinished; // true when done loading
	private Semaphore animSem[];
	
	private static final int maxGlass = 1;
	
	private enum GUIBreakState {stop, stopped, restart, running};
	GUIBreakState guiBreakState = GUIBreakState.running; // Value that determines whether the GUI truck is broken or not	
	
	// *** MESSAGES ***
	public void msgPositionFree() {
		// dummy method, not used: just need this so we can implement LineComponent
	}
	
	public void msgHereIsGlass(Glass g) {
		glasses.add(g);
		stateChanged();
	}
	
	/* React to events on TRUCK channel. */
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (event == TEvent.TRUCK_GUI_LOAD_FINISHED) {
			animSem[0].release();
			alreadyTold = false;
			if (glasses.size() == maxGlass)
				loadFinished = true;
			stateChanged();
		} else if (event == TEvent.TRUCK_GUI_EMPTY_FINISHED) {
			animSem[1].release();
		}
	}

	/* This message is from the GUI to stop or restart. */
	public void msgGUIBreak(boolean stop) {
		if (stop && guiBreakState == GUIBreakState.running) {
			guiBreakState = GUIBreakState.stop;
			stateChanged();
		} 
		else if (!stop && guiBreakState == GUIBreakState.stopped) {
			guiBreakState = GUIBreakState.restart;
			stateChanged();
		}
	}
	
	// *** SCHEDULER ***
	public boolean pickAndExecuteAnAction() {
		// Check to see if a GUI break message came in
		if (guiBreakState == GUIBreakState.stop) {
			actBreakTruckOff();
			return false; // Make sure the method does not call again
		}
		
		else if (guiBreakState == GUIBreakState.restart) {
			actBreakTruckOn();
			return true; 		
		}
		
		else if (guiBreakState == GUIBreakState.stopped) {
			return false; // C'mon the Truck is broken!  It shouldn't run until this state is changed
		}
		
		if (loadFinished || (!glasses.isEmpty() && prev.isEmpty())) {
			actLoadAndEmptyTruck();
			return true;
		} else if (!alreadyTold && glasses.size() < maxGlass) {
			actTellPrevPosFree();
			return true;
		}
		return false;
	}
	
	// *** ACTIONS ***
	private void actTellPrevPosFree() {
		alreadyTold = true;
		prev.msgPositionFree();
	}

	private void actLoadAndEmptyTruck() {
		doLoadGlass();
		doEmptyTruck();
		glasses.clear();
		loadFinished = false;
	}
	
	// New Non-norm GUI actions
	private void actBreakTruckOff() {
		guiBreakState = GUIBreakState.stopped; 
	}
	
	private void actBreakTruckOn() {
		guiBreakState = GUIBreakState.running;
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
		if (l instanceof GeneralConveyorAgent)
			prev = (GeneralConveyorAgent)l;
	}
}
