package factory_david.test.mock;

import java.util.List;

import shared.Glass;
import factory_david.agents.PopupAgent.PopupState;
import factory_david.agents.PopupAgent.WorkstationState;
import factory_david.interfaces.Popup;
import factory_david.misc.ConveyorFamilyEntity.MyGlass;

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
