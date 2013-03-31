package factory.test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import transducer.Transducer;
import factory.agents.ConveyorAgent.ConveyorState;
import factory.misc.ConveyorFamilyEntity;
import factory.test.mock.MockSensor;
import factory.test.mock.MockWorkstation;

/**
 * Tests for the conveyor in ConveyorFamilyEntity, i.e., family.conveyor
 */
public class ConveyorTests {
	private MockSensor sensor;
	
	private MockWorkstation workstation1, workstation2;
	private Transducer transducer;
	
	private ConveyorFamilyEntity family;
	
	@Before
	public void setUp() {
		workstation1 = new MockWorkstation("Workstation 1"); // workstations just needed for family constructor
		workstation2 = new MockWorkstation("Workstation 2");
		transducer = new Transducer();
		
		family = new ConveyorFamilyEntity(transducer, workstation1, workstation2);
		
		sensor = new MockSensor("Sensor");
		family.setSensor(sensor);
	}

	@Test
	public void testMsgHereIsGlass() {
		assertThat(family.conv.getState(), is(ConveyorState.NOTHING_TO_DO));
		assertThat(family.conv.getGlasses().isEmpty(), is(true));
	}

	@Test @Ignore
	public void testMsgTakingGlass() {

	}	
	
	
	/**
	 * Test one glass piece moving along the conveyor.
	 */
	@Test @Ignore("later")
	public void testPassSequence() {
		fail("Not yet implemented");
	}

}
