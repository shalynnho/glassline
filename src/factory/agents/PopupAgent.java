package factory.agents;

import transducer.TChannel;
import transducer.TEvent;
import engine.agent.Agent;
import factory.interfaces.Popup;

public class PopupAgent extends Agent implements Popup {
	// *** Constructor(s) ***
	public PopupAgent(ConveyorFamily f, Transducer transducer, TChannel workstationChannel) {
		family = f;
		t = transducer;
		this.workstationChannel = workstationChannel;

		t.register(this, TChannel.SENSOR);
		t.register(this, this.workstationChannel)
	}

	// *** DATA ***
	private ConveyorFamily family, nextFamily; // how do I pass the next family?
	private Transducer t;
	private TChannel workstationChannel;
	private List<MyGlass> glasses = Collections.synchronizedList(new ArrayList<MyGlass>()); // uses MyGlass instead of just Glass so it contains GlassState
	private boolean nextPosFree = false;
	private boolean sensorOccupied = false; // roughly equivalent to family.runningState.OFF_BC_WAITING_AT_SENSOR, but needed for popup to internally decide to move to ON_BC_SENSOR_TO_POPUP
	
	// Mainly helps in differentiating between waiting for a transducer event to fire (WAIT) and when popup should actually check scheduler events
	// if (ACTIVE) is used in scheduler, if (WAIT) is used in eventFired
	private enum PopupState { WAIT, ACTIVE } 
	private enum WorkstationState { FREE, BUSY, DONE_BUT_STILL_HAS_GLASS }
	PopupState state = PopupState.WAIT;
	WorkstationState wsState = WorkstationState.FREE;

	// *** MESSAGES ***
	@Override
	public void msgGlassComing(MyGlass myGlass) { // Just to pass on glass object to sensor's list
		// At this point, this should be true: family.runningState == RunningState.ON_BC_CONVEYOR_TO_SENSOR
		glasses.add(myGlass);
		stateChanged();
	}

	@Override
	public void msgPositionFree() {
		nextPosFree = true;
		stateChanged();
	}

	@Override
	public void msgGlassDone(Glass g) {
		MyGlass myG = findGlass(g); // from glasses list
		g.setState(GlassState.FINISHED);
		stateChanged();
	}	

	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		// Only take action if the next position is free
		if (nextPosFree) {
			if (state == DONE_BUT_SENSOR_OCCUPIED) { //(sensorOccupied) {
				// This should be true: !glasses.isEmpty()
				MyGlass g = glasses.get(0);
				
				// Case 1: Glass needs processing and there is nothing currently processing
				if (g.needsProcessing() && !machineOccupied) {
					// TODONOW: Decide how popup manages among robot
					// Just load right onto machine, no issues
					actLoadSensorsGlassOntoWorkstation(g);
					// actLoadGlassOntoMachine(g);
				} 
				// Case 2: Glass needs processing and something is currently processing
				else if (g.needsProcessing() && machineOccupied) {
					// Wait for machine to finish
					state = PopupState.WAITING_FOR_WORKSTATION;
				}
				// Case 3: Glass does not need processing and there is something currently processing
				else if (!g.needsProcessing() && machineOccupied) {
					// Just move the glass onward
					actLoadSensorsGlassOntoWorkstation(g);
					// actReleaseGlassFromWorkstation();
				} 
				// Case 4: Glass does not need processing and there is nothing currently processing
				else { // (!g.needsProcessing() && !machineOccupied)
					// Just move the glass onward
					actLoadSensorsGlassOntoWorkstation(g);
					// actReleaseGlassFromWorkstation();	
				}
				return true;
			} else if (!glasses.isEmpty()) {
				if (state == PopupState.GLASS_READY_TO_PASS_ON) {
					state = PopupState.PASSING_ON_GLASS;
					actReleaseGlassFromWorkstation();
					return true;
				}
				state = PopupState.DONE_WITH_TASKS;
			}
		} // optional: could make a little more efficient: the only thing to check for if !nextPosFree is to take glass from machine and pass on
		return false;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// Most checks here involve seeing if state is a form of WAITING, which happen from actions.

		// Exception: we must update sensor status regardless of the state.
		if (!sensorOccupied) { // should only bother to check if sensor is not occupied - here, the popup only cares about listening to see if a glass has arrived at the preceding sensor
			if (channel == TChannel.SENSOR) {
				// When the sensor right before the popup has been pressed, allow loading of glass onto popup
				if (event == TEvent.SENSOR_GUI_PRESSED) {
					// parse args to check if it is this sensor
					// if so:
					sensorOccupied = true;
					stateChanged();
				}
			}
		}
		// From actLoadSensorsGlassOntoWorkstation, step 2
		if (state == PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_GOING_TO_WORKSTATION) {
			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_LOAD_FINISHED) {
				// parse args to check if it is this sensor
				// if so:
				// #2
				sensorOccupied = false;
				family.runningState = RunningState.OFF_BC_QUIET;
				//CONVEYOR_DO_STOP
				//POPUP_DO_MOVE_UP
				state = PopupState.WAITING_FOR_HIGH_POPUP;
				// Next: we want WORKSTATION_DO_LOAD_GLASS
			}
		}
		// From actLoadSensorsGlassOntoWorkstation, step 3 (final)
		if (state == PopupState.WAITING_FOR_HIGH_POPUP) {
			if (channel == TChannel.POPUP && event == POPUP_GUI_MOVED_UP) {
				MyGlass g = findGlassWithState(GlassState.NEEDS_PROCESSING);
				workstation.msgHereIsGlass(g.getGlass());
			}
		}

		// From actReleaseGlassFromWorkstation so popup knows to remove glass from list, step 2 (final)
		if (state == PopupState.WAITING_FOR_WORKSTATION_GLASS_RELEASE) {
			if (channel == this.workstationChannel && event == TEvent.WORKSTATION_RELEASE_FINISHED) {
				state = PopupState.ACTIVE;
				Glass glass = g.getGlass();
				glasses.remove(g).getGlass(); // g is the glass with isFinished() as true
				nextFamily.msgHereIsGlass(glass);
			}
		}
	}
	// *** ACTIONS ***
	public void actLoadSensorsGlassOntoWorkstation(MyGlass g) {
		// The popupagent would know when the glass reached the sensor via eventFired. That's what sensorOccupied is.
		// When that is true, then this can be reached.
		family.conv.msgTakingGlass();
		//POPUP_DO_MOVE_DOWN
		//CONVEYOR_DO_START
		family.runningState = RunningState.ON_BC_SENSOR_TO_POPUP;
		state = PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_GOING_TO_WORKSTATION;
		// ...until sensor fires POPUP_GUI_LOAD_FINISHED, at which conveyor should stop.
	}

	// Passes on glass to next conveyor family from workstation
	public void actReleaseGlassFromWorkstation() {
		//WORKSTATION_RELEASE_PART
		state = PopupState.WAITING_FOR_WORKSTATION_GLASS_RELEASE;
		// Then, wait for WORKSTATION_RELEASE_FINISHED in eventFired.
		// There, do glasses.remove(g); where g is the glass with isFinished() as true, and message next fam.
	}

	// IMPORTANT: DO I NEED TO WAIT AND LISTEN FOR A TEVENT IN EACH OF THESE ACTIONS? WHAT IF I CALLED TWO FIRE EVENTS IMMEDIATELY?
	// You need to listen so you know when to appropriately change the state. e.g., popup needs to set sensorOccupied = false when the callback comes in.
	// also: you have to send them in order b/c if you do conveyor_do_start to move from sensor to popup, you can't do popup_move_up right after because that happens immediately and the glass would crash!
	// you need to wait for the callback, knowing when the glass is officially on the popup - THEN you can lift up.
	// So, YES you need to wait.

	// *** EXTRA ***
}
