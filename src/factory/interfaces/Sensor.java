package factory.interfaces;

import shared.Glass;

public interface Sensor {
	// *** MESSAGES ***
	public abstract void msgHereIsGlass(Glass g); // from previous agent
	public abstract void msgPositionFree(); // from next agent

	// *** ACTIONS ***
	public abstract void actPassOnGlass(Glass g);
	public abstract void actTellPrevFamilyPositionFree();
	
	// *** EXTRA ***
}
