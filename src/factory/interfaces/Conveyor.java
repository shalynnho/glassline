package factory.interfaces;

public interface Conveyor {
	
	// *** MESSAGES ***
	public abstract void msgHereIsGlass(); // from previous sensor
	public abstract void msgTakingGlass(); // from following popup
	
	// *** ACTIONS ***
	public abstract void actReadyForGlass();
	
	
	// *** EXTRA ***
}
