package engine.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import shared.Glass;
import shared.interfaces.ConveyorFamily;
import shared.interfaces.Workstation;
import transducer.TChannel;
import transducer.TEvent;

// INCOMPLETE
public class SmallOnlineConveyorFamily extends Agent {
	// *** Constructor(s) ***
	// Make sure to do setNextConveyorFamily, etc. upon creation
	public SmallOnlineConveyorFamily(int cIndex, Workstation w) {
		this.conveyorIndex = cIndex;
		this.workstation = w;
	}

	// *** DATA ***
	private int conveyorIndex;
	private Workstation workstation;
	
	private boolean posFree = false;
	private boolean sensorReached = false;
	private boolean started = false;
	private List<Glass> glasses = Collections.synchronizedList(new ArrayList<Glass>());
	private ConveyorFamily prev, next;

	private Semaphore animSem[]; // not used yet

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
			// When the glass passes the 1st sensor
			if (thisSensor1(args)) {
				prev.msgPositionFree();
			}
			// When the glass passes the 2nd sensor
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
		
		// TODO: listen for workstation events and work animSem
		
		
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
		transducer.fireEvent(workstation.getChannel(), TEvent.WORKSTATION_DO_LOAD_GLASS, new Integer[] { workstation.getIndex() });
		// TODO: wait until workstation done loading
		transducer.fireEvent(workstation.getChannel(), TEvent.WORKSTATION_DO_ACTION, new Integer[] { workstation.getIndex() });
		// TODO: wait until workstation done action
	}

	private void doPassOnGlass(Glass g) {
		transducer.fireEvent(workstation.getChannel(), TEvent.WORKSTATION_RELEASE_GLASS, new Integer[] { workstation.getIndex() });
	}

	// *** EXTRA ***
	public void setNextConveyorFamily(ConveyorFamily f) {
		next = f;
	}

	public void setPreviousConveyorFamily(ConveyorFamily f) {
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
}
