package game;

import gtp.*;

/**
 * Doesn't allow playing in eyes or passing.  Game is over when there are no more legal moves.
 * @author Piotr Kaminski
 */
public class SimulateGame extends TrompTaylorGame {

	public SimulateGame(int sideSize) {
		super(sideSize, true);
	}
	
	public void recordMove(int z) {
		super.recordMove(z);
		calculateGameOver();
	}

	
	public boolean isOver() {
		if(passes > 0)
			return true;
		return false;
		//return gameOver;
	}
	
	public SimulateGame duplicate() {
		try {
			return (SimulateGame) clone();
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
		if(avoidEyes) {
			CHECK_EYE: {
				temp1.clear();
				temp1.addNeighbors(z);
				for (int i=0; i<temp1.size(); i++) if (getPoint(temp1.get(i)) != nextToPlay) break CHECK_EYE;
				temp1.clear();
				temp1.addDiagonalNeighbors(z);
				Color other = nextToPlay.inverse();
				int numOther = 0;
				for (int i=0; i<temp1.size(); i++) if (getPoint(temp1.get(i)) == other) numOther++;
				if (numOther == 0 || numOther == 1 && temp1.size() == 4) return false;
			}
		}
		return super.validateBoard(z, oldBoard);
	}
	
	public String getName() {
		return "Semi-Primitive";
	}

	public boolean isPassingAllowed() {
		return true;
	}

}
