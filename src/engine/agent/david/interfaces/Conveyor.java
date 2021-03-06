package engine.agent.david.interfaces;

import java.util.List;

import shared.Glass;
import shared.interfaces.NonnormBreakInteraction;
import engine.agent.david.agents.ConveyorAgent.ConveyorState;

public interface Conveyor extends NonnormBreakInteraction {
	
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
