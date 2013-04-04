package engine.agent.evan.interfaces;

import shared.Glass;
import shared.interfaces.PopupWorkstationInterface;

public interface Popup extends PopupWorkstationInterface {
	public void msgNextGlass(Glass g);
	public void msgPositionFree();
}
