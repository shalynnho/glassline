/**
 * 
 */
package factory.test.mock;

/**
 * This is the base class for a mock entity, like an agent, or a class that contains agents. 
 * It only defines that the entity should contain a name.
 */
public class MockEntity {
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
