package gui.test;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

/** 
 * Class to reliably test offline cf integrated with the animation
 * Turns conveyors leading up to cf all on, makes some glass move along. All three offline cfs are made as my own version of the cf. 
 * @author David Zhang
 */
public class DavidsOfflineCFIntegrationTest extends GuiTestSM {
	public DavidsOfflineCFIntegrationTest(Transducer trans) {
		startInitialConveyors();
		prepareAgents();

		super(trans); // fires bin creation
	}

	// Start conveyors leading up to offline cf
	private void startInitialConveyors() {
		// TODO: in eventFired?
	}

	private void prepareAgents() {
		// Prepare agents
		OfflineWorkstationAgent grinderWks1 = new OfflineWorkstationAgent("Grinder workstation 1", MachineType.GRINDER, 0, transducer);
		OfflineWorkstationAgent grinderWks2 = new OfflineWorkstationAgent("Grinder workstation 2", MachineType.GRINDER, 1, transducer);
		
		ConveyorFamilyEntity grinderFamily = new ConveyorFamilyEntity(transducer, 5, 0, grinderWks1, grinderWks2);
		
		// Make sure all conveyors leading up to my first conveyor family are on and just passing the glass along
		// TODO
		
		// Make sure the last one sends msgHereIsGlass
		// TODO
		
		// Start agent threads
		grinderWks1.startThread();
		grinderWks2.startThread();
		grinderFamily.startThreads();
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED) {
		}
	}
}

