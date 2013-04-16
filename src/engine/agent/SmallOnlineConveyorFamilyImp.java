package engine.agent;

import javax.swing.Timer;

import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.LineComponent;
import transducer.Transducer;

public class SmallOnlineConveyorFamilyImp implements LineComponent {
	// components
	private GeneralConveyorAgent conveyor;
	private OnlineWorkstationAgent workstation;
	
	/* Constructor creates components and sets internal next/prev. */
	public SmallOnlineConveyorFamilyImp(GeneralConveyorAgent c, OnlineWorkstationAgent w) {
		conveyor = c;
		workstation = w;
		
		conveyor.setNext(workstation);
		workstation.setPrev(conveyor);
	}

	/* Messages */
	public void msgHereIsGlass(Glass g) {
		conveyor.msgHereIsGlass(g);
	}
	
	public void msgPositionFree() {
		workstation.msgPositionFree();
	}
	
	/* Setters */
	public void setPreviousLineComponent(LineComponent lc) {
		conveyor.setPrev(lc);
	}
	
	public void setNextLineComponent(LineComponent lc) {
		workstation.setNext(lc);
	}
	
	public void startThreads() {
		conveyor.startThread();
		workstation.startThread();
	}
}
