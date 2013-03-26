package factory.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import shared.Glass;
import transducer.Transducer;
import factory.agents.SensorAgent.SensorState;
import factory.misc.ConveyorFamilyEntity;
import factory.test.mock.MockConveyor;
import factory.test.mock.MockConveyorFamily;
import factory.test.mock.MockWorkstation;

/**
 * Tests for the sensor in ConveyorFamilyEntity, i.e., family.sensor
 */
public class SensorTests {
	private MockConveyorFamily prevFamily;
	private MockWorkstation workstation;
	private Transducer transducer;
	private MockConveyor conveyor;
	
	private ConveyorFamilyEntity family;
	
	@Before
	public void setUp() {
		prevFamily = new MockConveyorFamily("Previous Family");
		workstation = new MockWorkstation("Workstation");
		transducer = new Transducer();
		conveyor = new MockConveyor("Conveyor");
		
		family = new ConveyorFamilyEntity(transducer, workstation);
		family.setPreviousConveyorFamily(prevFamily);
		family.setConveyor(conveyor);
	}
	
	@Test
	public void testReceiveHereIsGlass() {
		assertEquals("Sensor's state starts out as NOTHING_TO_DO", family.sensor.getState(), SensorState.NOTHING_TO_DO);
		assertThat(family.sensor.getGlasses().isEmpty(), is(true));
		
		Glass g = new Glass();
		Glass g2 = new Glass();
		
		family.msgHereIsGlass(g);
		
		assertEquals("Sensor's state becomes GLASS_JUST_ARRIVED when the family receives a glass", family.sensor.getState(), SensorState.GLASS_JUST_ARRIVED);
		assertThat(family.sensor.getGlasses().size(), is(1));
		
		int idOfGlassAtSensor = family.sensor.getGlasses().get(0).getID();
		
		assertEquals("Sensor should receive proper glass", idOfGlassAtSensor, g.getID());
		assertThat(idOfGlassAtSensor, is(not(g2.getID())));
		
		// how do I mock family.conveyor if conveyorfamilyentity is used and isn't a mock?
		// set mockconveyor to override family.conveyor
	
		// next step
//		family.sensor.pickAndExecuteAnAction();
//		assertTrue("Conveyor receives hereIsGlass from the sensor", ((MockConveyor)(family.conv)).log.containsString("msgHereIsGlass"));
	}
	

}
