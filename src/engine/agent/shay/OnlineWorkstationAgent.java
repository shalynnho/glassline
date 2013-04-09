package engine.agent.shay;

import java.util.concurrent.Semaphore;

import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.Workstation;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;

public class OnlineWorkstationAgent extends Agent implements Workstation {
	private MachineType type;
	private TChannel channel;
	private Glass glass;

	enum GlassState {
		pending, arrived, processing, processed, releasing, released, done
	};

	private GlassState state;
	private ConveyorAgent before, after;
	private Semaphore aniSem;
	private boolean recPosFree;

	public OnlineWorkstationAgent(String name, MachineType mt, Transducer t) {
		super(name, t);
		type = mt;
		recPosFree = false;
		channel = type.getChannel();
		aniSem = new Semaphore(0);
		transducer.register(this, channel);
	}

	// ***** MESSAGES ***** //
	public void msgHereIsGlass(Glass g) {
		glass = g;
		state = GlassState.pending;
		stateChanged();
	}

	public void msgPositionFree() {
		recPosFree = true;
		stateChanged();
	}

	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (event == TEvent.WORKSTATION_LOAD_FINISHED) {
			state = GlassState.arrived;
			stateChanged();
		} else if (event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
			aniSem.release();
		} else if (event == TEvent.WORKSTATION_RELEASE_FINISHED) {
			state = GlassState.released;
			stateChanged();
		}
	}

	// ***** SCHEDULER ***** //

	@Override
	public boolean pickAndExecuteAnAction() {
		if (state == GlassState.arrived) {
			processGlass();
			return true;
		}
		if (state == GlassState.processed) {
			releaseGlass();
			return true;
		}
		if (state == GlassState.released) {
			passToNext();
			return true;
		}
		return false;
	}

	// ***** ACTIONS ***** //

	private void processGlass() {
		doStartProcessing();
		doWaitProcessing();
		after.msgHereIsGlass(glass);
	}

	private void doStartProcessing() {
		transducer.fireEvent(channel, TEvent.WORKSTATION_DO_ACTION, null);
		state = GlassState.processing;
	}

	private void doWaitProcessing() {
		try {
			aniSem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		state = GlassState.processed;
	}

	private void releaseGlass() {
		transducer.fireEvent(channel, TEvent.WORKSTATION_RELEASE_GLASS, null);
		state = GlassState.releasing;
	}

	private void passToNext() {
		if (recPosFree) {
			after.msgHereIsGlass(glass);
			state = GlassState.done;
			recPosFree = false;
		}
	}

	// ***** ACCESSORS & MUTATORS ***** //

	@Override
	public MachineType getType() {
		return type;
	}

	@Override
	public TChannel getChannel() {
		return channel;
	}

	@Override
	public int getIndex() {
		return -1;
	}

}
