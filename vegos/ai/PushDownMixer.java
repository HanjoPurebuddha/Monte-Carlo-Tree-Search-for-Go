package com.ideanest.vegos.ai;

import java.util.Random;

/**
 * Pushes a move down by one with probability pswap.
 * @author Piotr Kaminski
 */
public class PushDownMixer extends CoolingMixer {
	
	public PushDownMixer(CoolingSchedule schedule) {
		super(schedule);
	}
	
	public String toString() {
		return "pushdown" + super.toString();
	}
	
	public void mix(MoveSequence seq, int index) {
		if (rnd.nextFloat() < pswap) seq.swap(index, index+1);
	}
	
}
