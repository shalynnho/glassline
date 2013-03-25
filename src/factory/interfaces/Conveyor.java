package factory.interfaces;

import shared.Glass;

public interface Conveyor {
	
	// *** MESSAGES ***
	public abstract void msgHereIsGlass(Glass g); // from previous sensor
	public abstract void msgTakingGlass(); // from following popup
	
	// *** ACTIONS ***
	public abstract void actTellPopupGlassOnConveyor(Glass g);
	public abstract void actTellSensorPositionFree();
	
	// *** EXTRA ***
}
