package engine.agent.tim.misc;

import shared.Glass;

public class MyGlassSensor {
	public enum onSensor {justEntered, yes, no}; // Is the glass on an given sensor?
	public enum location {entry, popup, exit}; // Which sensor the glass is currently on – this will not be needed if using the multiple inheritance design paradigm
	
	public Glass glass; // Holds a reference to the glass
	public onSensor onSensor; 
	public location location; 
	
	public MyGlassSensor(Glass glass, location location, onSensor onSensor) {
		this.glass = glass;
		this.location = location;
		this.onSensor = onSensor;		
	}	
}
