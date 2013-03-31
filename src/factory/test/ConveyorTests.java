package factory.test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import shared.Glass;
import transducer.Transducer;
import factory.agents.ConveyorAgent.ConveyorState;
import factory.misc.ConveyorFamilyEntity;
import factory.test.mock.EventLog;
import factory.test.mock.MockPopup;
import factory.test.mock.MockSensor;
import factory.test.mock.MockWorkstation;

/**
 * Tests for the conveyor in ConveyorFamilyEntity, i.e., family.conv
 */
public class ConveyorTests {
	private MockSensor sensor;
	private MockPopup popup;
	
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
		
		popup = new MockPopup("Popup");
		family.setPopup(popup);
	}

	@Test
	public void testMsgHereIsGlass() {
		// initial state testing
		assertThat(family.conv.getState(), is(ConveyorState.NOTHING_TO_DO));
		assertThat(family.conv.getGlasses().isEmpty(), is(true));
		
		Glass g = new Glass();
		Glass g2 = new Glass();

		// the message
		family.conv.msgHereIsGlass(g);
		assertThat(family.conv.getState(), is(ConveyorState.GLASS_JUST_ARRIVED));
		assertThat(family.conv.getGlasses().size(), is(1));

		assertThat(family.conv.getGlasses().get(0).getID(), is(g.getID()));
		assertThat(family.conv.getGlasses().get(0).getID(), is(not(g2.getID())));

		// scheduler
		family.conv.pickAndExecuteAnAction();
		
		// after-state
		assertThat(family.conv.getState(), is(ConveyorState.WAITING_FOR_GLASS_TO_REACH_ENDING_SENSOR));

		EventLog popupLog = family.getMockPopup().log;
		assertThat(popupLog.containsString("msgGlassComing"), is(true));
	}

	@Test
	public void testMsgTakingGlass() {
		// start out with one glass moving along - prepare initial state (initial state already tested above)
		Glass g = new Glass();
		family.conv.msgHereIsGlass(g);
		family.conv.pickAndExecuteAnAction();

		assertThat(family.conv.getState(), is(ConveyorState.WAITING_FOR_GLASS_TO_REACH_ENDING_SENSOR));
		assertThat(family.conv.getState(), is(not(ConveyorState.SHOULD_NOTIFY_POSITION_FREE)));

		// suddenly, tell glass msgTakingGlass - what we want to test and what the popup sends
		family.conv.msgTakingGlass();

		assertThat(family.conv.getState(), is(ConveyorState.SHOULD_NOTIFY_POSITION_FREE));

		// scheduler
		family.conv.pickAndExecuteAnAction();

		// after-state
		assertThat(family.conv.getState(), is(ConveyorState.NOTHING_TO_DO));

		// make sure previous sensor got message
		EventLog sensorLog = family.getMockSensor().log;
		assertThat(sensorLog.containsString("msgPositionFree"), is(true));
	}	
	
	
//	/**
//	 * Test one glass piece moving along the conveyor.
//	 */
	@Test @Ignore("later")
	public void testPassSequence() {
		fail("moved");
	}

}
