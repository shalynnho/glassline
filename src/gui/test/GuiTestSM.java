package gui.test;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

public class GuiTestSM implements TReceiver {
	Transducer t;

	boolean offlineDone = false;

	public GuiTestSM(Transducer t) {
		this.t = t;
		t.register(this, TChannel.CUTTER);
		t.register(this, TChannel.SENSOR);
		t.register(this, TChannel.BREAKOUT);
		t.register(this, TChannel.MANUAL_BREAKOUT);
		t.register(this, TChannel.POPUP);
		t.register(this, TChannel.DRILL);
		t.register(this, TChannel.UV_LAMP);
		t.register(this, TChannel.WASHER);
		t.register(this, TChannel.OVEN);
		t.register(this, TChannel.PAINTER);
		t.register(this, TChannel.TRUCK);

		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED) {
			System.out.println("SENSOR_GUI_PRESSED");
			Integer[] newArgs = new Integer[1];
			if (((Integer) args[0] % 2) == 0) { // args[0] is index of sensor; if even # sensor (or 0), starts conveyor
				newArgs[0] = (Integer) args[0] / 2; // index of the conveyor to pass as arg
				
				
				System.out.println("Starting conveyor "+newArgs[0]);
				t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
				
				
			} 
			// testing
//			else if ((Integer) args[0] == 11) { // sensor right before drill
//				// Stopping conveyor that DRILL is on 
//				newArgs[0] = 5;
//				System.out.println("Stopping conveyor "+newArgs[0]);
//				t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);
//			}
			
		} else if (channel == TChannel.CUTTER && event == TEvent.WORKSTATION_LOAD_FINISHED) {
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
		} else if (channel == TChannel.TRUCK && event == TEvent.TRUCK_GUI_LOAD_FINISHED) {
			System.out.println("TRUCK_GUI_LOAD_FINISHED for TRUCK");

			t.fireEvent(TChannel.TRUCK, TEvent.TRUCK_DO_EMPTY, null);
		}
	}
}
