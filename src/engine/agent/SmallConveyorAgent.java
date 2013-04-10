package engine.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import shared.Glass;
import shared.interfaces.LineComponent;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

public class SmallConveyorAgent extends Agent implements LineComponent {
	// *** Constructor(s) ***
	// Make sure to do setNextLineComponent, etc. upon creation
	public SmallConveyorAgent(String name, Transducer trans, SmallOnlineConveyorFamilyImp cf, int cIndex) {
		super(name, trans);
		this.family = cf;
		this.conveyorIndex = cIndex;
		
		transducer.register(this, TChannel.SENSOR);
	}

	// *** DATA ***
	private SmallOnlineConveyorFamilyImp family;
	private int conveyorIndex;
	
	private boolean posFree = false;
	private boolean sensorReached = false;
	private boolean started = false;
	private List<Glass> glasses = Collections.synchronizedList(new ArrayList<Glass>());

	// *** MESSAGES ***
	public void msgHereIsGlass(Glass glass) {
		print("Received msgHereIsGlass");
		glasses.add(glass);
		stateChanged();
	}

	public void msgPositionFree() {
		print("Received msgPositionFree");
		posFree = true;
		stateChanged();
	}
	
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (event == TEvent.SENSOR_GUI_RELEASED) {
			// When the glass moves past the 1st sensor
			if (thisSensor1(args)) {
				family.prev.msgPositionFree();
			}
			// When the glass moves past the 2nd sensor
			else if (thisSensor2(args)) {
				sensorReached = false;
			}
		} else if (event == TEvent.SENSOR_GUI_PRESSED) {
			// When the glass reaches the 2nd sensor
			if (thisSensor2(args)) {
				sensorReached = true;
				doStopConveyor();
			}
		}
	}

	// *** SCHEDULER ***
	
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

	// *** ACTIONS ***
	public void actLoadGlassOntoWorkstationAndPassOn() {
		print("Doing actLoadGlassOntoWorkstationAndPassOn");
		Glass g = glasses.remove(0);
		//doLoadGlassOntoWorkstation(); TODO: delete this line after understanding repercussions
		//doPassOnGlass(g); TODO
		family.next.msgHereIsGlass(g);
		posFree = false;
	}

	public void actStartConveyor() {
		print("Doing actStartConveyor");
		doStartConveyor();
	}

	// *** ANIMATION ACTIONS ***
	private void doStartConveyor() {
		started = true;
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, new Integer[] { conveyorIndex });
	}

	private void doStopConveyor() {
		started = false;
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, new Integer[] { conveyorIndex });
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
