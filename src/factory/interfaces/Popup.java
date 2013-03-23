package factory.interfaces;

public interface Popup {
	// *** MESSAGES ***
	public abstract void msgGlassComing(MyGlass myGlass); // from conveyor
	public abstract void msgPositionFree(); // from next family
	public abstract void msgGlassDone(); // from robot (in workstation)

	// *** ACTIONS ***
	
	// *** EXTRA ***
}
