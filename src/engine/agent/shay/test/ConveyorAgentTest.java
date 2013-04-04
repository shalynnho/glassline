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
import engine.agent.shay.ConveyorAgent;
import engine.agent.shay.ConveyorFamily;
import engine.agent.shay.Sensor;
import engine.agent.shay.enums.ConveyorState;
import engine.agent.shay.enums.PopupGlassState;
import engine.agent.shay.test.mock.MockConveyor;
import engine.agent.shay.test.mock.MockPopup;
import engine.agent.shay.test.mock.MockTransducer;

public class ConveyorAgentTest {

	public ConveyorAgent conveyor; // index 0
	public MockPopup popup; // index 0, type DRILL
	public MockTransducer transducer;
	public ConveyorFamily cf, previous, next; // cf type DRILL
	public Sensor start; // index 0
	public Sensor end; // index 1

	public Glass glassDrill;
	public Glass glassGrinder;
	public Glass glassDrillGrinder;
	
	public Object[] args;

	@Before
	public void setUp() throws Exception {
		transducer = new MockTransducer();
		start = new Sensor(SensorPosition.START, 0, conveyor);
		end = new Sensor(SensorPosition.END, 1, conveyor);
		conveyor = new ConveyorAgent("Conveyor0", transducer, 0, start, end);
		popup = new MockPopup("Popup0", 0, MachineType.DRILL);
		cf = new ConveyorFamily(conveyor, popup, transducer);

		MockConveyor mockC2 = new MockConveyor("MockConveyor2", 2);
		MockPopup mockP2 = new MockPopup("MockPopup2", 2, MachineType.CROSS_SEAMER);
		next = new ConveyorFamily(mockC2, mockP2, transducer);
		cf.setNextCF(next);

		MockConveyor mockC1 = new MockConveyor("Conveyor1", 1);
		MockPopup mockP1 = new MockPopup("Popup1", 1, MachineType.BREAKOUT);
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
		
		args = new Object[2];
	}

	/**
	 * Test preconditions & construction
	 */
	@Test
	public void testInitialState() {
		// check initial state
		assertTrue("Initial state should be ON_POS_FREE", conveyor.getState().equals(ConveyorState.ON_POS_FREE));
		assertTrue("Check CA index", conveyor.getIndex() == 0);
		assertTrue("Check CF type", conveyor.getFamily().getType() == MachineType.DRILL);

		assertTrue("Check start sensor index 0", conveyor.getStartSensor().getIndex() == 0);
		assertTrue("Check start sensor pos", conveyor.getStartSensor().getPosition().equals(SensorPosition.START));
		assertFalse("Check start sensor state", conveyor.getStartSensor().getPressed());

		assertTrue("Check end sensor index 0", conveyor.getEndSensor().getIndex() == 1);
		assertTrue("Check end sensor pos", conveyor.getEndSensor().getPosition().equals(SensorPosition.END));
		assertFalse("Check end sensor state", conveyor.getEndSensor().getPressed());

		assertTrue("No glass on conveyor yet.", conveyor.getGlassList().size() == 0);

		assertTrue("Transducer registers conveyor on Conveyor channel.", transducer.log.containsString("register TReciever: Conveyor0 on TChannel: CONVEYOR"));
		assertTrue("Transducer registers conveyor on Sensor channel.", transducer.log.containsString("register TReciever: Conveyor0 on TChannel: SENSOR"));
		assertTrue("Transducer receives event to start conveyor.", transducer.log.containsString("fireEvent on TChannel: CONVEYOR for TEvent: CONVEYOR_DO_START"));
		assertTrue("CA sends pos free to CF", cf.getConveyorPosFree());
		
	}

	/**
	 * Normative conveyor scenario with 1 piece of glass.
	 * This CF (==> ConveyorAgent) is of MachineType.DRILL
	 * (but this doesn't matter for the conveyor).
	 */
	@Test
	public void testNormativeOne() {
		assertTrue("CA sends pos free to CF", cf.getConveyorPosFree());
		assertFalse("There should be no scheduler rule anymore." + getLogs(), conveyor.pickAndExecuteAnAction());

		// from CF, msgHereIsGlass (not immediately added, wait for scheduler call)
		conveyor.msgHereIsGlass(glassDrill, cf);
		assertTrue("State changed, receiving glass.", conveyor.getState() == ConveyorState.ON_RECEIVING);
		assertTrue("Correct piece of glass set", conveyor.getGlassToReceive().equals(glassDrill));
		assertTrue("Glass list still empty.", conveyor.getGlassList().size() == 0);
		
		// call scheduler to receive glass part
		assertTrue(conveyor.pickAndExecuteAnAction());
		assertTrue("Glass added.", conveyor.getGlassList().size() == 1);
		//assertTrue("glassToReceive null again.", conveyor.getGlassToReceive() == null);
		assertTrue("State changed", conveyor.getState() == ConveyorState.ON_POS_FREE);
		assertTrue("CA sends pos free to CF", cf.getConveyorPosFree());
		assertFalse("There should be no scheduler rule anymore." + getLogs(), conveyor.pickAndExecuteAnAction());
		
		// start sensor pressed
		args[0] = 0;
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_PRESSED"));
		assertFalse(conveyor.getStartSensor().getPressed());
		// manually be the transducer
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(conveyor.getStartSensor().getPressed());
		
		// start sensor released
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_RELEASED"));
		assertTrue(conveyor.getStartSensor().getPressed());
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertFalse(conveyor.getStartSensor().getPressed());
		
		// end sensor pressed
		args[0] = 1;
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_PRESSED"));
		assertFalse(conveyor.getEndSensor().getPressed());
		// manually be the transducer
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(conveyor.getEndSensor().getPressed());
		
		// check that popup received msgIHaveGlass
		assertTrue(popup.log.containsString("Received msgIHaveGlass from Conveyor: Conveyor0, index: 0"));
		assertTrue(conveyor.getPopupGlassState() == PopupGlassState.OFFERED_GLASS);
				
		// end sensor released
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_RELEASED"));
		assertTrue(conveyor.getEndSensor().getPressed());
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertFalse(conveyor.getEndSensor().getPressed());

		// from popup to conveyor
		assertFalse("There should be no scheduler rule anymore." + getLogs(), conveyor.pickAndExecuteAnAction());
		conveyor.msgPassMeGlass(popup);
		assertTrue(conveyor.getPopupGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(conveyor.pickAndExecuteAnAction());
		assertTrue(popup.log.containsString("Received msgHereIsGlass from Conveyor: Conveyor0, index: 0"));	
		assertTrue(conveyor.getGlassList().size() == 0);
		assertTrue(conveyor.getPopupGlassState() == PopupGlassState.NO_ACTION);
		assertTrue(conveyor.getState() == ConveyorState.ON_POS_FREE);
		assertTrue("CA sends pos free to CF", cf.getConveyorPosFree());		
	}
	
	/**
	 * Normative conveyor scenario with 1 piece of glass.
	 * This CF (==> ConveyorAgent) is of MachineType.DRILL
	 * (but this doesn't matter for the conveyor).
	 */
	@Test
	public void testNormativeTwo() {
		assertTrue("CA sends pos free to CF", cf.getConveyorPosFree());
		assertFalse("There should be no scheduler rule anymore." + getLogs(), conveyor.pickAndExecuteAnAction());

		// from CF, msgHereIsGlass (not immediately added, wait for scheduler call)
		conveyor.msgHereIsGlass(glassDrill, cf);
		assertTrue("State changed, receiving glass.", conveyor.getState() == ConveyorState.ON_RECEIVING);
		assertTrue("Correct piece of glass set", conveyor.getGlassToReceive().equals(glassDrill));
		assertTrue("Glass list still empty.", conveyor.getGlassList().size() == 0);
		
		// call scheduler to receive glass part
		assertTrue(conveyor.pickAndExecuteAnAction());
		assertTrue("Glass added.", conveyor.getGlassList().size() == 1);
		//assertTrue("glassToReceive null again.", conveyor.getGlassToReceive() == null);
		assertTrue("State changed", conveyor.getState() == ConveyorState.ON_POS_FREE);
		assertTrue("CA sends pos free to CF", cf.getConveyorPosFree());
		assertFalse("There should be no scheduler rule anymore." + getLogs(), conveyor.pickAndExecuteAnAction());
		
		// start sensor pressed
		args[0] = 0;
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_PRESSED"));
		assertFalse(conveyor.getStartSensor().getPressed());
		// manually be the transducer
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(conveyor.getStartSensor().getPressed());
		
		// start sensor released
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_RELEASED"));
		assertTrue(conveyor.getStartSensor().getPressed());
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertFalse(conveyor.getStartSensor().getPressed());
		
		
		
		// *** second piece of glass *** //
		// from CF, msgHereIsGlass (not immediately added, wait for scheduler call)
		conveyor.msgHereIsGlass(glassGrinder, cf);
		assertTrue("State changed, receiving glass.", conveyor.getState() == ConveyorState.ON_RECEIVING);
		assertTrue("Glass list has 1 item.", conveyor.getGlassList().size() == 1);
		assertTrue("Correct piece of glass set", conveyor.getGlassToReceive().equals(glassGrinder));
		
		
		// call scheduler to receive glass part
		assertTrue(conveyor.pickAndExecuteAnAction());
		assertTrue("Glass added.", conveyor.getGlassList().size() == 2);
		//assertTrue("glassToReceive null again.", conveyor.getGlassToReceive() == null);
		assertTrue("State changed", conveyor.getState() == ConveyorState.ON_POS_FREE);
		assertTrue("CA sends pos free to CF", cf.getConveyorPosFree());
		assertFalse("There should be no scheduler rule anymore." + getLogs(), conveyor.pickAndExecuteAnAction());
		
		// start sensor pressed
		args[0] = 0;
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_PRESSED"));
		assertFalse(conveyor.getStartSensor().getPressed());
		// manually be the transducer
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(conveyor.getStartSensor().getPressed());
		
		// start sensor released
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_RELEASED"));
		assertTrue(conveyor.getStartSensor().getPressed());
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertFalse(conveyor.getStartSensor().getPressed());
		
		// *** first piece again *** //
		// end sensor pressed for first piece (drill)
		args[0] = 1;
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_PRESSED"));
		assertFalse(conveyor.getEndSensor().getPressed());
		// manually be the transducer
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(conveyor.getEndSensor().getPressed());
		
		// check that popup received msgIHaveGlass for first piece
		assertTrue(popup.log.containsString("Received msgIHaveGlass from Conveyor: Conveyor0, index: 0"));
		assertTrue(conveyor.getPopupGlassState() == PopupGlassState.OFFERED_GLASS);
				
		// end sensor released
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_RELEASED"));
		assertTrue(conveyor.getEndSensor().getPressed());
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertFalse(conveyor.getEndSensor().getPressed());

		// from popup to conveyor
		assertFalse("There should be no scheduler rule anymore." + getLogs(), conveyor.pickAndExecuteAnAction());
		conveyor.msgPassMeGlass(popup);
		assertTrue(conveyor.getPopupGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(conveyor.pickAndExecuteAnAction());
		assertTrue(popup.log.containsString("Received msgHereIsGlass from Conveyor: Conveyor0, index: 0"));	
		assertTrue(conveyor.getGlassList().size() == 1);
		assertTrue(conveyor.getPopupGlassState() == PopupGlassState.NO_ACTION);
		assertTrue(conveyor.getState() == ConveyorState.ON_POS_FREE);
		assertTrue("CA sends pos free to CF", cf.getConveyorPosFree());	
		
		// *** second piece *** //
		// end sensor pressed for first piece (drill)
		args[0] = 1;
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_PRESSED"));
		assertFalse(conveyor.getEndSensor().getPressed());
		// manually be the transducer
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(conveyor.getEndSensor().getPressed());
		
		// check that popup received msgIHaveGlass for first piece
		assertTrue(popup.log.containsString("Received msgIHaveGlass from Conveyor: Conveyor0, index: 0"));
		assertTrue(conveyor.getPopupGlassState() == PopupGlassState.OFFERED_GLASS);
				
		// end sensor released
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_RELEASED"));
		assertTrue(conveyor.getEndSensor().getPressed());
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertFalse(conveyor.getEndSensor().getPressed());

		// from popup to conveyor
		assertFalse("There should be no scheduler rule anymore." + getLogs(), conveyor.pickAndExecuteAnAction());
		conveyor.msgPassMeGlass(popup);
		assertTrue(conveyor.getPopupGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(conveyor.pickAndExecuteAnAction());
		assertTrue(popup.log.containsString("Received msgHereIsGlass from Conveyor: Conveyor0, index: 0"));	
		assertTrue(conveyor.getGlassList().size() == 0);
		assertTrue(conveyor.getPopupGlassState() == PopupGlassState.NO_ACTION);
		assertTrue(conveyor.getState() == ConveyorState.ON_POS_FREE);
		assertTrue("CA sends pos free to CF", cf.getConveyorPosFree());	
	}
	
	
	@Test
	public void testCoveyorStopsWhenFull() {
		List<MachineType> recipe = new ArrayList<MachineType>();
		recipe.add(MachineType.GRINDER);
		
		for (int i = 0; i < 7; i++) {
			
			cf.msgHereIsGlass(new Glass(recipe)); //1
			assertTrue("Glass list still empty.", conveyor.getGlassList().size() == i);
			assertTrue(conveyor.pickAndExecuteAnAction());
			assertTrue("Glass added.", conveyor.getGlassList().size() == (i + 1));
	
			System.out.println("glass.size: " + conveyor.getGlassList().size());
			// start sensor pressed
			args[0] = 0;
			transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
			assertFalse(conveyor.getStartSensor().getPressed());
			conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
			assertTrue(conveyor.getStartSensor().getPressed());
			
			// start sensor released
			transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
			assertTrue(conveyor.getStartSensor().getPressed());
			conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
			assertFalse(conveyor.getStartSensor().getPressed());

		}
		
		args[0] = 1;
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_PRESSED"));
		assertFalse(conveyor.getEndSensor().getPressed());
		// manually be the transducer
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		assertTrue(conveyor.getEndSensor().getPressed());
		
		// check that popup received msgIHaveGlass for first piece
		assertTrue(popup.log.containsString("Received msgIHaveGlass from Conveyor: Conveyor0, index: 0"));
		assertTrue(conveyor.getPopupGlassState() == PopupGlassState.OFFERED_GLASS);
				
		// end sensor released
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertTrue(transducer.log.containsString("fireEvent on TChannel: SENSOR for TEvent: SENSOR_GUI_RELEASED"));
		assertTrue(conveyor.getEndSensor().getPressed());
		conveyor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		assertFalse(conveyor.getEndSensor().getPressed());

		// from popup to conveyor, coveyor is OFF
		assertFalse("There should be no scheduler rule anymore." + getLogs(), conveyor.pickAndExecuteAnAction());
		assertTrue(conveyor.getState() == ConveyorState.OFF);
		System.out.println("state: " + conveyor.getState());
		
		// popup asks for glass
		conveyor.msgPassMeGlass(popup);
		assertTrue(conveyor.getPopupGlassState() == PopupGlassState.PASS_ME_GLASS);
		assertTrue(conveyor.getState() == ConveyorState.OFF);
		
		// conveyor starts up
		assertTrue("Transducer receives event to start conveyor.", transducer.log.getLastLoggedEvent().toString().contains("fireEvent on TChannel: CONVEYOR for TEvent: CONVEYOR_DO_START"));
		conveyor.eventFired(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, null);
		assertTrue(conveyor.getState() == ConveyorState.ON_POS_FREE);
		
		assertTrue(conveyor.pickAndExecuteAnAction());
		assertTrue(popup.log.containsString("Received msgHereIsGlass from Conveyor: Conveyor0, index: 0"));	
		assertTrue(conveyor.getGlassList().size() == 6);
		assertTrue(conveyor.getPopupGlassState() == PopupGlassState.NO_ACTION);
		assertTrue(conveyor.getState() == ConveyorState.ON_POS_FREE);
		assertTrue("CA sends pos free to CF", cf.getConveyorPosFree());	
			
			
	}
	
	
	/**
	 * This is a helper function to print out the logs from the two MockCustomer
	 * objects. This should help to assist in debugging.
	 * 
	 * @return a string containing the logs from the two mock customers
	 */
	public String getLogs() {
		StringBuilder sb = new StringBuilder();
		String newLine = System.getProperty("line.separator");
		sb.append("-------Popup Log-------");
		sb.append(newLine);
		sb.append(popup.log.toString());
		sb.append(newLine);
		sb.append("-------End Customer 2 Log-------");

		sb.append(newLine);

		sb.append("-------Transducer Log-------");
		sb.append(newLine);
		sb.append(transducer.log.toString());
		sb.append("-------End Customer 2 Log-------");
		
		sb.append(newLine);

		return sb.toString();

	}

}
