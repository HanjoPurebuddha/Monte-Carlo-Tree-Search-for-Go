package ai;


import java.util.ArrayList;

import mcts.ElapsedTimer;
import mcts.UCB;
import game.Color;
import game.Game;
import game.SimulateGame;
import gtp.Vertex;
import mcts.TreeNode;

/**
 * A player that uses MCTS.
 * @author Thomas Ager (with thanks to mcts.ai)
 */

public class MCTSPlayer extends Player {
	
	int time;
	int iterations;
	boolean rememberTree;
	Configuration nodeRuleSet;
	TreeNode playNode;
	OpeningBook openingBook = null;
	boolean useOpeningBook;
	boolean surrender = false;
	public MCTSPlayer(int time, int iterations, boolean surrender, boolean rememberTree,  boolean useOpeningBook, 
			boolean binaryScoring, boolean uct, boolean rave, boolean weightedRave, double initialWeight, double finalWeight, 
			 int raveSkip, double firstPlayUrgency, double bonusPatterns, double bonusAvoidEyes, int explorationWeight,
			 boolean simulateAvoidEyes, boolean simulateAtari, boolean simulatePatterns, boolean simulateTakePieces, boolean simulateMercyRule,
			 double varySimEyes, double varySimAtari, double varySimPatterns, double varySimPieces,
			 boolean pickMostSimulated, boolean pickHighestMean, boolean pickUCB,  boolean clearMemory,
			 int pruneNodes, int developPruning,
			 boolean ucb, boolean simpleUcb, boolean randomUcb, boolean ucbTuned,
	    		boolean captureScoring, boolean livingScoring, boolean averageScoring, int evenScoring) {
		super("TimedPlayer");
		this.time = time;
		this.iterations = iterations;
		this.rememberTree = rememberTree;
		this.useOpeningBook = useOpeningBook;
		this.surrender = surrender;
		/* set the values for different features */
    	this.nodeRuleSet = new Configuration(binaryScoring, uct, rave, weightedRave, 
    			initialWeight, finalWeight, raveSkip, firstPlayUrgency, bonusPatterns, bonusAvoidEyes, explorationWeight,
    			simulateAvoidEyes, simulateAtari, simulatePatterns, simulateTakePieces, simulateMercyRule,
    			varySimEyes, varySimAtari, varySimPatterns, varySimPieces,
    			pickMostSimulated, pickHighestMean, pickUCB,clearMemory, pruneNodes, developPruning,
    			ucb, simpleUcb, randomUcb, ucbTuned, captureScoring, livingScoring, averageScoring, evenScoring);//
    	
    	 
	}
	public void setGame(Game game) {
		this.game = game;
		if(openingBook == null && useOpeningBook) {
			this.openingBook = new OpeningBook(game.getSideSize(), game);//
			this.firstMoves = new int[40];
		}
	}
	
	
	int[] firstMoves;
	private int movesTaken = 0;
	public int playMove() {
		int move = 0;
		movesTaken +=2;
		if(surrender && movesTaken >= ((game.getSideSize() * game.getSideSize()) -1) && game.mercy()) {
			/* just end the game */
			return -2;
		}
		if(useOpeningBook && game.getMove(0) != -3 && openingBook.movesTaken < 15) {
			
			firstMoves[openingBook.movesTaken] = game.getMove(0);
			openingBook.movesTaken++;
		}
		if(useOpeningBook && openingBook.movesTaken < 15 && openingBook.playOpeningBookMove(firstMoves)) {
			move = openingBook.move;
			/* play the move on the board for this player */
		    game.play(move);
		    firstMoves[openingBook.movesTaken] = move;
		    openingBook.movesTaken++;
		    noTree = false;
		} else {
			/* if the tree hasn't been initialized or we aren't remembering the tree */
			if(!noTree || !rememberTree) {
			/* initialize the node that represents the players current position */
				noTree = true;
				UCB ucbTracker = new UCB(nodeRuleSet);
				playNode = new TreeNode(game, game.getMove(0), 0, side, nodeRuleSet, ucbTracker);
			} else if(rememberTree) {
				TreeNode oldPlayNode = playNode;
				/* set the current node to the child of the previous last move played that matches the move last played */
		    	playNode = playNode.getChild(game.getMove(0));
		    	if(nodeRuleSet.clearMemory) {
		    		oldPlayNode.clearParentMemory(playNode, oldPlayNode.getChildren());
		    	}
		    	if(nodeRuleSet.pruneNodes > 0) {
		    		playNode.pruneNodes();
		    	}
		    }
			System.out.println("Before developing");
			/* if the player is on time or iterations */
			if(time > 0) {
				ElapsedTimer t = new ElapsedTimer();
				while(t.elapsed() < time) {
			        /* develop the tree in the node with the players current position recorded */
					playNode.developTree();
					if(nodeRuleSet.checkPruning()) {
						playNode.pruneNodes();
					}
			       
			    }
			}
			if (iterations > 0) {
				for(int i=0;i<iterations;i++) {
			        /* develop the tree in the node with the players current position recorded */
					playNode.developTree();
					if(nodeRuleSet.checkPruning()) {
						playNode.pruneNodes();
					}
			    }
			}
			System.out.println("After developing");
		    /* select the move from within the node with the developed tree, making use of the recorded position */
		    move = playNode.getHighestValueMove();
		    System.out.println("After move chosen");
	
		    
		    /* play the move on the board for this player */
		    game.play(move);
		    System.out.println("After move played");
		    if(useOpeningBook && openingBook.movesTaken < 15) {
			    firstMoves[openingBook.movesTaken] = move;
			    openingBook.movesTaken++;
		    }
		    System.out.println("After move played");
		    /* if remembering the tree */
		    if(rememberTree) {
		    	TreeNode oldPlayNode = playNode;
				/* set the current node to the child of the previous last move played that matches the move last played */
		    	playNode = playNode.getChild(game.getMove(0));
		    	System.out.println("Weight of chosen node: " + playNode.ucbTracker.calculateWeight(playNode) + " "); //
		    	if(nodeRuleSet.clearMemory) {
		    		oldPlayNode.clearParentMemory(playNode, oldPlayNode.getChildren());
		    	}
		    }
		}

    	System.gc();
	    /* return the move */
	    return move;
		
	}
	
}
