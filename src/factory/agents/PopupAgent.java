package factory.agents;

import transducer.TChannel;
import transducer.TEvent;
import engine.agent.Agent;
import factory.interfaces.Popup;

public class PopupAgent extends Agent implements Popup {
	// *** Constructor(s) ***
	public PopupAgent(ConveyorFamily f, Transducer transducer) {
		family = f;
		t = transducer;
		t.register(this, TChannel.SENSOR);
	}

	// *** DATA ***
	private ConveyorFamily family;
	private Transducer t;
	private List<MyGlass> glasses = Collections.synchronizedList(new ArrayList<MyGlass>()); // uses MyGlass instead of just Glass so it contains GlassState
	private boolean nextPosFree = false;
	private boolean machineOccupied = false;
	private boolean sensorOccupied = false; // roughly equivalent to family.runningState.OFF_BC_WAITING_AT_SENSOR, but needed for popup to internally decide to move to ON_BC_SENSOR_TO_POPUP

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

	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		// Only take action if the next position is free
		if (nextPosFree) {
			if (sensorOccupied && !glasses.isEmpty()) {
				MyGlass g = glasses.get(0);
				actLoadGlassOntoPopup(g);
				
				// Case 1: Glass needs processing and there is nothing currently processing
				if (g.needsProcessing() && !machineOccupied) {
					// TODO: Decide how popup manages among robot
				} 
				// Case 2: Glass needs processing and something is currently processing
				else if (g.needsProcessing() && machineOccupied) {

				}
				// Case 3: Glass does not need processing and there is something currently processing
				else if (!g.needsProcessing() && machineOccupied) {

				} 
				// Case 4: Glass does not need processing and there is nothing currently processing
				else { // (!g.needsProcessing() && !machineOccupied)

				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (!sensorOccupied) { // should only bother to check if sensor is not occupied - the popup only cares about listening to see if a glass has arrived at the preceding sensor
			if (channel == TChannel.SENSOR) {
				// When the sensor right before the popup has been pressed, allow loading of glass onto popup
				if (event == TEvent.SENSOR_GUI_PRESSED) {
					// parse args to check if it is this sensor
					// if so:
					sensorOccupied = true;
					stateChanged();
				}
			}
		} else { // in the middle of moving piece from sensor to popup, so check for glass leaving the sensor & therefore officially on popup
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

	// *** EXTRA ***
}
