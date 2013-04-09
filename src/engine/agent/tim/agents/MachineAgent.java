package engine.agent.tim.agents;

import java.util.*;

import engine.agent.Agent;
import engine.agent.tim.interfaces.Machine;
import engine.agent.tim.misc.ConveyorFamilyImp;
import engine.agent.tim.misc.MyGlassMachine;
import engine.agent.tim.misc.MyGlassMachine.processState;
import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.OfflineConveyorFamily;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

public class MachineAgent extends Agent implements Machine {

	//Name: MachineAgent

	//Description:  Will compete a process upon a piece of glass.

	//Data:
	private List<MyGlassMachine> glassToBeProcessed;
	private MachineType processType; // Designates what process this machine performs
	
	private ConveyorFamilyImp cf; // Reference to the conveyor family.  This was not previously needed, because thr robot handled this, but now the robot agent is not being used, so the machine agent needs a reference to the conveyor family
	
	private int machineChannel; // The channel number for this machine, which will be 0 or 1
	
	// Timer for waking up the agent
    Timer timer = new Timer();
    boolean timerCalled = false; // Makes sure that the timer is not called too many times 
	
	//Constructors:
	public MachineAgent(String name, Transducer transducer, MachineType processType, int machineChannel, OfflineConveyorFamily cf) { // Will exclude the robot unless it is needed
		// Initialize the variables based upon the constructor parameters first
		super(name, transducer);
		this.cf = (ConveyorFamilyImp) cf;
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
		stateChanged();
	}

	// Transducer specific message
	public void msgDoneProcessingGlass(Glass g) {
		for (MyGlassMachine glass: glassToBeProcessed) {
			if (glass.glass.getID() == g.getID()) {
				glass.processState = processState.doneProcessing;
				transducer.fireEvent(TChannel.ALL_GUI, TEvent.POPUP_DO_MOVE_UP, null); // Make sure to move the GUI popUp up
				print("Glass with ID (" + g.getID() + ") done being processed");
				stateChanged();
				break;
			}
		}
	}

	//Scheduler:
	public boolean pickAndExecuteAnAction() {	
		for (MyGlassMachine g: glassToBeProcessed) {
			if (g.processState == processState.doneProcessing && cf.getPopUp().getGlassToBeProcessed().size() == 0) {
				actPassGlassToCF(g); return true;
			}
		}
		
		for (MyGlassMachine g: glassToBeProcessed) {
			if (g.processState == processState.unprocessed) {
				actProcessGlass(g); return true;
			}
		}
		
		if (glassToBeProcessed.size() > 0 && timerCalled == false) { // Then allow the agent to wake up periodically to until it offloads its glass
			timer.schedule(new CallTask(this), 100);
			timerCalled = true;
		}
	
		return false;
	}

	//Actions:
	private void actProcessGlass(MyGlassMachine g) {
		//transducer.sendProcessGlassMessage(); // Stub for when the transducer is set up to send a processing message to the animation
		g.processState = processState.processing;
		print("Glass with ID (" + g.glass.getID() + ") currently processing...");
	}

	private void actPassGlassToCF(MyGlassMachine g) {
		g.glass.getRecipe().remove(this.processType); // Done with process, does not need to be in recipe anymore
		print("Glass with ID (" + g.glass.getID() + ") passed to PopUp");
		//robot.msgDoneProcessingGlass(g.glass);
		cf.msgGlassDone(g.glass, machineChannel);
		glassToBeProcessed.remove(g);
	}

	//Other Methods:
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (args[0] instanceof Glass) { // There should be a glass reference from the GUI glass inside this array
			Glass glass = (Glass) args[0];
			msgDoneProcessingGlass(glass);						
		}			
	}

	@Override
	public MachineType getProcessType() {
		return processType;
	}
	
	private class CallTask extends TimerTask { // Used to keep the number of checks for the shared data array down
		MachineAgent m;
		public CallTask(MachineAgent m) {
			this.m = m;	
		}
		public void run() {
			m.print("Attempting to put glass onto the popUp.");
			timerCalled = false;
			stateChanged();
		}
	}

	@Override
	public void setCF(OfflineConveyorFamily cf) {
		this.cf = (ConveyorFamilyImp) cf;	
	}
}
