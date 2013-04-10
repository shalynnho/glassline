package engine.agent.david.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import shared.Glass;
import shared.interfaces.OfflineConveyorFamily;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.david.interfaces.Sensor;
import engine.agent.david.misc.ConveyorFamilyEntity;
import engine.agent.david.misc.ConveyorFamilyEntity.RunningState;

public class SensorAgent extends Agent implements Sensor {
	// *** Constructor(s) ***
	public SensorAgent(ConveyorFamilyEntity f, Transducer transducer) {
		super(f.type+" sensor", transducer);
		family = f;
	}

	// *** DATA ***
	private ConveyorFamilyEntity family;
	public enum SensorState { SHOULD_NOTIFY_POSITION_FREE, NOTHING_TO_DO, GLASS_JUST_ARRIVED }
	private SensorState state = SensorState.NOTHING_TO_DO;

	private List<Glass> glasses = Collections.synchronizedList(new ArrayList<Glass>());
	
	// *** MESSAGES ***
	public void msgHereIsGlass(Glass g) {
		print("Received msgHereIsGlass");
		state = SensorState.GLASS_JUST_ARRIVED;
		glasses.add(g);
		stateChanged();
	}
	
	public void msgPositionFree() {
		print("Received msgPositionFree");
		state = SensorState.SHOULD_NOTIFY_POSITION_FREE;
		stateChanged();
	}
	
	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		if (state == SensorState.GLASS_JUST_ARRIVED) {
			state = SensorState.NOTHING_TO_DO;
			// !glasses.isEmpty() should be true
			actPassOnGlass(glasses.remove(0)); // remove because sensor passes on immediately no matter what
			return false;
		} else if (state == SensorState.SHOULD_NOTIFY_POSITION_FREE) {
			state = SensorState.NOTHING_TO_DO;
			actTellPrevFamilyPositionFree();
			return false;
		} else { // NOTHING_TO_DO
			return false;
		}
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// Nothing to do
		// Maybe detect when glass is /truly/ here before passing it on?
	}

	// *** ACTIONS ***
	public void actPassOnGlass(Glass g) {
		print("Doing actPassOnGlass");
		while (family.runningState != RunningState.OFF_BC_QUIET) { // only supports one glass at a time
			// Wait until conveyor is officially in the proper off state.
			// This should be very quick and is only here in the event that *right after* conveyor tells this sensor msgPositionFree and this sensor tells the previous family, that family sends the next glass.
		}
		family.doStartConveyor();
		family.runningState = RunningState.ON_BC_SENSOR_TO_CONVEYOR;
		family.conv.msgHereIsGlass(g); // ERROR: this somehow make conveyor stop
	}
	
	public void actTellPrevFamilyPositionFree() {
		print("Doing actTellPrevFamilyPositionFree");
		family.prev.msgPositionFree();
	}
	
	// *** EXTRA ***
	public List<Glass> getGlasses() {
		return glasses;
	}
	
	public SensorState getState() {
		return state;
	}
}