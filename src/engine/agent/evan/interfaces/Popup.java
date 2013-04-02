package engine.agent.evan.interfaces;

import shared.Glass;

public interface Popup {
	public void msgNextGlass(Glass g);
	public void msgPositionFree();
	public void msgGlassDone(Glass g, int index);
}
