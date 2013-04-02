package factory_david.interfaces;

import java.util.List;

import shared.Glass;
import factory_david.agents.SensorAgent.SensorState;

public interface Sensor {
	// *** MESSAGES ***
	public void msgHereIsGlass(Glass g); // from previous agent
	public void msgPositionFree(); // from next agent

	// *** SCHEDULER ***
	public boolean pickAndExecuteAnAction();
	
	// *** ACTIONS ***
	public void actPassOnGlass(Glass g);
	public void actTellPrevFamilyPositionFree();
	
	// *** EXTRA ***
	public List<Glass> getGlasses();
	public SensorState getState();
}
