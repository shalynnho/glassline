package engine.agent.shay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import shared.Glass;
import shared.agents.OnlineWorkstationAgent;
import shared.enums.MachineType;
import shared.interfaces.Workstation;
import engine.agent.shay.interfaces.Conveyor;
import engine.agent.shay.interfaces.TransducerIfc;

public class BigOnlineConveyorFamily implements shared.interfaces.ConveyorFamily {

	private shared.interfaces.ConveyorFamily previous, next;
	private TransducerIfc transducer;
	
	private Conveyor startConveyor;
	private Conveyor endConveyor;
	private Workstation workstation;
	private MachineType type;
	private boolean recPosFree;
	
	private List<Glass> glass = Collections.synchronizedList(new ArrayList<Glass>());
	
	public BigOnlineConveyorFamily(ConveyorAgent start, ConveyorAgent end, Workstation o, TransducerIfc t) {
		startConveyor = start;
		endConveyor = end;
		workstation = (OnlineWorkstationAgent) o;
		type = workstation.getType();
		transducer = t;
		recPosFree = true;
	}
		
	// ***** MESSAGES ***** //
	@Override
	public void msgHereIsGlass(Glass glass) {
		startConveyor.msgHereIsGlass(glass); // forwarding glass piece
	}

	@Override
	public void msgPositionFree() {
		recPosFree = true; // from next ConveyorFamily
	}

	@Override
	public void msgGlassDone(Glass glass, int machineIndex) {
	}
	
	public boolean getPosFree() {
		return recPosFree;
	}
}
