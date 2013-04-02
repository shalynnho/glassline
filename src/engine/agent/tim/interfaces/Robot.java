package engine.agent.tim.interfaces;

import shared.Glass;

public interface Robot {
	public abstract void msgProcessGlass(Glass glass);
	public abstract void msgDoneProcessingGlass(Glass Glass);	
}
