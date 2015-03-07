package com.ideanest.vegos.compete;

import java.io.*;

import com.ideanest.vegos.ai.*;
import com.ideanest.vegos.game.SemiPrimitiveGame;
import com.ideanest.vegos.game.Game;

/**
 * 
 * @author Piotr Kaminski
 */
public class Runner {

	public static void main(String[] args) throws IOException {
		Game[] games = new Game[] {
			new SemiPrimitiveGame(9),
		};
		
		Player stanley_null = createStanley(1, 5000, new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10)), new NullMoveCounter());
		Player stanley_local = createStanley(1, 5000, new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10)), new LocalMoveCounter());
		Player stanley_global = createStanley(1, 5000, new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10)), new GlobalMoveCounter());
		
		Player stanley_100 = createStanley(1, 100, new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10)), new NullMoveCounter());
		Player stanley_300 = createStanley(1, 300, new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10)), new NullMoveCounter());
		Player stanley_500 = createStanley(1, 500, new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10)), new NullMoveCounter());
		Player stanley_1000 = createStanley(1, 1000, new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10)), new NullMoveCounter());
		Player stanley_2500 = createStanley(1, 2500, new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10)), new NullMoveCounter());
		Player stanley_5000 = createStanley(1, 5000, new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10)), new NullMoveCounter());
		
		Player stanley_2_500 = createStanley(2, 500, new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10)), new NullMoveCounter());
		Player stanley_4_250 = createStanley(4, 250, new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10)), new NullMoveCounter());
		Player stanley_2_1250 = createStanley(2, 1250, new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10)), new NullMoveCounter());
		Player stanley_4_625 = createStanley(4, 625, new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10)), new NullMoveCounter());
		
		Player ruby_100 = createRuby(1, 100, new NullMoveCounter());
		Player ruby_300 = createRuby(1, 300, new NullMoveCounter());		
		Player ruby_500 = createRuby(1, 500, new NullMoveCounter());
		Player ruby_1000 = createRuby(1, 1000, new NullMoveCounter());
		Player ruby_2500 = createRuby(1, 2500, new NullMoveCounter());
		Player ruby_5000 = createRuby(1, 5000, new NullMoveCounter());

		Player[] players0 = new Player[] {
			stanley_300,
			stanley_500,
			stanley_1000,
		};
		Player[] players1 = new Player[] {
			stanley_2_500,
			stanley_4_250,
			stanley_2_1250,
			stanley_4_625,
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
	
	
}
