package factory.interfaces;

import shared.Glass;
import factory.misc.ConveyorFamily.MyGlass;

public interface Popup {
	// *** MESSAGES ***
	public abstract void msgGlassComing(MyGlass myGlass); // from conveyor
	public abstract void msgPositionFree(); // from next family
	public abstract void msgGlassDone(Glass g); // from workstation

	// *** ACTIONS ***
	
	// *** EXTRA ***
}
