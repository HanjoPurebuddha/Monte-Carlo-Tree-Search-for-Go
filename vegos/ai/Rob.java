package com.ideanest.vegos.ai;

import com.ideanest.vegos.game.Color;
import com.ideanest.vegos.game.Game;

/**
 * 
 * @author Piotr Kaminski
 */
public class Rob extends VaultPlayer {

	public Rob(Game originalGame, Color side, MoveCounter moveCounter) {
		super("Rob", originalGame, side, moveCounter);
	}

	public int playMove() {
		int move = playRandomLegalMove(true);
		moveVault.playedMove(move);
		return move;
	}

}
