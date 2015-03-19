package compete;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

import game.*;
import gtp.*;

/**
 * Keeps statistics about a series of games.
 * @author Piotr Kaminski
 */
public class Statistics {
	
	private int[] numGamesWon = new int[2];
	private List gameResults = new ArrayList();
	private List constGameResults = Collections.unmodifiableList(gameResults);
	
	public static class GameResult {
		public final float score;
		public final int numMoves;
		public GameResult(Game game) {
			this.score = game.score();
			this.numMoves = game.getNumMoves();
		}
	}

	public void add(Game game) {
		GameResult gr = new GameResult(game);
		if (gr.score > 0) numGamesWon[Color.BLACK.getIndex()]++;
		else if (gr.score < 0) numGamesWon[Color.WHITE.getIndex()]++;
		gameResults.add(gr);
	}
	
	public Color getWinningSide() {
		int spread = getSpread();
		if (spread > 0) return Color.BLACK;
		else if (spread < 0) return Color.WHITE;
		else return null;
	}
	
	public int getSpread() {
		return numGamesWon[0] - numGamesWon[1];
	}
	
	public int getNumGames() {return gameResults.size();}
	
	public int getNumGamesWon(Color side) {
		return numGamesWon[side.getIndex()];
	}
	
	public int getNumGamesDrawn() {
		return getNumGames() - getNumGamesWon(Color.BLACK) - getNumGamesWon(Color.WHITE);
	}
	
	public float getAverageScore() {
		float total = 0;
		for (Iterator it = gameResults.iterator(); it.hasNext();) {
			GameResult gr = (GameResult) it.next();
			total += gr.score;
		}
		return total / gameResults.size();
	}
	
	public List getGameResults() {return constGameResults;}
}
