package com.ideanest.vegos.compete;

import java.util.ArrayList;
import java.util.List;

import com.ideanest.vegos.ai.Player;
import com.ideanest.vegos.game.Color;
import com.ideanest.vegos.game.Game;

/**
 * A series of rounds between two players.  Both the players and the rules stay fixed.
 * 
 * @author Piotr Kaminski
 */
public class Match {
	private final Player[] players = new Player[2];
	private final Game game;
	
	private final Statistics stats = new Statistics();

	public Match(Game game, Player blackPlayer, Player whitePlayer) {
		this.game = game;
		players[Color.BLACK.getIndex()] = blackPlayer;
		players[Color.WHITE.getIndex()] = whitePlayer;
	}
	
	public Player getPlayer(Color side) {
		return players[side.getIndex()];
	}
	
	public void play(int numRounds) {
		for (int i=0; i<numRounds; i++) play();
	}
	
	public Game play() {
		System.out.print('.');
		Game g = game.duplicate();
		for (int i=0; i<2; i++) players[i].startGame(g, Color.get(i));
		while (!g.isOver()) {
			players[g.getNextToPlay().getIndex()].playMove();
		}
		for (int i=0; i<2; i++) players[i].endGame();
		stats.add(g);
		return g;
	}
	
	public Statistics getStats() {return stats;}
	
	public Game getGame() {
		return game;
	}

}