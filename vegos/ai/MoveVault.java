package com.ideanest.vegos.ai;

import java.util.*;

import com.ideanest.vegos.game.Color;
import com.ideanest.vegos.game.Game;

/**
 * A table of weighted moves, for a specific player.  The data structure is a table
 * with one row per count; the count is how many times that move was played
 * earlier in the game.
 */
class MoveVault {
	private final Color side;
	private final List table = new ArrayList();
	private final int numPossibleMoves;
	private final MoveCounter moveCounter;
	private int size;

	public MoveVault(Game game, Color side, MoveCounter moveCounter) {
		this.numPossibleMoves = game.getNumPoints()+1;
		this.side = side;
		this.moveCounter = moveCounter;
		table.add(new WeightedMoveList(numPossibleMoves));
	}
	
	/**
	 * Return the first row of moves, the ones that should be best to make first.
	 * @return the row of weighted moves to make first
	 */
	public WeightedMoveList getFirstRow() {
		return (WeightedMoveList) table.get(0);
	}
	
	/**
	 * Record that the given move was played.
	 * @param move the move just made
	 */
	public void playedMove(int move) {
		// increment global move count
		int count = moveCounter.countMove(side, move);
		// find right row in table for the move count
		WeightedMoveList row;
		if (count < table.size()) row = (WeightedMoveList) table.get(count);
		else table.add(row = new WeightedMoveList(numPossibleMoves));
		// record move played and adjust size
		if (row.playedMove(move)) size++;
	}
	
	/**
	 * Update the weights of the moves based on the final score of a game.
	 * @param score the weight of the last game played
	 */
	public void updateWeights(float score) {
		for (Iterator it = table.iterator(); it.hasNext();) {
			WeightedMoveList row = (WeightedMoveList) it.next();
			row.updateWeights(score);
		}
	}
	
	/**
	 * Return the total number of moves held in the table.  Only moves that
	 * were played at least once count.
	 * @return the number of moves in the table
	 */
	public int size() {return size;}
	
	/**
	 * Copy all weighted moves that were played at least once to the given array.  If the array
	 * given is too small to contain them, create a new array that is just the right size.  Return
	 * the array that holds all the weighted moves.
	 * @param wmoves the array to copy the moves into, if it's large enough; if null, always create a new one
	 * @return an array holding all the moves played at least once
	 */
	public WeightedMove[] toArray(WeightedMove[] wmoves) {
		if (wmoves == null || wmoves.length < size) wmoves = new WeightedMove[size];
		int j = 0;
		for (Iterator it = table.iterator(); it.hasNext();) {
			WeightedMoveList row = (WeightedMoveList) it.next();
			j = row.copy(wmoves, j);
		}
		assert j == size;
		return wmoves;
	}

}
