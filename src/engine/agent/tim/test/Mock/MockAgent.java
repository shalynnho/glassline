/**
 * 
 */
package engine.agent.tim.test.Mock;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;

/**
 * This is the base class for a mock agent. It only defines that an agent should
 * contain a name.
 * 
 * @author Sean Turner
 * 
 */
public abstract class MockAgent extends Agent {
	protected String name;
	
	public EventLog log = new EventLog(); // Will use this in my MockAgents 

	public MockAgent(String name, Transducer transducer) {
		super(name, transducer);
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return this.getClass().getName() + ": " + name;
	}

	@Override
	public boolean pickAndExecuteAnAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public abstract void eventFired(TChannel channel, TEvent event, Object[] args);

}
