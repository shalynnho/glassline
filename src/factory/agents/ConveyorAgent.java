package factory.agents;

import transducer.TChannel;
import transducer.TEvent;
import engine.agent.Agent;
import factory.interfaces.Conveyor;

public class ConveyorAgent extends Agent implements Conveyor {
	// *** Constructor(s) ***
	public ConveyorAgent(ConveyorFamily f, Transducer transducer) {
		family = f;
		t = transducer;
		t.register(this, TChannel.SENSOR);
	}
	
	// *** DATA ***
	private ConveyorFamily family;
	private Transducer t;
	//TODONOW: ENUM FOR STATES AFTER GLASS REMOVED
	private enum ConveyorState { }

	private List<Glass> glasses = Collections.synchronizedList(new ArrayList<Glass>());

	// *** MESSAGES ***
	@Override
	public void msgHereIsGlass(Glass g) {
		glasses.add(g);
		stateChanged();
	}

	@Override
	public void msgTakingGlass() {
		glasses.remove(0);
		stateChanged();
	}
	
	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		if (!glasses.isEmpty()) {// state == ConveyorState.RUNNING_WITH_GLASS) {
			actGlassOnConveyor(glasses.get(0));
			return true;
		}
		return false;
	}
	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (channel == TChannel.SENSOR) {
			// When the sensor right after the conveyor has been pressed, stop the conveyor
			if (event == TEvent.SENSOR_GUI_PRESSED) {
				// parse args to check if it is this sensor
				// if so:
				// t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, thisIndex);
				
			}
		}
	}
	
	// *** ACTIONS ***
	public void actReadyForGlass() {
//		sensor1.msgPositionFree();
	}

	public void actGlassOnConveyor(Glass g) {
		GlassState glassState = family.decideIfGlassNeedsProcessing(g); // conveyor decides this since it has time
		MyGlass myGlass = new MyGlass(g, glassState);
		
		family.popup.msgGlassComing(myGlass);
		stateChanged();

		// trust that conveyor knows to stop glass the moment the right sensor fires
	}

	
	
	// *** EXTRA ***
}
