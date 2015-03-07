package com.ideanest.vegos.ai;

import java.util.Collections;
import java.util.List;

import com.ideanest.vegos.game.Color;
import com.ideanest.vegos.game.Game;

/**
 * 
 * @author Piotr Kaminski
 */
public abstract class VaultPlayer extends SimPlayer {
	protected final MoveVault moveVault;

	public VaultPlayer(String name, Game originalGame, Color side, MoveCounter moveCounter) {
		super(name);
		this.moveVault = new MoveVault(originalGame, side, moveCounter);
	}
	
	public void influence(WeightedMoveList otherMoves) {
		otherMoves.addAll(getFirstMoves());
	}

	public WeightedMoveList getFirstMoves() {
		return moveVault.getFirstRow();
	}

	public void endGame() {
		moveVault.updateWeights(game.score(side));
		super.endGame();
	}
	

	

}
