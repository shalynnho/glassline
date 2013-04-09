package engine.agent.shay.interfaces;

import shared.Glass;

public interface Conveyor {
	
	public void msgHereIsGlass(Glass g);
	
	public boolean pickAndExecuteAnAction();
		
	public String getName();
	
	public int getIndex();
	
}
