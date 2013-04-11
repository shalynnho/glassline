package engine.agent.tim.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.OfflineConveyorFamily;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.OfflineWorkstationAgent;
import engine.agent.tim.agents.ConveyorAgent;
import engine.agent.tim.agents.PopUpAgent;
import engine.agent.tim.agents.SensorAgent;
import engine.agent.tim.interfaces.Machine;
import engine.agent.tim.interfaces.Sensor;
import engine.agent.tim.misc.ConveyorFamilyImp;
import engine.agent.tim.misc.MyGlassConveyor.conveyorState;
import engine.agent.tim.misc.MyGlassPopUp.processState;
import engine.agent.tim.test.Mock.MockAnimation;
import engine.agent.tim.test.Mock.MockConveyor;
import engine.agent.tim.test.Mock.MockConveyorFamily;
import engine.agent.tim.test.Mock.MockMachine;
import engine.agent.tim.test.Mock.MockPopUp;
import engine.agent.tim.test.Mock.MockAnimation.PopUpHeightState;
import engine.agent.tim.misc.MachineCom;;

public class PopUpTestCases {

	// These cases will only test to make sure that the INNER functionality of this agent works.  These tests will not involve the entire conveyor family
	
	@Test
	public void testPopUpOneGlassProcessingNo() {
		// This test will test the agent messaging system of the PopUp agent, and not the animation stuff necessarily.  The latter will be tested outside of JUnit within the factory backend.
		
		// Set up the conveyor family
		
		System.out.println("/****************Test: testPopUpOneGlassProcessingNo****************/");
		
		// Create a piece of glass to use for the test
		Glass glass = new Glass(); // Since processing is not an issue with this test, let's just leave that field blank
		
		// Instantiate the transducer
		Transducer transducer = new Transducer();
		
		// Create the three sensor agents
		SensorAgent sensor = new SensorAgent("sensor", transducer, 0, 1);
		
		// Make the Conveyor
		ConveyorAgent conveyor = new ConveyorAgent("Conveyor", transducer, 0);
		
		// Create the machines
		OfflineWorkstationAgent workstation_0 = new OfflineWorkstationAgent("workstation_0", MachineType.CROSS_SEAMER, 0, transducer);
		OfflineWorkstationAgent workstation_1 = new OfflineWorkstationAgent("workstation_1", MachineType.CROSS_SEAMER, 1, transducer);
		
		// Make the list of machines to send to the popUp
		List<OfflineWorkstationAgent> machines = new ArrayList<OfflineWorkstationAgent>();
		machines.add(workstation_0);
		machines.add(workstation_1);
		
		// Make the Mock PopUp
		PopUpAgent popUp = new PopUpAgent("PopUp", transducer, machines, 0);
		
		// Instantiate the conveyorFamilies and place everything inside them
		MockConveyorFamily mockPrevCF = new MockConveyorFamily("mockPrevCF");
		MockConveyorFamily mockNextCF = new MockConveyorFamily("mockNextCF");
		ConveyorFamilyImp realCF = new ConveyorFamilyImp("realCF", conveyor, sensor, popUp);
		
		// Link up the conveyor families
		realCF.setPrevCF(mockPrevCF);
		realCF.setNextCF(mockNextCF);
		mockNextCF.setPrevCF(realCF);
		
		// Now begin the testing for the sensor and conveyor

		// Begin the test by checking the pre-conditions for the conveyor and the popUp		
		assertTrue(conveyor.getGlassSheets().size() == 0); 
		assertTrue(popUp.getGlassToBeProcessed().size() == 0);
		assertTrue(popUp.isPopUpDown() == true);
		assertTrue(popUp.isPassNextCF() == true);
		
		// Send a piece of glass to the conveyor, and then have it be passed to be popUp
		conveyor.msgGiveGlassToConveyor(glass);
		
		// Let's skip the entry sensor stuff, since that's been tested within SensorTestCases.java
		conveyor.getGlassSheets().get(0).conveyorState = conveyorState.beforePopUpSensor;
		
		// Now have the GUI glass hit the popUp sensor
		Integer[] args = {1};	
		Integer[] args_0 = {0};
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		
		// Process all transducer events
		while (transducer.processNextEvent());
		
		// Have the conveyor run it's scheduler now
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); 
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor); 

		assertTrue(popUp.getGlassToBeProcessed().size() == 1);
		assertTrue(popUp.getGlassToBeProcessed().get(0).processState == processState.awaitingArrival);
		
		assertTrue(popUp.isPopUpDown() == true);
		assertTrue(popUp.isPassNextCF() == true);

		// Hack, release the semaphore so things work
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args_0);
		
		// Process all transducer events
		while (transducer.processNextEvent());
				
		// Now run the scheduler for the popUp
		popUp.pickAndExecuteAnAction();
		
		// Now check the postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); 
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor); 

		assertTrue(popUp.getGlassToBeProcessed().size() == 1);
		assertTrue(popUp.getGlassToBeProcessed().get(0).processState == processState.awaitingRemoval);
		
		assertTrue(popUp.isPopUpDown() == true);
		assertTrue(popUp.isPassNextCF() == true);
		
		// Run the conveyor scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Now check the postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); 
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor); 

		assertTrue(popUp.getGlassToBeProcessed().size() == 1);
		assertTrue(popUp.getGlassToBeProcessed().get(0).processState == processState.awaitingRemoval);
		
		assertTrue(popUp.isPopUpDown() == true);
		assertTrue(popUp.isPassNextCF() == true);
		
		// Now let's run some transducer events
		
		// Have the glass leave the popUp
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		
		// Process all transducer events
		while (transducer.processNextEvent());
		
		// Run conveyor scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Now check the postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0);

		assertTrue(popUp.getGlassToBeProcessed().size() == 1);
		assertTrue(popUp.getGlassToBeProcessed().get(0).processState == processState.awaitingRemoval);
		
		assertTrue(popUp.isPopUpDown() == true);
		assertTrue(popUp.isPassNextCF() == true);
		
		// Hack, release the semaphore so things work
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args_0);
		
		// Process all transducer events
		while (transducer.processNextEvent());
		
		// Run PopUp Scheduler
		popUp.pickAndExecuteAnAction();
		
		// Check Post conditions
		// Now check the postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0);

		assertTrue(popUp.getGlassToBeProcessed().size() == 0);
				
		assertTrue(popUp.isPopUpDown() == true);
		assertTrue(popUp.isPassNextCF() == false);
	}
	
	@Test
	public void testPopUpOneGlassProcessingYes() {
		// This test will test the agent messaging system of the PopUp agent, and not the animation stuff necessarily.  The latter will be tested outside of JUnit within the factory backend.
		
		// Set up the conveyor family
		
		System.out.println("/****************Test: testPopUpOneGlassProcessingYes****************/");
		
		// Create a piece of glass to use for the test		
		MachineType[] processes = {MachineType.CROSS_SEAMER};		
		Glass glass = new Glass(processes); // Since processing is not an issue with this test, let's just leave that field blank
		
		// Instantiate the transducer
		Transducer transducer = new Transducer();
		
		// Create the three sensor agents
		SensorAgent sensor = new SensorAgent("sensor", transducer, 0, 1);
		
		// Make the Conveyor
		ConveyorAgent conveyor = new ConveyorAgent("Conveyor", transducer, 0);
		
		// Create the machines
		OfflineWorkstationAgent workstation_0 = new OfflineWorkstationAgent("workstation_0", MachineType.CROSS_SEAMER, 0, transducer);
		OfflineWorkstationAgent workstation_1 = new OfflineWorkstationAgent("workstation_1", MachineType.CROSS_SEAMER, 1, transducer);
		
		// Make the list of machines to send to the popUp
		List<OfflineWorkstationAgent> machines = new ArrayList<OfflineWorkstationAgent>();
		machines.add(workstation_0);
		machines.add(workstation_1);
		
		// Make the Mock PopUp
		PopUpAgent popUp = new PopUpAgent("PopUp", transducer, machines, 0);
		
		// Set this popUp to the workstations
		workstation_0.setPopupWorkstationInteraction(popUp);
		workstation_1.setPopupWorkstationInteraction(popUp);
		
		// Instantiate the conveyorFamilies and place everything inside them
		MockConveyorFamily mockPrevCF = new MockConveyorFamily("mockPrevCF");
		MockConveyorFamily mockNextCF = new MockConveyorFamily("mockNextCF");
		ConveyorFamilyImp realCF = new ConveyorFamilyImp("realCF", conveyor, sensor, popUp);
		
		// Link up the conveyor families
		realCF.setPrevCF(mockPrevCF);
		realCF.setNextCF(mockNextCF);
		mockNextCF.setPrevCF(realCF);
		
		// Now begin the testing for the sensor and conveyor

		// Begin the test by checking the pre-conditions for the conveyor and the popUp		
		assertTrue(conveyor.getGlassSheets().size() == 0); 
		assertTrue(popUp.getGlassToBeProcessed().size() == 0);
		assertTrue(popUp.isPopUpDown() == true);
		assertTrue(popUp.isPassNextCF() == true);
		
		// Send a piece of glass to the conveyor, and then have it be passed to be popUp
		conveyor.msgGiveGlassToConveyor(glass);
		
		// Let's skip the entry sensor stuff, since that's been tested within SensorTestCases.java
		conveyor.getGlassSheets().get(0).conveyorState = conveyorState.beforePopUpSensor;
		
		// Now have the GUI glass hit the popUp sensor
		Integer[] args = {1};	
		Integer[] args_0 = {0};
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		
		// Process all transducer events
		while (transducer.processNextEvent());
		
		// Have the conveyor run it's scheduler now
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); 
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor); 

		assertTrue(popUp.getGlassToBeProcessed().size() == 1);
		assertTrue(popUp.getGlassToBeProcessed().get(0).processState == processState.awaitingArrival);
		
		assertTrue(popUp.isPopUpDown() == true);
		assertTrue(popUp.isPassNextCF() == true);

		// Hack, release the semaphore so things work
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args_0);
		
		// Process all transducer events
		while (transducer.processNextEvent());
				
		// Now run the scheduler for the popUp
		popUp.pickAndExecuteAnAction();
		
		// Now check the postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); 
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor); 

		assertTrue(popUp.getGlassToBeProcessed().size() == 1);
		assertTrue(popUp.getGlassToBeProcessed().get(0).processState == processState.unprocessed);
		
		assertTrue(popUp.isPopUpDown() == true);
		assertTrue(popUp.isPassNextCF() == true);
		
		// Run the conveyor scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Now check the postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1); 
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor); 

		assertTrue(popUp.getGlassToBeProcessed().size() == 1);
		assertTrue(popUp.getGlassToBeProcessed().get(0).processState == processState.unprocessed);
		
		assertTrue(popUp.isPopUpDown() == true);
		assertTrue(popUp.isPassNextCF() == true);
		
		// Now let's run some transducer events
		
		// Have the glass leave the popUp
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		
		// Process all transducer events
		while (transducer.processNextEvent());
		
		// Run conveyor scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Now check the postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0);

		assertTrue(popUp.getGlassToBeProcessed().size() == 1);
		assertTrue(popUp.getGlassToBeProcessed().get(0).processState == processState.unprocessed);
		
		assertTrue(popUp.isPopUpDown() == true);
		assertTrue(popUp.isPassNextCF() == true);
		
		// Hack, release the semaphore so things work
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args_0);
		transducer.fireEvent(TChannel.POPUP, TEvent.WORKSTATION_LOAD_FINISHED, args_0);
		
		// Process all transducer events
		while (transducer.processNextEvent());
		
		// Run PopUp Scheduler
		popUp.pickAndExecuteAnAction();

		// Now check the postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0);

		assertTrue(popUp.getGlassToBeProcessed().size() == 1);
		assertTrue(popUp.getGlassToBeProcessed().get(0).processState == processState.processing);
		assertTrue(popUp.getMachineComs().get(0).inUse == true);
		
		assertTrue(popUp.isPopUpDown() == false);
		assertTrue(popUp.isPassNextCF() == true);
		
		// Hack, assume that glass is done processing and send it back to conveyor
		popUp.msgGlassDone(glass, popUp.getMachineComs().get(0).machineIndex);
		
		// Now check the postconditions
		assertTrue(popUp.getGlassToBeProcessed().size() == 1);
		assertTrue(popUp.getGlassToBeProcessed().get(0).processState == processState.doneProcessing);
		assertTrue(popUp.getMachineComs().get(0).inUse == false);
		
		assertTrue(popUp.isPopUpDown() == false);
		assertTrue(popUp.isPassNextCF() == true);
		
		// Set up all of the trasnducer hacks to release the semaphores
		// Hack, release the semaphore so things work
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args_0);
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args_0);
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args_0);
		
		// Process all transducer events
		while (transducer.processNextEvent());
		
		// Run the popUp scheduler
		popUp.pickAndExecuteAnAction();
		
		// Check post conditions
		assertTrue(popUp.getGlassToBeProcessed().size() == 1);
		assertTrue(popUp.getGlassToBeProcessed().get(0).processState == processState.awaitingRemoval);
		assertTrue(popUp.getMachineComs().get(0).inUse == false);
		
		assertTrue(popUp.isPopUpDown() == true);
		assertTrue(popUp.isPassNextCF() == true);
		
		// Hack, release the semaphore so things work
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args_0);
				
		// Process all transducer events
		while (transducer.processNextEvent());
		
		// Run the scheduler
		popUp.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0);

		assertTrue(popUp.getGlassToBeProcessed().size() == 0);
				
		assertTrue(popUp.isPopUpDown() == true);
		assertTrue(popUp.isPassNextCF() == false);
		
		// Now the processed glass has been removed from the popUp		
	}
}
