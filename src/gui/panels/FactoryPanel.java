package gui.panels;

import engine.agent.BigOnlineConveyorFamilyImp;
import engine.agent.BinRobotAgent;
import engine.agent.OfflineWorkstationAgent;
import engine.agent.SmallOnlineConveyorFamilyImp;
import engine.agent.david.misc.ConveyorFamilyEntity;
import engine.agent.evan.ConveyorFamilyImplementation;
import engine.agent.tim.misc.ConveyorFamilyImp;
import gui.drivers.FactoryFrame;
import gui.test.TimsOfflineCFIntegrationTest;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import shared.Glass;
import shared.enums.MachineType;
import shared.interfaces.OfflineWorkstation;
import transducer.Transducer;

/**
 * The FactoryPanel is highest level panel in the actual kitting cell. The FactoryPanel makes all the back end components, connects them to the GuiComponents in the DisplayPanel. It is responsible for
 * handling communication between the back and front end.
 */
@SuppressWarnings("serial")
public class FactoryPanel extends JPanel {
	private enum RunMode{ OFFLINE_CF_TEST, FINAL_SUBMISSION }
	private static final RunMode RUN_MODE = RunMode.OFFLINE_CF_TEST; // FINAL_SUBMISSION;
	
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
	// Cutter
	private BigOnlineConveyorFamilyImp cutterFamily;

	// Breakout
	private SmallOnlineConveyorFamilyImp breakoutFamily;

	// Manual Breakout
	private BigOnlineConveyorFamilyImp manualBreakoutFamily;

	// Drill - Evan's
	private OfflineWorkstationAgent drillWorkstation1;
	private OfflineWorkstationAgent drillWorkstation2;
	private ConveyorFamilyImplementation drillFamily;

	// CrossSeamer - Tim's
	private OfflineWorkstationAgent crossSeamerWorkstation1;
	private OfflineWorkstationAgent crossSeamerWorkstation2;
	private ConveyorFamilyImp crossSeamerFamily;

	// Grinder - David's
	private OfflineWorkstationAgent grinderWorkstation1;
	private OfflineWorkstationAgent grinderWorkstation2;
	private ConveyorFamilyEntity grinderFamily;

	// Washer
	private BigOnlineConveyorFamilyImp washerFamily;

	// Painter
	private SmallOnlineConveyorFamilyImp painterFamily;

	// UV Lamp
	private BigOnlineConveyorFamilyImp lampFamily;

	// Oven
	private BigOnlineConveyorFamilyImp ovenFamily;

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
		// Initialize and start Agent threads
		// ===========================================================================

		if (RUN_MODE == RunMode.FINAL_SUBMISSION) {
			/* Instantiate Agents */

			// Initial robot that has the glasses
			binRobot = new BinRobotAgent("Bin Robot", transducer);
			
			// Cutter
			cutterFamily = new BigOnlineConveyorFamilyImp(MachineType.CUTTER, transducer, 0);

			// Breakout
			breakoutFamily = new SmallOnlineConveyorFamilyImp(MachineType.BREAKOUT, transducer, 2);

			// Manual Breakout
			manualBreakoutFamily = new BigOnlineConveyorFamilyImp(MachineType.MANUAL_BREAKOUT, transducer, 3);

			// Drill - Evan's
			drillWorkstation1 = new OfflineWorkstationAgent("Drill workstation", MachineType.DRILL, 0, transducer);
			drillWorkstation2 = new OfflineWorkstationAgent("Drill workstation", MachineType.DRILL, 1, transducer);
			drillFamily = new ConveyorFamilyImplementation(transducer, new OfflineWorkstation[]{ drillWorkstation1, drillWorkstation2 }, MachineType.DRILL, 5, 0);
			// Evan, please check this / wrap up your family setup

			// CrossSeamer - Tim's
			crossSeamerWorkstation1 = new OfflineWorkstationAgent("CrossSeamer workstation", MachineType.CROSS_SEAMER, 0, transducer);
			crossSeamerWorkstation2 = new OfflineWorkstationAgent("CrossSeamer workstation", MachineType.CROSS_SEAMER, 1, transducer);
			// crossSeamerFamily = new ConveyorFamilyImp(...) 
			// Tim, please finish this part with however you like to setup your family

			// Grinder - David's
			grinderWorkstation1 = new OfflineWorkstationAgent("Grinder workstation", MachineType.GRINDER, 0, transducer);
			grinderWorkstation2 = new OfflineWorkstationAgent("Grinder workstation", MachineType.GRINDER, 1, transducer);
			grinderFamily = new ConveyorFamilyEntity(transducer, 7, 2, grinderWorkstation1, grinderWorkstation2);
			grinderWorkstation1.setPopupWorkstationInteraction(grinderFamily.popup);
			grinderWorkstation2.setPopupWorkstationInteraction(grinderFamily.popup);

			// Washer
			washerFamily = new BigOnlineConveyorFamilyImp(MachineType.WASHER, transducer, 8);

			// Painter
			painterFamily = new SmallOnlineConveyorFamilyImp(MachineType.PAINT, transducer, 10);

			// UV Lamp
			lampFamily = new BigOnlineConveyorFamilyImp(MachineType.UV_LAMP, transducer, 11);

			// Oven
			ovenFamily = new BigOnlineConveyorFamilyImp(MachineType.OVEN, transducer, 13);
			
			/* Connect Agents */

			// TODO


			// Set things in motion!
			createGlassesAndRun();
		} else if (RUN_MODE == RunMode.OFFLINE_CF_TEST) {
			System.err.println("Running in OFFLINE TEST MODE");
			TimsOfflineCFIntegrationTest tTest = new TimsOfflineCFIntegrationTest(transducer);
			//DavidsOfflineCFIntegrationTest dTest = new DavidsOfflineCFIntegrationTest(transducer);
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

		// TODO
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
