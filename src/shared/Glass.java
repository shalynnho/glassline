package shared;

import java.util.*;

/**
 * @author Evan Brown
 */
public class Glass {
	private final int id;                // the identifier of this piece of glass
	private static int currentID = 0;    // a count of how many pieces of glass have been created
	
	// a map from the machine ID string to whether or not the glass needs to be processed by that machine
	private Map<String, Boolean> recipe;
	
	// the set of all machine ID strings
	private static final String machineIDs[] = {"NCCutter", "CrossSeamer", "Grinder", "Drill", "Washer", "Painter", "UV", "Oven"};
	
	/**
	 * Glass constructor. Gives glass a unique id based on how many pieces of glass have been created so far.
	 * Creates recipe map by adding true for machine IDs in the argument array and false for all other machine IDs.
	 * @param r An array of strings containing which machines the glass should be processed by.
	 */
	public Glass(String[] r) {
		id = ++currentID;
		recipe = new HashMap<String, Boolean>();
		
		for (String machID : machineIDs) {
			boolean needsProc = false; // does this glass need to be processed by this machine?
			
			// if the string machID is in the recipe array argument r, then set needsProc to true
			for (String recID : r) {
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
		this(new String[0]);
	}
	
	// Turn ArrayList into array and call other constructor
	public Glass(List<String> recipe) {
		this(recipe.toArray(new String[recipe.size()]));
	}
	
	/* Getters */
	public boolean getRecipe(String machID) {
		return recipe.get(machID);
	}
	
	public int getID() {
		return id;
	}
	
	public static int getCurrentID() {
		return currentID;
	}
	
	// test constructor and getters
	public static void main(String[] args) {
		Glass g[] = new Glass[4];
		g[0] = new Glass();
		
		String rec[] = {"NCCutter"};
		g[1] = new Glass(rec);
		
		String rec1[] = {"Grinder", "Drill", "Washer"};
		g[2] = new Glass(rec1);
		
		List<String> rec2 = new ArrayList<String>();
		rec2.add("Painter");
		rec2.add("CrossSeamer");
		g[3] = new Glass(rec2);
		
		System.out.println("Num Glass Created: " + Glass.getCurrentID());
		
		for (int i = 0; i < g.length; ++i) {
			System.out.println("Glass " + g[i].getID() + " Recipe:");
			
			for (String machID : machineIDs)
				System.out.println(machID + ": " + g[i].getRecipe(machID));
			
			System.out.println();
		}
	}
}
