package factory_david.interfaces;

import java.util.List;

import shared.Glass;
import factory_david.agents.ConveyorAgent.ConveyorState;

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
