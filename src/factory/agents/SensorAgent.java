package factory.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import shared.Glass;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import factory.interfaces.Sensor;
import factory.misc.ConveyorFamily;
import factory.misc.ConveyorFamily.RunningState;

public class SensorAgent extends Agent implements Sensor {
	// *** Constructor(s) ***
	public SensorAgent(ConveyorFamily f, Transducer transducer) {
		family = f;
		t = transducer;
	}

	// *** DATA ***
	private ConveyorFamily family;
	private Transducer t;
	private enum SensorState { SHOULD_NOTIFY_POSITION_FREE, NOTHING_TO_DO, GLASS_JUST_ARRIVED }
	private SensorState state = SensorState.NOTHING_TO_DO;

	private List<Glass> glasses = Collections.synchronizedList(new ArrayList<Glass>());
	
	// *** MESSAGES ***
	public void msgHereIsGlass(Glass g) {
		state = SensorState.GLASS_JUST_ARRIVED;
		glasses.add(g);
		stateChanged();
	}
	
	public void msgPositionFree() {
		state = SensorState.SHOULD_NOTIFY_POSITION_FREE;
		stateChanged();
	}
	
	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		if (state == SensorState.SHOULD_NOTIFY_POSITION_FREE) {
			state = SensorState.NOTHING_TO_DO;
			actTellPrevFamilyPositionFree();
			return false;
		} else if (state == SensorState.GLASS_JUST_ARRIVED) {
			state = SensorState.NOTHING_TO_DO;
			// !glasses.isEmpty() should be true
			actPassOnGlass(glasses.remove(0)); // remove because sensor passes on immediately no matter what
			return false;
		} else { // NOTHING_TO_DO
			return false;
		}
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// Nothing to do
	}

	// *** ACTIONS ***
	public void actPassOnGlass(Glass g) {
		while (family.runningState != RunningState.OFF_BC_QUIET) {
			// Wait until conveyor is officially in the proper off state.
			// This should be very quick and is only here in the event that *right after* conveyor tells this sensor msgPositionFree and this sensor tells the previous family, that family sends the next glass.
		}
		// DO_START_CONVEYOR
		family.runningState = RunningState.ON_BC_SENSOR_TO_CONVEYOR;
		family.conv.msgHereIsGlass(g);
	}
	
	public void actTellPrevFamilyPositionFree() {
		family.prevFamily.msgPositionFree();		
	}	
	// *** EXTRA ***
}
