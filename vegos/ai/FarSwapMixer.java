package com.ideanest.vegos.ai;

import java.util.Random;

/**
 * Swaps a move with one n slots down.
 * @author Piotr Kaminski
 */
public class FarSwapMixer extends CoolingMixer {
	
	public FarSwapMixer(CoolingSchedule schedule) {
		super(schedule);
	}
	
	public String toString() {
		return "farswap" + super.toString();
	}
	
	public void mix(MoveSequence seq, int index) {
		int n = 0;
		while (rnd.nextFloat() < pswap) n++;
		seq.swap(index, index+n);
	}

}
