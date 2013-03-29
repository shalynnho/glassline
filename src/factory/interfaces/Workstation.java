package factory.interfaces;

import shared.Glass;
import shared.enums.MachineType;
import transducer.TChannel;

public interface Workstation {
	// *** MESSAGES ***
	public void msgHereIsGlass(Glass g); // from popup
	
	// *** ACTIONS ***
	
	// *** EXTRA ***
	public MachineType getType();
	public TChannel getChannel();
	public int getIndex();
}
