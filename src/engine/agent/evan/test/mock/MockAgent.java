package engine.agent.evan.test.mock;

/**
 * This is the base class for a mock agent. It only defines that a mock agent should contain a name and an EventLog.
 * 
 * @author Sean Turner
 * 
 */
public class MockAgent {
	private String name;
	public EventLog log;

	public MockAgent(String name) {
		this.name = name;
		this.log = new EventLog();
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return this.getClass().getName() + ": " + name;
	}

}
