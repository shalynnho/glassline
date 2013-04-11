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
	
	public TimsOfflineCFIntegrationTest(Transducer trans) {
		super(trans, false);
		
		transducer = trans;
		
		startOtherConveyors();
		prepareAgents();

		// Create glasses and kick things off
		test1Glass();
//		test2Glasses();
//		test3Glasses();
//		testBigSequence();
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
		realCF.setPrevCF(mockPrevCF);
		realCF.setNextCF(mockNextCF);
		mockNextCF.setPrevCF(realCF);
		
		// Start all of the agent threads
		sensor.startThread();
		conveyor.startThread();
		popUp.startThread();
		
		workstation_0.startThread();
		workstation_1.startThread();
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		//if (channel == TChannel.SENSOR || channel == TChannel.POPUP && args[0] != null)
			//System.out.println("Channel: " + channel + " Index: " + (Integer) args[0]);
		
		// Hack: When glass reaches last sensor right before my 1st cf to test, send msg immediately 
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_RELEASED) {
			if ((Integer) args[0] == 11) {
				realCF.msgHereIsGlass(glasses.remove(0));
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
			System.out.println("POPUP_GUI_LOAD_FINISHED for POPUP");

			if ((Integer) args[0] != 1) {
				t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
				
				// Stopping conveyor that DRILL is on to see if glass stops at popup
				// Conclusion: it doesn't, so the moment popup is down, glass automoves to next family
				Integer[] newArgs = new Integer[1];
				newArgs[0] = 0;
				//System.out.println("Stopping conveyor "+newArgs[0]);
				//t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);
				System.out.println("Releasing Glass from PopUp: "+args[0]);
				t.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);
				offlineDone = false;
			}
			else if ((Integer) args[0] == 0)
				t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, args);
		} else if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_UP) {
			System.out.println("POPUP_GUI_MOVED_UP for POPUP");

			Integer[] newArgs = new Integer[1];
			newArgs[0] = 0;
			t.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_LOAD_GLASS, newArgs);
		} else if (channel == TChannel.DRILL && event == TEvent.WORKSTATION_LOAD_FINISHED) {
			System.out.println("WORKSTATION_LOAD_FINISHED for DRILL");

			t.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_ACTION, args);
		} else if (channel == TChannel.DRILL && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
			System.out.println("WORKSTATION_GUI_ACTION_FINISHED for DRILL");
			// looks like 
			t.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_RELEASE_GLASS, args);
			offlineDone = true;
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
		glasses.add(new Glass(new MachineType[] { }));
		//glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER })); // yes
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
	}
	
	@SuppressWarnings("unused")
	private void test2Glasses() { // uncomment a section and try it out
		// yes
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
		// yes
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
		// yes
//		glasses.add(new Glass(new MachineType[] { MachineType.GRINDER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
		// yes
//		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER, MachineType.GRINDER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
		// yes
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
		// 
		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER, MachineType.GRINDER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(2000);
		glasses.add(new Glass(new MachineType[] { MachineType.GRINDER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
		// yes
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
		// yes - second glass runs through machine, but stays put there since next pos is not free
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { MachineType.GRINDER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
		// yes
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
		// yes 
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER, MachineType.GRINDER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER, MachineType.GRINDER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);

		// yes
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER, MachineType.GRINDER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
	}
	
	@SuppressWarnings("unused")
	private void test3Glasses() {
		// yes
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER, MachineType.GRINDER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER, MachineType.GRINDER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
		// yes
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER, MachineType.GRINDER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER, MachineType.GRINDER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2000);
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER, MachineType.GRINDER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
		// yes - but needs a little extra time do 3rd piece enters on time
//		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2500);
//		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER, MachineType.GRINDER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
//		wait(2500);
//		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER }));
//		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		
		// yes
		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(2500);
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER, MachineType.GRINDER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(2500);
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER, MachineType.GRINDER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
	}
	
	@SuppressWarnings("unused")
	public void testBigSequence() {
		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(2500);
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER, MachineType.GRINDER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(2500);
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER, MachineType.GRINDER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(2500);
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER, MachineType.GRINDER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(2500);
		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER, MachineType.GRINDER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(2500);
		glasses.add(new Glass(new MachineType[] { }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		wait(2500);
		glasses.add(new Glass(new MachineType[] { }));
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