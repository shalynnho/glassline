package shared.interfaces;

import shared.Glass;

/* Shared interface for the Workstation to interact with various popup agent implementations. */
public interface PopupWorkstationInterface {
	public void msgGlassDone(Glass g, int index);
}
