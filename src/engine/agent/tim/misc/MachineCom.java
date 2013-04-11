package engine.agent.tim.misc;

import engine.agent.OfflineWorkstationAgent;

public class MachineCom { // Will hold a communication channel to a robot, allowing for the possibility to communicate to multiple robots at once
	public OfflineWorkstationAgent machine; // Robot reference
	public boolean inUse; // Is this channel currently occupied by a piece of glass
	public int machineIndex; // Where the machine is located within the animation
	
	public MachineCom(OfflineWorkstationAgent machine, int machineIndex) {
		this.machine = machine;
		this.inUse = false; // At start, this channel is obviously not being used, so it has to be false
		this.machineIndex = machineIndex; 
	}
}
