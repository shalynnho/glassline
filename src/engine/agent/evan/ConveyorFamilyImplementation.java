package engine.agent.evan;

import shared.interfaces.*;
import shared.Glass;
import shared.enums.MachineType;
import transducer.Transducer;

public class ConveyorFamilyImplementation implements OfflineConveyorFamily {
	// *** DATA ***
	
	private ConveyorAgent c;
	private PopupAgent p;
	
	public ConveyorFamilyImplementation(ConveyorAgent con, PopupAgent pop) {
		c = con;
		p = pop;
		c.setPopup(p);
	}
	
	// *** MESSAGES ***
	
	public void msgHereIsGlass(Glass g) {
		c.msgHereIsGlass(g);
	}
	
	public void msgPositionFree() {
		p.msgPositionFree();
	}
	
	public void msgGlassDone(Glass g, int index) {
		p.msgGlassDone(g, index);
	}
	
	// *** EXTRA ***
	public void startThreads() {
		c.startThread();
		p.startThread();
	}
	
	/* Setters */
	public void setPreviousLineComponent(LineComponent lc) {
		c.setPrev(lc);
	}
	
	public void setNextLineComponent(LineComponent lc) {
		p.setNext(lc);
	}
	
	public void msgGUIBreakWorkstation(boolean stop, int index) {}
	public void msgGUIBreakRemovedGlassFromWorkstation(int index) {}
	public void msgGUIBreak(boolean stop) {}
}
