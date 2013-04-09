package engine.agent.evan.interfaces;

import shared.Glass;
import shared.interfaces.PopupWorkstationInteraction;

public interface Popup extends PopupWorkstationInteraction {
	public void msgNextGlass(Glass g);
	public void msgPositionFree();
}
