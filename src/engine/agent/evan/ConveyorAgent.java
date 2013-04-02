package engine.agent.evan;

import shared.Glass;
import shared.interfaces.ConveyorFamily;
import transducer.*;
import engine.agent.Agent;
import engine.agent.evan.interfaces.*;

public class ConveyorAgent extends Agent implements Conveyor, TReceiver {
	// *** DATA ***
	
	private ConveyorFamily prevCF;
	private Popup p;
	private Transducer t;
	private int id; // place in GUI

	private Glass g; // current piece of glass

	enum GlassState {pending, arrived, moving, atEnd, waiting, sent, done, none};
	private GlassState gs;

	private boolean posFree; // popup ready
	
	/* Assigns references from arguments and sets other data appropriately. */
	public ConveyorAgent(String name, ConveyorFamily cf, Popup pop, Transducer trans, int index) {
		super(name, trans);
		
		prevCF = cf;
		p = pop;
		t = trans;
		t.register(this, TChannel.SENSOR);
		id = index;
		
		g = null;
		gs = GlassState.none;
		posFree = false;
		prevCF.msgPositionFree(); // conveyor starts open
	}
	
	// *** MESSAGES ***
	
	/* From previous CF. */
	public void msgHereIsGlass(Glass g) {
		this.g = g;
		if (gs == GlassState.none) // it could have arrived already, maybe in a buggy scenario (if-statement included for resiliency)
			gs = GlassState.pending;
		stateChanged();
	}
	
	/* From popup. */
	public void msgPositionFree() {
		posFree = true;
		stateChanged();
	}
	
	/* Transducer event. Always on the SENSOR TChannel. */
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		int sensorID = (Integer)args[0];
		
		if (sensorID == id * 2) {
			if (event == TEvent.SENSOR_GUI_PRESSED) { // if front sensor pressed
				gs = GlassState.arrived;
				stateChanged();
			}/* else if (event == TEvent.SENSOR_GUI_RELEASED) { // if front sensor released
				gs = GlassState.moving;
				stateChanged();
			} */
		} else if (sensorID == id * 2 + 1) {
			if (event == TEvent.SENSOR_GUI_PRESSED) { // if back sensor pressed
				gs = GlassState.atEnd;
				stateChanged();
			} else if (event == TEvent.SENSOR_GUI_RELEASED) { // if back sensor released
				gs = GlassState.done;
				stateChanged();
			}
		}
	}
	
	/* Scheduler.  Determine what action is called for, and do it. */
	public boolean pickAndExecuteAnAction() {
		if (gs == GlassState.pending) {}
		else if (gs == GlassState.arrived) {
			startConveyor();
			return true;
		} else if (gs == GlassState.moving) {}
		else if (gs == GlassState.atEnd) {
			tellPopupReadyAndWait();
			return true;
		} else if (gs == GlassState.waiting && posFree) {
			sendGlass(); // send to popup
			return true;
		} else if (gs == GlassState.sent) {}
		else if (gs == GlassState.done) {
			readyForMore(); // tell popup glass has arrived
			return true;
		} else if (gs == GlassState.none) {}

		return false;
	}
	
	// *** ACTIONS ***
	
	/* Start conveyor in animation and change state. */
	private void startConveyor() {
		doStartConveyor();
		gs = GlassState.moving;
	}

	/* Tell Popup about next glass and wait to pass it off. */
	private void tellPopupReadyAndWait() {
		doStopConveyor();
		p.msgNextGlass(g);
		gs = GlassState.waiting;
	}
	
	/* Start this conveyor so animation sends glass to popup. Popup is waiting for glass right now. */
	private void sendGlass() {
		doStartConveyor();
		gs = GlassState.sent;
	}
	
	/* Reset states and tell previous CF ready. */
	private void readyForMore() {
		gs = GlassState.none;
		posFree = false; // popup now occupied
		doStopConveyor();
		prevCF.msgPositionFree(); // this conveyor now free
	}
	
	// *** ANIMATION ACTIONS ***
	
	/* Make animation start this conveyor. */
	private void doStartConveyor() {
		Integer[] args = {id};
		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
	}
	
	/* Make animation stop this conveyor. */
	private void doStopConveyor() {
		Integer[] args = {id};
		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
	}
	
	// *** EXTRA ***
	
	public void setPopup(Popup pop) {
		p = pop;
	}
}
