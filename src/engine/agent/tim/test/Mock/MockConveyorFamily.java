package engine.agent.tim.test.Mock;

import java.util.ArrayList;
import java.util.List;

import engine.agent.tim.interfaces.Conveyor;
import engine.agent.tim.interfaces.PopUp;
import engine.agent.tim.interfaces.Sensor;

import shared.Glass;
import shared.interfaces.OfflineConveyorFamily;

public class MockConveyorFamily implements OfflineConveyorFamily {
	//Data:
	public List<Glass> glassSheets = new ArrayList<Glass>(); // Will hold the glass to be received from the previous ConveyorFamily
	public String name;
	public OfflineConveyorFamily prevCF;
	public OfflineConveyorFamily nextCF;
	
	public EventLog log = new EventLog();
	
	//Constructors:
	public MockConveyorFamily(String name) {
		this.name = name;
	}

	//Messages:
	public void msgHereIsGlass(Glass g) {
		glassSheets.add(g);
		System.out.println(name + ": Glass added to here.");
	}
	
	public void msgPositionFree() {
		System.out.println(name + ": Messaged popUp that glass can to passed to next conveyor system.");
		log.add(new LoggedEvent(name + ": Messaged popUp that glass can to passed to next conveyor system."));
	}
	
	public void sendGlassToNextCF() { // Hack method for testing, I will make sure to add in the glass through msgHereIsGlass before calling this method in testing
		nextCF.msgHereIsGlass(glassSheets.get(0));
	}

	@Override
	public void msgGlassDone(Glass glass, int machineIndex) {
		// TODO Auto-generated method stub
		
	}

	public OfflineConveyorFamily getPrevCF() {
		return prevCF;
	}

	public void setPrevCF(OfflineConveyorFamily prevCF) {
		this.prevCF = prevCF;
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
	
	public void sendPositionFree() { // Hack method for unit testing
		prevCF.msgPositionFree(); // Allow the prevCF to send another piece of glass over
	}

	public Sensor getSensor(String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	public Conveyor getConveyor() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setConveyor(Conveyor conveyor) {
		// TODO Auto-generated method stub
		
	}

	public PopUp getPopUp() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPopUp(PopUp popUp) {
		// TODO Auto-generated method stub
		
	}
}
