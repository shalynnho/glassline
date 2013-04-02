package engine.agent.shay;

import shared.enums.SensorPosition;
import engine.agent.shay.interfaces.Conveyor;

public class Sensor {

	private boolean pressed = false;
	private SensorPosition position;
	private int myIndex;
	private Conveyor conveyor;
	
	public Sensor(SensorPosition p, int index, Conveyor c) {
		position = p;
		myIndex = index;
		conveyor = c;
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
}
