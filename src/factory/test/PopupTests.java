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
import factory.agents.PopupAgent.PopupState;
import factory.misc.ConveyorFamilyEntity;
import factory.test.mock.EventLog;
import factory.test.mock.MockConveyor;
import factory.test.mock.MockWorkstation;

/**
 * Tests for the popup in ConveyorFamilyEntity, i.e., family.popup
 */
public class PopupTests {
	private MockConveyor conv;
	private MockWorkstation workstation1, workstation2;
	private Transducer transducer;
	
	private ConveyorFamilyEntity family;
	
	@Before
	public void setUp() {
		workstation1 = new MockWorkstation("Workstation 1");
		workstation2 = new MockWorkstation("Workstation 2");
		transducer = new Transducer();
		
		family = new ConveyorFamilyEntity(transducer, workstation1, workstation2);
		
		conv = new MockConveyor("Conveyor");
		family.setConveyor(conv);
	}
	@Test @Ignore
	public void testMsgGlassDone() {
		// initial state testing
		assertThat(family.popup.getState(), is(PopupState.DOING_NOTHING));
		assertThat(family.popup.getGlasses().isEmpty(), is(true));
		
		Glass g = new Glass();
//		Glass g2 = new Glass();

		// the message
		family.popup.msgGlassDone(g, workstation1.getIndex());
//		assertThat(family.conv.getState(), is(ConveyorState.GLASS_JUST_ARRIVED));
//		assertThat(family.conv.getGlasses().size(), is(1));

//		assertThat(family.conv.getGlasses().get(0).getID(), is(g.getID()));
//		assertThat(family.conv.getGlasses().get(0).getID(), is(not(g2.getID())));

		// scheduler
		family.popup.pickAndExecuteAnAction();
		
		// after-state
//		assertThat(family.conv.getState(), is(ConveyorState.WAITING_FOR_GLASS_TO_REACH_ENDING_SENSOR));

//		EventLog popupLog = family.getMockPopup().log;
//		assertThat(popupLog.containsString("msgGlassComing"), is(true));
	}

}
