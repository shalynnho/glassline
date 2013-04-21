package gui.test;

import java.util.ArrayList;
import java.util.List;

import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.LineComponent;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.OfflineWorkstationAgent;
import engine.agent.david.misc.ConveyorFamilyEntity;
import engine.agent.evan.ConveyorFamilyImplementation;
import engine.agent.tim.agents.ConveyorAgent;
import engine.agent.tim.agents.PopUpAgent;
import engine.agent.tim.agents.SensorAgent;
import engine.agent.tim.misc.ConveyorFamilyImp;
import engine.agent.tim.test.Mock.MockConveyorFamily;

/** 
 * Class to reliably test offline cf integrated with the animation
 * Turns conveyors leading up to cf all on, makes some glass move along. All three offline cfs are made as my own version of the cf. 
 * *Must manually make sure glasses enter first family fairly* (the 'before' family does not wait for msgPositionFree
 * The 'after' family never sends msgPositionFree.
 * @author David Zhang
 */
public class TimsOfflineCFIntegrationTest extends GuiTestSM {// implements TReceiver {
	private List<Glass> glasses = new ArrayList<Glass>();
	
	/*Tim's test stuff*/
	// Create a piece of glass to use for the test		
	MachineType[] processes = {MachineType.CROSS_SEAMER};		
	Glass glass;
	
	// Instantiate the transducer
	Transducer transducer = new Transducer();
	
	// Create the three sensor agents
	SensorAgent sensor;
	
	// Make the Conveyor
	ConveyorAgent conveyor;
	
	// Create the machines
	OfflineWorkstationAgent workstation_0;
	OfflineWorkstationAgent workstation_1;
	
	// Make the list of machines to send to the popUp
	List<OfflineWorkstationAgent> machines;
	
	// Make the Mock PopUp
	PopUpAgent popUp;
	
	// Instantiate the conveyorFamilies and place everything inside them
	MockConveyorFamily mockPrevCF;
	MockConveyorFamily mockNextCF;
	ConveyorFamilyImp realCF;
	
	// Secordary test values
	OfflineWorkstationAgent[] crossSeamerWorkstation = new OfflineWorkstationAgent[2];
	ConveyorFamilyImp crossSeamerFamily;
	
	public TimsOfflineCFIntegrationTest(Transducer trans) {
		super(trans, false);
		
		transducer = trans;
		
		startOtherConveyors();
		prepareAgentsB();

		// Create glasses and kick things off
//		test1Glass();
//		test2Glasses();
//		test3Glasses();
		testBigSequence();
	}

	private void prepareAgents() {		
		// Create the three sensor agents
		sensor = new SensorAgent("sensor", transducer, 12, 13);
		
		// Make the Conveyor
		conveyor = new ConveyorAgent("Conveyor", transducer, 6);
		
		// Create the machines
		workstation_0 = new OfflineWorkstationAgent("workstation_0", MachineType.CROSS_SEAMER, 0, transducer);
		workstation_1 = new OfflineWorkstationAgent("workstation_1", MachineType.CROSS_SEAMER, 1, transducer);
		
		// Make the list of machines to send to the popUp
		List<OfflineWorkstationAgent> machines = new ArrayList<OfflineWorkstationAgent>();
		machines.add(workstation_0);
		machines.add(workstation_1);
		
		// Make the PopUp
		popUp = new PopUpAgent("PopUp", transducer, machines, 1);
		
		// Set this popUp to the workstations
		workstation_0.setPopupWorkstationInteraction(popUp);
		workstation_1.setPopupWorkstationInteraction(popUp);
		
		// Instantiate the conveyorFamilies and place everything inside them
		mockPrevCF = new MockConveyorFamily("mockPrevCF");
		mockNextCF = new MockConveyorFamily("mockNextCF");
		realCF = new ConveyorFamilyImp("realCF", conveyor, sensor, popUp);
		
		// Link up the conveyor families
		realCF.setPreviousLineComponent(mockPrevCF);
		realCF.setNextLineComponent(mockNextCF);
		mockNextCF.setPrevCF(realCF);
		
		// Start all of the agent threads
		sensor.startThread();
		conveyor.startThread();
		popUp.startThread();
		
		workstation_0.startThread();
		workstation_1.startThread();
	}
	
	private void prepareAgentsB() { // Alternate setup to test		
		// Make the list of machines to send to the popUp
		crossSeamerWorkstation = new OfflineWorkstationAgent[2];
		for (int i = 0; i < 2; ++i) {
			crossSeamerWorkstation[i] = new OfflineWorkstationAgent(MachineType.CROSS_SEAMER.toString() + i, MachineType.CROSS_SEAMER, i, transducer);
		}
		
		// Create the main CF
		crossSeamerFamily = new ConveyorFamilyImp("Cross Seamer Family", transducer, "Sensors", 12, 13, "Conveyor", 6, "PopUp", 1, crossSeamerWorkstation, MachineType.CROSS_SEAMER);
		
		// Link the machines to the popUp
		for (int i = 0; i < 2; ++i) {
			crossSeamerWorkstation[i].setPopupWorkstationInteraction(crossSeamerFamily.getPopUp());
		}
		
		// Instantiate the Mocks
		mockPrevCF = new MockConveyorFamily("mockPrevCF");
		mockNextCF = new MockConveyorFamily("mockNextCF");
		
		// Link up the conveyor families
		crossSeamerFamily.setPreviousLineComponent(mockPrevCF);
		crossSeamerFamily.setNextLineComponent(mockNextCF);
		mockNextCF.setPrevCF(crossSeamerFamily);
		
		// Start the agent threads
		crossSeamerFamily.startThreads();
		for (int i = 0; i < 2; ++i) {
			crossSeamerWorkstation[i].startThread();
		}
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		//if (channel == TChannel.SENSOR || channel == TChannel.POPUP && args[0] != null)
			//System.out.println("Channel: " + channel + " Index: " + (Integer) args[0]);
		
		// Hack: When glass reaches last sensor right before my 1st cf to test, send msg immediately 
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_RELEASED) {
			if ((Integer) args[0] == 11) {
				if (realCF != null)
					realCF.msgHereIsGlass(glasses.remove(0));
				else
					crossSeamerFamily.msgHereIsGlass(glasses.remove(0));
			}
			if ((Integer) args[0] == 14) { // Hack: Then send msgPositionFree() to realCF
				if (realCF != null)
					realCF.msgPositionFree();
				else
					crossSeamerFamily.msgPositionFree();
			}
		}
		
		// Partly copied from GuiTestAM to make piece move along automatically in places I don't care about
		// Conveyors w/index 0-4:
		if (channel == TChannel.CUTTER && event == TEvent.WORKSTATION_LOAD_FINISHED) {
			System.out.println("WORKSTATION_LOAD_FINISHED from CUTTER");
			t.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_DO_ACTION, null);
		} else if (channel == TChannel.CUTTER && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
			System.out.println("WORKSTATION_GUI_ACTION_FINISHED");
			t.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_RELEASE_GLASS, null);

		} else if (channel == TChannel.BREAKOUT && event == TEvent.WORKSTATION_LOAD_FINISHED) {
			System.out.println("WORKSTATION_LOAD_FINISHED from BREAKOUT");
			t.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_DO_ACTION, null);
		} else if (channel == TChannel.BREAKOUT && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
			System.out.println("WORKSTATION_GUI_ACTION_FINISHED for BREAKOUT");

			t.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_RELEASE_GLASS, null);
		} else if (channel == TChannel.MANUAL_BREAKOUT && event == TEvent.WORKSTATION_LOAD_FINISHED) {
			System.out.println("WORKSTATION_LOAD_FINISHED for MANUAL_BREAKOUT");

			t.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_DO_ACTION, null);
		} else if (channel == TChannel.MANUAL_BREAKOUT && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
			System.out.println("WORKSTATION_GUI_ACTION_FINISHED for MANUAL_BREAKOUT");

			t.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_RELEASE_GLASS, null);
		} else if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_LOAD_FINISHED) {
			if ((Integer) args[0] != 1)
				t.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);
		
		} else if (channel == TChannel.WASHER && event == TEvent.WORKSTATION_LOAD_FINISHED) {
			System.out.println("WORKSTATION_LOAD_FINISHED for WASHER");

			t.fireEvent(TChannel.WASHER, TEvent.WORKSTATION_DO_ACTION, null);
		} else if (channel == TChannel.WASHER && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
			System.out.println("WORKSTATION_GUI_ACTION_FINISHED for WASHER");

			t.fireEvent(TChannel.WASHER, TEvent.WORKSTATION_RELEASE_GLASS, null);
		} else if (channel == TChannel.UV_LAMP && event == TEvent.WORKSTATION_LOAD_FINISHED) {
			System.out.println("WORKSTATION_LOAD_FINISHED for UV_LAMP");

			t.fireEvent(TChannel.UV_LAMP, TEvent.WORKSTATION_DO_ACTION, null);
		} else if (channel == TChannel.UV_LAMP && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
			System.out.println("WORKSTATION_GUI_ACTION_FINISHED for UV_LAMP");

			t.fireEvent(TChannel.UV_LAMP, TEvent.WORKSTATION_RELEASE_GLASS, null);
		} else if (channel == TChannel.PAINTER && event == TEvent.WORKSTATION_LOAD_FINISHED) {
			System.out.println("WORKSTATION_LOAD_FINISHED for PAINTER");

			t.fireEvent(TChannel.PAINTER, TEvent.WORKSTATION_DO_ACTION, null);
		} else if (channel == TChannel.PAINTER && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
			System.out.println("WORKSTATION_GUI_ACTION_FINISHED for PAINTER");

			t.fireEvent(TChannel.PAINTER, TEvent.WORKSTATION_RELEASE_GLASS, null);
		} else if (channel == TChannel.OVEN && event == TEvent.WORKSTATION_LOAD_FINISHED) {
			System.out.println("WORKSTATION_LOAD_FINISHED for OVEN");

			t.fireEvent(TChannel.OVEN, TEvent.WORKSTATION_DO_ACTION, null);
		} else if (channel == TChannel.OVEN && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
			System.out.println("WORKSTATION_GUI_ACTION_FINISHED for OVEN");

			t.fireEvent(TChannel.OVEN, TEvent.WORKSTATION_RELEASE_GLASS, null);
		} else if (channel == TChannel.TRUCK && event == TEvent.TRUCK_GUI_LOAD_FINISHED) {
			System.out.println("TRUCK_GUI_LOAD_FINISHED for TRUCK");

			t.fireEvent(TChannel.TRUCK, TEvent.TRUCK_DO_EMPTY, null);
		}
	}

	// Start conveyors leading up to offline cf
	private void startOtherConveyors() {
		for (int i=0; i<6; i++) {
			Integer args[] = new Integer[1];
			args[0] = i;
			t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
		}
		
		// Start conveyors after conveyor 6 also
		for (int i=7; i<15; i++) {
			Integer args[] = new Integer[1];
			args[0] = i;
			t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
		}
	}
	
	private void wait(int time) {
		try {
			Thread.sleep(time);
		} catch(Exception e) {
			System.err.println("Error: "+e.getMessage());
		}
	}
	
	/* Testing: wait(2000) between creating new glasses b/c the 'before conveyor family' does not wait for msgPositionFree */ 
	
	@SuppressWarnings("unused")
	private void test1Glass() { // uncomment one, and test. all work.
		//glasses.add(new Glass(new MachineType[] { }));
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER })); // yes
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
	}
	
	@SuppressWarnings("unused")
	private void test2Glasses() { // uncomment a section and try it out
		/*
		// No glass with processing -- Works
		glasses.add(new Glass(new MachineType[] {}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(700);
		glasses.add(new Glass(new MachineType[] {}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		*/
		
		/*
		// One Glass with processing, one without -- Works
		glasses.add(new Glass(new MachineType[] {MachineType.CROSS_SEAMER}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(700);
		glasses.add(new Glass(new MachineType[] {}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		*/
		
		/*
		// One Glass with processing, one without, different order -- Works
		glasses.add(new Glass(new MachineType[] {}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(700);
		glasses.add(new Glass(new MachineType[] {MachineType.CROSS_SEAMER}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		*/
		
		///*
		// Two glasses with processing -- Works
		glasses.add(new Glass(new MachineType[] {MachineType.CROSS_SEAMER}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(700);
		glasses.add(new Glass(new MachineType[] {MachineType.CROSS_SEAMER}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		//*/

	}
	
	@SuppressWarnings("unused")
	private void test3Glasses() {
	
		///*
		// All three glasses need processing -- Works
		glasses.add(new Glass(new MachineType[] {MachineType.CROSS_SEAMER}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(700);
		glasses.add(new Glass(new MachineType[] {MachineType.CROSS_SEAMER}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(700);
		glasses.add(new Glass(new MachineType[] {MachineType.CROSS_SEAMER}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		//*/
		
		/*
		// Two pieces of glass need processing, last one does not -- Works
		glasses.add(new Glass(new MachineType[] {MachineType.CROSS_SEAMER}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(700);
		glasses.add(new Glass(new MachineType[] {MachineType.CROSS_SEAMER}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(700);
		glasses.add(new Glass(new MachineType[] {}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		*/
		
		/*
		// Alternation between glass the needs porcessing and does not -- Works
		glasses.add(new Glass(new MachineType[] {MachineType.CROSS_SEAMER}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(700);
		glasses.add(new Glass(new MachineType[] {}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(700);
		glasses.add(new Glass(new MachineType[] {MachineType.CROSS_SEAMER}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		*/
		/*
		// Similar as above test, but with different scheme -- works
		glasses.add(new Glass(new MachineType[] {}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(700);
		glasses.add(new Glass(new MachineType[] {MachineType.CROSS_SEAMER}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(700);
		glasses.add(new Glass(new MachineType[] {}));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		*/		
	}
	
	@SuppressWarnings("unused")
	public void testBigSequence() {
		// This test works, as long as the glass does not come in too fast
		// that should not be an issue once this test code for getting glass across is replaced 
		// by real conveyor families and positionFree() (the glass will stop in ways it currently does not with the test code)
		
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(1000);
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(1000);
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(1000);
		glasses.add(new Glass(new MachineType[] { }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(1000);
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
		wait(1000);
		glasses.add(new Glass(new MachineType[] { }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(1000);
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(1000);
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(1000);
		glasses.add(new Glass(new MachineType[] {  }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(1000);
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
	}
	
	// Simple class to deal with before and after the cfs to be tested
	private class SurroundingConveyorAgent extends Agent implements LineComponent {
		// boolean posFree; // [conveyor before my 1st cf]'s internal boolean for if my 1st cf is ready
//		LineComponent prev, next;
		// List<Glass> glasses = new ArrayList<Glass>();

		public SurroundingConveyorAgent(String name) {
			super(name);
		}

		@Override
		public boolean pickAndExecuteAnAction() {
			// if (posFree && !glass.isEmpty()) {
			// 	actPassOnGlass();
			// }
			return false;
		}

		@Override
		public void eventFired(TChannel channel, TEvent event, Object[] args) {
			// // If 1st sensor of conveyor right before the conveyor of 1st cf, add to list
			// if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED) {
			// 	if ((Integer) args[0] == 8) {

			// 	}
			// }
		}

		// Only for 'before' conveyor
		public void msgPositionFree() {
			print("Received msgPositionFree");
		}

		// Only for 'after' conveyor
		public void msgHereIsGlass(Glass g) {
			print("Received msgHereIsGlass");
		}

		public void setNextLineComponent(LineComponent lc) {
//			next = lc;
		}

		public void setPreviousLineComponent(LineComponent lc) {
//			prev = lc;
		}
	}
}