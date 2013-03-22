package factory.interfaces;

public interface Sensor {
	// *** MESSAGES ***
	public abstract void msgHereIsGlass(); // from previous agent
	public abstract void msgPositionFree(); // from next agent

	// *** ACTIONS ***
	public abstract void actPassOnGlass();
	
	// *** EXTRA ***
}
