package com.ideanest.vegos.ai;

/**
 * A player that only makes random moves.
 * @author Piotr Kaminski
 */
public class Randy extends Player {
	
	public Randy() {
		super("Randy");
	}

	public int playMove() {
		return playRandomLegalMove(game.isPassingAllowed());
	}
	
}
