package engine.agent.tim.misc;

import java.util.*;

import engine.agent.Agent;
import engine.agent.OfflineWorkstationAgent;
import engine.agent.tim.agents.*;
import engine.agent.tim.interfaces.*;
import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.*;
import transducer.Transducer;

public class ConveyorFamilyImp implements OfflineConveyorFamily {
	//Name: ConveyorFamilyImp

	//Description:  Will act as a wrapper class for a set of conveyors, sensors, and pop-ups.  It will also contain a reference to robots and machines through its components

	//Data:
	private LineComponent nextCF; // reference to the next ConveyorFamily – this could even be the final truck at the end of the line
	private LineComponent prevCF; // reference to the previous conveyor family, will be NULL if it does not exist
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
	
	// Alternate Constructor that creates the agents inside of the conveyorFamily with names and indexes given in the constructor, along with two machines
	public ConveyorFamilyImp(String name, Transducer transducer,
			String sensorName, int entrySensorIndex, int popUpSensorIndex, 
			String conveyorName, int conveyorIndex, 
			String popUpName, int popUpIndex,
			OfflineWorkstationAgent[] machines, MachineType processType) 
	{
		this.name = name;
		
		// Now set up all of the agents
		this.sensors = new SensorAgent(name + " " + sensorName, transducer, entrySensorIndex, popUpSensorIndex); 
		this.conveyor = new ConveyorAgent(name + " " + conveyorName, transducer, conveyorIndex);
		this.popUp = new PopUpAgent(name + " " + popUpName, transducer, machines, popUpIndex);
		
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
		popUp.msgPositionFree();
		System.out.println(name + ": Messaged popUp that glass can to passed to next conveyor system.");
	}

	public void msgGlassDone(Glass g, int machineIndex) {
		getPopUp().msgGlassDone(g, machineIndex);
		System.out.println(name + ": Messaged pop up with processed glass.");
	}
	
	//Other Methods:
	
	public void startThreads() {
		// Start all of the agent threads
		((Agent) this.sensors).startThread();
		((Agent) this.conveyor).startThread();
		((Agent) this.popUp).startThread();
	}

	public Conveyor getConveyor() {
		return conveyor;
	}

	public void setConveyor(Conveyor conveyor) {
		this.conveyor = conveyor;
	}

	public LineComponent getPrevCF() {
		return prevCF;
	}

	public void setPreviousLineComponent(LineComponent prevCF) {
		this.prevCF = prevCF;
	}

	public PopUp getPopUp() {
		return popUp;
	}

	public void setPopUp(PopUp popUp) {
		this.popUp = popUp;
	}

	public LineComponent getNextCF() {
		return nextCF;
	}

	public void setNextLineComponent(LineComponent nextCF) {
		this.nextCF = nextCF;
	}

	public String getName() {
		return name;
	}
	
	public Sensor getSensor() {
		return sensors;
	}
}
