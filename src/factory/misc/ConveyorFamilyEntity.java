package factory.misc;

import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.ConveyorFamily;
import transducer.Transducer;
import factory.agents.ConveyorAgent;
import factory.agents.PopupAgent;
import factory.agents.SensorAgent;
import factory.interfaces.Conveyor;
import factory.interfaces.Popup;
import factory.interfaces.Sensor;
import factory.interfaces.Workstation;
import factory.test.mock.MockConveyor;
import factory.test.mock.MockConveyorFamily;

/**
 * Key class that represents my version of the ConveyorFamily design (hence its implementation of ConveyorFamily).
 * Contains agents to represent the ConveyorFamily, whose interface is broadly used by other team members.
 * Follows the "sensor-conveyor-popup" pattern, so the sensor b/w the conveyor and popup is absorbed.
 * @author David Zhang
 */
public class ConveyorFamilyEntity implements ConveyorFamily {
	// *** Constructor(s) ***
	public ConveyorFamilyEntity(Transducer transducer, Workstation workstation1, Workstation workstation2) {
		sensor = new SensorAgent(this, transducer);
		conv = new ConveyorAgent(this, transducer);
		popup = new PopupAgent(this, transducer, workstation1, workstation2);
		
		this.type = workstation1.getType(); // workstations should have same type
	}

	// *** DATA - mostly accessible by contained agents ***
	private MachineType type;
	
	public Sensor sensor;
	public Conveyor conv;
	public Popup popup;

	public ConveyorFamily nextFamily;
	public ConveyorFamily prevFamily;

	public enum GlassState { NEEDS_PROCESSING, DOES_NOT_NEED_PROCESSING }

	// State of conveyor family so we know if the conveyor is on or off because (BC) of whatever reasons; mainly used for testing/validation
	public RunningState runningState = RunningState.OFF_BC_QUIET;
	public enum RunningState {
		// On states are listed in order of how they would appear. Off states come in between.
		ON_BC_SENSOR_TO_CONVEYOR, ON_BC_CONVEYOR_TO_SENSOR, ON_BC_SENSOR_TO_POPUP,
		OFF_BC_QUIET, OFF_BC_WAITING_AT_SENSOR
	}
	
	public class MyGlass {
		public MyGlass(Glass g, GlassState s) {
			this.glass = g;
			this.state = s;
		}
		private Glass glass; 
		private GlassState state;

		public boolean needsProcessing() {
			return state == GlassState.NEEDS_PROCESSING;
		}
		public void setState(GlassState s) {
			state = s;
		}
		public GlassState getState() {
			return state;
		}
		public Glass getGlass() {
			return glass;
		}
	}

	// *** MESSAGES - just passes on immediately to appropriate agent ***
	public void msgHereIsGlass(Glass g) {
		sensor.msgHereIsGlass(g);
	}

	public void msgPositionFree() {
		popup.msgPositionFree();
	}
	
	public void msgGlassDone(Glass g, int index) {
		popup.msgGlassDone(g, index); // pass to popup
	}

	// *** EXTRA ***
	public void setNextConveyorFamily(ConveyorFamily f) {
		nextFamily = f;
	}
	public void setPreviousConveyorFamily(ConveyorFamily f) {
		prevFamily = f;
	}
	public GlassState decideIfGlassNeedsProcessing(Glass g) {
		GlassState gs = null;
		if (g.getNeedsProcessing(this.type))
			gs = GlassState.NEEDS_PROCESSING;
		else
			gs = GlassState.DOES_NOT_NEED_PROCESSING;
		return gs;
	}
	
	/* Testing helpers */
	public void setConveyor(Conveyor c) {
		conv = c;
	}
	public void setSensor(Sensor s) {
		sensor = s;
	}
	
	public MockConveyor getMockConveyor() {
		return (MockConveyor) conv;
	}
	public MockConveyorFamily getMockPrevConveyorFamily() {
		return (MockConveyorFamily) prevFamily;
	}
}
