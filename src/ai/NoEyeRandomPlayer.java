package ai;

import game.Board;

/**
 * A player that only makes random moves
 * @author Piotr Kaminski
 */

/* that it is capable of playing! */

public class NoEyeRandomPlayer extends Player {
	
	public NoEyeRandomPlayer() {
		super("NoEyeRandomPlayer");
	}

	public int playMove() {
		int move = playRandomLegalMove(false);
		boolean canPlay = game.play(move);
		if(canPlay) {
			return move;
		} else {
			return -1;
		}
	}

}
