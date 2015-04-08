package ai;

import game.Board;

/**
 * A player that only makes random moves
 * @author Piotr Kaminski
 */

/* that it is capable of playing! */

public class SimulatePlayer extends Player {
	
	public SimulatePlayer() {
		super("NoEyeRandomPlayer");
	}

	public int playMove() {
		int move = playRandomLegalMove(false);
		return move;
	}

}
