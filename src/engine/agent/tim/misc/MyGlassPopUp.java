package engine.agent.tim.misc;

import shared.Glass;

public class MyGlassPopUp {
	public enum processState {unprocessed, doneProcessing};
	public Glass glass;
	public processState processState;
	
	public MyGlassPopUp(Glass glass, processState processState) {
		this.glass = glass;
		this.processState = processState;
	}
}
// The reason why there is not a middle stage is because this glass is removed from the pop-up 
// during processing – there should be no reference to a glass sheet that is being processed in the 
// pop-up agent when it is not with the pop-up agent and with the robot or machine agents