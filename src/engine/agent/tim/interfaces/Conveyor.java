package engine.agent.tim.interfaces;

import java.util.List;

import engine.agent.tim.misc.MyGlassConveyor;

import shared.Glass;
import shared.interfaces.ConveyorFamily;

public interface Conveyor {
	public abstract void msgGiveGlassToConveyor(Glass glass);
	public abstract void msgGiveGlassToPopUp(Glass glass);
	public abstract void msgPassOffGlass(Glass glass);
	public abstract void msgUpdateGlass(Glass glass);
	public abstract void msgPositionFree();
	
	// Getters/Setters
	public abstract boolean isConveyorOn();
	public abstract List<MyGlassConveyor> getGlassSheets();
	public abstract void setCF(ConveyorFamily conveyorFamilyImp);	
	
	// These methods will specifically be used for testing purposes -- do not have to be always be implemented
	public abstract void runScheduler();
}
