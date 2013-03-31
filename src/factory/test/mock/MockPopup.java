package factory.test.mock;

import shared.Glass;
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

}
