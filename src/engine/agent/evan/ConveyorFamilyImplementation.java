package engine.agent.evan;

import engine.agent.evan.interfaces.*;
import shared.interfaces.*;
import shared.Glass;
import shared.enums.MachineType;
import transducer.Transducer;

public class ConveyorFamilyImplementation implements ConveyorFamily {
	// *** DATA ***
	
	private ConveyorAgent c;
	private PopupAgent p;
	
	public ConveyorFamilyImplementation(ConveyorFamily prev, ConveyorFamily next, Transducer t,
			WorkStation mach[], MachineType mt, int cid, int pid) {
		c = new ConveyorAgent("c" + cid, prev, p, t, cid);
		p = new PopupAgent("p" + pid, next, c, mach, mt, t, pid);
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
}
