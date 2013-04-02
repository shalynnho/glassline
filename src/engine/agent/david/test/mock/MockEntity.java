/**
 * 
 */
package engine.agent.david.test.mock;


/**
 * This is like MockAgent, except we call it MockEntity so we can include not only agents, but also classes that contain agents (e.g., ConveyorFamily!) 
 * It only defines that the entity should contain a name.
 */
public class MockEntity {
	private String name;
	public EventLog log = new EventLog();

	public MockEntity(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return this.getClass().getName() + ": " + name;
	}
}
