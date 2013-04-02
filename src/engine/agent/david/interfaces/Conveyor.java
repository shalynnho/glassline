package engine.agent.david.interfaces;

import java.util.List;

import engine.agent.david.agents.ConveyorAgent.ConveyorState;

import shared.Glass;

public interface Conveyor {
	
	// *** MESSAGES ***
	public void msgHereIsGlass(Glass g); // from previous sensor
	public void msgTakingGlass(); // from following popup
	
	// *** SCHEDULER ***
	public boolean pickAndExecuteAnAction();
	
	// *** ACTIONS ***
	public void actTellPopupGlassOnConveyor(Glass g);
	public void actTellSensorPositionFree();
	
	// *** EXTRA ***
	public ConveyorState getState();
	public List<Glass> getGlasses();
}
