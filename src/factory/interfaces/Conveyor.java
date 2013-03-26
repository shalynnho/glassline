package factory.interfaces;

import shared.Glass;

public interface Conveyor {
	
	// *** MESSAGES ***
	public void msgHereIsGlass(Glass g); // from previous sensor
	public void msgTakingGlass(); // from following popup
	
	// *** ACTIONS ***
	public void actTellPopupGlassOnConveyor(Glass g);
	public void actTellSensorPositionFree();
	
	// *** EXTRA ***
}
