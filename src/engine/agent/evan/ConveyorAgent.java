package engine.agent.evan;

import java.util.*;

import shared.Glass;
import shared.interfaces.*;
import transducer.*;
import engine.agent.Agent;
import engine.agent.evan.interfaces.*;

public class ConveyorAgent extends Agent implements Conveyor, TReceiver {
	// *** DATA ***
	
	private ConveyorFamily prevCF;
	private Popup p;
	private Transducer t;
	private int id; // place in GUI
	
	enum GlassState {pending, arrived, moving, atEnd, waiting, sent, done};
	private class MyGlass {
		public Glass g;
		public GlassState gs;
		
		public MyGlass(Glass g) {
			this.g = g;
			this.gs = GlassState.pending;
		}
	}
	
	private List<MyGlass> glasses;
	
	private boolean posFree, moving; // popup ready
	
	/* Assigns references from arguments and sets other data appropriately. */
	public ConveyorAgent(String name, ConveyorFamily cf, Popup pop, Transducer trans, int index) {
		super(name, trans);
		
		prevCF = cf;
		p = pop;
		t = trans;
		t.register(this, TChannel.SENSOR);
		id = index;
		
		glasses = new ArrayList<MyGlass>();
		posFree = false;
		moving = false;
		prevCF.msgPositionFree(); // conveyor starts open
	}
	
	// *** MESSAGES ***
	
	/* From previous CF. */
	public void msgHereIsGlass(Glass g) {
		glasses.add(new MyGlass(g));
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
				for (MyGlass mg : glasses)
					if (mg.gs == GlassState.pending) {
						mg.gs = GlassState.arrived;
						break;
					}
				stateChanged();
			} else if (event == TEvent.SENSOR_GUI_RELEASED) { // if front sensor released
				for (MyGlass mg : glasses)
					if (mg.gs == GlassState.arrived) {
						mg.gs = GlassState.moving;
						prevCF.msgPositionFree();
						break;
					}
				stateChanged();
			}
		} else if (sensorID == id * 2 + 1) {
			if (event == TEvent.SENSOR_GUI_PRESSED) { // if back sensor pressed
				for (MyGlass mg : glasses)
					if (mg.gs == GlassState.moving) {
						mg.gs = GlassState.atEnd;
						break;
					}
				stateChanged();
			} else if (event == TEvent.SENSOR_GUI_RELEASED) { // if back sensor released
				for (MyGlass mg : glasses)
					if (mg.gs == GlassState.sent) {
						mg.gs = GlassState.done;
						break;
					}
				stateChanged();
			}
		}
	}
	
	/* Scheduler.  Determine what action is called for, and do it. */
	public boolean pickAndExecuteAnAction() {
		for (MyGlass mg : glasses)
			if (mg.gs == GlassState.done) {
				removeGlass(mg); // remove mg from glasses
				return true;
			}
		for (MyGlass mg : glasses)
			if (mg.gs == GlassState.waiting) {
				if (posFree) {
					sendGlass(mg); // send to popup
					return true;
				} else {
					return false; // shouldn't do anything else if glass waiting and next conveyor is full
				}
			}
		for (MyGlass mg : glasses)
			if (mg.gs == GlassState.atEnd) {
				tellPopupReadyAndWait(mg);
				return true;
			}
		for (MyGlass mg : glasses)
			if (mg.gs == GlassState.arrived) {
				doStartConveyor(); // start conveyor if conveyor isn't moving
				return true;
			}
		
		return false;
	}
	
	// *** ACTIONS ***

	/* Tell Popup about next glass and wait to pass it off. */
	private void tellPopupReadyAndWait(MyGlass mg) {
		doStopConveyor();
		p.msgNextGlass(mg.g);
		mg.gs = GlassState.waiting;
	}
	
	/* Start this conveyor so animation sends glass to popup. Popup is waiting for glass right now. */
	private void sendGlass(MyGlass mg) {
		doStartConveyor();
		mg.gs = GlassState.sent;
	}
	
	/* Remove mg from glasses. */
	private void removeGlass(MyGlass mg) {
		glasses.remove(mg);
	}
	
	// *** ANIMATION ACTIONS ***
	
	/* Make animation start this conveyor. */
	private void doStartConveyor() {
		if (!moving) {
			Integer[] args = {id};
			t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
			moving = true;
		}
	}
	
	/* Make animation stop this conveyor. */
	private void doStopConveyor() {
		if (moving) {
			Integer[] args = {id};
			t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
			moving = false;
		}
	}
	
	// *** EXTRA ***
	
	public void setPopup(Popup pop) {
		p = pop;
	}
}
