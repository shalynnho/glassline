package engine.agent.tim.agents;

import java.util.*;

import engine.agent.Agent;
import engine.agent.OfflineWorkstationAgent;
import engine.agent.tim.interfaces.Machine;
import engine.agent.tim.interfaces.PopUp;
import engine.agent.tim.misc.ConveyorFamilyImp;
import engine.agent.tim.misc.MyGlassPopUp;
import engine.agent.tim.misc.MyGlassPopUp.processState;
import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.OfflineConveyorFamily;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

public class PopUpAgent extends Agent implements PopUp {

	// Name: PopUpAgent

	// Description:  Will act as a mediator between the conveyor agent and the robot agents for getting glass to the processing machines, if necessary.
	// Of course, this agent may not be needed because there is NO ROBOT in the animation. but I will leave it in for now.

	// Data:	
	private class MachineCom { // Will hold a communication channel to a robot, allowing for the possibility to communicate to multiple robots at once
		OfflineWorkstationAgent machine; // Robot reference
		boolean inUse; // Is this channel currently occupied by a piece of glass
		int machineIndex; // Where the machine is located within the animation
		
		public MachineCom(OfflineWorkstationAgent machine, int machineIndex) {
			this.machine = machine;
			this.inUse = false; // At start, this channel is obviously not being used, so it has to be false
			this.machineIndex = machineIndex; 
		}
	}

	private List<MyGlassPopUp> glassToBeProcessed; // This name will be abbreviated as glassToBeProcessed in many functions to save on space and complexity
	private List<MachineCom> machineComs; // Channels for communicating with the machines, since there will most likely be two per offline process

	// Positional variable for whether the Pop-Up in the GUI is up or down, and it will be changed through the transducer and checked within one of the scheduler rules
	private boolean popUpDown; // Is this value is true, then the associated popUp is down (will be changed through the appropriate transducer eventFired(args[]) function.
	
	private ConveyorFamilyImp cf; // Reference to the current conveyor family
	
	MachineType processType; // Will hold what the concurrent workstation agents can process for any given popUp – it is safe to assume that the workstations process the same thing
	
	boolean passNextCF; // Is it possible to pass to the next conveyor family yet?

	int guiIndex; // Needed to communicate with the transducer
	
	
	// Constructors:
	public PopUpAgent(String name, Transducer transducer, List<OfflineWorkstationAgent> machines, int guiIndex) {  
		// Set the passed in values first
		super(name, transducer);
		
		// Then set the values that need to be initialized within this class, specifically
		glassToBeProcessed = Collections.synchronizedList(new ArrayList<MyGlassPopUp>());
		machineComs = Collections.synchronizedList(new ArrayList<MachineCom>());
		
		// This loop will go for the number of machines that are in the machines argument
		int i = 0; // Machine indexes related to the GUI machinea
		for (OfflineWorkstationAgent m: machines) {			
			machineComs.add(new MachineCom(m, i));
			i++;
		}
		
		popUpDown = true; // The popUp has to be down when the system starts...
		passNextCF = true; // The next conveyor will always be available when the system starts
		
		this.guiIndex = guiIndex;
		
		initializeTransducerChannels();		
	}
	
	private void initializeTransducerChannels() { // Initialize the transducer channels and everything else related to it
		// Register any appropriate channels
		transducer.register(this, TChannel.POPUP); // Set this agent to listen to the POPUP channel of the transducer
		transducer.register(this, processType.getChannel()); // Set this agent to listen to the processType channel of the transducer
	}


	//Messages:
	public void msgGiveGlassToPopUp(Glass g) { // Get Glass from conveyor to PopUp
		glassToBeProcessed.add(new MyGlassPopUp(g, processState.awaitingArrival));
		print("Glass with ID (" + g.getID() + ") added");
		stateChanged();
	}

	public void msgGlassDone(Glass g, int index) { // Adds glass back from a machine and then resets the machine channel to be free
		synchronized (glassToBeProcessed) {
			for (MyGlassPopUp glass: glassToBeProcessed) {
				if (glass.glass.getID() == g.getID()) {
					glass.processState = processState.doneProcessing;
					machineComs.get(index).inUse = false;
					stateChanged();
					break;
				}
			}
			// Should never get here
		}
	}
	
	public void msgPositionFree() {
		passNextCF = true;
		stateChanged();
	}

	//Scheduler:
	public boolean pickAndExecuteAnAction() {
		// Use null variables for determining is value is found from synchronized loop
		MyGlassPopUp glass = null;
		MachineCom machCom = null;
		
		synchronized(glassToBeProcessed) {
			for (MyGlassPopUp g: glassToBeProcessed) {
				if (g.processState == processState.awaitingRemoval) { // If glass needs to be sent out to next conveyor and a position is available
					if (passNextCF == true) {
						glass = g;
					}
					else {
						return false; // Do not want another piece of glass to collide, so shut the agent down until positionFree() is called
					}
				}				
			}
		}
		if (glass != null) {
			actPassGlassToNextCF(glass); return true;
		}
		
		synchronized(glassToBeProcessed) {
			for (MyGlassPopUp g: glassToBeProcessed) {
				if (g.processState == processState.doneProcessing) { // If glass needs to be sent out to next conveyor and a position is available
					glass = g;
				}				
			}
		}
		if (glass != null) {
			actRemoveGlassFromMachine(glass); return true;
		}
		
		
		synchronized(glassToBeProcessed) {
			for (MyGlassPopUp g: glassToBeProcessed) {
				if (g.processState == processState.unprocessed) { // If glass needs to be sent out to next conveyor and a position is available
					synchronized(machineComs) {
						for (MachineCom com: machineComs) {
							if ((com.inUse == false && popUpDown == true)) { // If there is an available machine and the popUp is down
								glass = g;
								machCom = com;
								break;
							}
						}
					}
				}				
			}
		}
		if (glass != null && machCom != null) {
			actPassGlassToMachine(glass, machCom); return true;
		}
		
		
		synchronized(glassToBeProcessed) {
			for (MyGlassPopUp g: glassToBeProcessed) {
				if (g.processState == processState.awaitingArrival) { // If glass needs to be sent out to next conveyor and a position is available
					synchronized(machineComs) {
						for (MachineCom com: machineComs) {
							if ((com.inUse == false) || !g.glass.getNeedsProcessing(processType)) { // If there is an available machine and the popUp is down
								glass = g;
								machCom = com;
								break;
							}
						}
					}
				}				
			}
		}
		if (glass != null && machCom != null) {
			actSendForGlass(glass); return true;
		}		
		
		return false;
	}
	
	//Actions:
	private void actSendForGlass(MyGlassPopUp glass) {
		// Fire transducer event to move the popUp down here index – make sure to stall the agent until the right time to prevent any weird synchronization issues	
		cf.getConveyor().msgPositionFree();
		// Fire transducer event to send glass from conveyor to popUp – wait until event is done
		if (glass.glass.getNeedsProcessing(processType))
			glass.processState = processState.unprocessed;
		else 
			glass.processState = processState.awaitingRemoval;

	}
	
	private void actPassGlassToNextCF(MyGlassPopUp glass) {
		cf.getNextCF().msgHereIsGlass(glass.glass);
		// Fire transducer event to release glass index – make sure to stall the agent until the glass arrives to prevent any weird synchronization issues
		passNextCF = false;
		glassToBeProcessed.remove(glass);		
	}
	
	private void actRemoveGlassFromMachine(MyGlassPopUp glass) {
		// Make sure to call Transducer events: 
		// Move PopUp up, 
		// Machine Release Glass, 
		// Move PopUp Down 
		// all with the correct timing so nothing is funky
		glass.processState = processState.awaitingRemoval;

	}
	
	private void actPassGlassToMachine(MyGlassPopUp glass, MachineCom com) {
		com.inUse = true;
		glass.processState = processState.processing;
		// Fire the PopUp up transducer event index – make sure to stall the agent until the glass arrives to prevent any weird synchronization issues
		com.machine.msgHereIsGlass(glass.glass);
		// Machine Load glass transducer events w/right index (can be attained from the machineCom machineIndex – make sure to stall the agent until the glass arrives to prevent any weird synchronization issues		
	}	

	//Other Methods:
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// Move the PopUp up or down, depending on what the protocol is -- This is an update from the animation, Mock or not
		if (event == TEvent.POPUP_DO_MOVE_DOWN) { // There will only be one boolean argument as of now, and that tells whether the popUp is UP or DOWN
			popUpDown = true;
		}
		else if (event == TEvent.POPUP_DO_MOVE_UP) { // There will only be one boolean argument as of now, and that tells whether the popUp is UP or DOWN
			popUpDown = false;
		}
		
	}
	
	// Getters and Setters
	
	public int getFreeChannels() {
		int freeChannels = 0;
		synchronized(machineComs) {	
			for (MachineCom com: machineComs) {
				if (com.inUse == false)
					
					freeChannels++;
			}
		}
		
		// Make sure to augment the free channels number by the amount of glasses that are currently within the popUp, so that two glasses do not come up when there shoulkd only be one
		
		freeChannels -= glassToBeProcessed.size();
		
		return freeChannels;
	}

	/**
	 * @return the glassToBeProcessed
	 */
	public List<MyGlassPopUp> getGlassToBeProcessed() {
		return glassToBeProcessed;
	}

	/**
	 * @return the popUpDown
	 */
	public boolean isPopUpDown() {
		return popUpDown;
	}

	@Override
	public void setCF(OfflineConveyorFamily conveyorFamilyImp) {
		cf = (ConveyorFamilyImp) conveyorFamilyImp;		
	}

	@Override
	public void runScheduler() {
		pickAndExecuteAnAction();		
	}

	@Override
	public boolean doesGlassNeedProcessing(Glass glass) { // Method invoked by the conveyor for a special case of sending glass down the popUp in the line
		if (glass.getNeedsProcessing(processType)) { // Both machines on every offline process do the same process
			return true;
		}
		else {
			return false;
		}
	}
}
