package factory.misc;

import shared.Glass;
import transducer.Transducer;
import factory.agents.ConveyorAgent;
import factory.agents.PopupAgent;
import factory.agents.SensorAgent;

/**
 * Key class that contains agents to represent the conveyor family.
 * ConveyorFamily design; follows the "sensor-conveyor-popup" pattern, so the sensor b/w the conveyor and popup is absorbed
 * @author David Zhang
 */
public class ConveyorFamily {
	// *** Constructor(s) ***
	public ConveyorFamily(Transducer transducer) {
		sensor = new SensorAgent(this, transducer);
		conv = new ConveyorAgent(this, transducer);
		popup = new PopupAgent(this, transducer);
	}

	// *** DATA - mostly accessible by agents ***
	public SensorAgent sensor;
	public ConveyorAgent conv;
	public PopupAgent popup;
	public enum GlassState { NEEDS_PROCESSING, DOES_NOT_NEED_PROCESSING, PROCESSED }

	// State of conveyor family so we know if the conveyor is on or off because (BC) of whatever reasons; mainly used for testing/validation
	public RunningState runningState = OFF_BC_QUIET;
	public enum RunningState = {
		ON_BC_SENSOR_TO_CONVEYOR, ON_BC_CONVEYOR_TO_SENSOR, ON_BC_SENSOR_TO_POPUP,
		OFF_BC_QUIET, OFF_BC_WAITING_AT_SENSOR
	}

	
	class MyGlass {
		public MyGlass(Glass g, GlassState s) {
			this.glass = g;
			this.state = s;
		}
		Glass glass; GlassState state;
	}

	// *** MESSAGES - just passes on immediately to appropriate agent ***
	@Override
	public void msgHereIsGlass(Glass g) {
		sensor.msgHereIsGlass(g);
	}

	@Override
	public void msgPositionFree() {
		popup.msgPositionFree();
	}
}
