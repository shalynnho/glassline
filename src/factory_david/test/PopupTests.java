package factory_david.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import shared.Glass;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import factory_david.agents.PopupAgent.PopupState;
import factory_david.agents.PopupAgent.WorkstationState;
import factory_david.misc.ConveyorFamilyEntity;
import factory_david.misc.ConveyorFamilyEntity.GlassState;
import factory_david.misc.ConveyorFamilyEntity.MyGlass;
import factory_david.test.mock.EventLog;
import factory_david.test.mock.MockConveyor;
import factory_david.test.mock.MockConveyorFamily;
import factory_david.test.mock.MockWorkstation;

/**
 * Tests for the popup in ConveyorFamilyEntity, i.e., family.popup
 * Some of these tests involve making the thread wait, hence the "timeout" option on the @Test
 */
public class PopupTests {
	private MockConveyor conv;
	private MockWorkstation workstation1, workstation2;
	private MockConveyorFamily nextFamily;
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
		
		nextFamily = new MockConveyorFamily("Next family");
		family.setNextConveyorFamily(nextFamily);
	}
	
	@Test
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
	}
	
	@Test(timeout = 10000)
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
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertThat(family.popup.getState(), is(PopupState.WAITING_FOR_LOW_POPUP_WITH_GLASS_FROM_WORKSTATION));
		
		t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, new Integer[]{family.getPopupIndex()});
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// this time, finishedGlasses should become empty
		assertThat(family.popup.getFinishedGlasses().size(), is(0));
		assertThat(family.popup.getState(), is(PopupState.ACTIVE));

		// next family should receive message
		EventLog nextFamLog = family.getMockNextConveyorFamily().log;
		assertThat(nextFamLog.containsString("msgHereIsGlass"), is(true));
	}

	@Test
	public void testMsgPositionFreeNothingElse() {
		// initial state testing
		assertThat(family.popup.getState(), is(PopupState.DOING_NOTHING));
		assertThat(family.popup.getGlasses().isEmpty(), is(true));
		
		assertThat(family.popup.getNextPosFree(), is(false));
		
		// the message
		family.popup.msgPositionFree();

		// boolean should have changed
		assertThat(family.popup.getNextPosFree(), is(true));
		
		// scheduler
		family.popup.pickAndExecuteAnAction();
		
		// nothing should change
		assertThat(family.popup.getNextPosFree(), is(true));
	}
	
	@Test(timeout = 10000)
	// Test that this has a glass to pass on and got position free
	public void testMsgPositionFreeHasGlass() {
		// initial state testing
		assertThat(family.popup.getState(), is(PopupState.DOING_NOTHING));
		assertThat(family.popup.getGlasses().isEmpty(), is(true));
		assertThat(family.popup.getNextPosFree(), is(false));

		// prepare popup state to simulate its being ready to pass on glass
		// here, just arbitrarily say popup isn't up yet
		family.popup.setIsUp(false);
		// seed finishedGlasses list
		family.popup.seedFinishedGlasses(); // seeds with 2 glasses, so this is like 1 glass finished on each machine
		// force workstations into proper state
		family.popup.setWorkstationState(1, WorkstationState.DONE_BUT_STILL_HAS_GLASS);
		family.popup.setWorkstationState(2, WorkstationState.DONE_BUT_STILL_HAS_GLASS);
		
		// the message
		family.popup.msgPositionFree();

		// boolean should have changed
		assertThat(family.popup.getNextPosFree(), is(true));
		assertThat(family.popup.getState(), is(PopupState.ACTIVE));
		
		// scheduler
		family.popup.pickAndExecuteAnAction();

		assertThat(family.popup.getState(), is(PopupState.WAITING_FOR_HIGH_POPUP_BEFORE_RELEASING_FROM_WORKSTATION));

		// popup should have to move up after firing event
		t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, new Integer[]{family.getPopupIndex()});
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertThat(family.popup.getState(), is(PopupState.WAITING_FOR_WORKSTATION_GLASS_RELEASE));
		
		// now launch release glass event
		t.fireEvent(workstation1.getChannel(), TEvent.WORKSTATION_RELEASE_FINISHED, new Integer[]{workstation1.getIndex()});
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertThat(family.popup.getState(), is(PopupState.WAITING_FOR_LOW_POPUP_WITH_GLASS_FROM_WORKSTATION));
		
		t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, new Integer[]{family.getPopupIndex()});
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertThat(family.popup.getState(), is(PopupState.ACTIVE));
		// Important: nextPosFree should now be false
		assertThat(family.popup.getNextPosFree(), is(false));
	}
	
	@Test
	public void testMsgGlassComing() {
		// initial state testing
		assertThat(family.popup.getState(), is(PopupState.DOING_NOTHING));
		assertThat(family.popup.getGlasses().isEmpty(), is(true));
		assertThat(family.popup.getNextPosFree(), is(false));
		
		Glass g = new Glass();
		MyGlass myGlass = family.new MyGlass(g, GlassState.NEEDS_PROCESSING);
		family.popup.msgGlassComing(myGlass);
		
		assertThat(family.popup.getGlasses().size(), is(1));
	}
	
	@Test @Ignore("dummy")
	public void testPassSequence() {
		fail("moved");
	}
}
