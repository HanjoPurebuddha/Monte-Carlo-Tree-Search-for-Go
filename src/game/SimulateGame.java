package game;
/**
 * Doesn't allow playing in eyes or passing.  Game is over when there are no more legal moves.
 * @author Piotr Kaminski
 */
// Very few things added here.
public class SimulateGame extends TrompTaylorGame {

	public SimulateGame(int sideSize) {
		super(sideSize, true);
	}
	
	public void recordMove(int z) {
		super.recordMove(z);
		calculateGameOver();
	}

	
	//Method changed to use passes.
	public boolean isOver() {
		if(passes > 0)
			return true;
		return false;
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

	// Changed this method into using avoidEyes, so that it can be turned on/off
	public boolean validateBoard(int z, Board oldBoard) {
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
