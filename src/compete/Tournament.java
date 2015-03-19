package compete;

import java.util.*;

import com.ideanest.util.Safe;
import ai.Player;
import game.Game;

/**
 * Runs a tournament between a bunch of players.
 * @author Piotr Kaminski
 */

public class Tournament implements Runnable {
	private final List games;
	private final List[] playerGroups = new List[2];
	private final int numGamesPerMatch;
	private final List matches = new ArrayList();
	private final List constMatches = Collections.unmodifiableList(matches);
	
	public Tournament(Game game, Player p1, Player p2, int numGamesPerMatch) {
		this(new Game[]{game}, new Player[]{p1}, new Player[]{p2}, numGamesPerMatch);
	}
	
	public Tournament(Game game, Player[] completeGroup, int numGamesPerMatch) {
		this(new Game[]{game}, completeGroup, completeGroup, numGamesPerMatch);
	}
		
	public Tournament(Game[] games, Player[] group1, Player[] group2, int numGamesPerMatch) {
		this.games = new ArrayList(Arrays.asList(games));
		this.playerGroups[0] = new ArrayList(Arrays.asList(group1));
		this.playerGroups[1] = new ArrayList(Arrays.asList(group2));
		this.numGamesPerMatch = numGamesPerMatch;
	}
	
	public void run() {
		for (Iterator it = games.iterator(); it.hasNext();) {
			play((Game) it.next());
		}
	}
	
	private Set pairsPlayed = new HashSet();
	
	protected void play(Game game) {
		pairsPlayed.clear();
		for (Iterator it1 = playerGroups[0].iterator(); it1.hasNext();) {
			Player p1 = (Player) it1.next();
			for (Iterator it2 = playerGroups[1].iterator(); it2.hasNext();) {
				Player p2 = (Player) it2.next();
				if (p1 == p2) continue;
				playMatch(game, p1, p2);
				playMatch(game, p2, p1);
			}
		}
	}
	
	protected void playMatch(Game game, Player blackPlayer, Player whitePlayer) {
		Pair pair = new Pair(blackPlayer, whitePlayer);
		if (!pairsPlayed.add(pair)) return;
		Match m = new Match(game, blackPlayer, whitePlayer);
		m.play(numGamesPerMatch);
		matches.add(m);
	}
	
	protected static class Pair {
		private Object o1, o2;
		protected Pair(Object o1, Object o2) {
			this.o1 = o1;
			this.o2 = o2;
		}
		public boolean equals(Object o) {
			if (o.getClass() != Pair.class) return false;
			Pair that = (Pair) o;
			return Safe.equals(this.o1, that.o1) && Safe.equals(this.o2, that.o2);
		}
		public int hashCode() {
			return hashCode();
		}
	}
	
	public List getMatches() {return constMatches;}
	public int getNumGamesPerMatch() {return numGamesPerMatch;}
		
}
