package engine.agent.tim.misc;

import shared.Glass;

public class MyGlassConveyor {
	public enum conveyorState {beforeEntrySensor, onEntrySensor, beforePopUpSensor, onPopUpSensor, referenceJustsentToPopUp, beforePopUp};
	public Glass glass;
	public conveyorState conveyorState;
	public MyGlassConveyor(Glass glass, conveyorState conveyorState) {
		this.glass = glass;
		this.conveyorState = conveyorState;
	}
}