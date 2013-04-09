package engine.agent.tim.agents;

import java.util.*;

import shared.Glass;
import shared.interfaces.OfflineConveyorFamily;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.tim.interfaces.Sensor;
import engine.agent.tim.misc.ConveyorEvent;
import engine.agent.tim.misc.ConveyorFamilyImp;
import engine.agent.tim.misc.MyGlassSensor;

public class SensorAgent extends Agent implements Sensor {

	//Name: SensorAgent

	//Description: Will detect if a piece of glass has entered, exited, or on a popup for any given set of conveyors.  Only one instance of this agent will be needed to check for which GUI sensor was hit, because this agent will watch for transducer messages from both GUI sensors

	//Data:
	public ConveyorFamilyImp cf; // Reference to the current conveyor family
	
	// GUI indexes needed to communicate with the transducer
	int guiIndexEnterSensor; // Needed to communicate with the transducer
	int guiIndexPopUpSensor; // Needed to communicate with the transducer
	
	//Constructors:
	public SensorAgent(String name, Transducer transducer, int guiIndexEnterSensor, int guiIndexPopUpSensor) {
		// Set the passed in values first
		super(name, transducer);
		
		this.guiIndexEnterSensor = guiIndexEnterSensor;
		this.guiIndexPopUpSensor = guiIndexPopUpSensor;
	
		// Initialize the transducer channels
		initializeTransducerChannels();
	}
	
	private void initializeTransducerChannels() { // Initialize the transducer channels and everything else related to it
		// Register any appropriate channels
		transducer.register(this, TChannel.SENSOR); // Set this agent to listen to the SENSOR channel of the transducer
	}

	// Messages -- will forward sensor states to the conveyor
	public void msgUpdateGlassEntrySensorEnter() {
		cf.getConveyor().msgUpdateGlass(ConveyorEvent.onEntrySensor);
	}

	public void msgUpdateGlassEntrySensorExit() {
		cf.getConveyor().msgUpdateGlass(ConveyorEvent.offEntrySensor);
	}

	public void msgUpdateGlassPopUpSensorEnter() {
		cf.getConveyor().msgUpdateGlass(ConveyorEvent.onPopUpSensor);
	}
	
	public void msgUpdateGlassPopUpSensorExit() {
		cf.getConveyor().msgUpdateGlass(ConveyorEvent.offPopUpSensor);
	}

	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (event == TEvent.SENSOR_GUI_RELEASED) { // If a piece of glass just left a sensor
			if ((Integer) args[0] == guiIndexEnterSensor) {
				msgUpdateGlassEntrySensorExit(); // Glass just left entry sensor
			}
			
			if ((Integer) args[0] == guiIndexPopUpSensor) {
				msgUpdateGlassPopUpSensorExit(); // Glass just left popUp sensor
			}
		}
		
		if (event == TEvent.SENSOR_GUI_PRESSED) { // If a piece of glass just landed on a sensor
			if ((Integer) args[0] == guiIndexEnterSensor) {
				msgUpdateGlassEntrySensorEnter(); // Glass just hit entry sensor
			}
			
			if ((Integer) args[0] == guiIndexPopUpSensor) {
				msgUpdateGlassEntrySensorExit(); // Glass just hit popUp sensor
			}
		}
	}
	
	// Getters and setters and other methods
	@Override
	public void setCF(OfflineConveyorFamily conveyorFamilyImp) {
		cf = (ConveyorFamilyImp) conveyorFamilyImp;		
	}

	@Override
	public void runScheduler() {
		pickAndExecuteAnAction();
	}

	
	// Delete these messages
	@Override
	public void msgHereIsGlass(Glass glass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgGlassOffSensor(Glass glass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MyGlassSensor> getGlassSheets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean pickAndExecuteAnAction() {
		// TODO Auto-generated method stub
		return false;
	}
}
