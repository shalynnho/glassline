cs201GlassLine_Team13
=====================

USC Spring 2013 - GlassLine Factory project for CS 201

*Note: We originally had 5 people, but one dropped, so it is now just 4 people*

## Organization
- Design Doc is in the Design folder organized by student name.
- Our agents are in src/engine/agent
	- Look in packages engine.agent.david, engine.agent.evan, engine.agent.shay, engine.agent.tim for each of our v0 code.
	- We split up work among other agents.
- Our agents are instantiated in the FactoryPanel class, and key code there connects our agents together.

## Running code
- Simply start the program via the standard FactoryDriver class. 
	- The animation should show 3 pieces of glass going through the conveyor (beyond the requirement of just showing 1 glass going through)
- Feel free to look in FactoryPanel's createInitialGlasses method to see the specific workstations that we arbitrarily chose