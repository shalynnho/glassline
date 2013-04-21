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
import engine.agent.tim.interfaces.Conveyor;
import engine.agent.tim.interfaces.PopUp;
import engine.agent.tim.interfaces.Sensor;
import engine.agent.tim.misc.ConveyorEvent;
import engine.agent.tim.misc.ConveyorFamilyImp;
import engine.agent.tim.misc.MyGlassConveyor.conveyorState;
import engine.agent.tim.misc.MyGlassSensor.onSensor;
import engine.agent.tim.test.Mock.MockAnimation;
import engine.agent.tim.test.Mock.MockConveyor;
import engine.agent.tim.test.Mock.MockConveyorFamily;
import engine.agent.tim.test.Mock.MockPopUp;

public class SensorAndConveyorTestCases {

	@Test
	public void sensorAndConveyorOneGlassTest() {
		// This will test if one piece of glass can get through the conveyor with sensor events helping out -- no animation used -- only testing agent messaging
		// The animation tests will be done with the real animation
		
		// Set up the conveyor family
		
		System.out.println("/****************Test: sensorAndConveyorOneGlassTest****************/");
		
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
		realCF.setPreviousLineComponent(mockPrevCF);
		realCF.setNextLineComponent(mockNextCF);
		mockNextCF.setPrevCF(realCF);
		
		// Now begin the testing for the sensor and conveyor
		
		// Check the preconditions for the conveyor and make sure that everything is set appropriately
		assertTrue(conveyor.getGlassSheets().size() == 0);
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Send glass to conveyor
		conveyor.msgGiveGlassToConveyor(glass);
		
		// Check Postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforeEntrySensor);
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Trigger sensor event -- a GUI glass just went onto the entry sensor
		Integer[] args1 = {0}; // Entry sensor/Popup index (the latter which is not directly nesceeary for this test)
		Integer[] args2 = {1}; // PopUp Sensor index

		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args1);
		
		// Process transducer events
		while (transducer.processNextEvent());
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforeEntrySensor);
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.onEntrySensor);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onEntrySensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Trigger sensor event -- a GUI glass just went off the entry sensor
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args1);
		
		// Process transducer events
		while (transducer.processNextEvent());
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onEntrySensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.offEntrySensor);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Trigger sensor event -- a GUI glass just went on the popUp sensor
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args2);
		
		// Process transducer events
		while (transducer.processNextEvent());
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.onPopUpSensor);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor);
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		assertTrue(popUp.getGlassToBeProcessed().size() == 1);
		
		// Currently, the Conveyor is waiting for acknowledgement from the PopUp sensor to turn back on, one it is recieved, then the conveyor will turn back on
		conveyor.msgPositionFree(); // Hack it to start, popUp is not being tested here
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor);
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.popUpFree);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Trigger sensor event -- a GUI glass just went off the popUp sensor
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args2);
		
		// Process transducer events
		while (transducer.processNextEvent());		
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.offPopUpSensor);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0);		
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Glass has been removed from the conveyor agent at this point.
	}	
	
	@Test
	public void sensorAndConveyorTwoGlassTest() {
		// This will test if two pieces of glass can get through the conveyor with sensor events helping out -- no animation used -- only testing agent messaging
		// The animation tests will be done with the real animation
		
		// Set up the conveyor family
		
		System.out.println("/****************Test: sensorAndConveyorTwoGlassTest****************/");
		
		// Create a piece of glass to use for the test
		Glass glass = new Glass(); // Since processing is not an issue with this test, let's just leave that field blank
		Glass glass_0 = new Glass(); // Since processing is not an issue with this test, let's just leave that field blank
		
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
		realCF.setPreviousLineComponent(mockPrevCF);
		realCF.setNextLineComponent(mockNextCF);
		mockNextCF.setPrevCF(realCF);
		
		// Now begin the testing for the sensor and conveyor
		
		/**********/
		
		// Check the preconditions for the conveyor and make sure that everything is set appropriately
		assertTrue(conveyor.getGlassSheets().size() == 0);
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Send glass to conveyor
		conveyor.msgGiveGlassToConveyor(glass);
		
		// Check Postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforeEntrySensor);
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Trigger sensor event -- a GUI glass just went onto the entry sensor
		Integer[] args1 = {0}; // Entry sensor/Popup index (the latter which is not directly nesceeary for this test)
		Integer[] args2 = {1}; // PopUp Sensor index

		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args1);
		
		// Process transducer events
		while (transducer.processNextEvent());
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforeEntrySensor);
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.onEntrySensor);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onEntrySensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Trigger sensor event -- a GUI glass just went off the entry sensor
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args1);
		
		// Process transducer events
		while (transducer.processNextEvent());
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onEntrySensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.offEntrySensor);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		/**********/
		// Now send the conveyor a second piece of glass
		conveyor.msgGiveGlassToConveyor(glass_0);
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.beforeEntrySensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Trigger sensor event -- a GUI glass just went onto the entry sensor
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args1);
		
		// Process transducer events
		while (transducer.processNextEvent());
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.beforeEntrySensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.onEntrySensor);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.onEntrySensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Trigger sensor event -- a GUI glass just went off the entry sensor
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args1);
		
		// Process transducer events
		while (transducer.processNextEvent());
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.onEntrySensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.offEntrySensor);	
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		/**********/
		
		// Trigger sensor event -- a GUI glass just went off the popUp sensor
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args2);
		
		// Process transducer events
		while (transducer.processNextEvent());
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.onPopUpSensor);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor);
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		assertTrue(popUp.getGlassToBeProcessed().size() == 1);
		
		// Currently, the Conveyor is waiting for acknowledgement from the PopUp sensor to turn back on, one it is recieved, then the conveyor will turn back on
		conveyor.msgPositionFree(); // Hack it to start, popUp is not being tested here
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor);
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.popUpFree);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor);
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Trigger sensor event -- a GUI glass just went off the PopUp sensor
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args2);
		
		// Process transducer events
		while (transducer.processNextEvent());		
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 2);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor);
		assertTrue(conveyor.getGlassSheets().get(1).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.offPopUpSensor);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		/**********/
		
		// Trigger sensor event -- a GUI glass just went on the popUp sensor
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args2);
		
		// Process transducer events
		while (transducer.processNextEvent());
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.beforePopUpSensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.onPopUpSensor);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor);
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		assertTrue(popUp.getGlassToBeProcessed().size() == 2);
		
		// Currently, the Conveyor is waiting for acknowledgement from the PopUp sensor to turn back on, one it is recieved, then the conveyor will turn back on
		conveyor.msgPositionFree(); // Hack it to start, popUp is not being tested here
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor);
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.popUpFree);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Trigger sensor event -- a GUI glass just went off the popUp sensor
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args2);
		
		// Process transducer events
		while (transducer.processNextEvent());		
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 1);
		assertTrue(conveyor.getGlassSheets().get(0).conveyorState == conveyorState.onPopUpSensor);
		assertTrue(conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 1);
		assertTrue(conveyor.getEvents().get(0) == ConveyorEvent.offPopUpSensor);
		
		// Run scheduler
		conveyor.pickAndExecuteAnAction();
		
		// Check postconditions
		assertTrue(conveyor.getGlassSheets().size() == 0);		
		assertTrue(!conveyor.isConveyorOn());
		assertTrue(conveyor.getEvents().size() == 0);
		
		// Glass has been removed from the conveyor agent at this point.	
		
		/**********/
		
		// Glass has been removed from the conveyor agent at this point.
	
	}
}
