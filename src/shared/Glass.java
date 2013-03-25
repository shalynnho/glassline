package shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shared.enums.MachineType;

public class Glass {
	private final int id; // the identifier of this piece of glass
	private static int currentID = 0; // a count of how many pieces of glass have been created; also used to dynamically determine 'id' upon constructor call

	// a map from the machine ID string to whether or not the glass needs to be processed by that machine
	private Map<MachineType, Boolean> recipe;

	// the set of all possible machine types
	private static final MachineType machineIDs[] = { MachineType.CUTTER, MachineType.CROSS_SEAMER, MachineType.GRINDER, MachineType.DRILL, MachineType.WASHER, MachineType.PAINT, MachineType.UV_LAMP, MachineType.OVEN };

	/**
	 * Glass constructor. Gives glass a unique id based on how many pieces of glass have been created so far. Creates recipe map by adding true for machine IDs in the argument array and false for all
	 * other machine IDs.
	 * 
	 * @param r An array of strings containing which machines the glass should be processed by.
	 */
	public Glass(MachineType[] r) {
		id = ++currentID;
		recipe = new HashMap<MachineType, Boolean>();

		for (MachineType machID : machineIDs) {
			boolean needsProc = false; // does this glass need to be processed by this machine?

			// if the string machID is in the recipe array argument r, then set needsProc to true
			for (MachineType recID : r) {
				if (machID.equals(recID)) {
					needsProc = true;
					break; // break out of inner for loop and skip to put statement
				}
			}

			recipe.put(machID, needsProc);
		}
	}

	// Call other constructor with empty array
	public Glass() {
		this(new MachineType[0]);
	}

	// Turn ArrayList into array and call other constructor
	public Glass(List<MachineType> recipe) {
		this(recipe.toArray(new MachineType[recipe.size()]));
	}

	/* Getters */
	public boolean getNeedsProcessing(MachineType machID) {
		return recipe.get(machID);
	}

	public int getID() {
		return id;
	}

	public static int getCurrentID() {
		return currentID;
	}

	// Quick test
	public static void main(String[] args) {
		Glass g[] = new Glass[4];
		
		g[0] = new Glass(); // first glass should have no machines 

		MachineType rec[] = { MachineType.CUTTER };
		g[1] = new Glass(rec);

		MachineType rec1[] = { MachineType.GRINDER, MachineType.DRILL, MachineType.WASHER };
		g[2] = new Glass(rec1);

		List<MachineType> rec2 = new ArrayList<MachineType>();
		rec2.add(MachineType.PAINT);
		rec2.add(MachineType.CROSS_SEAMER);
		g[3] = new Glass(rec2);

		System.out.println("Num Glass Created: " + Glass.getCurrentID());

		for (int i = 0; i < g.length; ++i) {
			System.out.println("Glass " + g[i].getID() + " Recipe:");

			for (MachineType machID : machineIDs)
				System.out.println(machID + ": " + g[i].getNeedsProcessing(machID));

			System.out.println();
		}
	}
}
