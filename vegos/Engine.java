package com.ideanest.vegos;

import java.text.DecimalFormat;

import com.ideanest.vegos.ai.*;
import com.ideanest.vegos.game.*;
import com.ideanest.vegos.gtp.*;
import com.ideanest.vegos.gtp.Move;
import com.ideanest.vegos.gtp.Vertex;

/**
 * 
 * @author Piotr Kaminski
 */
public class Engine {
	
	private boolean allowSuicides = false;
	private Game game = createGame(9, allowSuicides);  // some default game, to avoid null checks
	private Player[] players = new Player[2];
	{
		players[0] = new Stanley();
		players[1] = new Ruby();
	}
	
	private final DecimalFormat floatFormat = new DecimalFormat("0.0");
	
	public String name() {return "Vegos";}
	public String version() {return "0.1";}
	
	private Game createGame(int size, boolean allowSuicides) {
		 return new SemiPrimitiveGame(size);
	}
	
	public void boardsize(int size) {
		endGame();
		game = createGame(size, allowSuicides);
	}
	
	public void clear_board() {
		endGame();
		game = createGame(game.getSideSize(), allowSuicides);
	}
	
	public void allow_suicides(boolean flag) {
		endGame();
		this.allowSuicides = flag;
		game = createGame(game.getSideSize(), allowSuicides);
	}
	
	public void komi(float komi) {
		game.setKomi(komi);
	}
	
	public Vertex[] fixed_handicap(int numStones) {
		// this only works on an empty board, no need to check if game is being played
		game.setFixedHandicap(numStones);
		Board.PositionList stones = game.getNonEmptyPoints();
		assert stones.size() == numStones;
		Vertex[] vs = new Vertex[numStones];
		final Board.Grid grid = game.getGrid();
		for (int i=0; i<numStones; i++) {
			vs[i] = new Vertex(grid.x(stones.get(i)), grid.y(stones.get(i)));
		}
		return vs;
	}
	
	private void endGame() {
		for (int i = 0; i < players.length; i++) {
			if (players[i].getPlayingColor() != null) players[i].endGame();
		}
	}
	
	/**
	 * Set the AI that will take care of playing moves for the given color.
	 * @param color the side the AI will take
	 * @param className the name of the Player subclass to use; "com.ideanest.vegos.ai" package by default
	 */
	public void set_ai(Color color, String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class aiClass;
		try {
			aiClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			aiClass = Class.forName("com.ideanest.vegos.ai." + className);
		}
		Player p = (Player) aiClass.newInstance();
		int i = color.getIndex();
		if (players[i].getPlayingColor() != null) players[i].endGame();
		players[i] = p;
	}
	
	public void play(Move move) {
		if (game.isOver()) throw new IllegalStateException("game is over, no more moves allowed");
		if (move.color != game.getNextToPlay()) {
			// play out of order, simulate a pass
			boolean r = game.play(Game.MOVE_PASS);
			assert r;
		}
		assert move.color == game.getNextToPlay();
		boolean r = game.play(move.vertex.toPosition(game.getGrid()));
		if (!r) throw new IllegalArgumentException("illegal move");
	}
	
	public Vertex genmove(Color color) {
		if (game.isOver()) throw new IllegalStateException("game is over, no more moves allowed");
		Player player = players[color.getIndex()];
		if (player.getPlayingColor() != color) {
			// bring player into the game
			player.startGame(game, color);
		}
		int move = player.playMove();
		return Vertex.get(move, game.getGrid());
	}
	
	public String final_score() {
		float score = game.score();
		if (score == 0) return "0";
		else if (score > 0) return "B+" + score;
		else return "W+" + (-score);
	}
	
	public String play_values(Color pointOfView) {
		StringBuffer buf = new StringBuffer("\n");
		Player p = players[pointOfView.getIndex()];
		for (int i=game.getSideSize(); i>=1; i--) {
			for (int j=1; j<=game.getSideSize(); j++) {
				float val = p.getMoveValue(game.getGrid().at(j,i));
				if (val == Float.NaN) buf.append('?');
				else buf.append(floatFormat.format(val));
				buf.append(' ');
			}
			buf.append('\n');
		}
		return buf.toString();
	}
	
	public String play_confidences(Color pointOfView) {
		StringBuffer buf = new StringBuffer("\n");
		Player p = players[pointOfView.getIndex()];
		for (int i=game.getSideSize(); i>=1; i--) {
			for (int j=1; j<=game.getSideSize(); j++) {
				float val = p.getMoveConfidence(game.getGrid().at(j,i));
				if (val == Float.NaN) buf.append('?');
				else buf.append(floatFormat.format(val));
				buf.append(' ');
			}
			buf.append('\n');
		}
		return buf.toString();
	}
	
	public String showboard() {
		StringBuffer buf = new StringBuffer("\n");
		for (int i=game.getSideSize(); i>=1; i--) {
			for (int j=1; j<=game.getSideSize(); j++) {
				Point p = game.getPoint(game.getGrid().at(j,i));
				char c;
				if (p == Point.EMPTY) c = '.';
				else if (p == Color.BLACK) c = 'x';
				else if (p == Color.WHITE) c = 'o';
				else c = '?';
				buf.append(c);
			}
			buf.append('\n');
		}
		return buf.toString();
	}
	
	public void start_cycle(Color color, int numIters) {
		Player player = players[color.getIndex()];
		if (player.getPlayingColor() != color) {
			// bring player into the game
			player.startGame(game, color);
		}
		((SimulatedAnnealingPlayer) player).startCycle(numIters);
	}
	
	public void sim_cycle(Color color, int numIters) {
		((SimulatedAnnealingPlayer) players[color.getIndex()]).simulateCycle(numIters);
	}
	
	public String candidates(Color color) {
		WeightedMoveList wmoves = ((SimulatedAnnealingPlayer) players[color.getIndex()]).getCurrentCycleCandidates();
		StringBuffer buf = new StringBuffer("\n");
		for (int i=game.getSideSize(); i>=1; i--) {
			for (int j=1; j<=game.getSideSize(); j++) {
				float val = wmoves.getWeight(game.getGrid().at(j,i));
				if (val == Float.NaN) buf.append('?');
				else buf.append(floatFormat.format(val));
				buf.append(' ');
			}
			buf.append('\n');
		}
		return buf.toString();
	}
}
