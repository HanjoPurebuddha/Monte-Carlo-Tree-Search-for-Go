package compete;
import ai.Tom;
import java.io.*;

import ai.*;
import game.SemiPrimitiveGame;
import game.Game;

/**
 * 
 * @author Piotr Kaminski
 */
public class Runner {

	public static void main(String[] args) throws IOException {
		
		Game[] games = new Game[] {
			new SemiPrimitiveGame(9),
		};

		Player[] players0 = new Player[] {
			new Tom()
		};
		Player[] players1 = new Player[] {
			new Randy()
		};
		
		Tournament t = new Tournament(games, players0, players0, 5);
		t.run();
		
		String filename;
		for (int i=1; ; i++) {
			filename = "results" + i + ".html";
			if (!new File(filename).exists()) break;
		}
		TournamentWriter tw = new TournamentWriter(new FileWriter(filename));
		tw.write(t);
		tw.close();
		
	}
	/*
	public static Player createStanley(int numSimGames, int annealingLength, Mixer mixer, MoveCounter moveCounter) {
		Stanley p = new Stanley();
		p.setIterations(numSimGames, annealingLength, SimulatedAnnealingPlayer.INFINITE_DEPTH);
		p.setMixer(mixer);
		p.setMoveCounter(moveCounter);
		return p;
	}
	
	public static Player createRuby(int numSimGames, int annealingLength, MoveCounter moveCounter) {
		Ruby p = new Ruby();
		p.setIterations(numSimGames, annealingLength, SimulatedAnnealingPlayer.INFINITE_DEPTH);
		p.setMoveCounter(moveCounter);
		return p;
	}
	*/
	
	
}
