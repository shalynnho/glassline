package engine.agent.shay;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import shared.Glass;
import shared.interfaces.LineComponent;
import transducer.TChannel;
import transducer.TEvent;
import engine.agent.Agent;
import engine.agent.shay.interfaces.Conveyor;
import engine.agent.shay.interfaces.TransducerIfc;

public class ConveyorAgent extends Agent implements Conveyor {
	
	private BigOnlineConveyorFamily family;
	private LineComponent previous, next;

	private Semaphore aniSem;
	private boolean recPosFree;
	
	private int myIndex;
	
	enum GlassState {PENDING, ARRIVED, MOVING, END, WAITING, SENT, DONE, NO_ACTION};
	
	private class MyGlass {
		public Glass g;
		public GlassState gs;
		
		public MyGlass(Glass g) {
			this.g = g;
			this.gs = GlassState.PENDING;
		}
	}
	
	private LinkedList<MyGlass> glass;

	/*
	 * Make sure you setConveyorFamily and setWorkstation immediately after construction.
	 */
	public ConveyorAgent(String name, TransducerIfc t, int index) {
		super(name, t);
		
		transducer.register(this, TChannel.SENSOR);
		myIndex = index;
		glass = new LinkedList<MyGlass>();
		recPosFree = true;
		aniSem = new Semaphore(0);
		
		powerConveyor(true);
	}

	// ***** MESSAGES ***** //

	/**
	 * From CoveyorFamily (if BEFORE) / Workstation (if AFTER)
	 * 
	 * @param g
	 *            - the glass that is passed to the conveyor
	 */
	public void msgHereIsGlass(Glass g) {
		glass.add(new MyGlass(g));
		stateChanged();
	}

	// ***** SCHEDULER ***** //
	@Override
	public boolean pickAndExecuteAnAction() {
		
		for (MyGlass g : glass)
			if (g.gs == GlassState.DONE) {
				actRemoveFromList(g);
				return true;
			}
		
		for (MyGlass g : glass)
			if (g.gs == GlassState.WAITING) {
				if (recPosFree) {
					// send to next LineComponent
					actSendGlassToNext(g);
					return true;
				} else {
					// waiting and full
					return false;
				}
			}
		
		for (MyGlass g : glass)
			if (g.gs == GlassState.ARRIVED) {
				powerConveyor(true);
				return true;
			}
		
		return false;
	}

	// ***** ACTIONS ***** //
	
	private void actRemoveFromList(MyGlass g) {
		glass.remove(g);
	}
	
	private void actSendGlassToNext(MyGlass g) {
		next.msgHereIsGlass(g.g);
		previous.msgPositionFree();
	}

	//***** PRIVATE HELPER METHODS *****//
	
	private void powerConveyor(boolean start) {
		Integer[] args = {myIndex};
		if (start) {
			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
		} else {
			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
		}
	}
	
	// ***** ACCESSORS & MUTATORS ***** //
	
	@Override
	public int getIndex() {
		return myIndex;
	}
	
	public void setPreviousLineComponent(LineComponent p) {
		previous = p;
		previous.msgPositionFree();
	}
	
	public void setNextLineComponent(LineComponent n) {
		next = n;
	}
	

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		int sensorID = (Integer)args[0];
		
		if (channel.equals(TChannel.SENSOR)) {
			
		}
	}
}
