package factory.test.mock;


public class MockWorkstation extends MockEntity {
	public EventLog log = new EventLog();

	public MockWorkstation(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public void msgHereIsGlass(Glass g) {
		// 
		stateChanged();
	}
}
