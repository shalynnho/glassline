package engine.agent.tim.interfaces;

import java.util.List;

import engine.agent.tim.misc.ConveyorEvent;
import engine.agent.tim.misc.MyGlassConveyor;

import shared.Glass;
import shared.interfaces.NonnormBreakInteraction;
import shared.interfaces.OfflineConveyorFamily;

public interface Conveyor extends NonnormBreakInteraction {
	public abstract void msgGiveGlassToConveyor(Glass glass);
	public abstract void msgPositionFree();	
	public abstract void msgUpdateGlass(ConveyorEvent e);
	
	// Getters/Setters
	public abstract boolean isConveyorOn();
	public abstract List<MyGlassConveyor> getGlassSheets();
	public abstract void setCF(OfflineConveyorFamily conveyorFamilyImp);
	public abstract Integer getGUIIndex();	
	
}
