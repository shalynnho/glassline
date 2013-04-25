package engine.agent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Timer;

import shared.Glass;
import shared.interfaces.LineComponent;
import shared.interfaces.NonnormBreakInteraction;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

public class GeneralConveyorAgent extends Agent implements LineComponent, ActionListener, NonnormBreakInteraction {
	// *** DATA ***
	
	private LineComponent prev, next;
	private int id; // place in GUI
	
	enum GlassState {pending, arrived, moving, atEnd, waiting, sent, done};
	enum GUIBreakState {stop, stopped, restart};
	private GUIBreakState gbs;
	
	private class MyGlass {
		public Glass g;
		public GlassState gs;
		
		public MyGlass(Glass g) {
			this.g = g;
			this.gs = GlassState.pending;
		}
	}
	
	private List<MyGlass> glasses;
	
	private boolean posFree, moving, wasMoving, waitingToSendPosFree; // popup ready, is moving, waiting for glass to move off of front sensor
	private int glassMoved; // how far has glass moved
	private static final int glassMovedPosFreeTicks = 12; // how many ticks of GUITimer before glass is off front sensor
	
	/* Assigns references from arguments and sets other data appropriately. */
	public GeneralConveyorAgent(String name, Transducer trans, int index, Timer guiTimer) {
		super(name, trans);
		
		transducer.register(this, TChannel.SENSOR);
		id = index;
		
		glasses = Collections.synchronizedList(new ArrayList<MyGlass>());
		posFree = true;
		moving = false;
		wasMoving = false;
		waitingToSendPosFree = false;
		
		guiTimer.addActionListener(this); // timer from the GUI to prevent a dumb bug
		glassMoved = 0;
		gbs = null;
	}
	
	// *** MESSAGES ***
	
	/* From previous CF. */
	public void msgHereIsGlass(Glass g) {
		synchronized(glasses) {
			glasses.add(new MyGlass(g));
		}
		stateChanged();
	}
	
	/* From popup. */
	public void msgPositionFree() {
		posFree = true;
		stateChanged();
	}
	
	/* This message is from the GUI to stop or restart. */
	public void msgGUIBreak(boolean stop) {
		if  (stop) {
			gbs = GUIBreakState.stop;
		} else {
			gbs = GUIBreakState.restart;
		}
		stateChanged();
	}
	
	/* Transducer event. Always on the SENSOR TChannel. */
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		int sensorID = (Integer)args[0];
		
		if (sensorID == id * 2) {
			if (event == TEvent.SENSOR_GUI_PRESSED) { // if front sensor pressed
				synchronized(glasses) {
					for (MyGlass mg : glasses)
						if (mg.gs == GlassState.pending) {
							mg.gs = GlassState.arrived;
							break;
						}
				}
				stateChanged();
			} else if (event == TEvent.SENSOR_GUI_RELEASED) { // if front sensor released
				synchronized(glasses) {
					for (MyGlass mg : glasses)
						if (mg.gs == GlassState.arrived) {
							mg.gs = GlassState.moving;
							glassMoved = 0;
							waitingToSendPosFree = true;
							break;
						}
				}
				stateChanged();
			}
		} else if (sensorID == id * 2 + 1) {
			if (event == TEvent.SENSOR_GUI_PRESSED) { // if back sensor pressed
				synchronized(glasses) {
					for (MyGlass mg : glasses)
						if (mg.gs == GlassState.moving) { // only processes furthest along applicable piece of glass
							mg.gs = GlassState.atEnd;
							break; // don't process any more of them
						}
				}
				stateChanged();
			} else if (event == TEvent.SENSOR_GUI_RELEASED) { // if back sensor released
				synchronized(glasses) {
					for (MyGlass mg : glasses)
						if (mg.gs == GlassState.sent) {
							mg.gs = GlassState.done;
							break;
						}
				}
				stateChanged();
			}
		}
	}
	
	/* Called by guiTimer. Increment glassMove in order to tell how far glass has moved from front of conveyor. */
	public void actionPerformed(ActionEvent ae) {
		if (moving && waitingToSendPosFree && glassMoved < glassMovedPosFreeTicks)
			++glassMoved;
	}
	
	/* Scheduler.  Determine what action is called for, and do it. */
	public boolean pickAndExecuteAnAction() {
		if (gbs == GUIBreakState.stop) {
			guiStopConveyor();
			return false; // agent shouldn't do anything until it is unstopped
		} else if (gbs == GUIBreakState.restart) {
			guiRestartConveyor();
			return true;
		} else if (gbs == GUIBreakState.stopped) {
			return false; // don't do anything if stopped
		} else if (waitingToSendPosFree && glassMoved == glassMovedPosFreeTicks) {
			sendPositionFree();
			return true;
		}
		synchronized(glasses) {
			for (MyGlass mg : glasses)
				if (mg.gs == GlassState.done) {
					removeGlass(mg); // remove mg from glasses
					return true;
				}
		}
		synchronized(glasses) {
			for (MyGlass mg : glasses)
				if (mg.gs == GlassState.waiting) {
					if (posFree) {
						sendGlass(mg); // send to next LineComponent
						return true;
					} else {
						return false; // shouldn't do anything else if glass waiting and next LineComponent is full
					}
				}
		}
		synchronized(glasses) {
			for (MyGlass mg : glasses)
				if (mg.gs == GlassState.atEnd) {
					if (posFree) {
						sendGlass(mg);
						return true;
					} else {
						startWaiting(mg);
						return true;
					}
				}
		}
		synchronized(glasses) {
			for (MyGlass mg : glasses)
				if (mg.gs == GlassState.arrived) {
					doStartConveyor(); // start conveyor if conveyor isn't moving
					return true;
				}
		}
		
		return false;
	}
	
	// *** ACTIONS ***
	
	/* Stop conveyor when GUI says to break. */
	private void guiStopConveyor() {
		wasMoving = moving;
		doStopConveyor();
		gbs = GUIBreakState.stopped;
	}
	
	/* Restart conveyor on GUI command. */
	private void guiRestartConveyor() {
		if (wasMoving) doStartConveyor();
		gbs = null;
	}
	
	/* Send msgPositionFree after enough GUITimer ticks have passed while conveyor is moving since glass left front sensor. */
	private void sendPositionFree() {
		prev.msgPositionFree();
		waitingToSendPosFree = false;
	}

	/* Wait for msgPositionFree. */
	private void startWaiting(MyGlass mg) {
		doStopConveyor();
		mg.gs = GlassState.waiting;
	}
	
	/* Start this conveyor so animation sends glass to popup. Popup is waiting for glass right now. */
	private void sendGlass(MyGlass mg) {
		next.msgHereIsGlass(mg.g);
		doStartConveyor();
		mg.gs = GlassState.sent;
	}
	
	/* Remove mg from glasses. */
	private void removeGlass(MyGlass mg) {
		synchronized(glasses) {
			glasses.remove(mg);
		}
		posFree = false;
	}
	
	// *** ANIMATION ACTIONS ***
	
	/* Make animation start this conveyor. */
	private void doStartConveyor() {
		if (!moving) {
			Integer[] args = {id};
			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
			moving = true;
		}
	}
	
	/* Make animation stop this conveyor. */
	private void doStopConveyor() {
		if (moving) {
			Integer[] args = {id};
			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
			moving = false;
		}
	}
	
	// *** EXTRA ***
	
	/* Setters */
	public void setPrev(LineComponent lc) { prev = lc; }
	public void setNext(LineComponent lc) { next = lc; }
	
	/* Necessary for TruckAgent queries. */
	public boolean isEmpty() {
		return glasses.isEmpty();
	}
}
