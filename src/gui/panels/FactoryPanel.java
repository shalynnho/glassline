package gui.panels;

import engine.agent.BinRobotAgent;
import gui.drivers.FactoryFrame;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import shared.Glass;
import shared.enums.MachineType;
import transducer.Transducer;

/**
 * The FactoryPanel is highest level panel in the actual kitting cell. The FactoryPanel makes all the back end components, connects them to the GuiComponents in the DisplayPanel. It is responsible for
 * handling communication between the back and front end.
 */
@SuppressWarnings("serial")
public class FactoryPanel extends JPanel {
	/** The frame connected to the FactoryPanel */
	private FactoryFrame parent;

	/** The control system for the factory, displayed on right */
	private ControlPanel cPanel;

	/** The graphical representation for the factory, displayed on left */
	private DisplayPanel dPanel;

	/** Allows the control panel to communicate with the back end and give commands */
	private Transducer transducer;

	/**
	 * Constructor links this panel to its frame
	 */
	public FactoryPanel(FactoryFrame fFrame) {
		parent = fFrame;

		// initialize transducer
		transducer = new Transducer();
		transducer.startTransducer();

		// use default layout
		// dPanel = new DisplayPanel(this);
		// dPanel.setDefaultLayout();
		// dPanel.setTimerListeners();

		// initialize and run
		this.initialize();
		this.initializeBackEnd();
		this.createGlassesAndRun();
	}

	/**
	 * Initializes all elements of the front end, including the panels, and lays them out
	 */
	private void initialize() {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		// initialize control panel
		cPanel = new ControlPanel(this, transducer);

		// initialize display panel
		dPanel = new DisplayPanel(this, transducer); // gui popups and such in here

		// add panels in
		// JPanel tempPanel = new JPanel();
		// tempPanel.setPreferredSize(new Dimension(830, 880));
		// this.add(tempPanel);

		this.add(dPanel);
		this.add(cPanel);
	}

	/**
	 * Feel free to use this method to start all the Agent threads at the same time
	 */
	private void initializeBackEnd() {
		// ===========================================================================
		// TODO initialize asnd start Agent threads here
		// ===========================================================================

		// Initial robot that has the glasses
		BinRobotAgent binRobot = new BinRobotAgent();
		
		// Create some glasses to be run through the glassline, and give them to the initial robot
		List<Glass> glasses = new ArrayList<Glass>();
		glasses.add(new Glass(new MachineType[]{MachineType.CUTTER, MachineType.BREAKOUT, MachineType.CROSS_SEAMER }));
		binRobot.seedGlasses(glasses);
		
		// TODO: connect with our conveyor families
		// David's
		// WorkstationAgent breakoutWorkstation = new WorkstationAgent("Breakout", MachineType.BREAKOUT, 1, transducer);
		// SmallOnlineConveyorFamily breakoutFamily = new SmallOnlineConveyorFamily(2, breakoutWorkstation);

//		ConveyorFamilyEntity davidConvFamily = new ConveyorFamilyEntity(transducer, 7, 2, Workstation workstation1, Workstation workstation2);
//		davidConvFamily.setPrevConveyorFamily(f1);
//		davidConvFamily.setNextConveyorFamily(f);

		System.out.println("Back end initialization finished.");
	}

	/**
	 * Create glasses of various types and run
	 */
	private void createGlassesAndRun() {
		
	}
	
	/**
	 * Returns the parent frame of this panel
	 * 
	 * @return the parent frame
	 */
	public FactoryFrame getGuiParent() {
		return parent;
	}

	/**
	 * Returns the control panel
	 * 
	 * @return the control panel
	 */
	public ControlPanel getControlPanel() {
		return cPanel;
	}

	/**
	 * Returns the display panel
	 * 
	 * @return the display panel
	 */
	public DisplayPanel getDisplayPanel() {
		return dPanel;
	}
}
