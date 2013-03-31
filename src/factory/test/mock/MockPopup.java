package factory.test.mock;

import java.util.List;

import shared.Glass;
import factory.agents.PopupAgent.PopupState;
import factory.interfaces.Popup;
import factory.misc.ConveyorFamilyEntity.MyGlass;

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

}
