package engine.agent.david.interfaces;

import java.util.List;

import engine.agent.david.agents.PopupAgent.PopupState;
import engine.agent.david.agents.PopupAgent.WorkstationState;
import engine.agent.david.misc.ConveyorFamilyEntity.MyGlass;

import shared.Glass;
import shared.interfaces.PopupWorkstationInteraction;

public interface Popup extends PopupWorkstationInteraction {
	// *** MESSAGES ***
	public void msgGlassComing(MyGlass myGlass); // from conveyor
	public void msgPositionFree(); // from next family

	// *** SCHEDULER ***
	public boolean pickAndExecuteAnAction();
	
	// *** ACTIONS ***
	
	// *** EXTRA ***
	public List<MyGlass> getGlasses();
	public PopupState getState();
	public List<Glass> getFinishedGlasses();
	public void setIsUp(boolean b);
	public boolean getNextPosFree();
	public void seedFinishedGlasses();
	public void setWorkstationState(int i, WorkstationState s);
}
