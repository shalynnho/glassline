package gui.components;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import shared.ImageIcons;
import shared.enums.MachineType;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

/**
 * GUIComponentoffline is the superclass of GUI components off the conveyor
 */
@SuppressWarnings("serial")
public class GUIComponentOffline extends GuiAnimationComponent implements ActionListener, Serializable {
	/**
	 * The popup for the offline component
	 */

	GUIPopUp myPopUp;

	MachineType type;

	Integer index;

	Integer popUpIndex;

	TChannel channel;

	/**
	 * Frame counter
	 */
	int counter = 0;
	int animationCount = 0;
	int breakCount = 0;	int speed = 1;
	boolean breakGlass = false;

	/**
	 * List of icons for animations
	 */
	ArrayList<ImageIcon> imageicons = new ArrayList<ImageIcon>();
	
	/**
	 * Boolean to make it so when a piece of glass is stuck on the workstation that the WORKSTATION_LOAD_FINISHED is only sent once 
	 */
	private boolean glassSentOnLoad = false;

	/**
	 * Constructor for GUIComponentOffline
	 */
	public GUIComponentOffline(MachineType type, Transducer t) {
		super();
		transducer = t;
		this.type = type;
		initializeImages();
	}

	/**
	 * Method that initializes the imageicons for the specific machines based on the MachineType enum
	 */
	public void initializeImages() {
		if (type == MachineType.CROSS_SEAMER) {
			imageicons = (ArrayList<ImageIcon>) ImageIcons.getIconList("crossSeamer");
			channel = TChannel.CROSS_SEAMER;
			transducer.register(this, TChannel.CROSS_SEAMER);
		}

		else if (type == MachineType.DRILL) {
			imageicons = (ArrayList<ImageIcon>) ImageIcons.getIconList("drill");
			channel = TChannel.DRILL;
			transducer.register(this, TChannel.DRILL);

		} else if (type == MachineType.GRINDER) {
			imageicons = (ArrayList<ImageIcon>) ImageIcons.getIconList("grinder");
			channel = TChannel.GRINDER;
			transducer.register(this, TChannel.GRINDER);
		}
		setIcon(imageicons.get(0));
		setSize(getIcon().getIconWidth(), getIcon().getIconHeight());
	}

	/**
	 * Method that does the machine animation
	 */
	public void doAnimate() {
		if (counter < imageicons.size() && animationCount < (speed * imageicons.size())) {
			setIcon(imageicons.get(counter));
			counter++;
			animationCount++;
			if (counter == imageicons.size()) {
				counter = 0;
			}
		} else {

			setIcon(imageicons.get(0));
			counter = 0;
			animationCount = 0;

			Object[] args = new Object[1];
			args[0] = index;
			animationState = GuiAnimationComponent.AnimationState.DONE;
			transducer.fireEvent(channel, TEvent.WORKSTATION_GUI_ACTION_FINISHED, args);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (animationState.equals(AnimationState.MOVING)) {
			if (part != null) {
				movePartIn();
			}
		}
		if (animationState.equals(AnimationState.ANIMATING)) {
			doAnimate();
		}
		if (animationState.equals(AnimationState.BREAKING)) {
//			System.out.println("CALLING DOBREAKING()");
			doBreaking();
		}
	}
	
	public void breakGlass(boolean breakGlass) {
		this.breakGlass = breakGlass;
	}

	@Override
	public void addPart(GUIGlass part) {
		this.part = part;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	public MachineType getType() {
		return type;
	}

	public void paint(Graphics g) {
		super.paint(g);
	}

	private void movePartIn() {
		if (part.getCenterX() < getCenterX())
			part.setCenterLocation(part.getCenterX() + 1, part.getCenterY());
		else if (part.getCenterX() > getCenterX())
			part.setCenterLocation(part.getCenterX() - 1, part.getCenterY());

		if (part.getCenterY() < getCenterY())
			part.setCenterLocation(part.getCenterX(), part.getCenterY() + 1);
		else if (part.getCenterY() > getCenterY())
			part.setCenterLocation(part.getCenterX(), part.getCenterY() - 1);

		if (part.getCenterX() == getCenterX() && part.getCenterY() == getCenterY() && !glassSentOnLoad) {
			Object[] args = new Object[1];
			args[0] = index;
			transducer.fireEvent(channel, TEvent.WORKSTATION_LOAD_FINISHED, args);
			glassSentOnLoad = true; // While part is on WS, leave this as true
			if (breakGlass) {
				animationState = AnimationState.BREAKING;
				part.msgPartBroken();
			}
		}
		else if (!(part.getCenterX() == getCenterX() && part.getCenterY() == getCenterY())) {
			glassSentOnLoad = false; // Set this to false as soon as the part leaves, so the next one can be processed
		}
	}
	
	private void doBreaking() {
		if (breakCount < 15) {
			breakCount++;
		}
		
		if (breakCount == 15) {
			System.out.println("REMOVING BROKEN GLASS");
			parent.getActivePieces();
			parent.getParent().getGuiParent().getTimer().removeActionListener(part);
			parent.remove(part);
			breakCount = 0;
			animationState = AnimationState.IDLE;
		}
	}
	
	/**
	 * Sets the speed of processing for this workstation.
	 * @param s the speed of processing, higher number is slower
	 */
	public void setSpeed(int s) {
		speed = s;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (((Integer) args[0]).equals(index)) {
			if (event == TEvent.WORKSTATION_DO_ACTION && !breakGlass) {
				animationState = AnimationState.ANIMATING;
				return;
			}
			if (event == TEvent.WORKSTATION_DO_LOAD_GLASS) {
				animationState = AnimationState.MOVING;
				return;
			}
			if (event == TEvent.WORKSTATION_RELEASE_GLASS) {
				// added by monroe
				// animationState = AnimationState.DONE;
				this.transducer.fireEvent(this.channel, TEvent.WORKSTATION_RELEASE_FINISHED, args);
				animationState = AnimationState.IDLE;
				// above added by monroe

				nextComponent.addPart(part);
				return;
			}
		}
	}
}
