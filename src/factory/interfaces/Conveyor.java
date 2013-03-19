package factory.interfaces;

public interface Conveyor {
	
	// *** MESSAGES ***
	public abstract void msgHereIsGlass(); // from previous sensor (sensor1)
	public abstract void msgPositionFree(); // from next sensor (sensor2)
	
	// *** ACTIONS ***
	public abstract void actReadyForGlass();
	
	
	// *** EXTRA ***
}
