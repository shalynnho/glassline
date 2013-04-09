package engine.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.LineComponent;
import engine.agent.shay.ConveyorAgent;
import engine.agent.shay.interfaces.Conveyor;
import engine.agent.shay.interfaces.TransducerIfc;

public class BigOnlineConveyorFamily implements LineComponent {

	private shared.interfaces.OfflineConveyorFamily previous, next;
	private TransducerIfc transducer;
	
	private Conveyor startConveyor;
	private Conveyor endConveyor;
	private OnlineWorkstationAgent workstation;
	private MachineType type;
	private boolean recPosFree;
	
	private List<Glass> glass = Collections.synchronizedList(new ArrayList<Glass>());
	
	public BigOnlineConveyorFamily(ConveyorAgent start, ConveyorAgent end, OnlineWorkstationAgent o, TransducerIfc t) {
		startConveyor = start;
		endConveyor = end;
		workstation = o;
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

	public boolean getPosFree() {
		return recPosFree;
	}
}
