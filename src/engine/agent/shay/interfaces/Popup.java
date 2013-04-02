package engine.agent.shay.interfaces;

import shared.Glass;
import shared.enums.MachineType;
import engine.agent.shay.ConveyorFamily;

public interface Popup {

	public void msgIHaveGlass(Glass g, Conveyor c);
	
	public void msgHereIsGlass(Glass g, Conveyor c);
	
	public void msgGlassDone(Glass g, int machineIndex, ConveyorFamily c);
	
	public boolean pickAndExecuteAnAction();
	
	public void setConveyor(Conveyor conveyor);
	
	public void setConveyorFamily(ConveyorFamily cf);
	
	public MachineType getType();

	public String getName();

	public int getIndex();
}
