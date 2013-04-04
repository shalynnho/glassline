package engine.agent.shay.interfaces;

import shared.Glass;
import engine.agent.shay.ConveyorFamily;
import engine.agent.shay.enums.ConveyorState;

public interface Conveyor {

	public void msgPassMeGlass(Popup p);
	
	public void msgHereIsGlass(Glass g, ConveyorFamily cf);
	
	public boolean pickAndExecuteAnAction();
	
	public void setPopup(Popup p);

	public void setConveyorFamily(ConveyorFamily cf);

	public ConveyorState getState();
	
	public String getName();
	
	public int getIndex();
	
}
