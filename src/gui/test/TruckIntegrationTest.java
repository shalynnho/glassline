package gui.test;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

/**
 * @author David Zhang
 * Test for truck - abandoned because verified in actual running
 */
public class TruckIntegrationTest extends GuiTestSM {

	public TruckIntegrationTest(Transducer t) {
		super(t, false);
		startOtherConveyors();

		// Start things off
		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
	}
	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// If it is the last sensor of all the conveyors, send message...
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED) {
			
		} 
		
		
		// Other cases copied from super class
		else if (channel == TChannel.CUTTER && event == TEvent.WORKSTATION_LOAD_FINISHED) {
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

			if (offlineDone) {
				t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
				
				// Stopping conveyor that DRILL is on to see if glass stops at popup
				// Conclusion: it doesn't, so the moment popup is down, glass automoves to next family
				Integer[] newArgs = new Integer[1];
				newArgs[0] = 5;
				System.out.println("Stopping conveyor "+newArgs[0]);
				t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);
			}
			else
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
		}
		
		// personal additions to test callbacks
		else  if (channel == TChannel.DRILL && event == TEvent.WORKSTATION_RELEASE_FINISHED) {
			System.out.println("WORKSTATION_RELEASE_FINISHED for DRILL");
		}
		else if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_RELEASE_FINISHED) {
			System.out.println("POPUP_GUI_RELEASE_FINISHED");
		}
		
		
		else if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_DOWN) {
			System.out.println("POPUP_GUI_MOVED_DOWN for POPUP");

			t.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);
			
			// Test
//			System.out.println("Stopping conveyor right after popup release");
//			Integer[] newArgs = new Integer[1];
//			newArgs[0] = 6;
//			t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);
			
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
}
