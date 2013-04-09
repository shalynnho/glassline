package engine.agent.tim.agents;

import java.util.*;

import shared.Glass;
import shared.interfaces.OfflineConveyorFamily;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.tim.interfaces.Sensor;
import engine.agent.tim.misc.ConveyorFamilyImp;
import engine.agent.tim.misc.MyGlassSensor;
import engine.agent.tim.misc.MyGlassSensor.location;
import engine.agent.tim.misc.MyGlassSensor.onSensor;

public class SensorAgent extends Agent implements Sensor {

	//Name: SensorAgent

	//Description: Will detect if a piece of glass has entered, exited, or on a popup for any given set of conveyors.  Even though all of the sensor functionality currently melded into one agent, I may split this agent up into a base agent and three inheritance agents – EntrySensorAgent, ExitSensorAgent, and PopUpSensorAgent – during implementation

	//Data:
	private List<String> type; // Will hold the type of sensor this is, and it may be of more than one type -- types are "entry" "popUp" and "exit"
	private List<MyGlassSensor> glassSheets; // Will hold all glass references
	public ConveyorFamilyImp cf; // Reference to the current conveyor family
	
	//Constructors:
	public SensorAgent(String name, Transducer transducer, List<String> type) {
		// Set the passed in values first
		super(name, transducer);
		this.type = type; // This list will be made synchronized within the conveyor family itself
	
		// Then set the values that need to be initialized within this class, specifically
		glassSheets = Collections.synchronizedList(new ArrayList<MyGlassSensor>());
		
		// Initialize the transducer channels
		initializeTransducerChannels();
	}
	
	private void initializeTransducerChannels() { // Initialize the transducer channels and everything else related to it
		// Register any appropriate channels
		transducer.register(this, TChannel.SENSOR); // Set this agent to listen to the SENSOR channel of the transducer
	}

	//Messages:
	public void msgHereIsGlass(Glass glass) {
		glassSheets.add(new MyGlassSensor(glass, location.entry, onSensor.justEntered));
		print("Glass with ID (" + glass.getID() + ") just entered sensor.");
		stateChanged();
	}

	// The following messages will be special to transducer events, and will be called after parsing arguments in the EventFired(args[]) function.

	public void msgGlassOffSensor(Glass glass) { // If GuiGlass just went off of a sensor, then find the glass reference related to it and then begin to remove it from the sensor agent
		synchronized(glassSheets) {
			for (MyGlassSensor g: glassSheets) {
				if (g.glass.getID() == glass.getID()) {
					g.onSensor = onSensor.no;
					stateChanged();	
					print("Transducer call: glass with ID (" + g.glass.getID() + ") is off of the sensor");
					break;
				}
			}
		}
	}

	public void msgHereIsGlassTransducer(Glass glass, String strLocation) { // Transducer will pass down a glass reference to a sensorAgent when a GuiGlass just lands to a GuiSensor
		location l = location.entry;
		if (strLocation.equals("popUp")) {
			l = location.popup;
			glassSheets.add(new MyGlassSensor(glass, l, onSensor.justEntered));
			print("Transducer call: glass with ID (" + glass.getID() + ") is on the sensor ");
		}
		else if (strLocation.equals("exit")) {
			l = location.exit;
			glassSheets.add(new MyGlassSensor(glass, l, onSensor.justEntered));
			print("Transducer call: glass with ID (" + glass.getID() + ") is on the sensor");
		}
		else {
			print("Transducer call: glass with ID (" + glass.getID() + ") is NOT on the sensor");

		}
		stateChanged();
	}
	
	//Scheduler:
	@Override
	public boolean pickAndExecuteAnAction() {		
		MyGlassSensor glass = null; // Use null variable for determining is value is found from synchronized loop
		
		synchronized(glassSheets) {
			for (MyGlassSensor g: glassSheets) {
				if (g.onSensor == onSensor.justEntered) { // If a piece of glass JUST ENTERED the sensor, then pass it to the conveyor, depending on the sensor
					glass = g;
					break;
				}
			}
		}
		if (glass != null) {
			actPassGlassToConveyor(glass); return true;
		}
		
		synchronized(glassSheets) {		
			for (MyGlassSensor g: glassSheets) {
				if (g.onSensor == onSensor.no) { // If the glass just left the sensor, then remove it
					glass = g;
					break;
				}
			}
		}
		if (glass != null) {
			actRemoveGlass(glass); return true;
		}
		
		return false;	
	}


	//Actions:
	private void actPassGlassToConveyor(MyGlassSensor g) {
		if (g.location == location.entry) {
			cf.getConveyor().msgGiveGlassToConveyor(g.glass);
			print("Glass with ID (" + g.glass.getID() + ") passed to conveyor (for entry)");
			// Make sure to turn on the conveyor, assuming that nothing is waiting at somewhere else on the conveyor
			turnOnOffConveyor();
			// Remember to change the onSesnor state to yes
			g.onSensor = onSensor.yes;
		}
		else if (g.location == location.popup) { 
			cf.getConveyor().msgGiveGlassToPopUp(g.glass);
			print("Glass with ID (" + g.glass.getID() + ") passed to conveyor (for popUp)");
			// Make sure to turn on the conveyor, assuming that nothing is waiting at somewhere else on the conveyor
			turnOnOffConveyor();
			// Remember to change the onSesnor state to yes
			g.onSensor = onSensor.yes;
		}
		else if (g.location == location.exit) { 
			cf.getConveyor().msgPassOffGlass(g.glass);
			print("Glass with ID (" + g.glass.getID() + ") passed to conveyor (for exit)");
			// Make sure to turn on the conveyor, assuming that nothing is waiting at somewhere else on the conveyor
			turnOnOffConveyor();
			// Remember to change the onSesnor state to yes
			g.onSensor = onSensor.yes;
		}
	}

	private void actRemoveGlass(MyGlassSensor g) {
		glassSheets.remove(g);
		turnOnOffConveyor(); // Remember to turn the conveyor on or off based upon the conditions specified
		if (g.location == location.entry && cf.getPrevCF() != null) { // Tell the previous conveyor family that the sensor currently has nothing on it
			cf.getPrevCF().msgPositionFree();
			print("Glass with ID (" + g.glass.getID() + ") removed from sensor");
		}
	}

	//Other Methods:
	private void turnOnOffConveyor() { // The method will run through the sensors and the conveyor state and make sure that it is not already stopped for some reason
		// Check to see if the conveyor is off, first
		if (cf.getConveyor().isConveyorOn() == false) {
			// Check WHY this conveyor could be off -- if it is off for any of the following reasons, leave it off
			
			if (cf.getSensor("popUp").getGlassSheets().size() > 0) { // Is there still something on the popUp sensor 
				if (cf.getPopUp().isPopUpDown() == false) {
					print("Glass is on the Pop up sensor and the popUp is up, leave conveyor off");
					return;
				} // Is the popUp still up?
				
				if (cf.getPopUp().getGlassToBeProcessed().size() > 0) {
					print("Glass is on the Pop up sensor and the popUp still has glass on it, leave conveyor off");
					return;
				} // Is there still glass being processed on the popUp?
			}  
			
			// If these conditions do not exist, turn on the conveyor
			print("Conveyor will be turned on.  All conditions passed.");
			transducer.fireEvent(TChannel.ALL_GUI, TEvent.CONVEYOR_DO_START, null);
		}
		else { // Then the conveyor must be on
			// If any of the following conditions are met, turn off the conveyor, else, leave it on
			if (cf.getPopUp().isPopUpDown() == false) {	
				transducer.fireEvent(TChannel.ALL_GUI, TEvent.CONVEYOR_DO_STOP, null);
				print("PopUp is up, turn conveyor off");
				return;
			} // Is the popUp still up?
			
			if (cf.getPopUp().getGlassToBeProcessed().size() > 0 && cf.getSensor("popUp").getGlassSheets().size() > 0 && cf.getConveyor().getGlassSheets().size() > 0) { 
				transducer.fireEvent(TChannel.ALL_GUI, TEvent.CONVEYOR_DO_STOP, null);
				print("Glass is on the Pop up sensor and the popUp still has glass on it, turn conveyor off");
				return; 
			} // Is there still something on the popUp sensor and is there still glass beiong processed?

			if (cf.getConveyor().getGlassSheets().size() == 0) {
				transducer.fireEvent(TChannel.ALL_GUI, TEvent.CONVEYOR_DO_STOP, null);
				print("No glass on conveyor, turn conveyor off");
				return; 
			} // The conveyor does not need to be on when there is no glass on it
		}
	
	}
	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// For the sensorAgent, args[] will only contain ONE index with an argument for which sensor needs to be accessed and ANOTHER argument with the glass piece
		if (args[0] instanceof Glass && args[1] instanceof String && args.length == 2) { // There should be a glass reference from the GUI glass inside this array
			if (event == TEvent.SENSOR_GUI_PRESSED) { // If a piece of glass just landed on a sensor
				String sensorType = (String) args[1];				
				if (type.contains(sensorType)) { // If this glass is supposed to be a part of this sensor
					Glass glass = (Glass) args[0];					
					msgHereIsGlassTransducer(glass, sensorType);
				}
				
			}
			if (event == TEvent.SENSOR_GUI_RELEASED) { // If a piece of glass just left a sensor				
				String sensorType = (String) args[1];
				
				if (type.contains(sensorType)) { // If this glass is supposed to be a part of this sensor
					Glass glass = (Glass) args[0];
					msgGlassOffSensor(glass);		
				}
			}
		}
	}
	
	// Getters and setters and other methods
	
	public List<String> getType() {
		return type;
	}
	
	public List<MyGlassSensor> getGlassSheets() {
		return glassSheets;		
	}

	@Override
	public void setCF(OfflineConveyorFamily conveyorFamilyImp) {
		cf = (ConveyorFamilyImp) conveyorFamilyImp;		
	}

	@Override
	public void runScheduler() {
		pickAndExecuteAnAction();
	}
}
