package com.ideanest.vegos.ai;

import com.ideanest.vegos.game.Color;

/**
 * 
 * @author Piotr Kaminski
 */
public class NullMoveCounter implements MoveCounter {

	public int countMove(Color side, int move) {
		return 0;
	}

	public void reset() {
	}
	
	public String toString() {return "null";}

}
