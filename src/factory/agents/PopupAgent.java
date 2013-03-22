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
	private List<MyGlass> glasses = Collections.synchronizedList(new ArrayList<MyGlass>());
	private boolean nextPosFree = false;
	private boolean machineOccupied = false;
	private boolean sensorOccupied = false;

	// *** MESSAGES ***
	@Override
	public void msgGlassComing(MyGlass myGlass) {
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
				
				// Case 1: Glass needs processing and there is nothing currently processing
				if (g.needsProcessing() && !machineOccupied) {
					// TODO: Decide how popup manages among robot
					actLoadGlassOntoPopup(g);
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
		if (channel == TChannel.SENSOR) {
			// When the sensor right before the popup has been pressed, load glass onto popup
			if (event == TEvent.SENSOR_GUI_PRESSED) {
				// parse args to check if it is this sensor
				// if so:
				sensorOccupied = true;
			}
		}
		
	}
	// *** ACTIONS ***
	public void actLoadGlassOntoPopup(MyGlass g) {
		// The popupagent would know when the glass reached the sensor via eventFired. <==== need transducer set up TODONOW
		
		family.conv.msgTakingGlass();
		//POPUP_DO_MOVE_DOWN
		//CONVEYOR_DO_START until sensor fires SENSOR_GUI_RELEASED
	}

	// *** EXTRA ***
}
