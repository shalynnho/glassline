package engine.agent.david.test.mock;


import java.util.List;

import engine.agent.david.agents.ConveyorAgent.ConveyorState;
import engine.agent.david.interfaces.Conveyor;

import shared.Glass;

public class MockConveyor extends MockEntity implements Conveyor {
	public MockConveyor(String name) {
		super(name);
	}

	@Override
	public void msgHereIsGlass(Glass g) {
		log.add(new LoggedEvent("Received msgHereIsGlass"));
	}

	@Override
	public void msgTakingGlass() {
		log.add(new LoggedEvent("Received msgTakingGlass"));
	}

	@Override
	public void actTellPopupGlassOnConveyor(Glass g) {
		
	}
	
	@Override
	public void actTellSensorPositionFree() {
		
	}

	@Override
	public boolean pickAndExecuteAnAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ConveyorState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Glass> getGlasses() {
		// TODO Auto-generated method stub
		return null;
	}

}
