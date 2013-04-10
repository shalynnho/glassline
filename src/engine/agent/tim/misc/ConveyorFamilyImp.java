package engine.agent.tim.misc;

import java.util.*;

import engine.agent.tim.agents.*;
import engine.agent.tim.interfaces.*;
import shared.Glass;
import shared.interfaces.*;

public class ConveyorFamilyImp implements OfflineConveyorFamily {
	//Name: ConveyorFamilyImp

	//Description:  Will act as a wrapper class for a set of conveyors, sensors, and pop-ups.  It will also contain a reference to robots and machines through its components

	//Data:
	private OfflineConveyorFamily nextCF; // reference to the next ConveyorFamily – this could even be the final truck at the end of the line
	private OfflineConveyorFamily prevCF; // reference to the previous conveyor family, will be NULL if it does not exist
	private Conveyor conveyor;
	private Sensor sensors; // Will hold all of the sensors of different types in one place – adds to the modularity of the system
	private PopUp popUp;
	private String name;
	
	//Constructors:
	public ConveyorFamilyImp(String name, Conveyor conveyor, Sensor sensors, PopUp popUp) {
		this.name = name;
		this.conveyor = conveyor;
		this.sensors = sensors;
		this.popUp = popUp;
		
		// Set the CF references for these components
		this.conveyor.setCF(this);
		this.popUp.setCF(this);
		this.sensors.setCF(this);
	}

	//Messages:
	public void msgHereIsGlass(Glass g) {
		conveyor.msgGiveGlassToConveyor(g);
		System.out.println(name + ": Messaged conveyor with glass: " + g.getID());
	}

	public void msgPositionFree() {
		conveyor.msgPositionFree();
		System.out.println(name + ": Messaged conveyor that glass can to passed to next conveyor system.");
	}

	public void msgGlassDone(Glass g, int machineIndex) {
		getPopUp().msgGlassDone(g, machineIndex);
		System.out.println(name + ": Messaged pop up with processed glass.");
	}
	
	//Other Methods:

	public Conveyor getConveyor() {
		return conveyor;
	}

	public void setConveyor(Conveyor conveyor) {
		this.conveyor = conveyor;
	}

	public OfflineConveyorFamily getPrevCF() {
		return prevCF;
	}

	public void setPrevCF(OfflineConveyorFamily prevCF) {
		this.prevCF = prevCF;
	}

	public PopUp getPopUp() {
		return popUp;
	}

	public void setPopUp(PopUp popUp) {
		this.popUp = popUp;
	}

	public OfflineConveyorFamily getNextCF() {
		return nextCF;
	}

	public void setNextCF(OfflineConveyorFamily nextCF) {
		this.nextCF = nextCF;
	}

	public String getName() {
		return name;
	}
	
	public Sensor getSensor() {
		return sensors;
	}
}
