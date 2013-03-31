package factory.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import shared.Glass;
import transducer.Transducer;
import factory.agents.SensorAgent.SensorState;
import factory.misc.ConveyorFamilyEntity;
import factory.test.mock.EventLog;
import factory.test.mock.MockConveyor;
import factory.test.mock.MockConveyorFamily;
import factory.test.mock.MockWorkstation;

/**
 * Tests for the sensor in ConveyorFamilyEntity, i.e., family.sensor
 */
public class SensorTests {
	private MockConveyorFamily prevFamily;
	private MockWorkstation workstation1, workstation2;
	private Transducer transducer;
	private MockConveyor conveyor;
	
	private ConveyorFamilyEntity family;
	
	@Before
	public void setUp() {
		prevFamily = new MockConveyorFamily("Previous Family");
		workstation1 = new MockWorkstation("Workstation 1"); // workstations just needed for family constructor
		workstation2 = new MockWorkstation("Workstation 2");
		transducer = new Transducer();
		conveyor = new MockConveyor("Conveyor");
		
		family = new ConveyorFamilyEntity(transducer, workstation1, workstation2);
		family.setPreviousConveyorFamily(prevFamily);
		family.setConveyor(conveyor);
	}
	
	@Test
	public void testMsgHereIsGlass() {
		assertEquals("Sensor's state should start out as NOTHING_TO_DO", family.sensor.getState(), SensorState.NOTHING_TO_DO);
		assertThat(family.sensor.getGlasses().isEmpty(), is(true));
		
		Glass g = new Glass();
		Glass g2 = new Glass();
		
		family.msgHereIsGlass(g);
		
		assertEquals("Sensor's state should become GLASS_JUST_ARRIVED when the family receives a glass", family.sensor.getState(), SensorState.GLASS_JUST_ARRIVED);
		assertThat(family.sensor.getGlasses().size(), is(1));
		
		int idOfGlassAtSensor = family.sensor.getGlasses().get(0).getID();
		
		assertEquals("Sensor should receive proper glass", idOfGlassAtSensor, g.getID());
		assertThat(idOfGlassAtSensor, is(not(g2.getID())));
	
		family.sensor.pickAndExecuteAnAction();
		
		EventLog convLog = family.getMockConveyor().log;
		assertThat(family.sensor.getState(), is(SensorState.NOTHING_TO_DO));
		assertTrue("Conveyor should receive hereIsGlass from the sensor. Event log: [" + convLog.toString() + "]", convLog.containsString("msgHereIsGlass"));
		assertTrue("Sensor's list of glasses should now be empty.", family.sensor.getGlasses().isEmpty());
	}
	
	@Test
	public void testMsgPositionFree() {
		assertEquals("Sensor's state should start out as NOTHING_TO_DO", family.sensor.getState(), SensorState.NOTHING_TO_DO);
		assertThat(family.sensor.getGlasses().isEmpty(), is(true));
		
		family.sensor.msgPositionFree(); // normally sent by conveyor
		assertEquals("Sensor's state should become SHOULD_NOTIFY_POSITION_FREE", family.sensor.getState(), SensorState.SHOULD_NOTIFY_POSITION_FREE);

		family.sensor.pickAndExecuteAnAction();
		
		assertThat(family.sensor.getState(), is(SensorState.NOTHING_TO_DO));
		
		EventLog prevFamilyLog = family.getMockPrevConveyorFamily().log;
		
		assertTrue("Previous family should receive msgPositionFree. Event log: [" + prevFamilyLog.toString() + "]", prevFamilyLog.containsString("msgPositionFree"));
	}
}
