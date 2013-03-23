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
	private boolean machineOccupied = false;
	private boolean sensorOccupied = false; // roughly equivalent to family.runningState.OFF_BC_WAITING_AT_SENSOR, but needed for popup to internally decide to move to ON_BC_SENSOR_TO_POPUP
	private enum PopupState { }

	// *** MESSAGES ***
	@Override
	public void msgGlassComing(MyGlass myGlass) {
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
	public void msgGlassDone() {
		state = PopupState.GLASS_READY_TO_PASS_ON;
		stateChanged();
	}	

	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		// Only take action if the next position is free
		if (nextPosFree) {
			if (sensorOccupied) {
				// This should be true: !glasses.isEmpty()
				MyGlass g = glasses.get(0);
				
				// Case 1: Glass needs processing and there is nothing currently processing
				if (g.needsProcessing() && !machineOccupied) {
					// TODONOW: Decide how popup manages among robot
					// Just load right onto machine, no issues
					actLoadGlassOntoPopup(g);
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
					actLoadGlassOntoPopup(g);
					// actPassOnGlass(g);
				} 
				// Case 4: Glass does not need processing and there is nothing currently processing
				else { // (!g.needsProcessing() && !machineOccupied)
					// Just move the glass onward
					actLoadGlassOntoPopup(g);
					// actPassOnGlass(g);	
				}
				return true;
			} else if (!glasses.isEmpty()) {
				MyGlass g = glasses.get(0);
				if (state == PopupState.GLASS_READY_TO_PASS_ON) {
					state = PopupState.PASSING_ON_GLASS;
					actPassOnGlass(g);
					return true;
				}
				state = PopupState.DONE_WITH_TASKS;
			}
		} // optional: could make a little more efficient: the only thing to check for if !nextPosFree is to take glass from machine and pass on
		return false;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
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
		} else { // could be in the middle of moving piece from sensor to popup, so check for glass leaving the sensor & therefore officially on popup
			if (channel == TChannel.SENSOR) {
				// When the sensor right before the popup has been released, allow loading of glass onto popup
				if (event == TEvent.SENSOR_GUI_RELEASED) {
					// parse args to check if it is this sensor
					// if so:
					// #2
					sensorOccupied = false;
					family.runningState = RunningState.OFF_BC_QUIET;
					//CONVEYOR_DO_STOP
					// TODONOW: DECIDE B/W 4 CASES ABOVE
					// if sensor is occupied, does that mean anything about popup being occupied?
				}
			}
		}

		// If passing on glass, listen for WORKSTATION_RELEASE_FINISHED so popup knows to remove glass from list
		if (state == PopupState.PASSING_ON_GLASS) {
			if (channel == this.workstationChannel) {
				if (event == TEvent.WORKSTATION_RELEASE_FINISHED) {
					state = PopupState.DONE_WITH_TASKS;
					glasses.remove(g);
				}
			}

		}

		
	}
	// *** ACTIONS ***
	public void actLoadGlassOntoPopup(MyGlass g) {
		// The popupagent would know when the glass reached the sensor via eventFired. That's what sensorOccupied is.
		// When that is true, then this can be reached.
		family.conv.msgTakingGlass();
		// #1
		//POPUP_DO_MOVE_DOWN
		//CONVEYOR_DO_START
		family.runningState = RunningState.ON_BC_SENSOR_TO_POPUP;
		// ...until sensor fires SENSOR_GUI_RELEASED, at which conveyor should stop. See eventFired, '#2'
	}

	public void actPassOnGlass(MyGlass g) {
		//WORKSTATION_RELEASE_PART
		// Then, wait for WORKSTATION_RELEASE_FINISHED in eventFired.
		// There, do glasses.remove(g);
	}

	// *** EXTRA ***
}
