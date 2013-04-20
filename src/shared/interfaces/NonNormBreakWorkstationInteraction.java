package shared.interfaces;

public interface NonNormBreakWorkstationInteraction { // Will be used for popUp offline workstation breaks to figure out which machine is currently broken
	public abstract void msgGUIBreakWorkstation(boolean stop, int machineIndex); 
}
