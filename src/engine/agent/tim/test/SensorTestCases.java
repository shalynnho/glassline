package engine.agent.tim.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import shared.Glass;
import shared.interfaces.ConveyorFamily;
import transducer.Transducer;

import engine.agent.tim.agents.SensorAgent;
import engine.agent.tim.interfaces.Conveyor;
import engine.agent.tim.interfaces.PopUp;
import engine.agent.tim.interfaces.Sensor;
import engine.agent.tim.misc.ConveyorFamilyImp;
import engine.agent.tim.misc.MyGlassSensor.onSensor;
import engine.agent.tim.test.Mock.MockAnimation;
import engine.agent.tim.test.Mock.MockConveyor;
import engine.agent.tim.test.Mock.MockConveyorFamily;
import engine.agent.tim.test.Mock.MockPopUp;

public class SensorTestCases {

	// These cases will only test to make sure that the INNER functionality of this agent works.  These tests will not involve the entire conveyor family
	
	@Test
	public void threeMainSensorAgentTests() {
		/**
		 * This test will complete the following objectives:  Test all of the sensor agent functionality LOCAL to the agent so that we can see that the sensor agents work
		 * How this test will work:
		 * A.  Three instances of the sensor agent will be be used: entry, popup, and exit along with a transducer
		 * B.  Mocks to be used: Conveyor, ConveyorFamily, PopUp, Animation
		 * C.  A transducer will be used to simulate the animation events for the sensors
		 * D.  Outline:
		 * 	1.  Entry Sensor Test:
		 * 		a.  Test the following preconditions:  Entry sensor has no glass, Mock Conveyor Has no glass
		 * 		b.  Send the glass from a previous conveyor family (test code) to the entry sensor
		 * 		c.  Test the following postconditions:  Entry sensor has a glass with state justEntered, Mock Conveyor Has no glass
		 * 		d.  Run sensor scheduler
		 * 		e.  Test postconditions: Entry sensor still has glass with the Yes onSensor state, Mock Conveyor Has glass, GUI conveyor should have been turned on in MockAnimation
		 * 		f.  Have transducer call that glass has left sensor
		 * 		g.  Check postconditions:  Entry sensor still has glass with changed state, Mock Conveyor Has glass
		 * 		h.  Run scheduler
		 * 		i.  Check postconditions:  Entry sensor has no glass, Mock Conveyor Has glass, Mock Conveyor Family received positionFree msg()
		 * 
		 * 	2.  PopUp Sensor Test:
		 * 		a.  Test the following preconditions:  PopUp sensor has no glass, Mock Conveyor Has glass
		 * 		b.  Have transducer send a message to this sensor agent that glass has hit GUI popUp sensor
		 * 		c.  Test the following postconditions:  PopUp sensor has a glass with state justEntered, Mock Conveyor Has glass
		 * 		d.  Run sensor scheduler
		 * 		e.  Test postconditions: PopUp sensor still has glass with the Yes onSensor state, Mock Conveyor Has glass with updated state, GUI conveyor should still be turned on in MockAnimation
		 * 		f.  Have transducer call that glass has left sensor
		 * 		g.  Check postconditions:  PopUp sensor still has glass with changed state, Mock Conveyor Has glass
		 * 		h.  Run scheduler
		 * 		i.  Check postconditions:  PopUp sensor has no glass, Mock Conveyor Has glass
		 * 
		 * 	3.  Exit Sensor Test:
		 * 		a.  Test the following preconditions:  Exit sensor has no glass, Mock Conveyor Has glass
		 * 		b.  Have transducer send a message to this sensor agent that glass has hit GUI exit sensor
		 * 		c.  Test the following postconditions:  Exit sensor has a glass with state justEntered, Mock Conveyor Has glass
		 * 		d.  Run sensor scheduler
		 * 		e.  Test postconditions: Exit sensor still has glass with the Yes onSensor state, Mock Conveyor Has no glass
		 * 		f.  Have transducer call that glass has left sensor
		 * 		g.  Check postconditions:  Exit sensor still has glass with changed state, Mock Conveyor Has no glass
		 * 		h.  Run scheduler
		 * 		i.  Check postconditions:  Exit sensor has no glass, Mock Conveyor Has glass, GUI conveyor should be turned off
		 * 
		 *  4.  GUI conveyor tests:  Test all of the turnConveyorOnOff() condititons to make sure that the right transducer calls are made -- this is self-explanatory 
		 * */
		
		
		// For reference, the description of the test will be placed before it starts, so you do not have to scroll to the top 
		
		/*
		 * 	1.  Entry Sensor Test:
		 * 		a.  Test the following preconditions:  Entry sensor has no glass, Mock Conveyor Has no glass
		 * 		b.  Send the glass from a previous conveyor family (test code) to the entry sensor
		 * 		c.  Test the following postconditions:  Entry sensor has a glass with state justEntered, Mock Conveyor Has no glass
		 * 		d.  Run sensor scheduler
		 * 		e.  Test postconditions: Entry sensor still has glass with the Yes onSensor state, Mock Conveyor Has glass, GUI conveyor should have been turned on in MockAnimation
		 * 		f.  Have transducer call that glass has left sensor
		 * 		g.  Check postconditions:  Entry sensor still has glass with changed state, Mock Conveyor Has glass
		 * 		h.  Run scheduler
		 * 		i.  Check postconditions:  Entry sensor has no glass, Mock Conveyor Has glass, Mock Conveyor Family received positionFree msg(), and that GUI conveyor is still on
		 */
		
		System.out.println("/****************Test: threeMainSensorAgentTests****************/");
		
		// Create a piece of glass to use for the test
		Glass glass = new Glass(); // Since processing is not an issue with this test, let's just leave that field blank
		
		// Instantiate the transducer
		Transducer transducer = new Transducer();
		
		// List of types for each sensor
		List<String> typesA = new ArrayList<String>();
		typesA.add("entry");
		
		// List of types for each sensor
		List<String> typesB = new ArrayList<String>();
		typesB.add("popUp");
		
		// List of types for each sensor
		List<String> typesC = new ArrayList<String>();
		typesC.add("exit");
		
		// Create the three sensor agents
		Sensor entrySensor = new SensorAgent("entrySensor", transducer, typesA);
		Sensor popUpSensor = new SensorAgent("popUpSensor", transducer, typesB);
		Sensor exitSensor = new SensorAgent("exitSensor", transducer, typesC);
		
		// Add these sensors to a list and then put them within a conveyor family
		List<Sensor> sensors = new ArrayList<Sensor>();
		sensors.add(entrySensor);
		sensors.add(popUpSensor);
		sensors.add(exitSensor);
		
		// Make the Mock Conveyor
		MockConveyor mockConveyor = new MockConveyor("mockConveyor", transducer);
		
		// Make the Mock PopUp
		MockPopUp mockPopUp = new MockPopUp("mockPopUp", transducer);
		
		// Make the Mock Animation for the Tests outlined in 4.
		MockAnimation mockAnimation = new MockAnimation(transducer);
		
		// Instantiate the conveyorFamilies and place everything inside them
		MockConveyorFamily mockPrevCF = new MockConveyorFamily("mockPrevCF");
		MockConveyorFamily mockNextCF = new MockConveyorFamily("mockNextCF");
		ConveyorFamilyImp realCF = new ConveyorFamilyImp("realCF", mockConveyor, sensors, mockPopUp);
		
		// Link up the conveyor families
		realCF.setPrevCF(mockPrevCF);
		realCF.setNextCF(mockNextCF);
		mockNextCF.setPrevCF(realCF);
		
		// Now that everything has been set up within the conveyor families, let us begin the first set of entry sensor tests
		
		// Check for the following preconditions:
		assertTrue(realCF.getSensor("entry").getGlassSheets().size() == 0); // There should not be any glass within this array
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 0); // There should not be any glass within this array either
		
		// Now send a piece of glass from the previous conveyor family to the current one (this will be done by the test code)
		realCF.msgHereIsGlass(glass);
		
		// Check for the following postconditions:
		assertTrue(realCF.getSensor("entry").getGlassSheets().size() == 1); // Entry sensor should have glass
		assertEquals(realCF.getSensor("entry").getGlassSheets().get(0).onSensor, onSensor.justEntered); // Entry sensor glass state should be justEntered
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 0); // MockConveyor still should not have glass
		
		// Let's run the sensor scheduler
		realCF.getSensor("entry").runScheduler();
		
		// Check for the following postConditions:
		assertTrue(realCF.getSensor("entry").getGlassSheets().size() == 1); // Entry sensor should still have glass
		assertEquals(realCF.getSensor("entry").getGlassSheets().get(0).onSensor, onSensor.yes); // Glass should now be in the yes state
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // The Mockconveyor should have glass now
		
		// Now process the transducer event(s) and then check that the conveyor was turned on!
		while (transducer.processNextEvent());
		
		// Check for the following postConditions:
		assertTrue(realCF.getSensor("entry").getGlassSheets().size() == 1); // Entry sensor should still have glass
		assertEquals(realCF.getSensor("entry").getGlassSheets().get(0).onSensor, onSensor.yes); // Glass should now be in the yes state
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // The Mockconveyor should have glass now
		
		assertTrue(realCF.getConveyor().isConveyorOn() == true); // GUI conveyor should have been turned on
		
		
		// Now the GuiGlass has just left this sensor, so the MockAnimation will notify this sensor of the news via the transducer
		
		mockAnimation.fireEntrySensorExitGlass(glass); // Note that the parsing to get to this point will have to be done for real in v1
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Check for the following postconditions:
		
		// SensorAgent with glass should have a changed state, but the glass should still be there
		assertTrue(realCF.getSensor("entry").getGlassSheets().size() == 1); // Entry sensor should still have glass
		assertEquals(realCF.getSensor("entry").getGlassSheets().get(0).onSensor, onSensor.no); // Glass should now be in the no state
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // Nothing should have changed within the mock conveyor
		
		// Let's run the sensor scheduler
		realCF.getSensor("entry").runScheduler();
		
		// Check for the following postconditions:
		assertTrue(realCF.getSensor("entry").getGlassSheets().size() == 0); // Entry sensor should not have the glass now
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // Mock conveyor still has the glass
		assertTrue(realCF.getConveyor().isConveyorOn() == true); // GUI conveyor should still be turned on
		
		// Previous conveyor family should have received msgHereIsFree()
		assertTrue(mockPrevCF.log.containsString("mockPrevCF: Messaged conveyor that glass can to passed to next conveyor system."));
		
		// At this point, it is assumed that the glass has moved along the conveyor until it reaches the popUp sensor.  Thus, the following test will occur:
		
		/*
		 * 	2.  PopUp Sensor Test:
		 * 		a.  Test the following preconditions:  PopUp sensor has no glass, Mock Conveyor Has glass
		 * 		b.  Have transducer send a message to this sensor agent that glass has hit GUI popUp sensor
		 * 		c.  Test the following postconditions:  PopUp sensor has a glass with state justEntered, Mock Conveyor Has glass
		 * 		d.  Run sensor scheduler
		 * 		e.  Test postconditions: PopUp sensor still has glass with the Yes onSensor state, Mock Conveyor Has glass with updated state, GUI conveyor should still be turned on in MockAnimation
		 * 		f.  Have transducer call that glass has left sensor
		 * 		g.  Check postconditions:  PopUp sensor still has glass with changed state, Mock Conveyor Has glass
		 * 		h.  Run scheduler
		 * 		i.  Check postconditions:  PopUp sensor has no glass, Mock Conveyor Has glass, and that GUI conveyor is still on
		 *
		 */
		
		// Check for the following preconditions:
		assertTrue(realCF.getSensor("popUp").getGlassSheets().size() == 0); // There should not be any glass within this array
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // There should be glass here
		
		// Now send a piece of glass from the GuiPopUp sensor that was "just hit" by the glass currently on the conveyor
		mockAnimation.firePopUpSensorEnterGlass(glass);
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Check for the following postconditions:
		assertTrue(realCF.getSensor("popUp").getGlassSheets().size() == 1); // PopUp sensor should have glass
		assertEquals(realCF.getSensor("popUp").getGlassSheets().get(0).onSensor, onSensor.justEntered); // PopUp sensor glass state should be justEntered
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // MockConveyor should have glass
		
		// Let's run the sensor scheduler
		realCF.getSensor("popUp").runScheduler();
		
		// Check for the following postConditions:
		assertTrue(realCF.getSensor("popUp").getGlassSheets().size() == 1); // PopUp sensor should still have glass
		assertEquals(realCF.getSensor("popUp").getGlassSheets().get(0).onSensor, onSensor.yes); // Glass should now be in the yes state
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 0); // The Mockconveyor should have sent it's glass to the popUp
		
		// Let's also assume that the conveyor got it's glass back from processing from the mock popUp, and that the glass was removed from the popUp		
		mockPopUp.sendBackProcessedGlass(glass);
		
		// Now process the transducer event(s) and then check that the conveyor is still on!
		while (transducer.processNextEvent());
		
		// Check for the following postConditions:
		assertTrue(realCF.getSensor("popUp").getGlassSheets().size() == 1); // Entry sensor should still have glass
		assertEquals(realCF.getSensor("popUp").getGlassSheets().get(0).onSensor, onSensor.yes); // Glass should now be in the yes state
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // The Mockconveyor should have glass now
		
		assertTrue(realCF.getConveyor().isConveyorOn() == false); // GUI conveyor should have been turned off, nothing is currently on it (the glass is in the popUp)		
		
		// Now the GuiGlass has just left this sensor, so the MockAnimation will notify this sensor of the news via the transducer
		
		mockAnimation.firePopUpSensorExitGlass(glass); // Note that the parsing to get to this point will have to be done for real in v1
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Check for the following postconditions:
		
		// SensorAgent with glass should have a changed state, but the glass should still be there
		assertTrue(realCF.getSensor("popUp").getGlassSheets().size() == 1); // Entry sensor should still have glass
		assertEquals(realCF.getSensor("popUp").getGlassSheets().get(0).onSensor, onSensor.no); // Glass should now be in the no state
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // Nothing should have changed within the mock conveyor
		
		// Let's run the sensor scheduler
		realCF.getSensor("popUp").runScheduler();
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Check for the following postconditions:
		assertTrue(realCF.getSensor("popUp").getGlassSheets().size() == 0); // Entry sensor should not have the glass now
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // Mock conveyor still has the glass
		assertTrue(realCF.getPopUp().getGlassToBeProcessed().size() == 0); // Mock popUp has no glass
		assertTrue(realCF.getConveyor().isConveyorOn() == true); // GUI conveyor should still be turned on	
		
		// At this point, it is assumed that the glass has moved along the conveyor until it reaches the exit sensor.  Thus, the following test will occur:
		
		/*
		 * 	3.  Exit Sensor Test:
		 * 		a.  Test the following preconditions:  Exit sensor has no glass, Mock Conveyor Has glass
		 * 		b.  Have transducer send a message to this sensor agent that glass has hit GUI exit sensor
		 * 		c.  Test the following postconditions:  Exit sensor has a glass with state justEntered, Mock Conveyor Has glass
		 * 		d.  Run sensor scheduler
		 * 		e.  Test postconditions: Exit sensor still has glass with the Yes onSensor state, Mock Conveyor Has no glass
		 * 		f.  Have transducer call that glass has left sensor
		 * 		g.  Check postconditions:  Exit sensor still has glass with changed state, Mock Conveyor Has no glass
		 * 		h.  Run scheduler
		 * 		i.  Check postconditions:  Exit sensor has no glass, Mock Conveyor Has glass, GUI conveyor should be turned off
		 */
		
		// Check for the following preconditions:
		assertTrue(realCF.getSensor("exit").getGlassSheets().size() == 0); // There should not be any glass within this array
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // There should be glass here
		
		// Now send a piece of glass from the GuiPopUp sensor that was "just hit" by the glass currently on the conveyor
		mockAnimation.fireExitSensorEnterGlass(glass);
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Check for the following postconditions:
		assertTrue(realCF.getSensor("exit").getGlassSheets().size() == 1); // Exit sensor should have glass
		assertEquals(realCF.getSensor("exit").getGlassSheets().get(0).onSensor, onSensor.justEntered); // Exit sensor glass state should be justEntered
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // MockConveyor should have glass
		
		// Let's run the sensor scheduler
		realCF.getSensor("exit").runScheduler();
		
		// Check for the following postConditions:
		assertTrue(realCF.getSensor("exit").getGlassSheets().size() == 1); // Exit sensor should still have glass
		assertEquals(realCF.getSensor("exit").getGlassSheets().get(0).onSensor, onSensor.yes); // Glass should now be in the yes state
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 0); // The Mockconveyor should not have glass now 
		// the conveyor mock immediately passes off the glass to the next family as soon as the glass hits the exit sensor
		
		// Now process the transducer event(s) and then check that the conveyor is still on!
		while (transducer.processNextEvent());
		
		// Check for the following postConditions:
		assertTrue(realCF.getSensor("exit").getGlassSheets().size() == 1); // Entry sensor should still have glass
		assertEquals(realCF.getSensor("exit").getGlassSheets().get(0).onSensor, onSensor.yes); // Glass should now be in the yes state
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 0); // The Mockconveyor should have glass now
		
		assertTrue(realCF.getConveyor().isConveyorOn() == false); // GUI conveyor should have been turned off, becasue the glass is no longer on the conveyorfamily currently being tested on		
		
		// Now the GuiGlass has just left this sensor, so the MockAnimation will notify this sensor of the news via the transducer
		
		mockAnimation.fireExitSensorExitGlass(glass); // Note that the parsing to get to this point will have to be done for real in v1
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Check for the following postconditions:
		
		// SensorAgent with glass should have a changed state, but the glass should still be there
		assertTrue(realCF.getSensor("exit").getGlassSheets().size() == 1); // Entry sensor should still have glass
		assertEquals(realCF.getSensor("exit").getGlassSheets().get(0).onSensor, onSensor.no); // Glass should now be in the no state
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 0); // Nothing should have changed within the mock conveyor
		
		// Let's run the sensor scheduler
		realCF.getSensor("exit").runScheduler();
		
		// Check for the following postconditions:
		assertTrue(realCF.getSensor("exit").getGlassSheets().size() == 0); // Entry sensor should not have the glass now
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 0); // Mock conveyor does not has the glass	
		
		/*
		 * Since each sensor agent is only supoosed to hold ONE piece of glass at a time, it does not seem necessary to test with multiple pieces of glass since 
		 * it does not test any cases specific to multiple glasses, but make makes things more redundant, in terms to just testing the sensor agents to see that they
		 * work, versus testing how glass progresses through a conveyorFamily
		 */
	}
	
	@Test
	public void secondarySensorAgentTestTwoPlusGlass() {
		/**
		 * This test will complete the following objective: check to see that the conveyor stays on when 2+ glasses are on the conveyor, and one is leaving
		 * How this test will work:
		 * 1.  Two pieces of glass will be loaded into a mock conveyor, and then at the exit sensor removal stage, the conveyorOn status will be checked
		 * 2.  This test will be really basic, and will use the shortcut of just directly giving the conveyor the glass while avoiding Sensor entanglements.
		 *     That has already been tested in the previous code, and as seen, that already works according to the tests previously set up 
		 *     Also, the same paradigm applies from the test whether there are two pieces of glass or 20 pieces of glass, so I will only do this test with two pieces of glass
		 */		 
		
		/*
		 * 1. TwoPlusGlass Test:
		 *		a.  After initialization, check the precondition that there are no glasses in the conveyor
		 *		b.  Add the two glasses into the conveyor
		 *		c.  Test postconditions: There should be two glasses within the mock conveyor
		 *		d.  Fire the animation exit sensor glass entry from the mock animation
		 * 		e.  Test postconditions:  Exit sensor has a glass with state justEntered, Mock Conveyor Has glass
		 * 		f.  Run sensor scheduler
		 * 		g.  Test postconditions: Exit sensor still has glass with the Yes onSensor state, Mock Conveyor Has no glass
		 * 		h.  Have transducer call that glass has left sensor
		 * 		i.  Test postconditions:  Exit sensor still has glass with changed state, Mock Conveyor Has no glass
		 * 		j.  Run scheduler
		 * 		k.  Test postconditions:  Exit sensor has no glass, Mock Conveyor Has glass, GUI conveyor should be turned off
		 */
		
		System.out.println("/****************Test: secondarySensorAgentTestTwoPlusGlass****************/");
		
		// Create a piece of glass to use for the test
		Glass glass = new Glass(); // Since processing is not an issue with this test, let's just leave that field blank
		Glass glass2 = new Glass(); // Since processing is not an issue with this test, let's just leave that field blank

		
		// Instantiate the transducer
		Transducer transducer = new Transducer();
		
		// List of types for each sensor
		List<String> typesA = new ArrayList<String>();
		typesA.add("entry");
		
		// List of types for each sensor
		List<String> typesB = new ArrayList<String>();
		typesB.add("popUp");
		
		// List of types for each sensor
		List<String> typesC = new ArrayList<String>();
		typesC.add("exit");
		
		// Create the three sensor agents
		Sensor entrySensor = new SensorAgent("entrySensor", transducer, typesA);
		Sensor popUpSensor = new SensorAgent("popUpSensor", transducer, typesB);
		Sensor exitSensor = new SensorAgent("exitSensor", transducer, typesC);
		
		// Add these sensors to a list and then put them within a conveyor family
		List<Sensor> sensors = new ArrayList<Sensor>();
		sensors.add(entrySensor);
		sensors.add(popUpSensor);
		sensors.add(exitSensor);
		
		// Make the Mock Conveyor
		Conveyor mockConveyor = new MockConveyor("mockConveyor", transducer);
		
		// Make the Mock PopUp
		PopUp mockPopUp = new MockPopUp("mockPopUp", transducer);
		
		// Make the Mock Animation for the Tests outlined in 4.
		MockAnimation mockAnimation = new MockAnimation(transducer);
		
		// Instantiate the conveyorFamilies and place everything inside them
		MockConveyorFamily mockPrevCF = new MockConveyorFamily("mockPrevCF");
		MockConveyorFamily mockNextCF = new MockConveyorFamily("mockNextCF");
		ConveyorFamilyImp realCF = new ConveyorFamilyImp("realCF", mockConveyor, sensors, mockPopUp);
		
		// Link up the conveyor families
		realCF.setPrevCF(mockPrevCF);
		realCF.setNextCF(mockNextCF);
		mockNextCF.setPrevCF(realCF);
		
		// Now that everything has been set up within the conveyor families, let us begin
		
		// Check pre-conditions:  No glass within the mock conveyor
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 0);
		
		// Add the two pieces of glass
		realCF.getConveyor().msgGiveGlassToConveyor(glass);
		realCF.getConveyor().msgGiveGlassToConveyor(glass2);
		
		// Check post-conditions:  Two glasses within the mock conveyor
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 2);
		
		/***********/
		// Now let's complete all of the exit sensor logic and complete the test
		/***********/
		
		// Check for the following preconditions:
		assertTrue(realCF.getSensor("exit").getGlassSheets().size() == 0); // There should not be any glass within this array
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 2); // There should be glass here, two actually
		
		// Now send a piece of glass from the GuiPopUp sensor that was "just hit" by the glass currently on the conveyor
		mockAnimation.fireExitSensorEnterGlass(glass);
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Check for the following postconditions:
		assertTrue(realCF.getSensor("exit").getGlassSheets().size() == 1); // Exit sensor should have glass
		assertEquals(realCF.getSensor("exit").getGlassSheets().get(0).onSensor, onSensor.justEntered); // Exit sensor glass state should be justEntered
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 2); // MockConveyor should have two glasses still
		
		// Let's run the sensor scheduler
		realCF.getSensor("exit").runScheduler();
		
		// Check for the following postConditions:
		assertTrue(realCF.getSensor("exit").getGlassSheets().size() == 1); // Exit sensor should still have glass
		assertEquals(realCF.getSensor("exit").getGlassSheets().get(0).onSensor, onSensor.yes); // Glass should now be in the yes state
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // The Mockconveyor should only have one glass at this point
		// the conveyor mock immediately passes off the glass to the next family as soon as the glass hits the exit sensor
		
		// Now process the transducer event(s) and then check that the conveyor is still on!
		while (transducer.processNextEvent());
		
		// Check for the following postConditions:
		assertTrue(realCF.getSensor("exit").getGlassSheets().size() == 1); // Entry sensor should still have glass
		assertEquals(realCF.getSensor("exit").getGlassSheets().get(0).onSensor, onSensor.yes); // Glass should now be in the yes state
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // The Mockconveyor should have glass now
		
		assertTrue(realCF.getConveyor().isConveyorOn() == true); // GUI conveyor should have still be on, one glass has yet to go through the conveyor
		
		// Now the GuiGlass has just left this sensor, so the MockAnimation will notify this sensor of the news via the transducer
		
		mockAnimation.fireExitSensorExitGlass(glass); // Note that the parsing to get to this point will have to be done for real in v1
		
		// Now process the transducer event(s), if any
		while (transducer.processNextEvent());
		
		// Check for the following postconditions:
		
		// SensorAgent with glass should have a changed state, but the glass should still be there
		assertTrue(realCF.getSensor("exit").getGlassSheets().size() == 1); // Entry sensor should still have glass
		assertEquals(realCF.getSensor("exit").getGlassSheets().get(0).onSensor, onSensor.no); // Glass should now be in the no state
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // Nothing should have changed within the mock conveyor
		
		// Let's run the sensor scheduler
		realCF.getSensor("exit").runScheduler();
		
		// Check for the following postconditions:
		assertTrue(realCF.getSensor("exit").getGlassSheets().size() == 0); // Entry sensor should not have the glass now
		assertTrue(realCF.getConveyor().getGlassSheets().size() == 1); // Mock conveyor still has one glass.
		
		/*Now if the exit sensor case were to run again, it would be just like in the first test, so repitition does not seem necessary here*/
	}
	
	@Test
	public void testTurnOnOffConveyor() {
		/**
		 * This test will complete the following objective: check to see that the conveyor stays on/off based upon the conditions given in the testTurnOnOffConveyor() function
		 * How this test will work:
		 * 1.  The entry sensor will introduce and remove a piece of glass to test each case
		 * 2.  After each pice of glass is removed from the entrySensor, the value of the MockPopUp will be changed to test a new case, until all caess are tested.
		 * Cases to be tested:
		 * 1.  Conveyor turns from off to on
		 * 		a. cf.getSensor("popUp").getGlassSheets().size() == 0 && cf.getPopUp().isPopUpDown() == true && cf.getPopUp().getGlassToBeProcessed().size() == 0
		 * 2.  Conveyor Stays on while already on
		 * 3.  Conveyor Turns off while on
		 * 		a.  cf.getPopUp().isPopUpDown() == false
		 * 		b.  cf.getPopUp().getGlassToBeProcessed().size() > 0 && cf.getSensor("popUp").getGlassSheets().size() > 0
		 * 4.  Conveyor Stays off while off
		 * 		a.  cf.getSensor("popUp").getGlassSheets().size() > 0
		 * 			1)  cf.getPopUp().isPopUpDown() == false
		 * 			2)  cf.getPopUp().getGlassToBeProcessed().size() > 0
		 */
		
		// Now lets start the test
		System.out.println("/****************Test: testTurnOnOffConveyor****************/");
		
		// Create a piece of glass to use for the test
		Glass glass = new Glass(); // Since processing is not an issue with this test, let's just leave that field blank

		
		// Instantiate the transducer
		Transducer transducer = new Transducer();
		
		// List of types for each sensor
		List<String> typesA = new ArrayList<String>();
		typesA.add("entry");
		
		// List of types for each sensor
		List<String> typesB = new ArrayList<String>();
		typesB.add("popUp");
		
		// List of types for each sensor
		List<String> typesC = new ArrayList<String>();
		typesC.add("exit");
		
		// Create the three sensor agents
		Sensor entrySensor = new SensorAgent("entrySensor", transducer, typesA);
		Sensor popUpSensor = new SensorAgent("popUpSensor", transducer, typesB);
		Sensor exitSensor = new SensorAgent("exitSensor", transducer, typesC);
		
		// Add these sensors to a list and then put them within a conveyor family
		List<Sensor> sensors = new ArrayList<Sensor>();
		sensors.add(entrySensor);
		sensors.add(popUpSensor);
		sensors.add(exitSensor);
		
		// Make the Mock Conveyor
		MockConveyor mockConveyor = new MockConveyor("mockConveyor", transducer);
		
		// Make the Mock PopUp
		MockPopUp mockPopUp = new MockPopUp("mockPopUp", transducer);
		
		// Make the Mock Animation for the Tests outlined in 4.
		MockAnimation mockAnimation = new MockAnimation(transducer);
		
		// Instantiate the conveyorFamilies and place everything inside them
		MockConveyorFamily mockPrevCF = new MockConveyorFamily("mockPrevCF");
		MockConveyorFamily mockNextCF = new MockConveyorFamily("mockNextCF");
		ConveyorFamilyImp realCF = new ConveyorFamilyImp("realCF", mockConveyor, sensors, mockPopUp);
		
		// Link up the conveyor families
		realCF.setPrevCF(mockPrevCF);
		realCF.setNextCF(mockNextCF);
		mockNextCF.setPrevCF(realCF);
		
		// Change no values for the pop up, and let case 1.a. run
		
		// Check proconditions:  Conveyor should be off
		assertTrue(realCF.getConveyor().isConveyorOn() == false);

		/**********/
		// Now run case 1
		/**********/
		
		// *Note, since my first unit test proves the working functionality of the entry sensor message recpetion, scheduler, and action taking, it
		// does not seem necessary to repeat the code here and clutter things up here when it is not necessary
		
		// Send the entry sensor a piece of glass
		realCF.msgHereIsGlass(glass);
		
		// Let's run the sensor scheduler
		realCF.getSensor("entry").runScheduler();
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Now the conveyor should be on
		assertTrue(realCF.getConveyor().isConveyorOn() == true);
		// Lets make sure that the conditional expressions are correct
		assertTrue(realCF.getSensor("popUp").getGlassSheets().size() == 0 && realCF.getPopUp().isPopUpDown() == true && realCF.getPopUp().getGlassToBeProcessed().size() == 0);
		
		// Remove the glass from the entry sensor, and test the next case
		mockAnimation.fireEntrySensorExitGlass(glass);
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Let's run the sensor scheduler to remove the glass
		realCF.getSensor("entry").runScheduler();
		
		/**********/
		// Now let's test case 2
		/**********/
		
		// Send the entry sensor a piece of glass
		realCF.msgHereIsGlass(glass);
		
		// Let's run the sensor scheduler
		realCF.getSensor("entry").runScheduler();
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// The conveyor should STILL be on
		assertTrue(realCF.getConveyor().isConveyorOn() == true);
		
		// Remove the glass from the entry sensor, and test the next case
		mockAnimation.fireEntrySensorExitGlass(glass);
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Let's run the sensor scheduler to remove the glass
		realCF.getSensor("entry").runScheduler();
		
		/**********/
		// Let's test case 3.a.
		/**********/
		mockPopUp.popUpDown = false; 
		
		// Send the entry sensor a piece of glass
		realCF.msgHereIsGlass(glass);
		
		// Let's run the sensor scheduler
		realCF.getSensor("entry").runScheduler();
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// The conveyor should now be off
		assertTrue(realCF.getConveyor().isConveyorOn() == false);
		// Test the equality conditions so that they match the case number
		assertTrue(realCF.getPopUp().isPopUpDown() == false);
		
		// Remove the glass from the entry sensor, and test the next case
		mockAnimation.fireEntrySensorExitGlass(glass);
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Let's run the sensor scheduler to remove the glass
		realCF.getSensor("entry").runScheduler();
		
		/**********/
		// Let's test case 4.a.1
		/**********/
		mockPopUp.popUpDown = false; 
		// Add glass into the popUp sensor
		mockAnimation.firePopUpSensorEnterGlass(glass);
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Send the entry sensor a piece of glass
		realCF.msgHereIsGlass(glass);
		
		// Let's run the sensor scheduler
		realCF.getSensor("entry").runScheduler();
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// The conveyor should now be off
		assertTrue(realCF.getConveyor().isConveyorOn() == false);
		// Test the equality conditions so that they match the case number
		assertTrue(realCF.getSensor("popUp").getGlassSheets().size() > 0 && realCF.getPopUp().isPopUpDown() == false);
		
		// Remove the glass from the entry sensor, and test the next case
		mockAnimation.fireEntrySensorExitGlass(glass);
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Let's run the sensor scheduler to remove the glass
		realCF.getSensor("entry").runScheduler();
		
		/**********/
		// Let's test case 3.b.
		/**********/
		mockPopUp.popUpDown = true;
		// Add glass into popUp
		mockPopUp.msgGiveGlassToPopUp(glass);
		
		mockConveyor.conveyorOn = true; // Set the conveyorOn Variable to true
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Send the entry sensor a piece of glass
		realCF.msgHereIsGlass(glass);
		
		// Let's run the sensor scheduler
		realCF.getSensor("entry").runScheduler();
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// The conveyor should now be off
		assertTrue(realCF.getConveyor().isConveyorOn() == false);
		// Test the equality conditions so that they match the case number
		assertTrue(realCF.getPopUp().getGlassToBeProcessed().size() > 0 && realCF.getSensor("popUp").getGlassSheets().size() > 0);
		
		// Remove the glass from the entry sensor, and test the next case
		mockAnimation.fireEntrySensorExitGlass(glass);
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Let's run the sensor scheduler to remove the glass
		realCF.getSensor("entry").runScheduler();
		
		/**********/
		// Let's test case 4.a.2
		/**********/
		
		// Send the entry sensor a piece of glass
		realCF.msgHereIsGlass(glass);
		
		// Let's run the sensor scheduler
		realCF.getSensor("entry").runScheduler();
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// The conveyor should still be off
		assertTrue(realCF.getConveyor().isConveyorOn() == false);
		// Test the equality conditions so that they match the case number
		assertTrue(realCF.getSensor("popUp").getGlassSheets().size() > 0);
		assertTrue(realCF.getPopUp().getGlassToBeProcessed().size() > 0);
		
		// Remove the glass from the entry sensor, and test the next case
		mockAnimation.fireEntrySensorExitGlass(glass);
		
		// Now process the transducer event(s)
		while (transducer.processNextEvent());
		
		// Let's run the sensor scheduler to remove the glass
		realCF.getSensor("entry").runScheduler();
	}
}
