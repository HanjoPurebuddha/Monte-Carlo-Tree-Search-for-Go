package com.ideanest.vegos.ai;

import java.util.Random;

/**
 * A mixer that uses a cooling schedule.
 * @author Piotr Kaminski
 */
public abstract class CoolingMixer implements Mixer {
	protected final CoolingSchedule schedule;
	protected float pswap;
	protected final Random rnd = new Random();

	public CoolingMixer(CoolingSchedule schedule) {
		this.schedule = schedule;
	}
	
	public String toString() {
		return " (cooling " + schedule + ")";
	}
	public void nextIteration() {
		pswap = schedule.nextPSwap();
	}

	public void reset(int numIterations) {
		schedule.reset(numIterations);
	}
	
}
