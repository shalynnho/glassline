package factory.test.mock;

import shared.Glass;


public class MockWorkstation extends MockEntity {
	public EventLog log = new EventLog();

	public MockWorkstation(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public void msgHereIsGlass(Glass g) {
	}
}
