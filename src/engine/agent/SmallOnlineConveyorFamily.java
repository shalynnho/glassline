package engine.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import shared.Glass;
import shared.interfaces.ConveyorFamily;
import transducer.TChannel;
import transducer.TEvent;

// INCOMPLETE
public class SmallOnlineConveyorFamily extends Agent {
	// *** Constructor(s) ***
	public SmallOnlineConveyorFamily(int index) {
		this.conveyorIndex = index;

	}

	// *** DATA ***
	private int conveyorIndex;
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
			actPutGlassOnMachineAndPassOn();
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
	}

	// *** ACTIONS ***
	public void actPutGlassOnMachineAndPassOn() {
		Glass g = glasses.remove(0);
		doPutGlassOnMachine(g);
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
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, new Integer[]{conveyorIndex});
	}

	private void doStopConveyor() {
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, new Integer[]{conveyorIndex});
	}

	private void doPutGlassOnMachine(Glass g) {

	}

	private void doPassOnGlass(Glass g) {

	}

	// *** EXTRA ***
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
