import gtp.GTPClient;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

import ai.Player;
import ai.MCTSPlayer;
import ai.SimulatePlayer;
import mcts.ElapsedTimer;
import mcts.TreeNode;
import game.*;
import gtp.*;

/**
 * A random player that can connect to a game using the GTP (v2) protocol.  It doesn't
 * keep track of board state, and just randomly generates move based on the board size.
 * It's likely many of the generated moves will be illegal, but hopefully the server will
 * deal with that somehow.  Only the basic GTP commands are implemented.
 * 
 * Note that the public method names must match the GTP protocol commands <em>precisely</em>
 * in spelling, case and argument types.  You can add more methods here, to implement the optional
 * parts of the GTP protocol, and also to add your own commands.  Many GTP GUIs allow you to enter
 * arbitrary commands to converse with your engine.
 * 
 * Note that the GTPClient itself implements the following basic commands:
 *   protocol_version known_command list_commands help quit
 * 
 * @author Piotr Kaminski
 */
public class MCTSEngine {
	
	private int boardSize;
	private boolean allowSuicides = false;
	private Game game = null;  // some default game, to avoid null checks
	
	public String name() {return "MCTS";}
	public String version() {return "0.1";}	
	
	
	int iterations = 1000; // 3000 = 10 seconds max capacity
	int time = iterations*2;
	private Player[] players = new Player[2];
	{
		// time, iterations, pers/non-pers, surrender, ADD RESIGNING?, opening book, selectRandom
		// binaryScoring,  uct,  amaf,  rave,  weight,  amafSkip, bonusFpu,
		// first play urgency, bonusPatterns, bonusAvoidEyes, explorationWeight
		// simulate avoid eyes, simulate atari, simulate patterns, simulate taking pieces, sim mercy
		// varySimEyes, varySimAtari, varySimPatterns, varySimPieces
		// most simulated, highest mean value, UCB
		// clearMemory, pruneNodes, developPruning
		// ucb, simpleUCB, randomUcb, UCB-Tuned
		// captureScoring, livingScoring, averageScoring, evenScoring
		
		// time, iterations, pers/non-pers, surrender, opening book, selectRandom
		// binaryScoring,  uct,  amaf,  rave,  weight,  amafSkip, bonusFpu,
		// first play urgency, bonusPatterns, bonusAvoidEyes, explorationWeight
		// simulate avoid eyes, x, simulate patterns, x, sim mercy
		// varySimEyes, x, varySimPatterns, x
		// most simulated, highest mean value, UCB
		// clearMemory, pruneNodes, developPruning
		// ucb, simpleUCB, randomUcb, UCB-Tuned
		// x, livingScoring, x, evenScoring
		
		//black
        players[0] = new MCTSPlayer(0, iterations, true, true, false, true, //how good is opening book, pers
				true, false, false, true, 1, 0, 1000, 20, 0, //is aAmaf effective? Y/N, is UCT effective? Y/N
				200, 500, -5000, 0, //how good is bonusPatterns, bonusAvoid eyes, || is exploration weight effective? Y/N
				true, false, true, false, true, // how good is simulating avoid eyes, simulating avoid patterns, mercy
				0.01, 0, 0.3, 0, /*how good is varying sim eyes, */ /**varying sim patterns 0.3 */ //0.5 //0.75
				true, false, false, false, /**how good is most sim, as to most mean and most UCB  */
				true, 2, 0, /**how good is dev pruning vs no dev pruning timed,*/ // if dev pruning is good how good is 2 dev pruning to 4 dev pruning */
				false, false, false, true, /**how good is ucbTuned vs normal UCB, normal UCB vs simple UCB*/
				false, true, false, 0); //how good is higher even scoring vs lower even scoring
		//white
		players[1] = new MCTSPlayer(0, iterations, true, true, true, true,
				true, false, false, true, 1, 0, 1000, 20, 0,
				200, 500, -5000, 0, 
				true, false, true, false, true,
				0.01, 0, 0.3, 0,
				true, false, false, false,
				true, 4, 0, 
				false, false, false, true,
				false, true, false, 15);
		//white
		/*players[0] = new MCTSPlayer(0, iterations, false, false, false,
				false, true, false, false, 1, iterations * 1, 20, 
				0, 0, 0, iterations * 2,
				true, false, true, false, true,
				0.01, 0.1, 0, 0.1,
				true, false, false,
				true, 2, 20,
				false, false, false, true,
				false, true, false, 15);
		//white
		players[1] = new MCTSPlayer(0, iterations, false, false, false,
				false, true, false, false, 1, iterations * 1, 20, 
				0, 0, 0, iterations * 2,
				true, false, true, false, true,
				0.01, 0.1, 0, 0.1,
				true, false, false,
				true, 2, 20,
				false, false, false, true,
				false, true, false, 15);*/
		//players[0] = new SimulatePlayer(true, false, true, false, true, 0.1, 0.1, 0.1, 0.1);
		//players[1] = new SimulatePlayer(true, false, true, false, true, 0.1, 0.1, 0.1, 0.1);
		
		//players[0] = new RandomPlayer();
		//players[1] = new RandomPlayer();
	}

	public void boardsize(int size) {
		this.boardSize = size;
		game = createGame(boardSize, true);
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
			//players[i].endGame();
		}
		
	}
	
	/**
	 * Set the AI that will take care of playing moves for the given color.
	 * @param color the side the AI will take
	 * @param className the name of the Player subclass to use; "ai" package by default
	 */
	/*public void set_ai(Color color, String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class aiClass;
		try {
			aiClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			aiClass = Class.forName("ai." + className);
		}
		Player p = (Player) aiClass.newInstance();
		int i = color.getIndex();
		if (players[i].getPlayingColor() != null) players[i].endGame();
		players[i] = p;
	}*/
	

	
	public String final_score() {
		float score = game.score();
		if (score == 0) return "0";
		else if (score > 0) return "B+" + score;
		else return "W+" + (-score);
	}
	
	/*public String play_values(Color pointOfView) {
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
	}*/
	
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
	
	/*public void start_cycle(Color color, int numIters) {
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
	}*/
	
	/*public void play(Move move) {
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
	}*/
	
	public void play(Move move) {
		
		/* if the game is over or it isn't our move */
		if (game.isOver() || move.color != game.getNextToPlay()) { //throw new IllegalStateException("game is over, no more moves allowed");
			// play out of order, simulate a pass
			boolean r = game.play(Game.MOVE_PASS);
			assert r;
		}
		assert move.color == game.getNextToPlay();
		boolean r = game.play(move.vertex.toPosition(game.getGrid()));
		game.recordMove(move.vertex.toPosition(game.getGrid()));
		//if (!r) throw new IllegalArgumentException("illegal move");
	}
	
	boolean noTree = false;
	
	public Vertex genmove(Color color) {
		//try to use policy/open book/etc to find a move, if it fails...
		//get the move with the UCT tree with the game given
		
		Player player = players[color.getIndex()];
		if (player.getPlayingColor() != color) {
			// bring player into the game
			player.startGame(game, color);
		}
		//System.out.println(game);
		player.setGame(game);
		//System.out.println(player.game);
		//player.initializeTree();
		int move;
		
		if (game.isOver()) { //throw new IllegalStateException("game is over, no more moves allowed");
			move = -1;
		} else {
			move = player.playMove();
		}
		return Vertex.get(move, game.getGrid());
		
	}
	
	private Game createGame(int size, boolean allowSuicides) {
		//return new SimulateGame(size);
		return new TrompTaylorGame(size, false);
	}
	
	/**
	 * Start the engine as a GTP client on standard in/out streams.  You could attach it to sockets
	 * instead, if playing over a network, etc.
	 */
	public static void main(String[] args) {
		GTPClient client = new GTPClient(new InputStreamReader(System.in), new OutputStreamWriter(System.out));
		client.registerController(new MCTSEngine());
		client.run();	// could do client.start() instead to start it on another thread
	}
}
