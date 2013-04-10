package engine.agent.tim.interfaces;

import java.util.List;

import engine.agent.tim.misc.ConveyorEvent;
import engine.agent.tim.misc.MyGlassSensor;

import shared.Glass;
import shared.interfaces.OfflineConveyorFamily;

public interface Sensor {
	// Messages -- will forward sensor states to the conveyor
	public abstract void msgUpdateGlassEntrySensorEnter();
	public abstract void msgUpdateGlassEntrySensorExit();
	public abstract void msgUpdateGlassPopUpSensorEnter();	
	public abstract void msgUpdateGlassPopUpSensorExit();
	
	public abstract void setCF(OfflineConveyorFamily conveyorFamilyImp);
}
