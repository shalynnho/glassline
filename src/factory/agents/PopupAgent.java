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
	private ConveyorFamily family;
	private Transducer t;
	private TChannel workstationChannel;
	private List<MyGlass> glasses = Collections.synchronizedList(new ArrayList<MyGlass>()); // uses MyGlass instead of just Glass so it contains GlassState
	private boolean nextPosFree = false;
	private boolean sensorOccupied = false; // roughly equivalent to family.runningState.OFF_BC_WAITING_AT_SENSOR, but needed for popup to internally decide to move to ON_BC_SENSOR_TO_POPUP
	
	// Mainly helps in differentiating between waiting for a transducer event to fire (WAIT) and when popup should actually check scheduler events
	// if (ACTIVE) is used in scheduler, if (WAIT_FOR_SOMETHING) is used in eventFired to signal the popup is waiting for some animation to finish
	private enum PopupState { ACTIVE, WAIT }
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
		// ACTIVE is set by transducer and incoming messages. We only take action if we are 'active'.
		if (state == PopupState.ACTIVE) {
			// Case 1 (easy): Just deal with the workstation's finished glass by passing it on. No complications with sensor.
			if (!sensorOccupied) {
				if (nextPosFree && wsState == WorkstationState.DONE_BUT_STILL_HAS_GLASS) {
					// Keep state as ACTIVE. This is implied.
					actReleaseGlassFromWorkstation();
					return true;
				}
			} 
			// Case 2-x deal with when sensor is occupied, which adds complications.
			else {
				MyGlass g = getNextUnhandledGlass(); // the *unhandled* glass;
				if (g != null) { // should be present since sensorOccupied = true
					// Case 2: Regardless of workstation, just load sensor's glass and pass it on - no workstation interaction
					if (nextPosFree && !g.needsProcessing()) {
						actLoadSensorsGlassOntoPopupAndRelease();
						return true;
					} 
					// Case 3: Release workstation's finished glass to next family
					else if (g.needsProcessing() && wsState == WorkstationState.DONE_BUT_STILL_HAS_GLASS && nextPosFree) {
						actReleaseGlassFromWorkstation();
						return true;
					} 
					// Case 4: Load sensor's glass onto workstation. Must happen after case 3 if case 3 happens.
					else if (g.needsProcessing() && wsState == WorkstationState.FREE) {
						actLoadSensorsGlassOntoWorkstation();
						return true;
					}
				} else {
					throw("Null unhandled glass!")
				}
			}
		} 
		//state = waiting?
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
				// parse args to check if it is this popup
				MyGlass g = findGlassWithState(GlassState.NEEDS_PROCESSING);
				workstation.msgHereIsGlass(g.getGlass());
			}
		}

		// From actReleaseGlassFromWorkstation so popup knows to remove glass from list, step 2 (final)
		if (state == PopupState.WAITING_FOR_WORKSTATION_GLASS_RELEASE) {
			if (channel == this.workstationChannel && event == TEvent.WORKSTATION_RELEASE_FINISHED) {
				state = PopupState.ACTIVE;
				// todo: make sure g is the glass with isFinished() as true
				Glass glass = g.getGlass();
				family.nextFamily.msgHereIsGlass(glass);
				glasses.remove(g).getGlass();
			}
		}

		// From actLoadSensorsGlassOntoPopupAndRelease, step 2
		if (state == PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_RELEASING) {
			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_LOAD_FINISHED) {
				// parse args to check if it is this sensor
				// if so:
				sensorOccupied = false;
				family.runningState = RunningState.OFF_BC_QUIET;
				//CONVEYOR_DO_STOP
				state = PopupState.WAITING_FOR_POPUP_GLASS_RELEASE;
				//POPUP_RELEASE_GLASS
			}
		}
		// From actLoadSensorsGlassOntoPopupAndRelease, step 3 (final)
		if (state == PopupState.WAITING_FOR_POPUP_GLASS_RELEASE) {
			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_LOAD_FINISHED) {
				// parse args to check if it is this popup
				// todo: make sure g is the glass with isFinished() as true
				Glass glass = g.getGlass();
				family.nextFamily.msgHereIsGlass(glass);
				glasses.remove(g).getGlass();
			}
		}

	}
	// *** ACTIONS ***
	// Loads glass from sensor onto popup and then releases to next conveyor family
	// Multi-step with eventFired
	public void actLoadSensorsGlassOntoPopupAndRelease() {
		//POPUP_DO_MOVE_DOWN = t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, {thisIndex});
		//CONVEYOR_DO_START
		family.runningState = RunningState.ON_BC_SENSOR_TO_POPUP;
		state = PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_RELEASING;
	}

	// Multi-step with eventFired
	public void actLoadSensorsGlassOntoWorkstation(MyGlass g) {
		family.conv.msgTakingGlass();
		//POPUP_DO_MOVE_DOWN = t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, {thisIndex});
		//CONVEYOR_DO_START
		family.runningState = RunningState.ON_BC_SENSOR_TO_POPUP;
		state = PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_GOING_TO_WORKSTATION;
		// ...until sensor fires POPUP_GUI_LOAD_FINISHED, at which conveyor should stop.
	}

	// Releases glass from workstation to next conveyor family
	public void actReleaseGlassFromWorkstation() {
		//WORKSTATION_RELEASE_PART
		state = PopupState.WAITING_FOR_WORKSTATION_GLASS_RELEASE;
		// Then, wait for WORKSTATION_RELEASE_FINISHED in eventFired.
		// There, do glasses.remove(g); where g is the glass with isFinished() as true, and message next fam.
	}

	// *** EXTRA ***
	// Returns next MyGlass object from glasses that either needs processing or doesn't
	private MyGlass getNextUnhandledGlass() {
		MyGlass g = null;
		for (MyGlass mg : glasses) {
			if (mg.getState() == GlassState.NEEDS_PROCESSING || mg.getState() == GlassState.DOES_NOT_NEED_PROCESSING)
				return mg;
		}
		print("No glass to handle!");
		return null;
	}
}
