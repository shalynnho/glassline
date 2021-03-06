package engine.agent.evan;

import java.util.*;
import java.util.concurrent.Semaphore;
import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.*;
import transducer.*;
import engine.agent.Agent;
import engine.agent.evan.ConveyorAgent.GUIBreakState;
import engine.agent.evan.interfaces.*;

public class PopupAgent extends Agent implements Popup {
	// *** DATA ***
	
	private LineComponent next, c; // the next LineComponent and the previous conveyor
	private OfflineWorkstation mach[];
	private int id; // place in animation
	
	enum GlassState {pending, needsProcessing, atMachine, doneProcessing, waiting};
	private class MyGlass {
		public Glass g;
		public GlassState gs;
		public int i; // machine index
		
		public MyGlass(Glass glass) {
			g = glass;
			gs = GlassState.pending;
			i = -1; // not at a machine yet
		}
		
		public String toString() { return g.toString() + " " + gs + " " + i; }
	}
	private List<MyGlass> glasses;
	
	private MachineType mt;
	private TChannel mtc; // machine type channel
	
	private Semaphore animSem[]; // animation delay semaphores: load finished, move up, move down, release finished, machine load finished
	
	private boolean init, mFree[], mBroken[], up, posFree, broken; // machine free, up or down, nextCF position free, broken or not
	
	/* Assigns references from arguments and sets other data appropriately. */
	public PopupAgent(String name, LineComponent conv, OfflineWorkstation machines[], MachineType mType, Transducer trans, int index) {
		super(name, trans);
		
		c = conv;
		mach = machines;
		mt = mType;
		mtc = mt.getChannel();
		transducer.register(this, TChannel.POPUP);
		transducer.register(this, mtc);
		id = index;
		
		glasses = Collections.synchronizedList(new ArrayList<MyGlass>());
		animSem = new Semaphore[5];
		for (int i = 0; i < 5; ++i)
			animSem[i] = new Semaphore(0);
		
		mFree = new boolean[2];
		mFree[0] = mFree[1] = true;
		mBroken = new boolean[2];
		mBroken[0] = mBroken[1] = false;
		up = broken = false;
		posFree = init = true; // different procedure when receiving first piece of glass
	}
	
	// *** MESSAGES ***
	
	/* Conveyor sends next glass when glass is at end of conveyor. */
	public void msgNextGlass(Glass g) {
		synchronized(glasses) {
			glasses.add(new MyGlass(g));
		}
		stateChanged();
	}

	/* Next conveyor tells when it's ready for glass. */
	public void msgPositionFree() {
		posFree = true;
		stateChanged();
	}

	/* Machine tells when glass is done being processed. */
	public void msgGlassDone(Glass g, int index) {
		synchronized(glasses) {
			for (MyGlass mg : glasses)
				if (mg.g.equals(g))
					mg.gs = GlassState.doneProcessing;
		}
		stateChanged();
	}

	/* This message is from the GUI to stop or restart. */
	public void msgGUIBreak(boolean stop) {
		broken = stop;
		stateChanged();
	}
	
	/* This message is from the GUI telling that the workstation is broken or unbroken. */
	public void msgGUIBreakWorkstation(boolean stop, int index) {
		mBroken[index] = stop;
		stateChanged();
	}
	
	/* This message is from the GUI telling that a piece of glass was removed from the workstation. */
	public void msgGUIBreakRemovedGlassFromWorkstation(int index) {
		MyGlass toRemove = null;
		synchronized(glasses) {
			for (MyGlass mg : glasses)
				if (mg.i == index)
					toRemove = mg;
		}
		if (toRemove != null) // toRemove will never be null
			glasses.remove(toRemove);
		mFree[index] = true;
		stateChanged();
	}

	/* Transducer event. */
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (channel == TChannel.POPUP && (Integer)args[0] == id) { // if it's an event for this popup
			if (event == TEvent.POPUP_GUI_LOAD_FINISHED) // load finished
				animSem[0].release();
			else if (event == TEvent.POPUP_GUI_MOVED_UP) // moved up
				animSem[1].release();
			else if (event == TEvent.POPUP_GUI_MOVED_DOWN) // moved down
				animSem[2].release();
			else if (event == TEvent.POPUP_GUI_RELEASE_FINISHED) // release finished
				animSem[3].release();
		} else if (channel == mtc && event == TEvent.WORKSTATION_LOAD_FINISHED) // machine load finished
			animSem[4].release(); // only one WorkStation is loaded at a time so doesn't matter which one (ignore args)
	}

	/* Scheduler.  Determine what action is called for, and do it. */
	public boolean pickAndExecuteAnAction() {
		if (init)
			initialReceiveGlass(); // different procedure for receiving first piece of glass
		
		if (broken)
			return false; // wait to be unbroken
		
		MyGlass toProcess = null;
		synchronized(glasses) {
			for (MyGlass mg : glasses)
				if (mg.gs == GlassState.waiting) {
					if (posFree) {
						toProcess = mg;
						break;
					} else
						return false; // popup is holding glass so can�t do anything else
				}
		}
		if (toProcess != null) {
			sendGlass(toProcess);
			return true;
		}
		synchronized(glasses) {
			for (MyGlass mg : glasses)
				if (mg.gs == GlassState.needsProcessing) {
					toProcess = mg;
					break;
				}
		}
		if (toProcess != null) {
			int i;
			if (mFree[0] && !mBroken[0])
				i = 0;
			else if (mFree[1] && !mBroken[1])
				i = 1;
			// allows for optimization in case one workstation breaks after glass needing processing is taken onto popup
			else if (mBroken[0])
				i = 0;
			else
				i = 1;
			moveUpAndToMachine(toProcess, i);
			return true;
		}
		synchronized(glasses) {
			for (MyGlass mg : glasses)
				if (mg.gs == GlassState.doneProcessing) {
					toProcess = mg;
					break;
				}
		}
		if (toProcess != null) {
			removeFromMachine(toProcess);
			return true;
		}
		synchronized(glasses) {
			for (MyGlass mg : glasses)
				// if pending and (there is a machine free or mg doesn't need processing)
				if (mg.gs == GlassState.pending && ((mFree[0] && !mBroken[0]) || (mFree[1] && !mBroken[1]) || !mg.g.getNeedsProcessing(mt))) {
					toProcess = mg;
					break;
				}
		}
		if (toProcess != null) {
			readyForGlass(toProcess);
			return true;
		}
		
		return false;
	}
	
	// *** ACTIONS ***
	
	/* Wait to receive first piece of glass, conveyor assumes position is free in beginning. */
	private void initialReceiveGlass() {
		doWaitAnimation(0); // wait to receive first piece of glass
		MyGlass mg = glasses.get(0);
		mg.gs = mg.g.getNeedsProcessing(mt) ? GlassState.needsProcessing : GlassState.waiting;
		init = false;
	}
	
	/* Move glass up and load it into the machine. */
	private void moveUpAndToMachine(MyGlass mg, int i) {
		mg.i = i;
		mg.gs = GlassState.atMachine;
		doMoveUp();
		
		mach[i].msgHereIsGlass(mg.g);
		doMachineLoad(i);
	}
	
	/* Move up, release glass from machine, move down, and start waiting. */
	private void removeFromMachine(MyGlass mg) {
		doMoveUp(); // only moves if necessary
		
		doMachineRelease(mg.i);
		doMoveDown();
		mg.gs = GlassState.waiting;
	}
	
	/* Send glass to next CF and release glass in animation as well. Then remove glass from list and reset posFree. */
	private void sendGlass(MyGlass mg) {
		next.msgHereIsGlass(mg.g);
		doReleaseGlass(mg);
		synchronized(glasses) {
			glasses.remove(mg);
		}
		posFree = false; // next conveyor now occupied
	}
	
	/* Move down, tell conveyor to send glass, and wait to receive glass. */
	private void readyForGlass(MyGlass mg) {
		doMoveDown();
		c.msgPositionFree();
		doWaitAnimation(0); // wait for conveyor to finish sending glass
		mg.gs = mg.g.getNeedsProcessing(mt) ? GlassState.needsProcessing : GlassState.waiting;
	}
	
	// *** ANIMATION ACTIONS ***
	
	/* Make animation move up. */
	private void doMoveUp() {
		if (!up) { // move up only if necessary
			Integer args[] = {id};
			transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, args);
			doWaitAnimation(1); // wait for move up to finish
			up = true;
		}
	}
	
	/* Make animation move down. */
	private void doMoveDown() {
		if (up) { // move down only if necessary
			Integer args[] = {id};
			transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
			doWaitAnimation(2); // wait for move down to finish
			up = false;
		}
	}
	
	/* Make animation load machine i. */
	private void doMachineLoad(int i) {
		Integer args[] = {i};
		transducer.fireEvent(mtc, TEvent.WORKSTATION_DO_LOAD_GLASS, args);
		doWaitAnimation(4); // wait for machine load to finish
		mFree[i] = false;
	}
	
	/* Make animation release glass from machine i. */
	private void doMachineRelease(int i) {
		Integer args[] = {i};
		transducer.fireEvent(mtc, TEvent.WORKSTATION_RELEASE_GLASS, args);
		doWaitAnimation(0); // wait for popup load to finish
		mFree[i] = true;
	}
	
	/* Make animation release glass from popup. */
	private void doReleaseGlass(MyGlass mg) {
		Integer args[] = {id};
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);
		doWaitAnimation(3); // wait for glass release to finish
	}
	
	/* Wait on an animation action using a semaphore acquire. */
	private void doWaitAnimation(int i) {
		try {
			animSem[i].acquire(); // wait for animation action to finish
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// *** EXTRA ***
	
	/* Queries whether threads are waiting on an animation action. Strictly for unit testing. */
	public boolean isWaiting(int i) {
		return animSem[i].hasQueuedThreads();
	}
	
	/* Setters */
	public void setNext(LineComponent lc) {
		next = lc;
	}
}
