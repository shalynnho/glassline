package factory.interfaces;

import java.util.List;

import shared.Glass;
import factory.agents.PopupAgent.PopupState;
import factory.misc.ConveyorFamilyEntity.MyGlass;

public interface Popup {
	// *** MESSAGES ***
	public void msgGlassComing(MyGlass myGlass); // from conveyor
	public void msgPositionFree(); // from next family
	public void msgGlassDone(Glass g, int machineIndex); // from workstation

	// *** SCHEDULER ***
	public boolean pickAndExecuteAnAction();
	
	// *** ACTIONS ***
	
	// *** EXTRA ***
	public List<Glass> getGlasses();
	public PopupState getState();
}
