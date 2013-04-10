package engine.agent.tim.interfaces;

import java.util.List;

import engine.agent.tim.misc.ConveyorEvent;
import engine.agent.tim.misc.MyGlassSensor;

import shared.Glass;
import shared.interfaces.OfflineConveyorFamily;

public interface Sensor {
	public abstract void msgHereIsGlass(Glass glass); // Sometimes this message will be fired through a transducer event instead of just the conveyor family
	// The following messages will be special to transducer events, and will be called after parsing arguments in the EventFired(args[]) function.	
	public abstract void msgGlassOffSensor(Glass glass);
	
	// This method will be used to get the type for the sensor in both real sensors and mock sensors
	public abstract List<String> getType();
	public abstract List<MyGlassSensor> getGlassSheets();
	public abstract void setCF(OfflineConveyorFamily conveyorFamilyImp);

	// These methods will specifically be used for testing purposes -- do not have to be always be implemented
	public abstract void runScheduler();
}
