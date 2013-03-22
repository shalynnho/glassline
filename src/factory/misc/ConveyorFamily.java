package factory.misc;

import transducer.TChannel;
import transducer.TEvent;
import engine.agent.Agent;
import factory.interfaces.ConveyorFamily;

/**
 * Key class that contains agents to represent the conveyor family.
 * ConveyorFamily design; follows the "sensor-conveyor-popup" pattern, so the sensor b/w the conveyor and popup is absorbed
 * @author David Zhang
 */
public class ConveyorFamily {
	// *** Constructor(s) ***
	public ConveyorFamily(Transducer transducer) {
		sensor = new Sensor(this, transducer);
		conv = new ConveyorAgent(this, transducer);
		popup = new PopupAgent(this, transducer);
	}

	// *** DATA - accessible by agents ***
	SensorAgent sensor;
	ConveyorAgent conv;
	PopupAgent popup;
	public enum GlassState { NEEDS_PROCESSING, DOES_NOT_NEED_PROCESSING, PROCESSED }
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
