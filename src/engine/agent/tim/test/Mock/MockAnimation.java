package engine.agent.tim.test.Mock;

import shared.Glass;
import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

public class MockAnimation implements TReceiver { // This will hold the mock animation for the unit testing
	// This will contain the following Mocks
	
	// Mock Messages that will result in the addition or removal of glass within the sensor agents
	Transducer transducer;
	
	// A mock conveyor that will only turn on or off
	/** a boolean used to turn conveyor movement on or off */
	public boolean conveyorMoving;
	
	// A mock pop up that will only simulate the RISING,DROPPING,UP,DOWN actions;
	public enum PopUpHeightState{UP,DOWN};
	public PopUpHeightState popUpHeight;
	
	//Constructors:
	public MockAnimation(Transducer t) {
		this.transducer = t;
		
		// Initialize the transducer channels
		transducer.register(this, TChannel.ALL_GUI);
		
		// Initialize the other Mock Animation values
		popUpHeight = PopUpHeightState.DOWN;
		conveyorMoving = false;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		System.out.println("MockAnimation: In MockAnimation eventFired function");
		if (event == TEvent.CONVEYOR_DO_START) {
			conveyorMoving = true; // Set the height of the PopUp accordingly
			transducer.fireEvent(TChannel.CONVEYOR, event, args); // Send a follow up message to the conveyor
			System.out.println("MockAnimation: sent message to conveyor -- start");
		}
		else if (event == TEvent.CONVEYOR_DO_STOP) {
			conveyorMoving = false; // Set the height of the PopUp accordingly
			transducer.fireEvent(TChannel.CONVEYOR, event, args); // Send a follow up message to the conveyor
			System.out.println("MockAnimation: sent message to conveyor -- stop");
		}
		else if (event == TEvent.POPUP_DO_MOVE_DOWN) {
			popUpHeight = PopUpHeightState.DOWN; // Set the height of the PopUp accordingly
			transducer.fireEvent(TChannel.CONVEYOR, event, args); // Send a follow up message to the popUp
			System.out.println("MockAnimation: sent message to popUp -- down");
		}
		else if (event == TEvent.POPUP_DO_MOVE_UP) {
			popUpHeight = PopUpHeightState.UP;
			transducer.fireEvent(TChannel.CONVEYOR, event, args); // Send a follow up message to the popUp
			System.out.println("MockAnimation: sent message to popUp -- up");
		}		
	}

	public void fireEntrySensorExitGlass(Glass g) { // This hack method will parse a "GuiGlass" and "GuiSensor," 
		// getting the reference to an actual piece of glass that needs to be removed from a specific sensor agent
		// The next two methods work similarly, but with different sensor types
		Object[] args = new Object[2];
		
		args[0] = g;
		args[1] = "entry";
		
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		System.out.println("MockAnimation: sent message to entry sensor -- released");
	}
	
	public void firePopUpSensorExitGlass(Glass g) {
		Object[] args = new Object[2];
		
		args[0] = g;
		args[1] = "popUp";
		
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		System.out.println("MockAnimation: sent message to popUp sensor -- released");
	}
	
	public void fireExitSensorExitGlass(Glass g) {
		Object[] args = new Object[2];
		
		args[0] = g;
		args[1] = "exit";
		
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		System.out.println("MockAnimation: sent message to exit sensor -- released");
	}
	
	// These next two methods are similar to the previous three, but deal with sensor entry instead.  
	// The reason when I do not need a method like this for the entry sensor is because the glass 
	// is passed in through the conveyor family interactions for the entry sensor (that also occurs 
	// right when the exit sensor of the previous conveyor family is hit).
	
	public void firePopUpSensorEnterGlass(Glass g) {
		Object[] args = new Object[2];
		
		args[0] = g;
		args[1] = "popUp";
		
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		System.out.println("MockAnimation: sent message to popUp sensor -- pressed");
	}
	
	public void fireExitSensorEnterGlass(Glass g) {
		Object[] args = new Object[2];
		
		args[0] = g;
		args[1] = "exit";
		
		transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		System.out.println("MockAnimation: sent message to exit sensor -- pressed");
	}
	
	public void glassDoneProcessing(Glass g) {
		Object[] args = new Object[1];
		
		args[0] = g;		
		
		transducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.WORKSTATION_GUI_ACTION_FINISHED, args);
		System.out.println("MockAnimation: sent message to machine -- animation done");
	}
	
}
