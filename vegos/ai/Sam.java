package com.ideanest.vegos.ai;

import java.util.*;
import java.util.Collections;
import java.util.List;

import com.ideanest.vegos.game.Color;
import com.ideanest.vegos.game.Game;

/**
 * A virtual player for simulated annealing.
 * @author Piotr Kaminski
 */
public class Sam extends VaultPlayer {
	private MoveSequence moveSeq;
	private final Mixer mixer;
	private int moveIndex;
	
	Sam(Game originalGame, Color side, Mixer mixer, MoveCounter moveCounter) {
		super("Sam", originalGame, side, moveCounter);
		this.mixer = mixer;
	}
	
	public void startGame(Game game, Color side) {
		super.startGame(game, side);
		// extract all moves that were played so far, but keep extra space for new ones (hopefully to avoid growing array)
		WeightedMove[] allwmoves = new WeightedMove[moveVault.size()*3/2+1];
		moveSeq = new MoveSequence(moveVault.toArray(allwmoves), moveVault.size());
		// sort move sequence by weight, we'll mix it up later
		moveSeq.sortByWeight();
		// reset sequence index to beginning
		moveIndex = 0;
	}
	
	public int playMove() {
		int move;
		FIND_MOVE: {
			// find first legal move in sorted move list, if any
			while (true) {
				mixer.mix(moveSeq, moveIndex);
				WeightedMove wmove = moveSeq.get(moveIndex);
				if (wmove == null) break;
				if (game.play(move = wmove.z)) break FIND_MOVE;
				moveIndex++;  // try next move
			}
			// ran out of previously tried moves, generate a new one
			move = playRandomLegalMove(game.isPassingAllowed());
		}
		moveVault.playedMove(move);
		moveIndex++;	// this move is done
		return move;
	}
	
}
