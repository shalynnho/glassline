package engine.agent.shay;

import shared.enums.SensorPosition;
import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;
import engine.agent.shay.interfaces.Conveyor;
import engine.agent.shay.interfaces.TransducerIfc;

public class Sensor implements TReceiver {

	private boolean pressed = false;
	private SensorPosition position;
	private int myIndex;
	private Conveyor conveyor;
	private Transducer transducer;
	private TChannel channel = TChannel.SENSOR;
	
	public Sensor(SensorPosition p, int index, Conveyor c, TransducerIfc t) {
		position = p;
		myIndex = index;
		conveyor = c;
		transducer = (Transducer) t;
		transducer.register(this, channel);
	}
	
	public void setPressed(boolean p) {
		pressed = p;
	}
	
	public boolean getPressed() {
		return pressed;
	}
	
	public int getIndex() {
		return myIndex;
	}
	
	public SensorPosition getPosition() {
		return position;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (channel == this.channel && event == TEvent.SENSOR_GUI_PRESSED && args[0] == (Integer)myIndex) {
			pressed = true;
		} else if (channel == this.channel && event == TEvent.SENSOR_GUI_RELEASED && args[0] == (Integer)myIndex) {
			pressed = false;
		}
	}
}
