package engine.agent.shay;

import java.util.LinkedList;

import shared.Glass;
import shared.enums.MachineType;
import engine.agent.shay.interfaces.Conveyor;
import engine.agent.shay.interfaces.Popup;
import engine.agent.shay.interfaces.TransducerIfc;

public class ConveyorFamily implements shared.interfaces.ConveyorFamily {

	private shared.interfaces.ConveyorFamily previous;
	private shared.interfaces.ConveyorFamily next;
	private TransducerIfc transducer;

	private Conveyor conveyor;
	private Popup popup;
	private MachineType type;

	private LinkedList<Glass> glass;

	private boolean nextCFPosFree = false;
	private boolean conveyorPosFree = false;

	public ConveyorFamily(Conveyor c, Popup p, TransducerIfc t) {
		conveyor = c;
		popup = p;
		transducer = t;
		
		type = popup.getType();
		
		glass = new LinkedList<Glass>();

		conveyor.setConveyorFamily(this);
		popup.setConveyorFamily(this);

		introduceAgents();
		sendPositionFree();
	}

	@Override
	/**
	 * Sent from previous ConveyorFamily after receiving msgPositionFree.
	 * @param glass
	 */
	public void msgHereIsGlass(Glass g) {
		if (conveyor.getState() == ConveyorState.ON_POS_FREE) {
			conveyor.msgHereIsGlass(g, this);
			glass.add(g);
			conveyorPosFree = false;
		} else {
			//System.out.println("CF " + type + ": conveyor is not in waiting state / position is not free.");
		}
	}

	@Override
	/**
	 * Sent from next ConveyorFamily indicating ready for next piece of glass.
	 */
	public void msgPositionFree() {
		nextCFPosFree = true;
	}

	@Override
	/**
	 * Sent from Workstation when glass is finished processing.
	 * @param glass - the piece of glass
	 * @param machineIndex - index of the machine (0 or 1)
	 */
	public void msgGlassDone(Glass g, int machineIndex) {
		popup.msgGlassDone(g, machineIndex, this);
		glass.remove();
	}

	public void conveyorPositionFree() {
		conveyorPosFree = true;
		sendPositionFree();
	}
	
	public boolean popupDonePassGlass(Glass g) {
		if(nextCFPosFree) {
			next.msgHereIsGlass(g);
			nextCFPosFree = false;
			return true;
		} else {
			return false;
		}
	}

	// ***** PRIVATE METHODS ***** //

	private void introduceAgents() {
		conveyor.setPopup(popup);
		popup.setConveyor(conveyor);
	}

	private void sendPositionFree() {
		if (previous != null && conveyorPosFree) {
			previous.msgPositionFree();
			System.out.println("CF " + type + ": position free sent.");
		} else {
			//System.out.println("sendPositionFree failed: No previous ConveyorFamily (or BinRobot) set.");
		}
	}

	// ***** ACCESSORS & MUTATORS ***** //

	public void setPreviousCF(shared.interfaces.ConveyorFamily prev) {
		previous = prev;
	}

	public void setNextCF(shared.interfaces.ConveyorFamily nxt) {
		next = nxt;
	}
	
	public MachineType getType() {
		return type;
	}
	
	public boolean getNextCFPosFree() {
		return nextCFPosFree;
	}
	
	public boolean getConveyorPosFree() {
		return conveyorPosFree;
	}
	
}
