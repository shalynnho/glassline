package engine.agent.david.test.mock;

import java.util.List;

import engine.agent.david.agents.PopupAgent.PopupState;
import engine.agent.david.agents.PopupAgent.WorkstationState;
import engine.agent.david.interfaces.Popup;
import engine.agent.david.misc.ConveyorFamilyEntity.MyGlass;

import shared.Glass;

public class MockPopup extends MockEntity implements Popup {
	public MockPopup(String name) {
		super(name);
	}

	@Override
	public void msgGlassComing(MyGlass myGlass) {
		log.add(new LoggedEvent("Received msgGlassComing"));
	}

	@Override
	public void msgPositionFree() {
		log.add(new LoggedEvent("Received msgPositionFree"));
	}

	@Override
	public void msgGlassDone(Glass g, int index) {
		log.add(new LoggedEvent("Received msgGlassDone"));
	}

	@Override
	public boolean pickAndExecuteAnAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<MyGlass> getGlasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PopupState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Glass> getFinishedGlasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIsUp(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getNextPosFree() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void seedFinishedGlasses() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWorkstationState(int i, WorkstationState s) {
		// TODO Auto-generated method stub
		
	}

}
