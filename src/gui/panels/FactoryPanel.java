package gui.panels;

import engine.agent.BinRobotAgent;
import engine.agent.SmallOnlineConveyorFamilyImp;
import gui.drivers.FactoryFrame;
import gui.test.DavidsOfflineCFIntegrationTest;

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
	private enum RunMode{ OFFLINE_CF_TEST, FINAL_SUBMISSION }
	private static final RunMode RUN_MODE = RunMode.FINAL_SUBMISSION;
	
	/** The frame connected to the FactoryPanel */
	private FactoryFrame parent;

	/** The control system for the factory, displayed on right */
	private ControlPanel cPanel;

	/** The graphical representation for the factory, displayed on left */
	private DisplayPanel dPanel;

	/** Allows the control panel to communicate with the back end and give commands */
	private Transducer transducer;

	/**
	 * THE AGENTS
	 */
	// Initial robot agent
	private BinRobotAgent binRobot;

	/* ConveyorFamilies & accompanying workstation */
//	// Cutter
//	private BigOnlineConveyorFamily cutterFamily;
	private SmallOnlineConveyorFamilyImp cutterFamily; // temp

	// Breakout
	private SmallOnlineConveyorFamilyImp breakoutFamily;
//
//	// Manual Breakout
//	private BigOnlineConveyorFamily manualBreakoutFamily;
//
//	// Drill - Evan's
//	private OfflineWorkstationAgent drillWorkstation;
//	private ConveyorFamilyImplementation drillFamily;
//
//	// CrossSeamer - Tim's
//	private OfflineWorkstationAgent crossSeamerWorkstation;
//	private ConveyorFamilyImp crossSeamerFamily;
//
	// Grinder - David's
//	private OfflineWorkstationAgent grinderWorkstation;
//	private ConveyorFamilyEntity grinderFamily;
//
//	// Washer
//	private BigOnlineConveyorFamily washerFamily;
//
//	// Painter
//	private SmallOnlineConveyorFamilyImp painterFamily;
//
//	// UV Lamp
//	private BigOnlineConveyorFamily lampFamily;
//
//	// Oven
//	private BigOnlineConveyorFamily ovenFamily;

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
		// TODO initialize and start Agent threads here
		// ===========================================================================

		if (RUN_MODE == RunMode.FINAL_SUBMISSION) {
			// Initial robot that has the glasses
			binRobot = new BinRobotAgent("Bin Robot", transducer);
	
			// Cutter
	//		BigOnlineConveyorFamily cutterFamily = new BigOnlineConveyorFamily(..);
			cutterFamily = new SmallOnlineConveyorFamilyImp(MachineType.CUTTER, transducer, 0);
			
			// Breakout
			breakoutFamily = new SmallOnlineConveyorFamilyImp(MachineType.BREAKOUT, transducer, 1);
			
			// Connect them!
			binRobot.setNextLineComponent(cutterFamily);
			cutterFamily.setPreviousLineComponent(binRobot);
			
			cutterFamily.setNextLineComponent(breakoutFamily);
			breakoutFamily.setPreviousLineComponent(cutterFamily);
			
			// Set things in motion!
			createGlassesAndRun();
		} else if (RUN_MODE == RunMode.OFFLINE_CF_TEST) {
			System.err.println("Running in OFFLINE TEST MODE");
			DavidsOfflineCFIntegrationTest dTest = new DavidsOfflineCFIntegrationTest(transducer);
		}
		
		System.out.println("Backend initialization finished.");
	}

	/**
	 * Create glasses of various types and run
	 */
	private void createGlassesAndRun() {
		// Create some glasses to be run through the glassline, and give them to the initial robot (the bin robot)
		List<Glass> glasses = new ArrayList<Glass>();
		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, MachineType.GRINDER }));
		binRobot.seedGlasses(glasses);
		
		startAgentThreads();
	}
	
	private void startAgentThreads() {
		binRobot.startThread();
		cutterFamily.startThreads();
		breakoutFamily.startThreads();
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
