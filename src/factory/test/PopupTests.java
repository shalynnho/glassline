package factory.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import shared.Glass;
import transducer.TEvent;
import transducer.Transducer;
import factory.agents.PopupAgent.PopupState;
import factory.misc.ConveyorFamilyEntity;
import factory.test.mock.MockConveyor;
import factory.test.mock.MockWorkstation;

/**
 * Tests for the popup in ConveyorFamilyEntity, i.e., family.popup
 */
public class PopupTests {
	private MockConveyor conv;
	private MockWorkstation workstation1, workstation2;
//	private MockTransducer t;
	private Transducer t;
	
	private ConveyorFamilyEntity family;
	
	@Before 
	public void setUp() {
		workstation1 = new MockWorkstation("Workstation 1");
		workstation2 = new MockWorkstation("Workstation 2");
		t = new Transducer();
		t.startTransducer();
		
		family = new ConveyorFamilyEntity(t, workstation1, workstation2);
		
		conv = new MockConveyor("Conveyor");
		family.setConveyor(conv);
		
	}
	
	@Test @Ignore("transducer issue")
	public void testMsgGlassDoneAndNoPositionFree() {
		// initial state testing
		assertThat(family.popup.getState(), is(PopupState.DOING_NOTHING));
		assertThat(family.popup.getGlasses().isEmpty(), is(true));
		
		Glass g = new Glass();
		Glass g2 = new Glass();

		// the message
		family.popup.msgGlassDone(g, workstation1.getIndex());

		assertThat(family.popup.getFinishedGlasses().size(), is(1));
		assertThat(family.popup.getFinishedGlasses().get(0).getID(), is(g.getID()));
		assertThat(family.popup.getFinishedGlasses().get(0).getID(), is(not(g2.getID())));

		// scheduler
		family.popup.pickAndExecuteAnAction();
		
		// after-state
		// finishedGlasses should still have glass because popup hasn't been notified position free by next family
		assertThat(family.popup.getFinishedGlasses().size(), is(1));
		assertThat(family.popup.getFinishedGlasses().get(0).getID(), is(g.getID()));

//		EventLog popupLog = family.getMockPopup().log;
//		assertThat(popupLog.containsString("msgGlassComing"), is(true));
	}
	
	@Test @Ignore("strange state issue")
	public void testMsgGlassDoneAndYesPositionFree() {
		// initial state testing
		assertThat(family.popup.getState(), is(PopupState.DOING_NOTHING));
		assertThat(family.popup.getGlasses().isEmpty(), is(true));
		
		Glass g = new Glass();
		Glass g2 = new Glass();

		// initial state setup for this test: send position free beforehand and set popup already up
		family.msgPositionFree();
		family.popup.setIsUp(true);
		
		// the message
		family.popup.msgGlassDone(g, workstation1.getIndex());

		assertThat(family.popup.getFinishedGlasses().size(), is(1));
		assertThat(family.popup.getFinishedGlasses().get(0).getID(), is(g.getID()));
		assertThat(family.popup.getFinishedGlasses().get(0).getID(), is(not(g2.getID())));

		// scheduler
		family.popup.pickAndExecuteAnAction();
		
		// after-state
		// since the popup was already up, we should reach this state
		assertThat(family.popup.getState(), is(PopupState.WAITING_FOR_WORKSTATION_GLASS_RELEASE));
		
		// series of intermediate states after transducer fires
		
		t.fireEvent(workstation1.getChannel(), TEvent.WORKSTATION_RELEASE_FINISHED, new Integer[]{workstation1.getIndex()});
		
		assertThat(family.popup.getState(), is(PopupState.WAITING_FOR_LOW_POPUP_WITH_GLASS_FROM_WORKSTATION));
		
		// this time, finishedGlasses should eventually become empty
//		assertThat(family.popup.getFinishedGlasses().size(), is(0));

//		EventLog popupLog = family.getMockPopup().log;
//		assertThat(popupLog.containsString("msgGlassComing"), is(true));
	}

	@Test @Ignore("transducer issues")
	public void testMsgPositionFreeNothingElse() {
		// initial state testing
		assertThat(family.popup.getState(), is(PopupState.DOING_NOTHING));
		assertThat(family.popup.getGlasses().isEmpty(), is(true));
		
		assertThat(family.popup.getNextPosFree(), is(false));
		
		// the message
		family.popup.msgPositionFree();

		// boolean should have changed
		assertThat(family.popup.getNextPosFree(), is(true));
	}
	
	@Test @Ignore("transducer issues")
	// Test that this has a glass to pass on and got position free
	public void testMsgPositionFreeHasGlass() {
		// initial state testing
		assertThat(family.popup.getState(), is(PopupState.DOING_NOTHING));
		assertThat(family.popup.getGlasses().isEmpty(), is(true));
		
		assertThat(family.popup.getNextPosFree(), is(false));

		// set up popup as already up
		family.popup.setIsUp(false);
		
		// seed finishedGlasses list
		family.popup.seedFinishedGlasses(); // seeds with 2 glasses, so this is like 1 glass finished on each machine

		// the message
		family.popup.msgPositionFree();

		// boolean should have changed
		assertThat(family.popup.getNextPosFree(), is(true));
		
		assertThat(family.popup.getState(), is(PopupState.ACTIVE));
		
		// popup should have to move up after firing event
		
//		assertThat(family.popup.getState(), is(PopupState.WAITING_FOR_HIGH_POPUP_BEFORE_RELEASING_FROM_WORKSTATION));
	}
	
	@Test @Ignore("later")
	public void testPassSequence() {
		fail("moved");
	}
}
