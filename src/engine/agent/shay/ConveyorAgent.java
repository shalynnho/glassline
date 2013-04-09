package engine.agent.shay;

import java.util.LinkedList;

import shared.Glass;
import transducer.TChannel;
import transducer.TEvent;
import engine.agent.Agent;
import engine.agent.shay.enums.ConveyorState;
import engine.agent.shay.enums.PopupGlassState;
import engine.agent.shay.enums.PopupState;
import engine.agent.shay.interfaces.Conveyor;
import engine.agent.shay.interfaces.Popup;
import engine.agent.shay.interfaces.TransducerIfc;

public class ConveyorAgent extends Agent implements Conveyor {

	// Maximum number of glass pieces that can be on a conveyor
	private static final int MAX_NUM = 7;

	private OfflineConveyorFamily family;
	private MyPopup popup;
	private Sensor startSensor;
	private Sensor endSensor;

	private ConveyorState state;
	private int myIndex;

	private LinkedList<Glass> glass;

	private Glass glassToReceive;
		
	class MyPopup {
		Popup agent;
		PopupGlassState state;
		PopupState pos;
		
		MyPopup(Popup p, PopupGlassState s) {
			agent = p;
			state = s;
		}
	}

	public ConveyorAgent(String name, TransducerIfc t, int myIndex, Sensor ss, Sensor se) {
		super(name, t);
		startSensor = ss;
		endSensor = se;
		transducer.register(this, TChannel.CONVEYOR);
		transducer.register(this, TChannel.SENSOR);
		glass = new LinkedList<Glass>();
		state = ConveyorState.ON_POS_FREE;
		
		transducerPowerConveyor(true);
	}

	// ***** MESSAGES ***** //

	/**
	 * From popup to conveyor
	 */
	public void msgPassMeGlass(Popup p) {
		if (state == ConveyorState.OFF) {
			transducerPowerConveyor(true);
		}
		popup.state = PopupGlassState.PASS_ME_GLASS;
		stateChanged();
	}

	/**
	 * From CF to conveyor
	 * 
	 * @param g
	 *            - the glass that is passed to the conveyor
	 */
	public void msgHereIsGlass(Glass g, OfflineConveyorFamily c) {
		state = ConveyorState.ON_RECEIVING;
		glassToReceive = g;
		stateChanged();
	}

	// ***** SCHEDULER ***** //
	@Override
	public boolean pickAndExecuteAnAction() {

		if (popup.state == PopupGlassState.PASS_ME_GLASS) {
			if (state == ConveyorState.OFF) {
				transducerPowerConveyor(true);
			}
			actGiveGlassToPopup();
			return true;
		}

		if (state == ConveyorState.ON_RECEIVING) {
			if (glassToReceive != null) {
				actReceiveGlass();
				return true;
			}
		}

		return false;
	}

	// ***** ACTIONS ***** //

	private void actReceiveGlass() {
		if (!glass.contains(glassToReceive)) {
			glass.add(glassToReceive);
			//glassToReceive = null;
		} else {
			System.out.println("Glass already added");
		}
		
		if (glass.size() < MAX_NUM) {
			state = ConveyorState.ON_POS_FREE;
			sendPositionFree();
		} else {
			state = ConveyorState.OFF;
			transducerPowerConveyor(false);
		}
		stateChanged();
	}

	private void actGiveGlassToPopup() {
		transducerPowerConveyor(true);		
		popup.agent.msgHereIsGlass(glass.remove(), this);
		popup.state = PopupGlassState.NO_ACTION;
		state = ConveyorState.ON_POS_FREE;
		sendPositionFree();
		stateChanged();
	}
	
	// ***** PRIVATE HELPER METHODS ***** //

	private void sendPositionFree() {
		if (glass.size() < MAX_NUM) {
			family.conveyorPositionFree();
		}
	}
	
	private void transducerPowerConveyor(boolean start) {
		Object[] args = new Object[1];
		args[0] = myIndex;
		if (start) {
			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
		} else {
			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
		}
	}

	// ***** ACCESSORS & MUTATORS ***** //
/*
	public void setSensors(StartSensorAgent ss, EndSensorAgent se) {
		sensorStart = ss;
		sensorEnd = se;
	}*/
	
	public void setPopup(Popup p) {
		popup = new MyPopup(p, PopupGlassState.NO_ACTION);
	}

	public void setConveyorFamily(OfflineConveyorFamily cf) {
		family = cf;
		sendPositionFree();
	}

	public ConveyorState getState() {
		return state;
	}
	
	@Override
	public int getIndex() {
		return myIndex;
	}
	
	public Sensor getEndSensor() {
		return endSensor;
	}
	
	public Sensor getStartSensor() {
		return startSensor;
	}
	
	public LinkedList<Glass> getGlassList() {
		return glass;
	}
	
	public OfflineConveyorFamily getFamily() {
		return family;
	}
	
	public Glass getGlassToReceive() {
		return glassToReceive;
	}
	
	public PopupGlassState getPopupGlassState() {
		return popup.state;
	}
	
	public PopupState getPopupPositionState() {
		return popup.pos;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (channel.equals(TChannel.CONVEYOR)) {
			if (event.equals(TEvent.CONVEYOR_DO_START)) {
				if ((glass.size() < MAX_NUM) || popup.state == PopupGlassState.PASS_ME_GLASS) {
					state = ConveyorState.ON_POS_FREE;
					sendPositionFree();
				} else {
					state = ConveyorState.OFF;
				}
			}
			if (event.equals(TEvent.CONVEYOR_DO_STOP)) {
				state = ConveyorState.OFF;
			}
		}
		
		if (channel.equals(TChannel.SENSOR)) {
			if (event.equals(TEvent.SENSOR_GUI_PRESSED)) {
				if (startSensor.getIndex() == (Integer) args[0]) {
					startSensor.setPressed(true);
					//System.out.println("start sensor pressed.");
				} else if (endSensor.getIndex() == (Integer) args[0]) {
					endSensor.setPressed(true);
					if ((glass.size() > 0) && (popup.state == PopupGlassState.NO_ACTION)) {
						popup.agent.msgIHaveGlass(glass.get(0), this);
						popup.state = PopupGlassState.OFFERED_GLASS;
					}
				}
			}
			
			if (event.equals(TEvent.SENSOR_GUI_RELEASED)) {
				if (startSensor.getIndex() == (Integer) args[0]) {
					startSensor.setPressed(false);
				} else if (endSensor.getIndex() == (Integer) args[0]) {
					endSensor.setPressed(false);
				}
			}
		}
	}

}
