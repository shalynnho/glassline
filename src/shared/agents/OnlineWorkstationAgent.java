package shared.agents;

import java.util.concurrent.Semaphore;
import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.LineComponent;
import transducer.*;
import engine.agent.Agent;

public class OnlineWorkstationAgent extends Agent implements LineComponent {
	private MachineType type;
	private TChannel channel;
	private Glass glass;

	enum GlassState {pending, arrived, processing, processed, releasing, released};

	private GlassState state;
	private LineComponent before, after;
	private Semaphore aniSem;
	private boolean recPosFree;

	public OnlineWorkstationAgent(String name, MachineType mt, Transducer t, LineComponent b, LineComponent a) {
		super(name, t);
		type = mt;
		recPosFree = true;
		channel = type.getChannel();
		aniSem = new Semaphore(0);
		transducer.register(this, channel);
		before = b;
		after = a;
	}

	// *** MESSAGES ***
	
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

	// *** SCHEDULER ***
	public boolean pickAndExecuteAnAction() {
		if (state == GlassState.arrived) {
			processGlass();
			return true;
		}
		if (state == GlassState.processed) {
			releaseGlass();
			return true;
		}
		if (state == GlassState.released && recPosFree) {
			reset();
			return true;
		}
		return false;
	}

	// *** ACTIONS ***
	
	private void processGlass() {
		doStartProcessing();
		doWaitProcessing();
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
		after.msgHereIsGlass(glass);
	}
	
	private void reset() {
		state = null;
		glass = null;
		recPosFree = false;
		before.msgPositionFree();
	}
	
	// *** ACCESSORS & MUTATORS ***
	
	public MachineType getType() {
		return type;
	}
	
	public TChannel getChannel() {
		return channel;
	}
}
