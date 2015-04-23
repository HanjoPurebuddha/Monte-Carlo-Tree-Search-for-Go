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
	
	public float getLivingScore(float score) {
		if (Float.isNaN(score)) {
			score = -komi;
			for (int i=0; i<getNumPoints(); i++) {
				Point p = getPoint(i);
				if (p == Color.BLACK) score += 1;
				else if (p == Color.WHITE) score -= 1;
				else {
					assert p == Point.EMPTY;
					boolean reachesWhite = reaches(i, Color.WHITE);
					boolean reachesBlack = reaches(i, Color.BLACK);
					if (reachesWhite && !reachesBlack) score -= 1;
					else if (reachesBlack && !reachesWhite) score +=1;
				}
			}
		}
		return score;
	}
	
	public float getCaptureScore(float score) {
		score = (captures[0] - captures[1]) - komi;
		return score;
	}
	
	public float score(boolean captureScoring, boolean livingScoring, boolean averageScoring) {
		if(captureScoring) {
			score = getCaptureScore(score);
		}
		if(livingScoring) {
			score = getLivingScore(score);
		}
		if(averageScoring) {
			score = (getCaptureScore(score) + getLivingScore(score)) / 2;
		}
		return score;
	}
	
	public boolean validateBoard(int z, Board oldBoard) {
		// check if z is playing into what looks like an eye
		// (eye:  neighbours same color, no more than 1 diagonal other color, except at edges)
		if(avoidEyes) {
			if(checkEye(z) == false)
				return false;
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
