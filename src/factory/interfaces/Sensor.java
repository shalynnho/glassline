package factory.interfaces;

import java.util.List;

import factory.agents.SensorAgent.SensorState;
import shared.Glass;

public interface Sensor {
	// *** MESSAGES ***
	public void msgHereIsGlass(Glass g); // from previous agent
	public void msgPositionFree(); // from next agent

	// *** SCHEDULER ***
	public void pickAndExecuteAnAction();
	
	// *** ACTIONS ***
	public void actPassOnGlass(Glass g);
	public void actTellPrevFamilyPositionFree();
	
	// *** EXTRA ***
	public List<Glass> getGlasses();
	public SensorState getState();
}
