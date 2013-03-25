package factory.misc;

import shared.Glass;
import shared.enums.MachineType;
import transducer.Transducer;
import factory.agents.ConveyorAgent;
import factory.agents.PopupAgent;
import factory.agents.SensorAgent;
import factory.agents.WorkstationAgent;

/**
 * Key class that contains agents to represent the conveyor family.
 * ConveyorFamily design; follows the "sensor-conveyor-popup" pattern, so the sensor b/w the conveyor and popup is absorbed
 * @author David Zhang
 */
public class ConveyorFamily {
	// *** Constructor(s) ***
	public ConveyorFamily(Transducer transducer, WorkstationAgent workstation) {
		this.workstation = workstation;  
		
		sensor = new SensorAgent(this, transducer);
		conv = new ConveyorAgent(this, transducer);
		popup = new PopupAgent(this, transducer, workstation);
		
		this.type = type;
	}

	// *** DATA - mostly accessible by contained agents ***
	private MachineType type;
	
	public SensorAgent sensor;
	public ConveyorAgent conv;
	public PopupAgent popup;
	private WorkstationAgent workstation;

	public ConveyorFamily nextFamily;
	public ConveyorFamily prevFamily;

	public enum GlassState { NEEDS_PROCESSING, DOES_NOT_NEED_PROCESSING, FINISHED } // FINISHED means workstation is done processing it

	// State of conveyor family so we know if the conveyor is on or off because (BC) of whatever reasons; mainly used for testing/validation
	public RunningState runningState = RunningState.OFF_BC_QUIET;
	public enum RunningState {
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
		public boolean isFinished() {
			return state == GlassState.FINISHED;
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

	// *** EXTRA ***
	public void setNextConveyorFamily(ConveyorFamily f)	{
		nextFamily = f;
	}
	public void setPreviousConveyorFamily(ConveyorFamily f)	{
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
}
