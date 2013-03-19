package factory.agents;

import transducer.TChannel;
import transducer.TEvent;
import engine.agent.Agent;
import factory.interfaces.Conveyor;

public class ConveyorAgent extends Agent implements Conveyor {
	// *** Constructor(s) ***
	public ConveyorAgent() {
	}
	
	// *** DATA ***
	enum ConveyorState { STOPPED, READY_FOR_GLASS, RUNNING_WITH_GLASS }
	private SensorAgent sensor1, sensor2; // previous sensor, next sensor
	private ConveyorState state = ConveyorState.STOPPED;
			
	// *** MESSAGES ***
	@Override
	public void msgHereIsGlass() {
		// TODO Auto-generated method stub
		stateChanged();
	}

	@Override
	public void msgPositionFree() {
		// TODO Auto-generated method stub
		stateChanged();
	}
	
	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		return false;
	}
	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// TODO Auto-generated method stub
	}
	
	// *** ACTIONS ***
	public void actReadyForGlass() {
//		sensor1.msgPositionFree();
	}

	
	
	// *** EXTRA ***
}
