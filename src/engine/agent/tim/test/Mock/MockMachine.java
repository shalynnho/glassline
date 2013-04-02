package engine.agent.tim.test.Mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.ConveyorFamily;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.tim.interfaces.Machine;
import engine.agent.tim.misc.MyGlassMachine;
import engine.agent.tim.misc.MyGlassMachine.processState;

public class MockMachine extends MockAgent implements Machine {
	
	//Data:
	private List<MyGlassMachine> glassToBeProcessed;
	private MachineType processType; // Designates what process this machine performs
	
	private ConveyorFamily cf; // Reference to the conveyor family.  This was not previously needed, because thr robot handled this, but now the robot agent is not being used, so the machine agent needs a reference to the conveyor family
	
	private int machineChannel; // The channel number for this machine, which will be 0 or 1
	
	// Timer for waking up the agent
    Timer timer = new Timer();
    boolean timerCalled = false; // Makes sure that the timer is not called too many times 
	
	//Constructors:
	public MockMachine(String name, Transducer transducer, MachineType processType, int machineChannel) { // Will exclude the robot unless it is needed
		// Initialize the variables based upon the constructor parameters first
		super(name, transducer);
		this.name = name;
		this.processType = processType;
		this.machineChannel = machineChannel;
		
		// Then initialize everything else
		glassToBeProcessed = Collections.synchronizedList(new ArrayList<MyGlassMachine>());
		initializeTransducerChannels();		
	}
	
	private void initializeTransducerChannels() { // Initialize the transducer channels and everything else related to it
		// Register any appropriate channels
		transducer.register(this, TChannel.CROSS_SEAMER); // Set this agent to listen to the CROSS_SEAMER channel of the transducer
		// This machine will be used for testing purposes this time around
	}
	
	//Messages:
	public void msgProcessGlass(Glass g) {
		glassToBeProcessed.add(new MyGlassMachine(g, processState.unprocessed));
		transducer.fireEvent(TChannel.ALL_GUI, TEvent.POPUP_DO_MOVE_DOWN, null); // Make sure to move the GUI popUp down
		print("Glass with ID (" + g.getID() + ") recieved");
		log.add(new LoggedEvent("Glass with ID (" + g.getID() + ") recieved"));
	}

	// Transducer specific message
	public void msgDoneProcessingGlass(Glass g) {
		for (MyGlassMachine glass: glassToBeProcessed) {
			if (glass.glass.getID() == g.getID()) {
				glass.processState = processState.doneProcessing;
				transducer.fireEvent(TChannel.ALL_GUI, TEvent.POPUP_DO_MOVE_UP, null); // Make sure to move the GUI popUp up
				print("Glass with ID (" + g.getID() + ") done being processed");
				log.add(new LoggedEvent("Glass with ID (" + g.getID() + ") done being processed"));
				PassGlassToCF(glass); // Hack, send the glass back to the popUp here				
				break;
			}
		}
	}
	
	//Actions:
	public void PassGlassToCF(MyGlassMachine g) {
		g.glass.getRecipe().put(this.processType, false); // Done with process, bit changed to false
		print("Glass with ID (" + g.glass.getID() + ") passed to PopUp");
		log.add(new LoggedEvent("Glass with ID (" + g.glass.getID() + ") passed to PopUp"));
		//robot.msgDoneProcessingGlass(g.glass);
		cf.msgGlassDone(g.glass, machineChannel);
		glassToBeProcessed.remove(g);
	}

	//Other Methods:
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (event == TEvent.WORKSTATION_GUI_ACTION_FINISHED && args[0] instanceof Glass) { // There should be a glass reference from the GUI glass inside this array
			Glass glass = (Glass) args[0];
			msgDoneProcessingGlass(glass);
		}			
	}

	@Override
	public MachineType getProcessType() {
		return processType;
	}

	@Override
	public void setCF(ConveyorFamily cf) {
		this.cf = cf;		
	}
	
	public List<MyGlassMachine> getGlassToBeProcessed() {
		return glassToBeProcessed;
	}
}
