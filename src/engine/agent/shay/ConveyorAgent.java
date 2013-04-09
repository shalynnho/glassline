package engine.agent.shay;

import java.util.LinkedList;

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

	private boolean recPosFree;
	
	private int myIndex;
	
	enum GlassState {PENDING, ARRIVED, MOVING, WAITING_AT_END, PASSED, DONE};
	
	private class MyGlass {
		public Glass glass;
		public GlassState state;
		
		public MyGlass(Glass g) {
			this.glass = g;
			this.state = GlassState.PENDING;
		}
	}
	
	private LinkedList<MyGlass> glassList;

	/*
	 * Make sure you setConveyorFamily and setWorkstation immediately after construction.
	 */
	public ConveyorAgent(String name, TransducerIfc t, int index) {
		super(name, t);
		
		transducer.register(this, TChannel.SENSOR);
		myIndex = index;
		glassList = new LinkedList<MyGlass>();
		recPosFree = true;
		
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
		glassList.add(new MyGlass(g));
		stateChanged();
	}

	// ***** SCHEDULER ***** //
	@Override
	public boolean pickAndExecuteAnAction() {
		
		for (MyGlass g : glassList)
			if (g.state == GlassState.DONE) {
				actRemoveFromList(g);
				return true;
			}
		
		for (MyGlass g : glassList)
			if (g.state == GlassState.WAITING_AT_END) {
				if (recPosFree) {
					// send to next LineComponent
					actSendGlassToNext(g);
					return true;
				} else {
					// waiting and full
					return false;
				}
			}
		
		for (MyGlass g : glassList)
			if (g.state == GlassState.ARRIVED) {
				powerConveyor(true);
				return true;
			}
		
		return false;
	}

	// ***** ACTIONS ***** //
	
	private void actRemoveFromList(MyGlass g) {
		glassList.remove(g);
	}
	
	private void actSendGlassToNext(MyGlass g) {
		powerConveyor(true);
		g.state = GlassState.PASSED;
		next.msgHereIsGlass(g.glass);
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
			if (event == TEvent.SENSOR_GUI_PRESSED) {
				if (sensorID == myIndex * 2) {	// front sensor pressed (using Evan's alg)
					for (MyGlass g : glassList) {
						if (g.state == GlassState.PENDING) {
							g.state = GlassState.ARRIVED;
							previous.msgPositionFree();
							break;
						}
					}
					stateChanged();
				} else if (sensorID == myIndex * 2 + 1) {	// back sensor pressed
					for (MyGlass g : glassList) {
						if (g.state == GlassState.MOVING) {
							g.state = GlassState.WAITING_AT_END;
							powerConveyor(false);
							break;
						}
					}
					stateChanged();
				}
			} else if (event == TEvent.SENSOR_GUI_RELEASED) {
				if (sensorID == myIndex * 2) {	// front sensor released (using Evan's alg)
					for (MyGlass g : glassList) {
						if (g.state == GlassState.ARRIVED) {
							g.state = GlassState.MOVING;
							break;
						}
					}
					stateChanged();
				} else if (sensorID == myIndex * 2 + 1) {	// back sensor released
					for (MyGlass g : glassList) {
						if (g.state == GlassState.PASSED) {
							g.state = GlassState.DONE;
							break;
						}
					}
					stateChanged();
				}
			}
		}
	}
}
