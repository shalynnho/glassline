package engine.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import shared.Glass;
import shared.interfaces.ConveyorFamily;
import shared.interfaces.LineComponent;
import shared.interfaces.Workstation;
import transducer.TChannel;
import transducer.TEvent;

public class SmallConveyorAgent extends Agent implements LineComponent {
	// *** Constructor(s) ***
	// Make sure to do setNextLineComponent, etc. upon creation
	public SmallConveyorAgent(int cIndex, Workstation w) {
		this.conveyorIndex = cIndex;
		this.workstation = w;

		// Animation delay semaphores
		animSem = new Semaphore[2]; // index 0 -> WORKSTATION_LOAD_FINISHED, 1 -> WORKSTATION_GUI_ACTION_FINISHED
		for (int i=0; i<animSem.length; i++) {
			animSem[i] = new Semaphore(0);
		}
	}

	// *** DATA ***
	private int conveyorIndex;
	private Workstation workstation;
	
	private boolean posFree = false;
	private boolean sensorReached = false;
	private boolean started = false;
	private List<Glass> glasses = Collections.synchronizedList(new ArrayList<Glass>());
	private ConveyorFamily prev, next;

	private Semaphore animSem[];

	// *** MESSAGES ***
	public void msgHereIsGlass(Glass glass) {
		glasses.add(glass);
		stateChanged();
	}

	public void msgPositionFree() {
		posFree = true;
		stateChanged();
	}

	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		if (posFree && !glasses.isEmpty() && sensorReached) {
			actLoadGlassOntoWorkstationAndPassOn();
			return true;
		} else if (!glasses.isEmpty() && !sensorReached && !started) {
			actStartConveyor();
			return true;
		}
		return false;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_RELEASED) {
			// When the glass moves past the 1st sensor
			if (thisSensor1(args)) {
				prev.msgPositionFree();
			}
			// When the glass moves past the 2nd sensor
			else if (thisSensor2(args)) {
				sensorReached = false;
			}
		} else if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED) {
			// When the glass reaches the 2nd sensor
			if (thisSensor2(args)) {
				sensorReached = true;
				doStopConveyor();
			}
		}
		
		// Listen for workstation events so you know when an animation is done
		if (channel == workstation.getChannel() && event == TEvent.WORKSTATION_LOAD_FINISHED) {
			animSem[0].release();
		} else if (channel == workstation.getChannel() && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
			animSem[1].release();
		}
		
	}

	// *** ACTIONS ***
	public void actLoadGlassOntoWorkstationAndPassOn() {
		Glass g = glasses.remove(0);
		doLoadGlassOntoWorkstation();
		doPassOnGlass(g);
		next.msgHereIsGlass(g);
		posFree = false;
	}

	public void actStartConveyor() {
		started = true;
		doStartConveyor();
	}

	// *** ANIMATION ACTIONS ***
	private void doStartConveyor() {
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, new Integer[] { conveyorIndex });
	}

	private void doStopConveyor() {
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, new Integer[] { conveyorIndex });
	}

	private void doLoadGlassOntoWorkstation() {
		transducer.fireEvent(workstation.getChannel(), TEvent.WORKSTATION_DO_LOAD_GLASS, null);
		doWaitAnimation(0); // wait until workstation done loading, i.e., WORKSTATION_LOAD_FINISHED
		
		transducer.fireEvent(workstation.getChannel(), TEvent.WORKSTATION_DO_ACTION, null);
		doWaitAnimation(1); // wait until workstation done action, i.e., WORKSTATION_GUI_ACTION_FINISHED
	}

	private void doPassOnGlass(Glass g) {
		transducer.fireEvent(workstation.getChannel(), TEvent.WORKSTATION_RELEASE_GLASS, null);
	}

	// *** EXTRA ***
	public void setNextLineComponent(ConveyorFamily f) {
		next = f;
	}

	public void setPreviousLineComponent(ConveyorFamily f) {
		prev = f;
	}

	public int getSensor1Index() {
		return conveyorIndex * 2; // returns 1st sensor
	}

	public int getSensor2Index() {
		return conveyorIndex * 2 + 1; // returns 2nd sensor
	}

	public int getConveyorIndex() {
		return conveyorIndex;
	}

	// Quick helpers for parsing args in eventFired
	public boolean thisSensor1(Object args[]) {
		return (Integer) args[0] == getSensor1Index();
	}

	public boolean thisSensor2(Object args[]) {
		return (Integer) args[0] == getSensor2Index();
	}

	public boolean thisConveyor(Object args[]) {
		return (Integer) args[0] == getConveyorIndex();
	}

	// Wait on an animation action using a semaphore acquire
	private void doWaitAnimation(int i) {
		try {
			animSem[i].acquire(); // wait for animation action to finish
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
