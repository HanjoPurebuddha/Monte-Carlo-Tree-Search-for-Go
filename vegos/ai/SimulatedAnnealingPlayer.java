package com.ideanest.vegos.ai;

import java.util.*;

import com.ideanest.util.UnexpectedCaseException;
import com.ideanest.vegos.game.*;
import com.ideanest.vegos.gtp.*;

/**
 * Decides on a move using simulated annealing techniques.
 * @author Piotr Kaminski
 */
public abstract class SimulatedAnnealingPlayer extends Player {
	
	public static final int INFINITE_DEPTH = -1;
	
	int numSimGames = 1, annealingLength = 1000, maxSimGameDepth = INFINITE_DEPTH;
	float passDelta = 2.0f;
	
	WeightedMoveList moves;
	
	SimPlayer[] sims = new SimPlayer[2];
	
	public SimulatedAnnealingPlayer(String name) {
		super(name);
	}
	
	public String toString() {
		return "[" + getName() + "] " + numSimGames + " cooling cycles ("
			+ annealingLength + " ref/cycle)"  + (maxSimGameDepth == INFINITE_DEPTH ? "" : (" max depth " + maxSimGameDepth));
	}
	
	public void setIterations(int numSimGames, int annealingLength, int maxSimGameDepth) {
		this.numSimGames = numSimGames;
		this.annealingLength = annealingLength;
		this.maxSimGameDepth = maxSimGameDepth;
	}
	
	public float getMoveValue(int z) {
		if (z == Game.MOVE_RESIGN) return Float.NEGATIVE_INFINITY;
//		if (z == Game.MOVE_PASS) return game.score(side);
		if (moves == null) return Float.NaN;
		return moves.getWeight(z);
	}
	
	public float getMoveConfidence(int z) {
		if (z == Game.MOVE_RESIGN) return 1f;
		if (z == Game.MOVE_PASS) return 0f;
		if (moves == null) return 0f;
		return ((float) moves.getNumPlays(z)) / (numSimGames * annealingLength);
	}
	
	public abstract void startCycle(int numRefinements);
	
	public void endCycle() {
		Arrays.fill(sims, null);
	}
	
	protected void prepRefinement() {}
	
	public void simulateCycle(int numRefinements) {
		// simulate a bunch of games, refining the move lists
		for (int i=0; i<numRefinements; i++) {
			prepRefinement();
			Game simGame = game.duplicate();
			for (int j=0; j<2; j++) sims[j].startGame(simGame, Color.get(j));
			for (int n=0; !simGame.isOver() && (maxSimGameDepth == INFINITE_DEPTH || n < maxSimGameDepth); n++) {
				Color nextSide = simGame.getNextToPlay();
				sims[nextSide.getIndex()].playMove();
			}
			for (int j=0; j<2; j++) sims[j].endGame();
		}
	}
	
	public WeightedMoveList getCurrentCycleCandidates() {
		return sims[side.getIndex()].getFirstMoves();
	}
	
	public void simulate(int numCycles) {
		moves = new WeightedMoveList(game);
		for (int k=0; k<numCycles; k++) {
			startCycle(annealingLength);
			simulateCycle(annealingLength);
			// finished one cooling cycle, merge move weights into best move list
			sims[side.getIndex()].influence(moves);
			endCycle();
		}
	}
	
	public int playMove() {
		checkPlaying(true);

		// accept wins
		if (game.getMove(-1) == Game.MOVE_PASS && game.score(side) > 0) return playPass();
		
		simulate(numSimGames);
		
		// get best valid move for our side and play it
		WeightedMove[] bestMoves = moves.getBest();
		for (int i = 0; i < bestMoves.length; i++) {
			WeightedMove wmove = bestMoves[i];
			// if we're losing, do anything but pass!
			if (wmove.z == Game.MOVE_PASS && game.score(side) < 0) continue;
			// decide if we should initiate a pass; never do so if losing!
			if (game.isPassingAllowed() && game.score(side) >= 0 && wmove.weight + passDelta < game.score(side)) return playPass();
			if (game.play(wmove.z)) return wmove.z;
		}
		
		// no moves found, must pass; if game does not allow passing we should not have gotten here
		return playPass();
		
	}

}
