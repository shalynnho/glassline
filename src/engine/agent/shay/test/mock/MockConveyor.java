package engine.agent.shay.test.mock;

import shared.Glass;
import engine.agent.shay.interfaces.Conveyor;

public class MockConveyor extends MockAgent implements Conveyor {

	int index;
	
	public MockConveyor(String name, int index) {
		super(name);
		this.index = index;
	}

	public EventLog log = new EventLog();
	
//	@Override
//	public void msgPassMeGlass(Popup p) {
//		log.add(new LoggedEvent(
//				"Received msgPassMeGlass from Popup: " + p.getName() + ", index: " + p.getIndex()));		
//	}
//
//	@Override
//	public void msgHereIsGlass(Glass g, ConveyorFamily cf) {
//		log.add(new LoggedEvent(
//				"Received msgHereIsGlass from ConveyorFamily: " + cf.getType() + ", GlassID: " + g.getID()));
//	}

	@Override
	public boolean pickAndExecuteAnAction() {
		return false;
	}

//
//	@Override
//	public void setConveyorFamily(ConveyorFamily cf) {
//		// TODO Auto-generated method stub
//		
//	}
//	@Override
//	public ConveyorState getState() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setPopup(engine.agent.shay.interfaces.Popup p) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return index;
	}
@Override
public void msgHereIsGlass(Glass g) {
	// TODO Auto-generated method stub
	
}

}
