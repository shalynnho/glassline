package factory.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import shared.Glass;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import factory.interfaces.Conveyor;
import factory.misc.ConveyorFamily;
import factory.misc.ConveyorFamily.GlassState;
import factory.misc.ConveyorFamily.MyGlass;
import factory.misc.ConveyorFamily.RunningState;

public class ConveyorAgent extends Agent implements Conveyor {
	// *** Constructor(s) ***
	public ConveyorAgent(ConveyorFamily f, Transducer transducer) {
		family = f;
		t = transducer;
		t.register(this, TChannel.SENSOR);
	}
	
	// *** DATA ***
	private ConveyorFamily family;
	private Transducer t;
	private enum ConveyorState { GLASS_JUST_ARRIVED, WAITING_FOR_GLASS_TO_REACH_ENDING_SENSOR, SHOULD_NOTIFY_POSITION_FREE, NOTHING_TO_DO }
	public ConveyorState state = ConveyorState.NOTHING_TO_DO;

	private List<Glass> glasses = Collections.synchronizedList(new ArrayList<Glass>());

	// *** MESSAGES ***
	@Override
	public void msgHereIsGlass(Glass g) {
		state = ConveyorState.GLASS_JUST_ARRIVED; // previous sensor should have already started the conveyor
		// at this point, this should be true: family.runningState == RunningState.ON_BC_SENSOR_TO_CONVEYOR
		glasses.add(g);
		stateChanged();
	}

	@Override
	public void msgTakingGlass() {
		state = ConveyorState.SHOULD_NOTIFY_POSITION_FREE;
		glasses.remove(0);
		stateChanged();
	}
	
	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		if (state == ConveyorState.GLASS_JUST_ARRIVED) {
			// !glasses.isEmpty() should be true
			state = ConveyorState.WAITING_FOR_GLASS_TO_REACH_ENDING_SENSOR;
			actTellPopupGlassOnConveyor(glasses.get(0));
			return true;
		} else if (state == ConveyorState.WAITING_FOR_GLASS_TO_REACH_ENDING_SENSOR) { // technically could be merged into NOTHING_TO_DO
			// Do nothing. Next thing that happens is conveyor auto-stops via eventFired, popup agent realizes sensorOccupied = true, 
			// does actLoadGlassOntoPopup which *then tells this conveyor agent msgTakingGlass()*
			return false;
		} else if (state == ConveyorState.SHOULD_NOTIFY_POSITION_FREE) {
			state = ConveyorState.NOTHING_TO_DO;
			actTellSensorPositionFree();
			return false;
		} else { // NOTHING_TO_DO
			return false;
		}
	}
	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (state == ConveyorState.WAITING_FOR_GLASS_TO_REACH_ENDING_SENSOR) {
			if (channel == TChannel.SENSOR) {
				// When the sensor right after the conveyor has been pressed, stop the conveyor
				if (event == TEvent.SENSOR_GUI_PRESSED) {
					// parse args to check if it is this sensor
					// if so:
					// t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, thisIndex);
					family.runningState = RunningState.OFF_BC_WAITING_AT_SENSOR;
				}
			}
		}
	}
	
	// *** ACTIONS ***
	public void actTellPopupGlassOnConveyor(Glass g) {
		GlassState glassState = family.decideIfGlassNeedsProcessing(g); // conveyor decides this since it has time
		MyGlass myGlass = family.new MyGlass(g, glassState);

		family.runningState = RunningState.ON_BC_CONVEYOR_TO_SENSOR;
		family.popup.msgGlassComing(myGlass);

		// Trust that conveyor knows to stop glass the moment the right sensor fires. See eventFired.
	}

	public void actTellSensorPositionFree() {
		family.sensor.msgPositionFree();
	}
	
	
	// *** EXTRA ***
}
