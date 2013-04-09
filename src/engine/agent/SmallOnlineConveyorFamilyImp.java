package engine.agent;

import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.LineComponent;
import transducer.Transducer;

public class SmallOnlineConveyorFamilyImp implements LineComponent {
	
	public SmallOnlineConveyorFamilyImp(MachineType type, Transducer trans, int convIndex) {
		conveyor = new SmallConveyorAgent(type.toString()+" conveyor", trans, this, convIndex);
		workstation = new OnlineWorkstationAgent(type.toString()+" workstation", type, conveyor.transducer);
	}
	
	public SmallConveyorAgent conveyor;
	public OnlineWorkstationAgent workstation;
	public LineComponent prev, next; // the previous and next families
	
	@Override
	public void msgHereIsGlass(Glass glass) {
		conveyor.msgHereIsGlass(glass);
	}

	@Override
	public void msgPositionFree() {
		conveyor.msgPositionFree();
	}
	
	public void setNextLineComponent(LineComponent l) {
		next = l;
	}

	public void setPreviousLineComponent(LineComponent l) {
		prev = l;
	}
	
	public void startThreads() {
		conveyor.startThread();
		workstation.startThread();
	}

}
