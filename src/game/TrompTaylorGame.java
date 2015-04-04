package game;

import java.util.HashSet;
import java.util.Set;

import gtp.*;

/**
 * A game of Go that follows the standard Tromp-Taylor rules, with the exception
 * that handicap stones have fixed positions.
 * 
 * @see http://www.cwi.nl/~tromp/go.html
 * @author Piotr Kaminski
 */
public class TrompTaylorGame extends Game {
	
	protected Set previousBoards = new HashSet();
	protected int passes;
	protected float score = Float.NaN;
	
	protected final boolean suicidesAllowed;

	public TrompTaylorGame(int sideSize, boolean suicidesAllowed) {
		super(sideSize);
		this.suicidesAllowed = suicidesAllowed;
	}
	
	public boolean equals(Object o) {
		if (o.getClass() != TrompTaylorGame.class) return false;
		TrompTaylorGame that = (TrompTaylorGame) o;
		return
			super.equals(o) &&
			this.passes == that.passes &&
			this.suicidesAllowed == that.suicidesAllowed &&
			this.previousBoards.equals(that.previousBoards);
	}
	
	public String getName() {return "Tromp-Taylor";}
	
	public String toString() {
		return super.toString() + " suicide=" + suicidesAllowed;
	}
	
	public void setFixedHandicap(int handicap) {
		super.setFixedHandicap(handicap);
		
		previousBoards.clear();
		Board snapshot = board.duplicate();
		snapshot.setNextToPlay(Color.BLACK);  // positional superko, so don't care who's to play
		snapshot.freeze();
		previousBoards.add(snapshot);
	}
	
	public Object clone() throws CloneNotSupportedException {
		TrompTaylorGame that = (TrompTaylorGame) super.clone();
		that.previousBoards = (Set) ((HashSet) previousBoards).clone();
		return that;
	}
	
	public boolean validateBoard(int z, Board oldBoard) {
		Color other = nextToPlay.inverse();
		
		// check for kills
		int w;
		w = getGrid().left(z);  if (getPoint(w) == other) verifyString(w);
		w = getGrid().right(z);  if(getPoint(w) == other) verifyString(w);
		w = getGrid().up(z);  if (getPoint(w) == other) verifyString(w);
		w = getGrid().down(z);  if (getPoint(w) == other) verifyString(w);
		
		// check for suicide
		boolean suicide = !verifyString(z);
		if (!suicidesAllowed && suicide) return false;
		
		// superko check
		if (previousBoards.contains(board) || oldBoard.equals(board)) return false;
		
		return true;
	}
	

	public boolean play(int z) {
		if (!isPassingAllowed() && z == MOVE_PASS) return false;
		if (isOver()) return false;
		Color other = nextToPlay.inverse();
		if (z == MOVE_PASS || getPoint(z) != Point.EMPTY) {
			passes++;
			nextToPlay = other;
			return true;
		}
		if (getPoint(z) != Point.EMPTY) return false;
		Board backupBoard = board.duplicate();
		board.setPoint(z, nextToPlay);
		
		if (validateBoard(z, backupBoard)) {
			// valid move!
			backupBoard.freeze();
			previousBoards.add(backupBoard);
			passes = 0;  score = Float.NaN;
			nextToPlay = other;	// must do before recording move, because of Conway game over checking
			recordMove(z);
			return true;
		} else {
			board = backupBoard;
			return false;
		}
	}
	
	public boolean isOver() {
		return passes >= 2;
	}
	
	public float score() {
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
	
}
