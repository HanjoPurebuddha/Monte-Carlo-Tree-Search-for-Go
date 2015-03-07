package com.ideanest.vegos.ai;

import java.util.Arrays;

import com.ideanest.vegos.game.Color;

/**
 * 
 * @author Piotr Kaminski
 */
public class LocalMoveCounter implements MoveCounter {

	private GlobalMoveCounter[] counters = new GlobalMoveCounter[2];
	{for (int i=0; i<2; i++) counters[i] = new GlobalMoveCounter();}

	public int countMove(Color side, int move) {
		return counters[side.getIndex()].countMove(side, move);
	}
	
	public void reset() {
		for (int i=0; i<2; i++) counters[i].reset();
	}
	
	public String toString() {return "local";}

}
