package engine.agent.shay.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import shared.Glass;
import shared.enums.MachineType;
import shared.enums.SensorPosition;
import transducer.TChannel;
import transducer.TEvent;
import engine.agent.shay.ConveyorFamily;
import engine.agent.shay.PopupAgent;
import engine.agent.shay.PopupGlassState;
import engine.agent.shay.Sensor;
import engine.agent.shay.PopupAgent.MoveState;
import engine.agent.shay.PopupAgent.WSActive;
import engine.agent.shay.test.mock.MockConveyor;
import engine.agent.shay.test.mock.MockPopup;
import engine.agent.shay.test.mock.MockTransducer;

public class PopupAgentTest {
	
	public PopupAgent popup; // index 0, type DRILL
	public MockConveyor conveyor; // index 0
	public MockTransducer transducer;
	public ConveyorFamily cf, previous, next; // cf type DRILL
	public Sensor start; // index 0
	public Sensor end; // index 1

	public Glass glassDrill;
	public Glass glassGrinder;
	public Glass glassDrillGrinder;
	public Glass glassDrillUV;
	
	public Object[] args;

	
	@Before
	public void setUp() throws Exception {
		
		transducer = new MockTransducer();
//		start = new Sensor(SensorPosition.START, 0, conveyor);
		end = new Sensor(SensorPosition.END, 1, conveyor);
		conveyor = new MockConveyor("Conveyor0", 0);
		popup = new PopupAgent("Popup0", transducer, MachineType.DRILL, end , 0);

		cf = new ConveyorFamily(conveyor, popup, transducer);

		MockConveyor mockC2 = new MockConveyor("MockConveyor2", 2);
		MockPopup mockP2 = new MockPopup("MockPopup2", 2, MachineType.CROSS_SEAMER);
		next = new ConveyorFamily(mockC2, mockP2, transducer);
		cf.setNextCF(next);

		MockConveyor mockC1 = new MockConveyor("Conveyor1", 1);
		MockPopup mockP1 = new MockPopup("MockPopup1", 1, MachineType.BREAKOUT);
		next = new ConveyorFamily(mockC1, mockP1, transducer);
		cf.setPreviousCF(previous);

		List<MachineType> drill_recipe = new ArrayList<MachineType>();
		drill_recipe.add(MachineType.DRILL);
		glassDrill = new Glass(drill_recipe);

		List<MachineType> drillgrinder_recipe = new ArrayList<MachineType>();
		drillgrinder_recipe.add(MachineType.DRILL);
		drillgrinder_recipe.add(MachineType.GRINDER);
		glassDrillGrinder = new Glass(drillgrinder_recipe);

		List<MachineType> grinder_recipe = new ArrayList<MachineType>();
		grinder_recipe.add(MachineType.GRINDER);
		glassGrinder = new Glass(grinder_recipe);
		
		List<MachineType> uv_recipe = new ArrayList<MachineType>();
		uv_recipe.add(MachineType.UV_LAMP);
		uv_recipe.add(MachineType.DRILL);
		glassDrillUV = new Glass(uv_recipe);
		
		args = new Object[2];
		
	}

	@Test
	public void testInitialState() {
		
		assertTrue("Transducer registers popup on Popup channel.", transducer.log.containsString("register TReciever: Popup0 on TChannel: POPUP"));
		assertTrue("index", popup.getIndex() == 0);
		assertTrue(popup.getGlassList().size() == 0);
		assertTrue(popup.getGlassState() == PopupGlassState.NO_ACTION);
		assertTrue(popup.getMoveState() == MoveState.NO_ACTION);
		assertTrue(popup.getWSActiveState() == WSActive.NONE);
		assertTrue(popup.getType() == MachineType.DRILL);
		
	}
	
	/**
	 * One piece of glass needs processing.
	 */
	@Test
	public void testOneProcessingOne() {
		
		assertFalse(popup.pickAndExecuteAnAction());
		
		// msgIHaveGlass, begin
		popup.msgIHaveGlass(glassDrill, conveyor);
		assertTrue(popup.getGlassState() == PopupGlassState.OFFERED_GLASS);
		assertTrue(popup.getGlassPendingAdd().equals(glassDrill));
		
		// enter PASS_ME_GLASS state
		assertTrue(popup.pickAndExecuteAnAction());
		assertTrue(popup.getGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(conveyor.log.containsString("Received msgPassMeGlass from Popup: Popup0, index: 0"));
		
		// msgHereIsGlass
		assertFalse(popup.pickAndExecuteAnAction());	// not in correct MoveState
		popup.msgHereIsGlass(glassDrill, conveyor);
		assertTrue(popup.getMoveState() == MoveState.LOADING);
		assertTrue(popup.getGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(popup.getGlassReceived().equals(glassDrill));
		assertTrue(popup.getGlassPendingAdd() == null);
				
		// enter actTakeGlass()
		assertTrue(popup.pickAndExecuteAnAction());
		assertTrue(popup.getGlassState() == PopupGlassState.NO_ACTION);
		assertTrue(popup.getGlassReceived().getNeedsProcessing(popup.getType()));
		assertTrue(popup.getGlassList().size() == 1);
		assertTrue(popup.getWSActiveState() == WSActive.WS0);
		
		// transducer event (from GUI) signaling unload
		args[0] = 0;
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		// redundant
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		assertTrue(popup.getDown());
		assertTrue(popup.getMoveState() == MoveState.NO_ACTION);
		assertTrue(popup.getGlassState() == PopupGlassState.NO_ACTION);
		
		// msgGlassDone
		assertFalse(popup.pickAndExecuteAnAction());
		popup.msgGlassDone(glassDrill, 0, cf);
		assertTrue(popup.getMoveState() == MoveState.RELEASING);
		assertTrue(popup.getGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(popup.getGlassPendingDone().equals(glassDrill));
		assertTrue(popup.getWSActiveState() == WSActive.NONE);
		
		// test CF hold if pos not free
		assertFalse(cf.getNextCFPosFree()); // CF can't pass on glass to next CF yet
		assertTrue(popup.pickAndExecuteAnAction());
		cf.msgPositionFree();
		assertTrue(cf.getNextCFPosFree());
		
		// actReleaseGlass
		assertTrue(popup.pickAndExecuteAnAction());	// still in same state until CF pos is free
		assertTrue(popup.getGlassState() == PopupGlassState.NO_ACTION);
		assertTrue(transducer.log.containsString("Transducer received call to fireEvent on TChannel: POPUP for TEvent: POPUP_RELEASE_GLASS"));
		assertTrue(popup.getGlassList().size() == 0);		
		
		assertFalse(popup.pickAndExecuteAnAction());

	}

	/**
	 * One piece of glass, doesn't need processing.
	 */
	@Test
	public void testOneNoProcessing() {
		assertFalse(popup.pickAndExecuteAnAction());
		
		// msgIHaveGlass, begin
		popup.msgIHaveGlass(glassGrinder, conveyor);
		assertTrue(popup.getGlassState() == PopupGlassState.OFFERED_GLASS);
		assertTrue(popup.getGlassPendingAdd().equals(glassGrinder));
		
		// enter PASS_ME_GLASS state
		assertTrue(popup.pickAndExecuteAnAction());
		assertTrue(popup.getGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(conveyor.log.containsString("Received msgPassMeGlass from Popup: Popup0, index: 0"));
		
		// msgHereIsGlass
		assertFalse(popup.pickAndExecuteAnAction());	// not in correct MoveState
		popup.msgHereIsGlass(glassGrinder, conveyor);
		assertTrue(popup.getMoveState() == MoveState.LOADING);
		assertTrue(popup.getGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(popup.getGlassReceived().equals(glassGrinder));
		assertTrue(popup.getGlassPendingAdd() == null);
		
		// enter actTakeGlass()
		assertTrue(popup.pickAndExecuteAnAction());
		assertTrue(popup.getGlassState() == PopupGlassState.NO_ACTION);
		assertFalse(popup.getGlassReceived().getNeedsProcessing(popup.getType()));
		assertTrue(popup.getGlassList().size() == 1);
		assertTrue(popup.getWSActiveState() == WSActive.NONE);
		assertTrue(popup.getGlassPendingDone().equals(glassGrinder));
		
		// transducer event (from GUI) signaling unload
		args[0] = 0;
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		// redundant
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		assertTrue(popup.getDown());

		// actReleaseGlass
		assertTrue(popup.getGlassList().size() == 1);
		cf.msgPositionFree();
		assertTrue(cf.getNextCFPosFree());
		
		assertTrue(popup.getMoveState() == MoveState.RELEASING);		
		assertTrue(popup.pickAndExecuteAnAction());
		assertTrue(popup.getGlassList().size() == 0);
		assertTrue(popup.getGlassPendingDone() == null);
		
		assertTrue(transducer.log.containsString("POPUP_RELEASE_GLASS"));
		assertTrue(popup.getGlassState() == PopupGlassState.NO_ACTION);
		assertTrue(popup.getMoveState() == MoveState.NO_ACTION);
		
		assertFalse(popup.pickAndExecuteAnAction());

	}
	
	/**
	 * Both need processing.
	 */
	@Test
	public void testTwoTwoProcessing() {
		
		assertFalse(popup.pickAndExecuteAnAction());
		
		// msgIHaveGlass, begin #1
		popup.msgIHaveGlass(glassDrill, conveyor);
		assertTrue(popup.getGlassState() == PopupGlassState.OFFERED_GLASS);
		assertTrue(popup.getGlassPendingAdd().equals(glassDrill));
		
		// enter PASS_ME_GLASS state
		assertTrue(popup.pickAndExecuteAnAction());
		assertTrue(popup.getGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(conveyor.log.containsString("Received msgPassMeGlass from Popup: Popup0, index: 0"));
		
		// msgHereIsGlass
		assertFalse(popup.pickAndExecuteAnAction());	// not in correct MoveState
		popup.msgHereIsGlass(glassDrill, conveyor);
		assertTrue(popup.getMoveState() == MoveState.LOADING);
		assertTrue(popup.getGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(popup.getGlassReceived().equals(glassDrill));
		assertTrue(popup.getGlassPendingAdd() == null);
		
		// enter actTakeGlass()
		assertTrue(popup.pickAndExecuteAnAction());
		assertTrue(popup.getGlassState() == PopupGlassState.NO_ACTION);
		assertTrue(popup.getGlassReceived().getNeedsProcessing(popup.getType()));
		assertTrue(popup.getGlassList().size() == 1);
		assertTrue(popup.getWSActiveState() == WSActive.WS0);
		
		// transducer event (from GUI) signaling unload
		args[0] = 0;
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		// redundant
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		assertTrue(popup.getDown());
		assertTrue(popup.getMoveState() == MoveState.NO_ACTION);
		assertTrue(popup.getGlassState() == PopupGlassState.NO_ACTION);
		
		
		// glass #2
		//
		// msgIHaveGlass
		popup.msgIHaveGlass(glassDrillGrinder, conveyor);
		assertTrue(popup.getGlassState() == PopupGlassState.OFFERED_GLASS);
		assertTrue(popup.getGlassPendingAdd().equals(glassDrillGrinder));
		
		// enter PASS_ME_GLASS state
		assertTrue(popup.pickAndExecuteAnAction());
		assertTrue(popup.getGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(conveyor.log.containsString("Received msgPassMeGlass from Popup: Popup0, index: 0"));
		
		// msgHereIsGlass
		assertFalse(popup.pickAndExecuteAnAction());	// not in correct MoveState
		popup.msgHereIsGlass(glassDrillGrinder, conveyor);
		assertTrue(popup.getMoveState() == MoveState.LOADING);
		assertTrue(popup.getGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(popup.getGlassReceived().equals(glassDrillGrinder));
		assertTrue(popup.getGlassPendingAdd() == null);
		
		// enter actTakeGlass()
		assertTrue(popup.pickAndExecuteAnAction());
		assertTrue(popup.getGlassState() == PopupGlassState.NO_ACTION);
		assertTrue(popup.getGlassReceived().getNeedsProcessing(popup.getType()));
		assertTrue(popup.getGlassList().size() == 2);
		assertTrue(popup.getWSActiveState() == WSActive.WS01);
		
		// transducer event (from GUI) signaling unload
		args[0] = 0;
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		// redundant
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		assertTrue(popup.getDown());
		assertTrue(popup.getMoveState() == MoveState.NO_ACTION);
		assertTrue(popup.getGlassState() == PopupGlassState.NO_ACTION);
		

		
		// glass #1
		// msgGlassDone
		assertFalse(popup.pickAndExecuteAnAction());
		popup.msgGlassDone(glassDrill, 0, cf);
		assertTrue(popup.getMoveState() == MoveState.RELEASING);
		assertTrue(popup.getGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(popup.getGlassPendingDone().equals(glassDrill));
		assertTrue(popup.getWSActiveState() == WSActive.WS1);
		
		// test CF hold if pos not free
		assertFalse(cf.getNextCFPosFree()); // CF can't pass on glass to next CF yet
		assertTrue(popup.pickAndExecuteAnAction());
		cf.msgPositionFree();
		assertTrue(cf.getNextCFPosFree());
		
		// actReleaseGlass
		assertTrue(popup.pickAndExecuteAnAction());	// still in same state until CF pos is free
		assertTrue(popup.getGlassState() == PopupGlassState.NO_ACTION);
		assertTrue(transducer.log.containsString("Transducer received call to fireEvent on TChannel: POPUP for TEvent: POPUP_RELEASE_GLASS"));
		assertTrue(popup.getGlassList().size() == 1);		
		
		assertFalse(popup.pickAndExecuteAnAction());
		
		// glass #1 has passed through completely
		
		// glass #2
		// msgGlassDone
		assertFalse(popup.pickAndExecuteAnAction());
		popup.msgGlassDone(glassDrillGrinder, 1, cf);
		assertTrue(popup.getMoveState() == MoveState.RELEASING);
		assertTrue(popup.getGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(popup.getGlassPendingDone().equals(glassDrillGrinder));
		assertTrue(popup.getWSActiveState() == WSActive.NONE);
		
		// test CF hold if pos not free
		assertFalse(cf.getNextCFPosFree()); // CF can't pass on glass to next CF yet
		assertTrue(popup.pickAndExecuteAnAction());
		cf.msgPositionFree();
		assertTrue(cf.getNextCFPosFree());
		
		// actReleaseGlass
		assertTrue(popup.pickAndExecuteAnAction());	// still in same state until CF pos is free
		assertTrue(popup.getGlassState() == PopupGlassState.NO_ACTION);
		assertTrue(transducer.log.containsString("Transducer received call to fireEvent on TChannel: POPUP for TEvent: POPUP_RELEASE_GLASS"));
		assertTrue(popup.getGlassList().size() == 0);		
		
		assertFalse(popup.pickAndExecuteAnAction());

	}

}
