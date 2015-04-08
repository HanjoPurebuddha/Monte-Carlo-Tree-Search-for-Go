package ai;

import game.Board;

/**
 * A player that only makes random moves
 * @author Piotr Kaminski
 */

public class RandomPlayer extends Player {
	
	public RandomPlayer() {
		super("RandomPlayer");
	}

	public int playMove() {
		int move = playRandomLegalMove(false);
		return move;
	}

}
