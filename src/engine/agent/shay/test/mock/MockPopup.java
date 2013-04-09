
//package engine.agent.shay.test.mock;
//
//import shared.Glass;
//import shared.enums.MachineType;
//import engine.agent.shay.ConveyorFamily;
//import engine.agent.shay.interfaces.Conveyor;
//import engine.agent.shay.interfaces.Popup;
//
//public class MockPopup extends MockAgent implements Popup {
//	
//	int index;
//	MachineType type;
//
//	public MockPopup(String name, int index, MachineType t) {
//		super(name);
//		this.index = index;
//		type = t;
//	}
//	
//	public EventLog log = new EventLog();
//
//	@Override
//	public void msgIHaveGlass(Glass g, Conveyor c) {
//		log.add(new LoggedEvent(
//				"Received msgIHaveGlass from Conveyor: " + c.getName() + ", index: " + 
//						c.getIndex() + ", GlassID: " + g.getID()));
//	}
//
//	@Override
//	public void msgHereIsGlass(Glass g, Conveyor c) {
//		log.add(new LoggedEvent(
//				"Received msgHereIsGlass from Conveyor: " + c.getName() + ", index: " + 
//						c.getIndex() + ", GlassID: " + g.getID()));
//	}
//
//	@Override
//	public void msgGlassDone(Glass g, int machineIndex, ConveyorFamily c) {
//		log.add(new LoggedEvent(
//				"Received msgGlassDone from ConveyorFamily: " + c.getType()  + ", GlassID: " + g.getID() + ", machineIndex: " + machineIndex));
//	}
//
//	@Override
//	public boolean pickAndExecuteAnAction() {
//		return false;
//	}
//
//	@Override
//	public void setConveyor(Conveyor c) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setConveyorFamily(ConveyorFamily cf) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public MachineType getType() {
//		// TODO Auto-generated method stub
//		return type;
//	}
//
//	@Override
//	public int getIndex() {
//		// TODO Auto-generated method stub
//		return index;
//	}
//
//}
