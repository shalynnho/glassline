package factory.interfaces;

import java.util.List;

import shared.Glass;
import factory.agents.ConveyorAgent.ConveyorState;

public interface Conveyor {
	
	// *** MESSAGES ***
	public void msgHereIsGlass(Glass g); // from previous sensor
	public void msgTakingGlass(); // from following popup
	
	// *** ACTIONS ***
	public void actTellPopupGlassOnConveyor(Glass g);
	public void actTellSensorPositionFree();
	
	// *** EXTRA ***
	public ConveyorState getState();
	public List<Glass> getGlasses();
}
