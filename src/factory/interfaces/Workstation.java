package factory.interfaces;

import shared.Glass;
import shared.enums.MachineType;

public interface Workstation {
	// *** MESSAGES ***
	public abstract void msgHereIsGlass(Glass g); // from popup
	
	// *** ACTIONS ***
	
	// *** EXTRA ***
	public abstract MachineType getType();
}
