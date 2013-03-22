package factory.agents;

import transducer.TChannel;
import transducer.TEvent;
import engine.agent.Agent;
import factory.interfaces.Sensor;

public class SensorAgent extends Agent implements Sensor {
	// *** Constructor(s) ***
	public SensorAgent(ConveyorFamily f) {
		family = f;
	}

	// *** DATA ***
	ConveyorFamily family;
	// boolean nextPositionIsFree = false;

	private List<Glass> glasses = Collections.synchronizedList(new ArrayList<Glass>());
	
	// *** MESSAGES ***
	public void msgHereIsGlass(Glass g) {
		// state = SensorState.GLASS_JUST_ARRIVED;
		glasses.add(g);
		stateChanged();
	}
	
	public void msgPositionFree() {
		// todo
		// nextPositionIsFree = true;
		stateChanged();
	}
	
	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		if (!glasses.isEmpty()) { // state == SensorState.GLASS_JUST_ARRIVED) {
			actPassOnGlass(glasses.remove(0));
			return true;
		}
		return false;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// TODO Auto-generated method stub
		
	}
	// *** ACTIONS ***
	public void actPassOnGlass(Glass g) {
		family.conv.msgHereIsGlass(g);
		// DO_START_CONVEYOR ?
	}
	
	// *** EXTRA ***
}
