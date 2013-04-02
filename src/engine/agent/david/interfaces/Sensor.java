package engine.agent.david.interfaces;

import java.util.List;

import engine.agent.david.agents.SensorAgent.SensorState;

import shared.Glass;

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
