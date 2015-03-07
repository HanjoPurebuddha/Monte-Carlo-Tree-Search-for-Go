package com.ideanest.vegos.ai;

import java.util.NoSuchElementException;

/**
 * Decreases PSwap linearly, from a given maximum down to 0, which it holds for a set number of iterations.
 * @author Piotr Kaminski
 */
public class LinearCoolingSchedule implements CoolingSchedule {
	private final float firstPSwap;
	private final int numSettlingIterations;
	private int numIterations, iterationsLeft;
	
	public LinearCoolingSchedule(float firstPSwap, int numSettlingIterations) {
		this.firstPSwap = firstPSwap;
		this.numSettlingIterations = numSettlingIterations;
	}
	
	public String toString() {
		return "linear from " + firstPSwap + " coast for " + numSettlingIterations;
	}
	
	public void reset(int numIterations) {
		this.numIterations = numIterations;
		iterationsLeft = numIterations;
	}

	public float nextPSwap() {
		if (iterationsLeft == 0) throw new NoSuchElementException();
		float pswap;
		if (iterationsLeft <= numSettlingIterations) {
			pswap = 0;
		} else {
			pswap = firstPSwap * (iterationsLeft - numSettlingIterations) / (numIterations - numSettlingIterations);
		}
		iterationsLeft--;
		return pswap;
	}

}
