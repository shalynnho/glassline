package gui.panels;

import engine.agent.BinRobotAgent;
import engine.agent.SmallOnlineConveyorFamily;
import engine.agent.david.misc.ConveyorFamilyEntity;
import engine.agent.evan.ConveyorFamilyImplementation;
import engine.agent.tim.misc.ConveyorFamilyImp;
import gui.drivers.FactoryFrame;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import shared.Glass;
import shared.agents.OfflineWorkstationAgent;
import shared.agents.OnlineWorkstationAgent;
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
	 * THE AGENTS
	 */
	// Initial robot agent
	private BinRobotAgent binRobot;

	/* ConveyorFamilies & accompanying workstation */
	// Cutter
//	private OnlineWorkstationAgent cutterWorkstation;
//	private BigOnlineConveyorFamily cutterFamily;
//
//	// Breakout
//	private OnlineWorkstationAgent breakoutWorkstation;
//	private SmallOnlineConveyorFamily breakoutFamily;
//
//	// Manual Breakout
//	private OnlineWorkstationAgent manualBreakoutWorkstation;
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
//	// Grinder - David's
//	private OfflineWorkstationAgent grinderWorkstation;
//	private ConveyorFamilyEntity grinderFamily;
//
//	// Washer
//	private OnlineWorkstationAgent washerWorkstation;
//	private BigOnlineConveyorFamily washerFamily;
//
//	// Painter
//	private OnlineWorkstationAgent painterWorkstation;
//	private SmallOnlineConveyorFamily painterFamily;
//
//	// UV Lamp
//	private OnlineWorkstationAgent lampWorkstation;
//	private BigOnlineConveyorFamily lampFamily;
//
//	// Oven
//	private OnlineWorkstationAgent ovenWorkstation;
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
		// TODO initialize and start Agent threads here
		// ===========================================================================

		// Initial robot that has the glasses
		binRobot = new BinRobotAgent();

		// Cutter
//		cutterWorkstation = new OnlineWorkstationAgent("Cutter wks", MachineType.CUTTER, transducer);
//		BigOnlineConveyorFamily cutterFamily;

//		// Breakout
//		breakoutWorkstation = new WorkstationAgent("Breakout wks", MachineType.BREAKOUT, 1, transducer);
//		SmallOnlineConveyorFamily breakoutFamily;
//
//		// Manual Breakout
//		 WorkstationAgent manualBreakoutWorkstation;
//		 BigOnlineConveyorFamily manualBreakoutFamily;
//
//		// Drill - Evan's
//		WorkstationAgent drillWorkstation;
//		ConveyorFamilyImplementation drillFamily;
//
//		// CrossSeamer - Tim's
//		WorkstationAgent crossSeamerWorkstation;
//		ConveyorFamilyImp crossSeamerFamily;
//
//		// Grinder - David's
//		grinderWorkstation;
//		 grinderFamily = new ConveyorFamilyEntity(transducer, 7, 2, Workstation workstation1, Workstation workstation2);
//		 grinderFamily.setPrevConveyorFamily(f1);
//		 grinderFamily.setNextConveyorFamily(f);
//
//		// Washer
//		 WorkstationAgent washerWorkstation;
//		 BigOnlineConveyorFamily washerFamily;
//
//		// Painter
////		WorkstationAgent painterWorkstation;
////		SmallOnlineConveyorFamily painterFamily;
//
//		// UV Lamp
//		 WorkstationAgent lampWorkstation;
//		 BigOnlineConveyorFamily lampFamily;
//
//		// Oven
//		 WorkstationAgent ovenWorkstation;
//		 BigOnlineConveyorFamily ovenFamily;

		System.out.println("Backend initialization finished.");
	}

	/**
	 * Create glasses of various types and run
	 */
	private void createGlassesAndRun() {
		// Create some glasses to be run through the glassline, and give them to the initial robot (the bin robot)
		List<Glass> glasses = new ArrayList<Glass>();
		glasses.add(new Glass(new MachineType[] { MachineType.CUTTER, MachineType.BREAKOUT, MachineType.CROSS_SEAMER }));
		binRobot.seedGlasses(glasses);
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
