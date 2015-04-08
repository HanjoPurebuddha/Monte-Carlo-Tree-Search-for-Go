package game;

import gtp.*;

/**
 * Doesn't allow playing in eyes or passing.  Game is over when there are no more legal moves.
 * @author Piotr Kaminski
 */
public class RandomGame extends TrompTaylorGame {
	
	private boolean gameOver;

	public RandomGame(int sideSize) {
		super(sideSize, true);
	}
	
	public void recordMove(int z) {
		super.recordMove(z);
		calculateGameOver();
	}
	
	public boolean isOver() {
		return gameOver;
	}
	
	public RandomGame duplicate() {
		try {
			return (RandomGame) clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	public void calculateGameOver() {
		gameOver = true;
		for (int z=0; gameOver && z<getNumPoints(); z++) {
			if (getPoint(z) != Point.EMPTY) continue;
			Board backupBoard = board.duplicate();
			board.setPoint(z, nextToPlay);
			gameOver = !validateBoard(z, backupBoard);
			board = backupBoard;
		}
	}
	
	
	
	public boolean validateBoard(int z, Board oldBoard) {
		// check if z is playing into what looks like an eye
		// (eye:  neighbours same color, no more than 1 diagonal other color, except at edges)

		return super.validateBoard(z, oldBoard);
	}

	public String getName() {
		return "Semi-Primitive";
	}

	public boolean isPassingAllowed() {
		return true;
	}

}
