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

/** 
 * Class to reliably test offline cf integrated with the animation
 * Turns conveyors leading up to cf all on, makes some glass move along. All three offline cfs are made as my own version of the cf. 
 * Must manually make sure glasses enter first family fairly
 * @author David Zhang
 */
public class DavidsOfflineCFIntegrationTest extends GuiTestSM {// implements TReceiver {
	private OfflineWorkstationAgent drillWks1, drillWks2;
	private OfflineWorkstationAgent crossSeamerWks1, crossSeamerWks2;
	private OfflineWorkstationAgent grinderWks1, grinderWks2;

	private SurroundingConveyorAgent before, after;
	private ConveyorFamilyEntity drillFamily, crossSeamerFamily, grinderFamily;

	private List<Glass> glasses = new ArrayList<Glass>();
	
	public DavidsOfflineCFIntegrationTest(Transducer trans) {
		super(trans, false);
		
		before = new SurroundingConveyorAgent("Before");
		after = new SurroundingConveyorAgent("After");
		
		startOtherConveyors();
		prepareAgents();

		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, MachineType.GRINDER }));
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null); // fires creation of 1 glass!
	}

	private void prepareAgents() {
		drillWks1 = new OfflineWorkstationAgent("Drill workstation 1", MachineType.DRILL, 0, t);
		drillWks2 = new OfflineWorkstationAgent("Drill workstation 2", MachineType.DRILL, 1, t);
		drillFamily = new ConveyorFamilyEntity(t, 5, 0, drillWks1, drillWks2);

		crossSeamerWks1 = new OfflineWorkstationAgent("Cross Seamer workstation 1", MachineType.CROSS_SEAMER, 0, t);
		crossSeamerWks2 = new OfflineWorkstationAgent("Cross Seamer workstation 2", MachineType.CROSS_SEAMER, 1, t);
		crossSeamerFamily = new ConveyorFamilyEntity(t, 6, 1, crossSeamerWks1, crossSeamerWks2);

		grinderWks1 = new OfflineWorkstationAgent("Grinder workstation 1", MachineType.GRINDER, 0, t);
		grinderWks2 = new OfflineWorkstationAgent("Grinder workstation 2", MachineType.GRINDER, 1, t);
		grinderFamily = new ConveyorFamilyEntity(t, 7, 2, grinderWks1, grinderWks2);
		
		// Connect
		before.setNextLineComponent(drillFamily);
		drillFamily.setPreviousLineComponent(before);

		drillFamily.setNextLineComponent(crossSeamerFamily);
		crossSeamerFamily.setPreviousLineComponent(drillFamily);

		crossSeamerFamily.setNextLineComponent(grinderFamily);
		grinderFamily.setPreviousLineComponent(crossSeamerFamily);

		grinderFamily.setNextLineComponent(after);
		after.setPreviousLineComponent(grinderFamily);

		// Start agent threads
		drillWks1.startThread();
		drillWks2.startThread();
		drillFamily.startThreads();

		crossSeamerWks1.startThread();
		crossSeamerWks2.startThread();
		crossSeamerFamily.startThreads();

		grinderWks1.startThread();
		grinderWks2.startThread();
		grinderFamily.startThreads();
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// Hack: When glass reaches last sensor right before my 1st cf to test, send msg immediately 
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_RELEASED) {
			if ((Integer) args[0] == 9) {
				drillFamily.msgHereIsGlass(glasses.remove(0));
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
		}
	}

	// Start conveyors leading up to offline cf
	private void startOtherConveyors() {
		for (int i=0; i<5; i++) {
			Integer args[] = new Integer[1];
			args[0] = i;
			t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
		}
		
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

