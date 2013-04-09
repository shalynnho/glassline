package engine.agent;

import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.LineComponent;

public class SmallOnlineConveyorFamilyImp implements LineComponent {
	
	public SmallOnlineConveyorFamilyImp(int index, MachineType type) {
		conveyor = new SmallConveyorAgent(this, index);
		workstation = new OnlineWorkstationAgent(type.toString() + "wks", type, conveyor.transducer);
	}
	
	protected SmallConveyorAgent conveyor;
	protected OnlineWorkstationAgent workstation;
	
	@Override
	public void msgHereIsGlass(Glass glass) {
		conveyor.msgHereIsGlass(glass);
	}

	@Override
	public void msgPositionFree() {
		conveyor.msgPositionFree();
	}

}
