package com.ideanest.vegos.ai;

/**
 * Decreases simulated system temperature, producing a sequence of move swap probabilities.
 * @author Piotr Kaminski
 */
public interface CoolingSchedule {
	void reset(int numIterations);
	float nextPSwap();
}
