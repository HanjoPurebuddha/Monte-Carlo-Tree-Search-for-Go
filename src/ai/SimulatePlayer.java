package ai;

import game.Board;

/**
 * A player that makes moves according to pattern matching, atari matching and taking pieces
 * Based on which configurations have been enabled
 * @author Thomas Ager
 */


public class SimulatePlayer extends Player {
	
	/* simulation changes */
	public boolean simulateAvoidEyes;
	public boolean simulateAtari;
	public boolean simulatePatterns;
	public boolean simulateTakePieces;
	public boolean simulateMercyRule;
	
	public SimulatePlayer(boolean simulateAvoidEyes, boolean simulateAtari, boolean simulatePatterns, boolean simulateTakePieces, boolean simulateMercyRule) {
		super("SimulatePlayer");
		this.simulateAvoidEyes = simulateAvoidEyes;
		this.simulateAtari = simulateAtari;
    	this.simulatePatterns = simulatePatterns;
    	this.simulateTakePieces = simulateTakePieces;
    	this.simulateMercyRule = simulateMercyRule;
	}
	
	public int playMove() {
		/* get the last move of the game */
		int lastMove = game.getMove(0);
		
		/* if the opposing side has more than 30% of the board in captured pieces */
		if(simulateMercyRule && game.mercy()) {
			
			/* just end the game */
			return -2;
		}
		
		/* if the opponents move resulted in stones being in atari */
		if (simulateAtari && game.atari(lastMove)) {
			
			/* and the stones can be saved */
			if(game.saveStones(lastMove)) {
				
				/* if it returns true, that means they were saved, so return the saving move */
				return game.getMove(0);
			}
		}
		
		/* if the opponents move matches any mogo patterns */
		if (simulatePatterns && game.matchPattern(lastMove)) {
			
			/* if it returns true, that means a random mogo pattern was picked and played, so return that move*/
			return game.getMove(0);
		}
		
		/* if it is possible to take the opponents piece */
		if (simulateTakePieces && game.takePiece(lastMove)) {
			
			/* if it returns true, that means a piece was taken, so return that move*/
			return game.getMove(0);
		}
		
		/* if none of these match just randomly pick a move that isn't in an eye */
		int move = playRandomLegalMove(simulateAvoidEyes);
		return move;
	}

}
