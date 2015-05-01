package ai;

import java.util.Random;

import com.ideanest.util.UnexpectedCaseException;

import game.*;
import gtp.*;

/**
 * 
 * @author Piotr Kaminski
 */
public abstract class Player {
	public Game game;
	Color side;
	String name;
	final Random random = new Random();
	public boolean noTree;
	public int raveSkipCounter;
	private int movesTaken;
	OpeningBook openingBook = null;
	public void resetMovesTaken() {}
	
	private static final int MAX_RANDOM_MOVE_PICKS = 10;
	
	public Player(String name) {
		this.name = name;
	}
	
	public String toString() {return name;}
	
	/**
	 * Start playing the given game, but don't make a move yet.
	 * @param game the game this player will be playing in
	 * @param side the color this player will be playing
	 */
	public void startGame(Game game, Color side) {
		checkPlaying(false);
		this.game = game;
		this.side = side;
	}
	
	/**
	 * Stop playing the current game.
	 */
	public void endGame() {
		checkPlaying(true);
		this.game = null;
		this.side = null;
	}

	/**
	 * Play a move in the current game.
	 * @return the move played, or <code>Game.MOVE_RESIGN</code>
	 */
	public abstract int playMove();
	
	/**
	 * Get the name of this player.
	 * @return this player's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the seed used by the random number generator.
	 * @param seed the seed to reset to
	 */
	public void setRandomSeed(long seed) {
		random.setSeed(seed);
	}
	
	/**
	 * Check if this player is currently playing the game.
	 * @return the color this player is playing, or <code>null</code> if not playing
	 */
	public Color getPlayingColor() {
		return side;
	}
	
	public float getMoveConfidence(int i) {
		return 0f;
	}

	/**
	 * Return an estimate of the value of playing the given move.
	 * @param z the move to evaluate
	 * @return the expected value of the play, no limits; use NaN to indicate no known value
	 */
	public float getMoveValue(int z) {
		return Float.NaN;
	}

	protected int playPass() {
		boolean r = game.play(Game.MOVE_PASS);
		assert r;
		return Game.MOVE_PASS;
	}

	/**
	 * Check whether this player is currently playing the game.
	 * @param desiredState whether we want to be currently playing the game or not
	 * @throws IllegalStateException if current state doesn't match desired state
	 */
	protected void checkPlaying(boolean desiredState) {
		if (desiredState) {
			if (game == null) throw new IllegalStateException("not playing a game at the moment");
			// can't check if game is over here
		} else {
			if (game != null) throw new IllegalStateException("already playing a game");
		}
	}

	/**
	 * Play a random legal move in the current game (except resignation).
	 * @param considerPass consider passing as a legal move
	 * @return the move played
	 */
	protected int playRandomLegalMove(boolean avoidEyes) {
		game.avoidEyes = avoidEyes;
		checkPlaying(true);
		int move;
		// try a few random picks, in case the board is still fairly open
		// to avoid overhead of generating empty position array
		/*for (int i=0; i<MAX_RANDOM_MOVE_PICKS; i++) {
			move = random.nextInt(game.getNumPoints());
			if (game.play(move)) { 
				System.out.println("move was allowed for some reason " + move + " ");
				return move;
			}
		}*/
		// difficulty finding legal move, go through exhaustive search
		Board.PositionList emptyPoints = game.getPotentiallyPlayablePoints();
		emptyPoints.shuffle();
		//System.out.println(emptyPoints);
		for (int i=0; i<emptyPoints.size(); i++) {
			move = emptyPoints.get(i);
			if (game.play(move)) 
				return move;			
		}
		//System.out.println("no legal move left in the game");
		Game.gameOver = true;
		game.play(-1);
		return -1;
	}
	
	/* set the first node in the MCTS tree */

	public void initializeTree() {}

	public void setGame(Game game) {
		this.game = game;
		
	}

	public void setOpeningBook() {
		// TODO Auto-generated method stub
		
	}

	public void reset() {
		// TODO Auto-generated method stub
		
	}

}
