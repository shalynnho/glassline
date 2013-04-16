package shared.interfaces;

/* This interface allows for the GUI non-norm break and unbreak conveyor interaction. */
public interface NonnormConveyorInteraction {
	void msgGUIBreak(boolean stop); // should stop the conveyor or restart it
}
