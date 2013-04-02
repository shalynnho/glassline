package engine.agent.tim.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import shared.Glass;
import shared.interfaces.ConveyorFamily;
import transducer.Transducer;
import engine.agent.tim.agents.ConveyorAgent;
import engine.agent.tim.agents.SensorAgent;
import engine.agent.tim.interfaces.Conveyor;
import engine.agent.tim.interfaces.PopUp;
import engine.agent.tim.interfaces.Sensor;
import engine.agent.tim.misc.ConveyorFamilyImp;
import engine.agent.tim.misc.MyGlassConveyor.conveyorState;
import engine.agent.tim.test.Mock.MockAnimation;
import engine.agent.tim.test.Mock.MockConveyor;
import engine.agent.tim.test.Mock.MockConveyorFamily;
import engine.agent.tim.test.Mock.MockPopUp;

public class ConveyorTestCases {

	// These cases will only test to make sure that the INNER functionality of this agent works.  These tests will not involve the entire conveyor family
	
	@Test
	public void testConveyorOneGlass() {
		/**
		 * This test will complete the following objectives: It will test the messaging system of the ConveyorAgent, along with the scheduler and action taking
		 * How this test will work:
		 * A.  It will not take into account whether glass has been processed or not, because the conveyorAgent only looks at the ID, and not the glass reference, for comparison
		 *     The assumption will be that all glass, whether processed or not, is the same in the conveyorAgent's eyes
		 * B.  Since sensor functionality for the glass is already tested, and working, within the sensor tests file, I will use the test code to simulate sensor use and simplify things
		 * C.  Note that turning the conveyor on or off is controlled by the popUp and Sensor Agents, thus, no code to test this will be within this case. 
		 *     It is already assumed to work based upon the testing of the other conveyor family agents
		 * D.  Glass will Enter the conveyor, be passed to the popUp, be passed back from the popUp, and then be passed to a mock conveyor family 
		 * */
		
		/*
		 * Conveyor Glass Test:
		 * 1.  Test preconditions:  No glass within the conveyor, positionFreeCF is true
		 * 2.  Pass in glass from "entry" sensor
		 * 3.  Test postconditions:  Glass within conveyor, check glass state is onConveyor, positionFreeCF should not have changed
		 * 4.  Run Scheduler
		 * 5.  Test postConditions:  Nothings should have changed from the last check
		 * 6.  Pass in glass from "popUp" sensor
		 * 7.  Test postconditions:  Glass within conveyor, check glass state is passPopUp, positionFreeCF should not have changed
		 * 8.  Run Scheduler
		 * 9.  Test postconditions:  Glass should not be in conveyor but in Mock Popup, positionFreeCF should not have changed
		 * 10.  Have Mock PopUp pass glass back to the conveyor in a message
		 * 11.  Test postconditions:  Glass within conveyor, check glass state is onConveyor, positionFreeCF should not have changed
		 * 12.  Run Scheduler
		 * 13.  Test postConditions:  Nothings should have changed from the last check
		 * 14.  Pass in glass from "exit" sensor
		 * 15.  Test postconditions:  Glass within conveyor, check glass state is passCF, positionFreeCF should not have changed
		 * 16.  Run Scheduler
		 * 17.  Check postconditions: No glass on conveyor, positionFreeCF should be false, glass should be in nextCF
		 * 18.  Have nextCF message positionFree to the conveyor
		 * 19.  Check Postconditions: No glass in conveyor, positionFreeCF should be true
		 */
		
		// Set up the conveyor family
		
		System.out.println("/****************Test: testConveyorOneGlass****************/");
		
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
		
		// Make the Conveyor
		ConveyorAgent conveyor = new ConveyorAgent("Conveyor", transducer);
		
		// Make the Mock PopUp
		MockPopUp mockPopUp = new MockPopUp("mockPopUp", transducer);
		
		// Make the Mock Animation for the Tests outlined in 4.
		MockAnimation mockAnimation = new MockAnimation(transducer);
		
		// Instantiate the conveyorFamilies and place everything inside them
		MockConveyorFamily mockPrevCF = new MockConveyorFamily("mockPrevCF");
		MockConveyorFamily mockNextCF = new MockConveyorFamily("mockNextCF");
		ConveyorFamilyImp realCF = new ConveyorFamilyImp("realCF", conveyor, sensors, mockPopUp);
		
		// Link up the conveyor families
		realCF.setPrevCF(mockPrevCF);
		realCF.setNextCF(mockNextCF);
		mockNextCF.setPrevCF(realCF);
		
		// Now that the conveyorFamily is set up, let's test out the conveyorAgent
		assertTrue(conveyor.getGlassSheets().size() == 0); // No glass within conveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Now let's give a piece of glass through the "entry sensor"
		conveyor.msgGiveGlassToConveyor(glass);
		
		// Check post conditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Pass in glass update from the "popUp sensor"
		conveyor.msgGiveGlassToPopUp(glass);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.passPopUp); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0); // Glass not within conveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 1); // Glass should now be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Now, "return" the glass from processing
		mockPopUp.sendBackProcessedGlass(glass);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should not be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should not be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Pass in glass from the "exit sensor"
		conveyor.msgPassOffGlass(glass);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.passCF); // Glass state within conveyor is passCF
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true	
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0); // Glass not within conveyor		
		assertTrue(conveyor.getPositionFreeNextCF() == false); // PositionFreeCF should be false
		assertTrue(mockNextCF.glassSheets.size() == 1); // Glass should now be located within the conveyor family ahead
		
		// After "some time" has passed, the nextCF messages the conveyor that another piece of glass can be sent through
		mockNextCF.sendPositionFree();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0); // Glass not within conveyor		
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be false
		assertTrue(mockNextCF.glassSheets.size() == 1); // Glass should now be located within the conveyor family ahead
		
		// At this point, the conveyor would be able to process a piece of glass again in a similar fashion
	}
	
	@Test
	public void testConveyorTwoGlasses() {
		/**
		 * This test will complete the following objectives: It will test the messaging system of the ConveyorAgent, along with the scheduler and action taking
		 * How this test will work:
		 * A.  It will not take into account whether glass has been processed or not, because the conveyorAgent only looks at the ID, and not the glass reference, for comparison
		 *     The assumption will be that all glass, whether processed or not, is the same in the conveyorAgent's eyes
		 * B.  Since sensor functionality for the glass is already tested, and working, within the sensor tests file, I will use the test code to simulate sensor use and simplify things
		 * C.  Note that turning the conveyor on or off is controlled by the popUp and Sensor Agents, thus, no code to test this will be within this case. 
		 *     It is already assumed to work based upon the testing of the other conveyor family agents
		 * D.  Glass will Enter the conveyor, be passed to the popUp, be passed back from the popUp, and then be passed to a mock conveyor family
		 * E.  The same process will be done with two pieces of glass 
		 * */
		
		/*
		 * Conveyor Glass Test:
		 * 1.  Test preconditions:  No glass within the conveyor, positionFreeCF is true
		 * 2.  Pass in glass from "entry" sensor
		 * 3.  Test postconditions:  Glass within conveyor, check glass state is onConveyor, positionFreeCF should not have changed
		 * 4.  Run Scheduler
		 * 5.  Test postConditions:  Nothings should have changed from the last check
		 * 6.  Pass in glass from "popUp" sensor
		 * 7.  Test postconditions:  Glass within conveyor, check glass state is passPopUp, positionFreeCF should not have changed
		 * 8.  Run Scheduler
		 * 9.  Test postconditions:  Glass should not be in conveyor but in Mock Popup, positionFreeCF should not have changed
		 * 10.  Have Mock PopUp pass glass back to the conveyor in a message
		 * 11.  Test postconditions:  Glass within conveyor, check glass state is onConveyor, positionFreeCF should not have changed
		 * 12.  Run Scheduler
		 * 13.  Test postConditions:  Nothings should have changed from the last check
		 * 14.  Pass in glass from "exit" sensor
		 * 15.  Test postconditions:  Glass within conveyor, check glass state is passCF, positionFreeCF should not have changed
		 * 16.  Run Scheduler
		 * 17.  Check postconditions: No glass on conveyor, positionFreeCF should be false, glass should be in nextCF
		 * 18.  Have nextCF message positionFree to the conveyor
		 * 19.  Check Postconditions: No glass in conveyor, positionFreeCF should be true
		 */
		
		// Set up the conveyor family
		
		System.out.println("/****************Test: testConveyorTwoGlasses****************/");
		
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
		
		// Make the Conveyor
		ConveyorAgent conveyor = new ConveyorAgent("Conveyor", transducer);
		
		// Make the Mock PopUp
		MockPopUp mockPopUp = new MockPopUp("mockPopUp", transducer);
		
		// Make the Mock Animation for the Tests outlined in 4.
		MockAnimation mockAnimation = new MockAnimation(transducer);
		
		// Instantiate the conveyorFamilies and place everything inside them
		MockConveyorFamily mockPrevCF = new MockConveyorFamily("mockPrevCF");
		MockConveyorFamily mockNextCF = new MockConveyorFamily("mockNextCF");
		ConveyorFamilyImp realCF = new ConveyorFamilyImp("realCF", conveyor, sensors, mockPopUp);
		
		// Link up the conveyor families
		realCF.setPrevCF(mockPrevCF);
		realCF.setNextCF(mockNextCF);
		mockNextCF.setPrevCF(realCF);
		
		/**********/
		
		// Now that the conveyorFamily is set up, let's test out the conveyorAgent
		assertTrue(conveyor.getGlassSheets().size() == 0); // No glass within conveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Now let's give a piece of glass through the "entry sensor"
		conveyor.msgGiveGlassToConveyor(glass);
		
		// Check post conditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		/**********/
		// Now let's give a piece of glass through the "entry sensor"
		conveyor.msgGiveGlassToConveyor(glass2);
		
		// Check post conditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true				

		/**********/
		
		// Pass in glass update from the "popUp sensor"
		conveyor.msgGiveGlassToPopUp(glass);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.passPopUp); // Glass state within conveyor is passPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor, but less than before
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 1); // Glass should now be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		/**********/
		// Make sure to free the popUp for the next piece of glass that is coming on to it
		mockPopUp.processGlass();
		
		// Pass in glass update from the "popUp sensor"
		conveyor.msgGiveGlassToPopUp(glass2);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should also be within MockPopUp
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.passPopUp); // Glass state within conveyor is passPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0); // Glass not within conveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 1); // Glass should now be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true		
		/**********/
		// Make sure to free the popUp for the next piece of glass that is coming on to it
		mockPopUp.processGlass();
		
		// Now, "return" the glass from processing
		mockPopUp.sendBackProcessedGlass(glass);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should still be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should not be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		/**********/
		// Now, "return" the glass from processing
		mockPopUp.sendBackProcessedGlass(glass2);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should not be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should not be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true		
		/**********/
		
		// Pass in glass from the "exit sensor"
		conveyor.msgPassOffGlass(glass);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.passCF); // Glass state within conveyor is passCF
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true	
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass not within conveyor		
		assertTrue(conveyor.getPositionFreeNextCF() == false); // PositionFreeCF should be false
		assertTrue(mockNextCF.glassSheets.size() == 1); // Glass should now be located within the conveyor family ahead
		
		// After "some time" has passed, the nextCF messages the conveyor that another piece of glass can be sent through
		mockNextCF.sendPositionFree();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass not within conveyor		
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be false
		assertTrue(mockNextCF.glassSheets.size() == 1); // Glass should now be located within the conveyor family ahead
		
		// At this point, the conveyor would be able to process a piece of glass again in a similar fashion
		
		/**********/
		// Pass in glass from the "exit sensor"
		conveyor.msgPassOffGlass(glass2);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.passCF); // Glass state within conveyor is passCF
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true	
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0); // Glass not within conveyor		
		assertTrue(conveyor.getPositionFreeNextCF() == false); // PositionFreeCF should be false
		assertTrue(mockNextCF.glassSheets.size() == 2); // Glass should now be located within the conveyor family ahead
		
		// After "some time" has passed, the nextCF messages the conveyor that another piece of glass can be sent through
		mockNextCF.sendPositionFree();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0); // Glass not within conveyor		
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be false
		assertTrue(mockNextCF.glassSheets.size() == 2); // Glass should now be located within the conveyor family ahead
				
		// At this point, the conveyor would be able to process a piece of glass again in a similar fashion
		/**********/
	}
	
	@Test
	public void testConveyorThreeGlasses() {
		/**
		 * This test will complete the following objectives: It will test the messaging system of the ConveyorAgent, along with the scheduler and action taking
		 * How this test will work:
		 * A.  It will not take into account whether glass has been processed or not, because the conveyorAgent only looks at the ID, and not the glass reference, for comparison
		 *     The assumption will be that all glass, whether processed or not, is the same in the conveyorAgent's eyes
		 * B.  Since sensor functionality for the glass is already tested, and working, within the sensor tests file, I will use the test code to simulate sensor use and simplify things
		 * C.  Note that turning the conveyor on or off is controlled by the popUp and Sensor Agents, thus, no code to test this will be within this case. 
		 *     It is already assumed to work based upon the testing of the other conveyor family agents
		 * D.  Glass will Enter the conveyor, be passed to the popUp, be passed back from the popUp, and then be passed to a mock conveyor family
		 * E.  The same process will be done with two pieces of glass 
		 * */
		
		/*
		 * Conveyor Glass Test:
		 * 1.  Test preconditions:  No glass within the conveyor, positionFreeCF is true
		 * 2.  Pass in glass from "entry" sensor
		 * 3.  Test postconditions:  Glass within conveyor, check glass state is onConveyor, positionFreeCF should not have changed
		 * 4.  Run Scheduler
		 * 5.  Test postConditions:  Nothings should have changed from the last check
		 * 6.  Pass in glass from "popUp" sensor
		 * 7.  Test postconditions:  Glass within conveyor, check glass state is passPopUp, positionFreeCF should not have changed
		 * 8.  Run Scheduler
		 * 9.  Test postconditions:  Glass should not be in conveyor but in Mock Popup, positionFreeCF should not have changed
		 * 10.  Have Mock PopUp pass glass back to the conveyor in a message
		 * 11.  Test postconditions:  Glass within conveyor, check glass state is onConveyor, positionFreeCF should not have changed
		 * 12.  Run Scheduler
		 * 13.  Test postConditions:  Nothings should have changed from the last check
		 * 14.  Pass in glass from "exit" sensor
		 * 15.  Test postconditions:  Glass within conveyor, check glass state is passCF, positionFreeCF should not have changed
		 * 16.  Run Scheduler
		 * 17.  Check postconditions: No glass on conveyor, positionFreeCF should be false, glass should be in nextCF
		 * 18.  Have nextCF message positionFree to the conveyor
		 * 19.  Check Postconditions: No glass in conveyor, positionFreeCF should be true
		 */
		
		// Set up the conveyor family
		
		System.out.println("/****************Test: testConveyorThreeGlasses****************/");
		
		// Create a piece of glass to use for the test
		Glass glass = new Glass(); // Since processing is not an issue with this test, let's just leave that field blank		
		Glass glass2 = new Glass(); // Since processing is not an issue with this test, let's just leave that field blank
		Glass glass3 = new Glass(); // Since processing is not an issue with this test, let's just leave that field blank

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
		
		// Make the Conveyor
		ConveyorAgent conveyor = new ConveyorAgent("Conveyor", transducer);
		
		// Make the Mock PopUp
		MockPopUp mockPopUp = new MockPopUp("mockPopUp", transducer);
		
		// Make the Mock Animation for the Tests outlined in 4.
		MockAnimation mockAnimation = new MockAnimation(transducer);
		
		// Instantiate the conveyorFamilies and place everything inside them
		MockConveyorFamily mockPrevCF = new MockConveyorFamily("mockPrevCF");
		MockConveyorFamily mockNextCF = new MockConveyorFamily("mockNextCF");
		ConveyorFamilyImp realCF = new ConveyorFamilyImp("realCF", conveyor, sensors, mockPopUp);
		
		// Link up the conveyor families
		realCF.setPrevCF(mockPrevCF);
		realCF.setNextCF(mockNextCF);
		mockNextCF.setPrevCF(realCF);
		
		/**********/
		
		// Now that the conveyorFamily is set up, let's test out the conveyorAgent
		assertTrue(conveyor.getGlassSheets().size() == 0); // No glass within conveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Now let's give a piece of glass through the "entry sensor"
		conveyor.msgGiveGlassToConveyor(glass);
		
		// Check post conditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		/**********/
		// Now let's give a piece of glass through the "entry sensor"
		conveyor.msgGiveGlassToConveyor(glass2);
		
		// Check post conditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		/**********/
		
		// Pass in glass update from the "popUp sensor"
		conveyor.msgGiveGlassToPopUp(glass);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.passPopUp); // Glass state within conveyor is passPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor, but less than before
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 1); // Glass should now be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		/***************/
		// Now let's give a piece of glass through the "entry sensor"
		conveyor.msgGiveGlassToConveyor(glass3);
		
		// Check post conditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true		
		/***************/
		
		/**********/
		// Make sure to free the popUp for the next piece of glass that is coming on to it
		mockPopUp.processGlass();
		
		// Pass in glass update from the "popUp sensor"
		conveyor.msgGiveGlassToPopUp(glass2);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should also be within MockPopUp
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.passPopUp); // Glass state within conveyor is passPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass not within conveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 1); // Glass should now be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true		
		/**********/
		
		/***************/
		// Make sure to free the popUp for the next piece of glass that is coming on to it
		mockPopUp.processGlass();
		
		// Pass in glass update from the "popUp sensor"
		conveyor.msgGiveGlassToPopUp(glass3);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.passPopUp); // Glass state within conveyor is onConveyor
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0); // Glass not within conveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 1); // Glass should now be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true		
		
		// Now since both machine channels are currently occupied within the popUp, the glass3 reference only was let in because it had no processing
		// Due to that, it must go through the popUp before the other two do
		
		// Now, "return" the glass from processing
		mockPopUp.sendBackProcessedGlass(glass3);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should not be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should not be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true		
		/***************/
		
		// Now, "return" the glass from processing
		mockPopUp.sendBackProcessedGlass(glass);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should still be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should not be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		/**********/
		// Now, "return" the glass from processing
		mockPopUp.sendBackProcessedGlass(glass2);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 3); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(2).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should not be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 3); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(2).conveyorState == conveyorState.onConveyor); // Glass state within conveyor is onConveyor
		assertTrue(mockPopUp.getGlassToBeProcessed().size() == 0); // Glass should not be within MockPopUp
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true		
		/**********/
		
		/***************/
		// Pass in glass from the "exit sensor"
		conveyor.msgPassOffGlass(glass3);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 3); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.passCF); // Glass state within conveyor is passCF
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true	
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass not within conveyor		
		assertTrue(conveyor.getPositionFreeNextCF() == false); // PositionFreeCF should be false
		assertTrue(mockNextCF.glassSheets.size() == 1); // Glass should now be located within the conveyor family ahead
		
		// After "some time" has passed, the nextCF messages the conveyor that another piece of glass can be sent through
		mockNextCF.sendPositionFree();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass not within conveyor		
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be false
		assertTrue(mockNextCF.glassSheets.size() == 1); // Glass should now be located within the conveyor family ahead		
		/***************/
		
		// Pass in glass from the "exit sensor"
		conveyor.msgPassOffGlass(glass);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.passCF); // Glass state within conveyor is passCF
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true	
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass not within conveyor		
		assertTrue(conveyor.getPositionFreeNextCF() == false); // PositionFreeCF should be false
		assertTrue(mockNextCF.glassSheets.size() == 2); // Glass should now be located within the conveyor family ahead
		
		// After "some time" has passed, the nextCF messages the conveyor that another piece of glass can be sent through
		mockNextCF.sendPositionFree();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass not within conveyor		
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be false
		assertTrue(mockNextCF.glassSheets.size() == 2); // Glass should now be located within the conveyor family ahead		
		
		/**********/
		// Pass in glass from the "exit sensor"
		conveyor.msgPassOffGlass(glass2);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); // Glass within conveyor
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.passCF); // Glass state within conveyor is passCF
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be true	
		
		// Run the scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0); // Glass not within conveyor		
		assertTrue(conveyor.getPositionFreeNextCF() == false); // PositionFreeCF should be false
		assertTrue(mockNextCF.glassSheets.size() == 3); // Glass should now be located within the conveyor family ahead
		
		// After "some time" has passed, the nextCF messages the conveyor that another piece of glass can be sent through
		mockNextCF.sendPositionFree();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0); // Glass not within conveyor		
		assertTrue(conveyor.getPositionFreeNextCF() == true); // PositionFreeCF should be false
		assertTrue(mockNextCF.glassSheets.size() == 3); // Glass should now be located within the conveyor family ahead
				
		// At this point, the conveyor would be able to process a piece of glass again in a similar fashion
		/**********/
	}
}
