package shared.interfaces;

import shared.Glass;

public interface ConveyorFamily {
	public void msgHereIsGlass(Glass glass);
	public void msgPositionFree();
	public void msgGlassDone(Glass glass, int machineIndex);
}
