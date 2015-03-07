package com.ideanest.vegos.ai;

import java.util.Arrays;

import com.ideanest.vegos.game.Color;

/**
 * 
 * @author Piotr Kaminski
 */
public class GlobalMoveCounter implements MoveCounter {

	private int[] moveCounts = new int[100];

	public int countMove(Color side, int move) {
		move += 1;	// adjust for passes
		if (move >= moveCounts.length) {
			// expand array
			int[] temp = new int[Math.max(moveCounts.length*3/2, move)+1];
			System.arraycopy(moveCounts, 0, temp, 0, moveCounts.length);
			moveCounts = temp;
		}
		assert move < moveCounts.length;
		return moveCounts[move]++;
	}
	
	public void reset() {
		Arrays.fill(moveCounts, 0);
	}
	
	public String toString() {return "global";}

}
