package com.ideanest.vegos.ai;

import java.util.Arrays;
import java.util.Comparator;

import com.ideanest.vegos.game.Game;

/**
 * A list of weighted moves.
 * 
 * @author Piotr Kaminski
 */

public class WeightedMoveList {
	private WeightedMove[] moves;
	private int size;
	
	public WeightedMoveList(int numPossibleMoves) {
		moves = new WeightedMove[numPossibleMoves];
	}
	public WeightedMoveList(Game game) {
		this(game.getNumPoints()+1);
	}
	
	public WeightedMove get(int move) {
		// offset by 1 to allow PASS to be held as well
		WeightedMove wmove = moves[move+1];
		if (wmove == null) {
			wmove = moves[move+1] = new WeightedMove(move);
			size++;
		}
		return wmove;
	}
	
	/**
	 * Record the move as having been played.  Return whether it was a new move.
	 * @param move the move just played
	 * @return true if a new weighted move was created, false otherwise
	 */
	public boolean playedMove(int move) {
		int oldSize = size;
		get(move).wasPlayed();
		return size > oldSize;
	}
	
	/**
	 * Add the given weighted move to the list if its weight is larger than
	 * that currently set for this move.
	 */
	public void putMax(WeightedMove wmove) {
		WeightedMove oldwmove = moves[wmove.z+1];
		if (oldwmove == null) size++;
		if (oldwmove == null || wmove.weight > oldwmove.weight) moves[wmove.z+1] = wmove;
	}
	
	public float getWeight(int move) {
		int i = move + 1;
		if (i < 0 || i > moves.length) return Float.NaN;
		WeightedMove wmove = moves[i];
		return wmove == null ? Float.NaN : wmove.weight;
	}
	
	public int getNumPlays(int move) {
		int i = move + 1;
		if (i < 0 || i > moves.length) return 0;
		WeightedMove wmove = moves[i];
		return wmove == null ? 0 : wmove.numTotalPlays;
	}

	public void addAll(WeightedMoveList that) {
		for (int i=0; i<moves.length; i++) {
			if (this.moves[i] == null) {
				if (that.moves[i] != null) {
					this.moves[i] = that.moves[i].duplicate();
					size++;
				}
			} else if (that.moves[i] != null) {
				this.moves[i].add(that.moves[i]);
			}
		}
	}
	
	public void updateWeights(float score) {
		for (int i = 0; i < moves.length; i++) {
			WeightedMove smove = moves[i];
			if (smove != null) smove.updateWeight(score);
		}
	}
	
	public int size() {
		return size;
	}
	
	/**
	 * Return an array of all weighted moves that were created, ordered by
	 * decreasing weight.
	 */
	public WeightedMove[] getBest() {
		WeightedMove[] best = new WeightedMove[size()];
		if (best.length == moves.length) {
			System.arraycopy(moves, 0, best, 0, moves.length);
		} else {
			copy(best, 0);
		}
		Arrays.sort(best, WeightedMove.WEIGHT_COMPARATOR);
		return best;
	}
	
	public int copy(WeightedMove[] dest, int j) {
		for (int i=0; i<moves.length; i++) {
			if (moves[i] != null) dest[j++] = moves[i];
		}
		return j;
	}

}
