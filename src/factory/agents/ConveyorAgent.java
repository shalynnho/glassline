package factory.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import shared.Glass;
import shared.interfaces.ConveyorFamily;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import factory.interfaces.Conveyor;
import factory.misc.ConveyorFamilyEntity;
import factory.misc.ConveyorFamilyEntity.GlassState;
import factory.misc.ConveyorFamilyEntity.MyGlass;
import factory.misc.ConveyorFamilyEntity.RunningState;

public class ConveyorAgent extends Agent implements Conveyor {
	// *** Constructor(s) ***
	public ConveyorAgent(ConveyorFamilyEntity f, Transducer transducer) {
		family = f;
		t = transducer;
		t.register(this, TChannel.SENSOR);
	}
	
	// *** DATA ***
	public static final int CAPACITY = 3;
	private int spaceFree = CAPACITY;
	private boolean toldSensor = false;

	private ConveyorFamilyEntity family;
	private Transducer t;
	private enum ConveyorState { GLASS_JUST_ARRIVED, WAITING_FOR_GLASS_TO_REACH_ENDING_SENSOR, SHOULD_NOTIFY_POSITION_FREE, NOTHING_TO_DO }
	public ConveyorState state = ConveyorState.NOTHING_TO_DO;
	
	// Glasses that this conveyor must tell popup about
	private List<Glass> newGlasses = Collections.synchronizedList(new ArrayList<Glass>());
	// Normal list of glasses on conveyor
	private List<Glass> glassesOnConveyor = Collections.synchronizedList(new ArrayList<Glass>());

	// *** MESSAGES ***
	@Override
	public void msgHereIsGlass(Glass g) {
		spaceFree--;
		toldSensor = false;
		newGlasses.add(g);
		stateChanged();
	}

	@Override
	public void msgTakingGlass() {
		spaceFree++;
		glassesOnConveyor.remove(0);
		stateChanged();
	}
	
	// *** SCHEDULER ***
	@Override
	public boolean pickAndExecuteAnAction() {
		if (!newGlasses.isEmpty()) {
			actTellPopupGlassOnConveyor(); // removes glass from newGlasses and adds to glassesOnConveyor
			return true;
		} else if (spaceFree > 0 && !toldSensor) {
			actTellSensorPositionFree();
			return true;
		}
		return false;
	}
	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (state == ConveyorState.WAITING_FOR_GLASS_TO_REACH_ENDING_SENSOR) {
			if (channel == TChannel.SENSOR) {
				// When the sensor right after the conveyor has been pressed, stop the conveyor
				if (event == TEvent.SENSOR_GUI_PRESSED) {
					// parse args to check if it is this sensor
					// if so:
					doStopConveyor();

					family.runningState = RunningState.OFF_BC_WAITING_AT_SENSOR;
				}
			}
		}
	}
	
	// *** ACTIONS ***
	public void actTellPopupGlassOnConveyor() {
		Glass g = newGlasses.remove(0);
		glassesOnConveyor.add(g);

		GlassState glassState = family.decideIfGlassNeedsProcessing(g); // conveyor decides this since it has time
		MyGlass myGlass = family.new MyGlass(g, glassState);

		family.runningState = RunningState.ON_BC_CONVEYOR_TO_SENSOR;
		family.popup.msgGlassComing(myGlass);

		// Trust that conveyor knows to stop glass the moment the right sensor fires. See eventFired.
	}

	public void actTellSensorPositionFree() {
		toldSensor = true;
		family.sensor.msgPositionFree();
	}
	
	// *** TRANSDUCER / ANIMATION CALLS ***
	private void doStopConveyor() {
		// t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, thisIndex);
	}
	
	// *** EXTRA ***
}
