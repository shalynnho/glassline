package shared.interfaces;

import shared.Glass;

/* Shared interface for the Workstation to interact with various popup agent implementations. */
public interface PopupWorkstationInteraction extends NonnormBreakInteraction {
	public void msgGlassDone(Glass g, int index);
	public void msgGUIBreakWorkstation(boolean stop, int index);
	public void msgGUIBreakRemovedGlassFromWorkstation(int index);
}
