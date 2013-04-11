package engine.agent.tim.misc;

import shared.Glass;

public class MyGlassPopUp {
	public enum processState {awaitingArrival, unprocessed, processing, doneProcessing, awaitingRemoval};
	public Glass glass;
	public processState processState;
	public int machineIndex;
	
	public MyGlassPopUp(Glass glass, processState processState) {
		this.glass = glass;
		this.processState = processState;
		this.machineIndex = 0; // Dummy value to start, will be modified by PopUpAgent
	}
}