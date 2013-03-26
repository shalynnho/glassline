package factory.interfaces;

import shared.Glass;
import factory.misc.ConveyorFamilyEntity.MyGlass;

public interface Popup {
	// *** MESSAGES ***
	public void msgGlassComing(MyGlass myGlass); // from conveyor
	public void msgPositionFree(); // from next family
	public void msgGlassDone(Glass g, int index); // from workstation

	// *** ACTIONS ***
	
	// *** EXTRA ***
}
