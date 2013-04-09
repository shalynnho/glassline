package engine.agent.tim.test.Mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import shared.Glass;
import shared.interfaces.OfflineConveyorFamily;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.tim.interfaces.Conveyor;
import engine.agent.tim.misc.ConveyorFamilyImp;
import engine.agent.tim.misc.MyGlassConveyor;
import engine.agent.tim.misc.MyGlassConveyor.conveyorState;

public class MockConveyor extends MockAgent implements Conveyor {
	
	//Data:
	public List<MyGlassConveyor> glassSheets; // List to hold all of the glass sheets
	public boolean positionFreeNextCF; // Will determine if a piece of glass should be passed to the next conveyor family.  This will initially be set to true.
	public boolean conveyorOn;
	private ConveyorFamilyImp cf;
	
	// Constructors:
	public MockConveyor(String name, Transducer transducer) {
		// Set the passed in values first
		super(name, transducer);
		this.name = name;
		
		// Then set the values that need to be initialized within this class, specifically
		glassSheets = Collections.synchronizedList(new ArrayList<MyGlassConveyor>());
		positionFreeNextCF = true; // Obviously, there will be nothing in the next conveyor set when the system initializes, so I can make the assumption that nothing is there too
		conveyorOn = false; // The conveyor is off when this simulation starts
		
		initializeTransducerChannels();		
	}
	
	private void initializeTransducerChannels() { // Initialize the transducer channels and everything else related to it
		// Register any appropriate channels
		transducer.register(this, TChannel.CONVEYOR); // Set this agent to listen to the CONVEYOR channel of the transducer
	}
	
	// Messages/Actions

	//Messages:
	public void msgGiveGlassToConveyor(Glass g) {
		glassSheets.add(new MyGlassConveyor(g, conveyorState.onConveyor)); // conveyorState will always initializes to onConveyor
		print("Glass with ID (" + g.getID() + ") added to conveyor");
		log.add(new LoggedEvent("Glass with ID (" + g.getID() + ") added to conveyor"));
	}
	
	public void msgGiveGlassToPopUp(Glass g) {
		for (MyGlassConveyor glass: glassSheets) {
			if (glass.glass.getID() == g.getID()) {
				glass.conveyorState = conveyorState.passPopUp;
				print("Glass with ID (" + glass.glass.getID() + ") going to PopUp");
				log.add(new LoggedEvent("Glass with ID (" + glass.glass.getID() + ") going to PopUp"));
				cf.getPopUp().msgGiveGlassToPopUp(g); // Actually make the conveyor mock message the popUp with the appropriate glass
				glassSheets.remove(glass);
				break;
			}
		}
	}

	public void msgPassOffGlass(Glass g) {
		for (MyGlassConveyor glass: glassSheets) {
			if (glass.glass.getID() == g.getID()) {
				glass.conveyorState = conveyorState.passCF;
				print("Glass with ID (" + glass.glass.getID() + ") going to next ConveyorFamily");
				log.add(new LoggedEvent("Glass with ID (" + glass.glass.getID() + ") going to next ConveyorFamily"));
				glassSheets.remove(glass); // Hack: Just delete the glass to make it look like it moved to the next conveyor.				
				break;
			}
		}
	}

	public void msgPositionFree() {
		positionFreeNextCF = true;
		print("Next conveyor is available for a piece of glass.");
		log.add(new LoggedEvent("Next conveyor is available for a piece of glass."));

	}

	public void msgUpdateGlass(Glass g) { // This message is akin to a stub, but I wanted to match up to my current interaction diagram – I could just call msgGiveGlassToConveyor directly, but the semantics do not look as good that way
		msgGiveGlassToConveyor(g); 
	}
	
	// Getters and Setters
	@Override
	public boolean isConveyorOn() {
		return conveyorOn;
	}

	@Override
	public List<MyGlassConveyor> getGlassSheets() {
		return glassSheets;
	}

	@Override
	public void setCF(OfflineConveyorFamily conveyorFamilyImp) {
		cf = (ConveyorFamilyImp) conveyorFamilyImp;		
	}

	@Override
	public void runScheduler() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// Turn the conveyor on or off, depending on what the protocol is -- This is an update from the animation, Mock or not
		if (event == TEvent.CONVEYOR_DO_START) {
			conveyorOn = true;
		}
		else if (event == TEvent.CONVEYOR_DO_STOP) {
			conveyorOn = false;
		}		
	}
}
