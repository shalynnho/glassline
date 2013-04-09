package engine.agent.shay.interfaces;

import shared.Glass;
import engine.agent.shay.enums.ConveyorState;

public interface Conveyor {
	
	public void msgHereIsGlass(Glass g);
	
	public boolean pickAndExecuteAnAction();
	
	public ConveyorState getState();
	
	public String getName();
	
	public int getIndex();
	
}
