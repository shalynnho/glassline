package factory.test.mock;

import factory.interfaces.Popup;

public class MockPopup extends MockEntity implements Popup {

	public MockPopup(String name) {
		super(name);
	}

	@Override
	public void msgPositionFree() {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgGlassDone() {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgGlassComing(MyGlass myGlass) {
		// TODO Auto-generated method stub
		
	}

}
