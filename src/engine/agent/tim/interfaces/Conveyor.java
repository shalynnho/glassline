package engine.agent.tim.interfaces;

import java.util.List;

import engine.agent.tim.misc.ConveyorEvent;
import engine.agent.tim.misc.MyGlassConveyor;

import shared.Glass;
import shared.interfaces.OfflineConveyorFamily;

public interface Conveyor {
	public abstract void msgGiveGlassToConveyor(Glass glass);
	public abstract void msgGiveGlassToPopUp(Glass glass);
	public abstract void msgPassOffGlass(Glass glass);
	public abstract void msgUpdateGlass(Glass glass);
	public abstract void msgPositionFree();
	
	public abstract void msgUpdateGlass(ConveyorEvent e);
	
	// Getters/Setters
	public abstract boolean isConveyorOn();
	public abstract List<MyGlassConveyor> getGlassSheets();
	public abstract void setCF(OfflineConveyorFamily conveyorFamilyImp);	
	
	// These methods will specifically be used for testing purposes -- do not have to be always be implemented
	public abstract void runScheduler();
}
