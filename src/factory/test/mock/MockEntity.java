/**
 * 
 */
package factory.test.mock;


/**
 * This is like MockAgent, except we call it MockEntity so we can include not only agents, but also classes that contain agents (e.g., ConveyorFamily!) 
 * It only defines that the entity should contain a name.
 */
public class MockEntity {
	public EventLog log = new EventLog();
	private String name;

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
