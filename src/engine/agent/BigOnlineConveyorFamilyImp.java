package engine.agent;

import javax.swing.Timer;

import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.*;
import transducer.*;

public class BigOnlineConveyorFamilyImp implements LineComponent {
	// components
	private GeneralConveyorAgent startConveyor, endConveyor;
	private OnlineWorkstationAgent workstation;
	
	/* Constructor creates components and sets internal next/prev. */
	public BigOnlineConveyorFamilyImp(GeneralConveyorAgent sc, GeneralConveyorAgent ec, OnlineWorkstationAgent w) {
		startConveyor = sc;
		endConveyor = ec;
		workstation = w;
		
		startConveyor.setNext(workstation);
		workstation.setPrev(startConveyor);
		workstation.setNext(endConveyor);
		endConveyor.setPrev(workstation);
	}
		
	/* Messages */
	public void msgHereIsGlass(Glass glass) {
		startConveyor.msgHereIsGlass(glass); // forwarding glass piece
	}
	
	public void msgPositionFree() {
		endConveyor.msgPositionFree();
	}
	
	/* Setters */
	public void setPreviousLineComponent(LineComponent lc) {
		startConveyor.setPrev(lc);
	}
	
	public void setNextLineComponent(LineComponent lc) {
		endConveyor.setNext(lc);
	}
	
	public void startThreads() {
		startConveyor.startThread();
		workstation.startThread();
		endConveyor.startThread();
	}
}
