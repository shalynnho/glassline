package engine.agent.evan.test;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.Semaphore;

import junit.framework.TestCase;
import org.junit.Test;

import shared.Glass;
import transducer.*;
import engine.agent.evan.*;
import engine.agent.evan.test.mock.*;

public class ConveyorAgentUnitTest extends TestCase {
	
	//the agent to be tested
	ConveyorAgent conveyor;
	
	/* This test makes sure that the conveyor agent can handle 50 consecutive pieces of glass coming through. */
	public void testConsecutiveGlass() {
		MockConveyorFamily cf = new MockConveyorFamily("cf1");
		MockPopup p = new MockPopup("p1");
		Transducer t = new Transducer();
		t.startTransducer();
		
		// for waiting for the transducer
		Timer timer = new Timer();
		Semaphore sem = new Semaphore(0);
		
		int id = 100, frontSensorID = id * 2, backSensorID = frontSensorID + 1;
		// create arguments for transducer events
		Integer[] idArgs = {id}, frontSensorArgs = {frontSensorID}, backSensorArgs = {backSensorID};
		
		// channels for mock animation to listen to
		TChannel[] channels = {TChannel.CONVEYOR};
		MockAnimation anim = new MockAnimation("anim1", t, channels);
		
		conveyor = new ConveyorAgent("c" + id, cf, p, t, id);
		
		// when conveyor is constructed it tells previous CF it is open
		assertTrue(
				"Mock CF should have been told conveyor is open. Event log: "
						+ cf.log.toString(), cf.log
						.containsString("Received message msgPositionFree."));
		assertEquals(
				"Only 1 message should have been sent to the CF. Event log: "
						+ cf.log.toString(), 1, cf.log.size());
		cf.log.clear();
		
		// create new pieces of glass (conveyor does no processing so recipes don't matter)
		int numGlass = 50;
		Glass[] g = new Glass[numGlass];
		for (int i = 0; i < numGlass; ++i)
			g[i] = new Glass();
		
		int waitTime = 10; // ms to wait for transducer to fire events
		
		// test <numglass> pieces of glass consecutively
		for (int i = 0; i < numGlass; ++i) {
			conveyor.msgHereIsGlass(g[i]); // gs == pending
			conveyor.pickAndExecuteAnAction(); // nothing should happen; if it does something, test will fail later
			
			t.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, frontSensorArgs); // gs == arrived
			wait(timer, sem, waitTime);
			
			//System.out.println(conveyor.gs.toString());
			conveyor.pickAndExecuteAnAction(); // gs == moving, should tell anim to start conveyor
			wait(timer, sem, waitTime);
			
			t.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, frontSensorArgs); // gs == moving
			wait(timer, sem, waitTime);
			
			if (i == 0) { // only should have to start conveyor the first time
				assertTrue(
						"Mock animation should have been told to start conveyor. Event log: "
						+ anim.log.toString(), anim.log.containsString(
						"Channel: " + TChannel.CONVEYOR + ", Event: " + TEvent.CONVEYOR_DO_START + ", Arguments: " + id + "."));
				assertEquals(
						"Only 1 event should have been sent to the animation. Event log: "
								+ anim.log.toString(), 1, anim.log.size());
				anim.log.clear();
			}
			
			assertTrue(
					"Mock CF should have been told conveyor is open. Event log: "
							+ cf.log.toString(), cf.log
							.containsString("Received message msgPositionFree."));
			assertEquals(
					"Only 1 message should have been sent to the CF. Event log: "
							+ cf.log.toString(), 1, cf.log.size());
			anim.log.clear();
			p.log.clear();
			cf.log.clear();
		}
		
		for (int i = 0; i < numGlass; ++i) {
			conveyor.pickAndExecuteAnAction(); // nothing should happen; if it does something, test will fail later
			
			t.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, backSensorArgs); // gs == atEnd
			wait(timer, sem, waitTime);
			
			conveyor.pickAndExecuteAnAction(); // should stop conveyor, message popup, and then wait for popup to respond
			wait(timer, sem, waitTime);
			
			assertTrue(
					"Mock animation should have been told to stop conveyor. Event log: "
					+ anim.log.toString(), anim.log.containsString(
					"Channel: " + TChannel.CONVEYOR + ", Event: " + TEvent.CONVEYOR_DO_STOP + ", Arguments: " + id + "."));
			assertEquals(
					"Only 1 event should have been sent to the animation. Event log: "
							+ anim.log.toString(), 1, anim.log.size());
			assertTrue(
					"Mock popup should have been told about glass. Event log: "
							+ p.log.toString(), p.log
							.containsString("Received message msgNextGlass for " + g[i] + "."));
			assertEquals(
					"Only 1 message should have been sent to the popup. Event log: "
							+ p.log.toString(), 1, p.log.size());
			
			conveyor.pickAndExecuteAnAction(); // nothing should happen; if it does something, test will fail later
			
			anim.log.clear(); // or else can't tell if the next messages are new (because they are repeats of the old ones)
			
			conveyor.msgPositionFree(); // gs == waiting && posFree == true
			
			conveyor.pickAndExecuteAnAction(); // should start conveyor
			wait(timer, sem, waitTime);
			
			assertTrue(
					"Mock animation should have been told to start conveyor. Event log: "
					+ anim.log.toString(), anim.log.containsString(
					"Channel: " + TChannel.CONVEYOR + ", Event: " + TEvent.CONVEYOR_DO_START + ", Arguments: " + id + "."));
			assertEquals(
					"Only 1 event should have been sent to the animation. Event log: "
							+ anim.log.toString(), 1, anim.log.size());
			
			conveyor.pickAndExecuteAnAction(); // nothing should happen; if it does something, test will fail later
			
			t.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, backSensorArgs); // gs == done
			wait(timer, sem, waitTime);
			
			conveyor.pickAndExecuteAnAction(); // should remove glass from glass list
			wait(timer, sem, waitTime);
			
			conveyor.pickAndExecuteAnAction(); // nothing should happen
			assertEquals(
					"Only 1 event should have been sent to the animation. Event log: "
							+ anim.log.toString(), 1, anim.log.size());
			assertEquals(
					"Only 1 message should have been sent to the popup. Event log: "
							+ p.log.toString(), 1, p.log.size());
			assertEquals(
					"No messages should have been sent to the CF. Event log: "
							+ cf.log.toString(), 0, cf.log.size());
			
			anim.log.clear();
			p.log.clear();
			cf.log.clear();
		}
	}
	
	/* Waits. Used to wait for the transducer. */
	private void wait(Timer timer, final Semaphore sem, int time) {
		timer.schedule(new TimerTask(){ public void run() {		    
			sem.release(); } }, time); // sem will be released after <time> ms
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
