package engine.agent.shay;

import java.util.ArrayList;
import java.util.List;

import shared.Glass;
import shared.enums.MachineType;
import transducer.TChannel;
import transducer.TEvent;
import engine.agent.Agent;
import engine.agent.shay.interfaces.Conveyor;
import engine.agent.shay.interfaces.Popup;
import engine.agent.shay.interfaces.TransducerIfc;

public class PopupAgent extends Agent implements Popup {
	
	private ConveyorFamily family;
	private int myIndex;
	
	private List<Glass> glass;
	private Glass pendingAdd;
	private Glass received;
	private Glass pendingDone;
	
	private boolean down = true;
	private PopupGlassState glassState;
	private MachineType type;
	private MyConveyor conveyor;
	
	private MoveState moveState;
	private WSActive wsActive;
	
	public enum MoveState { LOADING, RELEASING, MOVING, NO_ACTION };
	public enum WSActive { WS0, WS1, WS01, NONE };
	
	/*private MyWorkstation workstation1;
	private MyWorkstation workstation2;
	
	class MyWorkstation {
		WorkstationAgent machine;
		WorkstationState state;
		int machineIndex;
		
		MyWorkstation(WorkstationAgent m, WorkstationState s, int i) {
			machine = m;
			state = s;
			machineIndex = i;
		}
	}*/
	
	class MyConveyor {
		Conveyor agent;
		ConveyorState state;
		
		MyConveyor(Conveyor c, ConveyorState s) {
			agent = c;
			state = s;
		}
	}
	
	public PopupAgent(String name, TransducerIfc t, MachineType ty, Sensor end, int index) {
		super(name, t);
		type = ty;
		myIndex = index;
		transducer.register(this, TChannel.POPUP);
		glass = new ArrayList<Glass>(3);
		glassState = PopupGlassState.NO_ACTION;
		moveState = MoveState.NO_ACTION;
		wsActive = WSActive.NONE;
	}
	
	
	// ***** MESSAGES ***** //
	
	/**
	 * From conveyor to popup
	 * @param g - unprocessed glass
	 */
	public void msgIHaveGlass(Glass g, Conveyor c) {
		// determine if glass needs processing & whether to take glass
		glassState = PopupGlassState.OFFERED_GLASS;
		pendingAdd = g;
		stateChanged();
	}
	
	/**
	 * From conveyor to popup
	 * @param g - unprocessed glass
	 */
	public void msgHereIsGlass(Glass g, Conveyor c) {
		moveState = MoveState.LOADING;
		pendingAdd = null;
		received = g;
		stateChanged();
	}
	
	/**
	 * Sent from the CF after receiving msgGlassDone from workstation
	 * @param g - the processed glass
	 * @param machineIndex - index of the machine which holds the glass
	 */
	public void msgGlassDone(Glass g, int machineIndex, ConveyorFamily c) {
		moveState = MoveState.RELEASING;
		glassState = PopupGlassState.PASS_ME_GLASS;
		pendingDone = g;
		
		if (machineIndex == 0) {
			if (wsActive == WSActive.WS0) {
				wsActive = WSActive.NONE;
			} else if (wsActive == WSActive.WS01) {
				wsActive = WSActive.WS1;
			} else if (wsActive == WSActive.NONE || wsActive == WSActive.WS1) {
				System.out.println("PopupAgent Error: no workstations active or WS0 not active");
			}
		} else if (machineIndex == 1) {
			if (wsActive == WSActive.WS1) {
				wsActive = WSActive.NONE;
			} else if (wsActive == WSActive.WS01) {
				wsActive = WSActive.WS1;
			} else if (wsActive == WSActive.NONE || wsActive == WSActive.WS0) {
				System.out.println("PopupAgent Error: no workstations active or WS1 not active");
			}
		}
		
		stateChanged();
	}
	
	
	// ***** SCHEDULER ***** //

	@Override
	public boolean pickAndExecuteAnAction() {
		if (moveState == MoveState.RELEASING) {
			if (glassState == PopupGlassState.PASS_ME_GLASS) {
				actReleaseGlass();
				return true;
			}
		}
		
		if (moveState == MoveState.LOADING) {
			if (glassState == PopupGlassState.PASS_ME_GLASS) {
				actTakeGlass();
				return true;
			}
		}
		
		if (moveState == MoveState.NO_ACTION) {
			if (glassState == PopupGlassState.OFFERED_GLASS) {
				if (((wsActive != WSActive.WS01 && 
						pendingAdd.getNeedsProcessing(type)) || (!pendingAdd.getNeedsProcessing(type)))) {
					actPassMeGlass();
					return true;
				}
			}
		}
		

		
		return false;
	}
	
	// ***** ACTIONS ***** //
	
	private void actPassMeGlass() {
		glassState = PopupGlassState.PASS_ME_GLASS;
		conveyor.agent.msgPassMeGlass(this);
		stateChanged();
	}
	
	private void actTakeGlass() {
		glassState = PopupGlassState.NO_ACTION;
		if (received != null) {
			if (received.getNeedsProcessing(type)) {
				glass.add(received);
				
				if (wsActive == WSActive.NONE) {
				wsActive = WSActive.WS0;
				} else if (wsActive == WSActive.WS0 || wsActive == WSActive.WS1) {
					wsActive = WSActive.WS01;
				} else {
					System.out.println("PopupAgent Error: Can't process this glass, no space.");
				}
				
			} else {
				// just passing through
				pendingDone = received;
				glass.add(pendingDone);
				moveState = MoveState.RELEASING;
				actReleaseGlass();
			}
			pendingAdd = null;
			
		} else {
			System.out.println("PopupAgent Error: No piece of glass was set to add.");
		}
		stateChanged();
	}
	
	private void actReleaseGlass() {
		if (family.popupDonePassGlass(pendingDone)) {
			glass.remove(pendingDone);

			pendingDone = null;
			glassState = PopupGlassState.NO_ACTION;
			
			Object[] args = new Object[1];
			args[0] = myIndex;
			transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);
			moveState = MoveState.NO_ACTION;
		}
		stateChanged();
	}	
	
	// ***** ACCESSORS & MUTATORS ***** //
	
	public void setConveyor(Conveyor c) {
		conveyor = new MyConveyor(c, ConveyorState.ON_POS_FREE);
	}
	
	public void setConveyorFamily(ConveyorFamily cf) {
		family = cf;
	}
	
	public MachineType getType() {
		return type;
	}

	@Override
	public int getIndex() {
		return myIndex;
	}	
	
	public boolean getDown() {
		return down;
	}
	
	public List<Glass> getGlassList() {
		return glass;
	}
	
	public ConveyorFamily getFamily() {
		return family;
	}
	
	public Glass getGlassPendingAdd() {
		return pendingAdd;
	}
	
	public Glass getGlassReceived() {
		return received;
	}
	
	public Glass getGlassPendingDone() {
		return pendingDone;
	}
	
	public PopupGlassState getGlassState() {
		return glassState;
	}
	
	public Conveyor getConveyor() {
		return conveyor.agent;
	}
	
	public MoveState getMoveState() {
		return moveState;
	}
	
	public WSActive getWSActiveState() {
		return wsActive;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (channel == TChannel.POPUP) {

			
			if (event == TEvent.POPUP_GUI_MOVED_DOWN) {
				moveState = MoveState.NO_ACTION;
				down = true;
				if (pendingDone != null) {
					moveState = MoveState.RELEASING;
					glassState = PopupGlassState.PASS_ME_GLASS;
				}
			}
			
			if (event == TEvent.POPUP_GUI_MOVED_UP) {
				moveState = MoveState.NO_ACTION;
				down = false;
			}
			
			if (event == TEvent.POPUP_GUI_LOAD_FINISHED) {
				moveState = MoveState.NO_ACTION;
				if (!down) {
					Object[] i = new Object[1];
					i[0] = myIndex;
					transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, i);
				} else {
					Object[] i = new Object[1];
					i[0] = myIndex;
					transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, i);
				}
			}
			
			if (event == TEvent.POPUP_GUI_RELEASE_FINISHED) {
				moveState = MoveState.NO_ACTION;
			}
		}
	}
}
