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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgPositionFree() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgGlassDone(Glass g, int index) {
		// TODO Auto-generated method stub
		
	}

}
