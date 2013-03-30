package factory.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import shared.Glass;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import factory.interfaces.Popup;
import factory.interfaces.Workstation;
import factory.misc.ConveyorFamilyEntity;
import factory.misc.ConveyorFamilyEntity.GlassState;
import factory.misc.ConveyorFamilyEntity.MyGlass;
import factory.misc.ConveyorFamilyEntity.RunningState;

public class PopupAgent extends Agent implements Popup {
	// *** Constructor(s) ***
	public PopupAgent(ConveyorFamilyEntity f, Transducer transducer, Workstation workstation1, Workstation workstation2, GUIPopUp gui) {
		family = f;
		t = transducer;
		this.workstation1 = workstation1;
		this.workstation2 = workstation2;
		this.workstationChannel = workstation1.getChannel(); // workstations should have same channel

		t.register(this, TChannel.SENSOR);
		t.register(this, this.workstationChannel);

		this.gui = gui;
	}

	// *** DATA ***
	private GUIPopUp gui;
	private ConveyorFamilyEntity family;
	private Transducer t;
	private Workstation workstation1; // top workstation, higher priority, one with lower index
	private Workstation workstation2; // bottom workstation
	private TChannel workstationChannel;
	private List<MyGlass> glasses = Collections.synchronizedList(new ArrayList<MyGlass>()); // uses MyGlass instead of just Glass so it contains GlassState
	// A glass is removed from glasses when it is messaged to a workstation. Then, workstation eventually sends glass back,
	// and the glass is added to finishedGlasses.
	private List<Glass> finishedGlasses = Collections.synchronizedList(new ArrayList<Glass>());
	private boolean nextPosFree = false;
	private boolean sensorOccupied = false; // roughly equivalent to family.runningState.OFF_BC_WAITING_AT_SENSOR, but needed for popup to internally decide to move to ON_BC_SENSOR_TO_POPUP
	
	// Mainly used to differentiate between waiting for a transducer event to fire (WAIT_FOR) and when popup should actually check scheduler events (ACTIVE)
	// if (ACTIVE) is used in scheduler, if (WAIT_FOR_SOMETHING) is used in eventFired to signal the popup is DOING_NOTHING for some animation to finish
	private enum PopupState { ACTIVE,
		WAITING_FOR_POPUP_GLASS_RELEASE, WAITING_FOR_HIGH_POPUP_BEFORE_LOADING_TO_WORKSTATION, 
		WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_RELEASING, 
		WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_LOADING_TO_WORKSTATION, 
		WAITING_FOR_WORKSTATION_GLASS_RELEASE, DOING_NOTHING 
		} // DOING_NOTHING is the default, doing nothing state - neither checking scheduler nor waiting for an animation to finish
	private enum WorkstationState { FREE, BUSY, DONE_BUT_STILL_HAS_GLASS }
	PopupState state = PopupState.DOING_NOTHING;
	WorkstationState wsState1 = WorkstationState.FREE;
	WorkstationState wsState2 = WorkstationState.FREE;

	// *** MESSAGES ***
	@Override
	public void msgGlassComing(MyGlass myGlass) { // Just to pass on glass object to sensor's list
		glasses.add(myGlass);
		if (state == PopupState.DOING_NOTHING) {
			state = PopupState.ACTIVE;
			stateChanged(); // only check scheduler if doing nothing
		}
	}

	@Override
	public void msgPositionFree() {
		nextPosFree = true;
		if (state == PopupState.DOING_NOTHING) {
			state = PopupState.ACTIVE;
			stateChanged(); // only check scheduler if doing nothing
		}
	}

	@Override
	public void msgGlassDone(Glass g, int machineIndex) { // int needed?
		/* 
		Need to enforce that one workstation is dealt with at a time.
		*/
		updateWorkstationState(machineIndex, WorkstationState.DONE_BUT_STILL_HAS_GLASS);
		finishedGlasses.add(g);

		if (state == PopupState.DOING_NOTHING) {
			state = PopupState.ACTIVE;
			stateChanged(); // only check scheduler if doing nothing
		}
		// otherwise, popup is busy WAITING_FOR something else to happen, or is already ACTIVE doing something perhaps for the other workstation
		
	}	

	// *** SCHEDULER ***
	@Override
	// TODONOW: DEAL WITH SECOND WORKSTATION. WHICH RELEASES FIRST? HOW?
	// sensor's glass -> workstation 1 -> workstation 2 (wks order dependent on who got part first?)
	public boolean pickAndExecuteAnAction() {
		// ACTIVE is set by transducer and incoming messages. We only take action if we are 'active'.
		if (state == PopupState.ACTIVE) {
			// Case 1 (easy): Just deal with the workstation's finished glass by passing it on. No complications with sensor.
			if (!sensorOccupied) {
				// If next position is free and there exists a glass in glasses list that is finished by a workstation
				if (nextPosFree && atLeastOneWorkstationIsDoneButStillHasGlass()) {
					// Keep state as ACTIVE. This is implied.
					actReleaseGlassFromWorkstation();
					return false;
				}
			} 
			// Case 2-x deal with when sensor is occupied, which adds complications.
			else {
				MyGlass g = getNextUnhandledGlass(); // the *unhandled* glass - we make the glass at the sensor more important than any glass at a workstation
				if (g != null) { // should be present since sensorOccupied = true
					// Case 2: Regardless of workstation, just load sensor's glass and pass it on - no workstation interaction
					if (nextPosFree && !g.needsProcessing()) {
						actLoadSensorsGlassOntoPopupAndRelease();
						return false;
					} 
					// Case 3: Release workstation's finished glass to next family
					else if (g.needsProcessing() && bothWorkstationsOccupiedButAtLeastOneIsDone() && nextPosFree) {
						actReleaseGlassFromWorkstation();
						return false;
					} 
					// Case 4: Load sensor's glass onto workstation. Must happen after case 3 if case 3 happens.
					else if (g.needsProcessing() && aWorkstationIsFree()) {
						actLoadSensorsGlassOntoWorkstation();
						return false;
					}
				} else {
					System.err.println("Null unhandled glass!");
				}

				// Handle finished glasses
			}
		} // returning true above is actually meaningless since all act methods lead to WAIT state, so we just reach false anyway. 
		state = PopupState.DOING_NOTHING; // this could interfere with other wait states if you returned true above
		return false;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// Most checks here involve seeing if state is a form of WAITING_FOR, which happen from scheduler actions.

		// Exception: we must update sensor status regardless of the state.
		if (!sensorOccupied) { // should only bother to check if sensor is not occupied - here, the popup only cares about listening to see if a glass has arrived at the preceding sensor
			if (channel == TChannel.SENSOR) {
				// When the sensor right before the popup has been pressed, allow loading of glass onto popup
				if (event == TEvent.SENSOR_GUI_PRESSED) {
					// TODO: parse args to check if it is this sensor
					state = PopupState.ACTIVE;
					sensorOccupied = true;
					stateChanged();
				}
			}
		}

		// From actLoadSensorsGlassOntoWorkstation, step 2 (sometimes)
		if (state == PopupState.WAITING_FOR_LOW_POPUP_BEFORE_LOADING_TO_WORKSTATION) {
			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_DOWN) {
				// TODO: parse args to check if it is this popup
				state = PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_LOADING_TO_WORKSTATION;
				doStartConveyor();
				family.runningState = RunningState.ON_BC_SENSOR_TO_POPUP;
				family.conv.msgTakingGlass();
			}
		}
		// From actLoadSensorsGlassOntoWorkstation, step 3
		if (state == PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_LOADING_TO_WORKSTATION) {
			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_LOAD_FINISHED) {
				// TODO: parse args to check if it is this sensor
				// if so:
				state = PopupState.WAITING_FOR_HIGH_POPUP_BEFORE_LOADING_TO_WORKSTATION;
				sensorOccupied = false;
				family.runningState = RunningState.OFF_BC_QUIET;
				doStopConveyor();
				doMovePopupUp();
			}
		}
		// From actLoadSensorsGlassOntoWorkstation, step 4 (final)
		if (state == PopupState.WAITING_FOR_HIGH_POPUP_BEFORE_LOADING_TO_WORKSTATION) {
			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_UP) {
				// TODO: parse args to check if it is this popup
				state = PopupState.ACTIVE;
				MyGlass g = glasses.remove(0); // first glass should be the one
				
				Workstation w = getWorkstationWithState(WorkstationState.FREE);
				updateWorkstationState(w, WorkstationState.BUSY);
				w.msgHereIsGlass(g.getGlass());

				doLoadGlassOntoWorkstation(w.getIndex());
				stateChanged();
			}
		}

		// From actReleaseGlassFromWorkstation step 2 (sometimes)
		if (state == PopupState.WAITING_FOR_HIGH_POPUP_BEFORE_RELEASING_FROM_WORKSTATION) {
			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_UP) {
				state = PopupState.WAITING_FOR_WORKSTATION_GLASS_RELEASE;
				doReleaseGlassFromProperWorkstation();
			}
		}
		// From actReleaseGlassFromWorkstation step 3
		if (state == PopupState.WAITING_FOR_WORKSTATION_GLASS_RELEASE) {
			if (channel == this.workstationChannel && event == TEvent.WORKSTATION_RELEASE_FINISHED) {
				state = PopupState.WAITING_FOR_LOW_POPUP_WITH_GLASS_FROM_WORKSTATION;
				doMovePopupDown();
			}
		}
		// From actReleaseGlassFromWorkstation step 4 (final)
		if (state == PopupState.WAITING_FOR_LOW_POPUP_WITH_GLASS_FROM_WORKSTATION) {
			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_DOWN) {
				state = PopupState.ACTIVE;

				// Here we can send the next family the message. No need to check POPUP_GUI_RELEASE_FINISHED b/c that is detected _after_ the next family's sensor already gets the glass.
				Glass glass = finishedGlasses.remove(0); // remove & return first element
				family.nextFamily.msgHereIsGlass(glass);

				doReleaseGlassFromPopup();

				stateChanged();
			}
		}

		// From actLoadSensorsGlassOntoPopupAndRelease, step 2 (sometimes)
		if (state == PopupState.WAITING_FOR_LOW_POPUP_BEFORE_RELEASE) {
			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_DOWN) {
				// TODO: parse args to check if it is this popup
				// if so:
				state = PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_RELEASING;
				doStartConveyor();
				family.runningState = RunningState.ON_BC_SENSOR_TO_POPUP;
			}
		}
		// From actLoadSensorsGlassOntoPopupAndRelease, step 3 (final)
		if (state == PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_RELEASING) {
			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_LOAD_FINISHED) {
				// TODO: parse args to check if it is this popup
				// if so:
				state = PopupState.ACTIVE;
				sensorOccupied = false;

				// Here we can send the next family the message. No need to check POPUP_GUI_RELEASE_FINISHED b/c that is detected _after_ the next family's sensor already gets the glass.
				MyGlass mg = glasses.remove(0); // should be first glass
				family.nextFamily.msgHereIsGlass(mg.getGlass());

				family.runningState = RunningState.OFF_BC_QUIET;
				doStopConveyor();
				doReleaseGlassFromPopup();

				stateChanged();
			}
		}
	}

	// *** ACTIONS ***
	// Note that each act method here sets the popup state to some form of DOING_NOTHING in order to work with animation calls.
	/**
	 * Loads glass from sensor onto popup and then releases to next conveyor family
	 * Multi-step with eventFired
	 */
	public void actLoadSensorsGlassOntoPopupAndRelease() {
		if (gui.isUp()) {
			state = PopupState.WAITING_FOR_LOW_POPUP_BEFORE_RELEASE;
			doMovePopupDown();
		} else if (gui.isDown()) { // popup already down
			state = PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_RELEASING;
			family.runningState = RunningState.ON_BC_SENSOR_TO_POPUP;
			doStartConveyor();
		} else {
			System.err.println("Popup should not have been in a state other than up or down - actLoadSensorsGlassOntoPopupAndRelease");
		}
	}

	// Multi-step with eventFired
	public void actLoadSensorsGlassOntoWorkstation() {
		if (gui.isUp()) {
			state = PopupState.WAITING_FOR_LOW_POPUP_BEFORE_LOADING_TO_WORKSTATION;			
			doMovePopupDown();
		} else if (gui.isDown()) { // popup already down
			state = PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_LOADING_TO_WORKSTATION;
			family.runningState = RunningState.ON_BC_SENSOR_TO_POPUP;
			doStartConveyor();
			family.conv.msgTakingGlass();
		} else {
			System.err.println("Popup should not have been in a state other than up or down - actLoadSensorsGlassOntoWorkstation");
		}
		
	}

	/**
	 * Releases glass from workstation to next conveyor family
	 */
	public void actReleaseGlassFromWorkstation() {
		// Note: popup must be up -> WORKSTATION_RELEASE_FINISHED -> POPUP_GUI_LOAD_FINISHED (implied) -> 
		// POPUP_GUI_MOVED_DOWN -> automatically moves on

		// Make sure gui is up first
		if (gui.isDown()) {
			state = PopupState.WAITING_FOR_HIGH_POPUP_BEFORE_RELEASING_FROM_WORKSTATION;
			doMovePopupUp();
		} else if (gui.isUp()) { // popup already up
			state = PopupState.WAITING_FOR_WORKSTATION_GLASS_RELEASE;
			doReleaseGlassFromProperWorkstation();
		} else {
			System.err.println("Popup should not have been in a state other than up or down - actReleaseGlassFromWorkstation");
		}
	}

	// *** TRANSDUCER / ANIMATION CALLS ***
	private void doStartConveyor() {
		// t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, thisIndex);
	}

	private void doStopConveyor() {
		// t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, thisIndex);
	}

	private void doMovePopupUp() {
		//POPUP_DO_MOVE_UP
	}

	private void doMovePopupDown() {
		//POPUP_DO_MOVE_DOWN = t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, {thisIndex});
	}

	private void doLoadGlassOntoWorkstation(int workstationIndex) {
		//WORKSTATION_DO_LOAD_GLASS
	}
	
	// Choose appropriate workstation and fires WORKSTATION_RELEASE_GLASS. Lower index has higher priority.
	private void doReleaseGlassFromProperWorkstation() {
		if (wsState1 == WorkstationState.DONE_BUT_STILL_HAS_GLASS) {
			//WORKSTATION_RELEASE_GLASS
		} else if (wsState2 == WorkstationState.DONE_BUT_STILL_HAS_GLASS) {
			//WORKSTATION_RELEASE_GLASS
		}
	}

	// *** EXTRA ***
	/**
	 * Returns next MyGlass object from glasses list that either needs processing or doesn't
	 */
	private MyGlass getNextUnhandledGlass() {
		MyGlass g = null;
		for (MyGlass mg : glasses) {
			if (mg.getState() == GlassState.NEEDS_PROCESSING || mg.getState() == GlassState.DOES_NOT_NEED_PROCESSING)
				return mg;
		}
		System.err.print("No glass to handle!");
		return null;
	}
	
	// /**
	//  * @return MyGlass object in glasses list with same Glass pointer
	//  */
	// private MyGlass findGlass(Glass g) { // no need for machine index since glass pointer preserved?
	// 	for (MyGlass mg : glasses) {
	// 		if (mg.getGlass() == g) {
	// 			return mg;
	// 		}
	// 	}
	// 	System.err.print("Could not find glass!");
	// 	return null;
	// }
	
	// /**
	//  * A safety method for ensuring we choose the correct MyGlass in glasses list based on state
	//  * @return first MyGlass object in glasses list with the given state 
	//  */
	// private MyGlass findGlassWithState(GlassState state) {
	// 	for (MyGlass mg : glasses) {
	// 		if (mg.getState() == state) {
	// 			return mg;
	// 		}
	// 	}
	// 	System.err.print("Could not find glass with state "+state);
	// 	return null;
	// }

	private boolean aWorkstationHasState(WorkstationState state) {
		return wsState1 == state || wsState2 == state;
	}

	private void updateWorkstationState(Workstation w, WorkstationState state) {
 		if (w.getIndex() % 2 == 0) // 0 or even index means top machine (machine 1)
			wsState1 = state;
		else
			wsState2 = state;
	}
	private void updateWorkstationState(int machineIndex, WorkstationState state) {
		if (machineIndex % 2 == 0) // 0 or even index means top machine (machine 1)
			wsState1 = state;
		else
			wsState2 = state;
	}

	// Returns true if there is a glass that is done processing on a workstation
	private boolean atLeastOneWorkstationIsDoneButStillHasGlass() {
		return aWorkstationHasState(WorkstationState.DONE_BUT_STILL_HAS_GLASS);
	}

	private boolean aWorkstationIsFree() {
		return aWorkstationHasState(WorkstationState.FREE);
	}

	private boolean bothWorkstationsOccupiedButAtLeastOneIsDone() {
		return wsState1 != WorkstationState.FREE && wsState2 != WorkstationState.FREE && atLeastOneWorkstationIsDoneButStillHasGlass();
	}
}
