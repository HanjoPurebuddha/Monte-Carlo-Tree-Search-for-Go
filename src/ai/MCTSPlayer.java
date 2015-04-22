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
	public MCTSPlayer(int time, int iterations, boolean rememberTree,
			boolean binaryScoring, boolean uct, boolean rave, boolean weightedRave, double initialWeight, double finalWeight, 
			 int raveSkip, int firstPlayUrgency, int bonusPatterns, int bonusAvoidEyes, 
			 boolean simulateAvoidEyes, boolean simulateAtari, boolean simulatePatterns, boolean simulateTakePieces, boolean simulateMercyRule,
			 boolean pickMostSimulated, boolean pickHighestMean, boolean pickUCB, boolean useOpeningBook,  boolean clearMemory,
			 int pruneNodes, boolean ucb, boolean simpleUcb, boolean randomUcb, boolean ucbTuned) {
		super("TimedPlayer");
		this.time = time;
		this.iterations = iterations;
		this.rememberTree = rememberTree;
		/* set the values for different features */
    	this.nodeRuleSet = new Configuration(binaryScoring, uct, rave, weightedRave, 
    			initialWeight, finalWeight, raveSkip, firstPlayUrgency, bonusPatterns, bonusAvoidEyes,
    			simulateAvoidEyes, simulateAtari, simulatePatterns, simulateTakePieces, simulateMercyRule,
    			pickMostSimulated, pickHighestMean, pickUCB, useOpeningBook, clearMemory, pruneNodes,
    			ucb, simpleUcb, randomUcb, ucbTuned);
    	
    	
	}
	
	public void setOpeningBook() {
		if(this.openingBook == null) {
			this.openingBook = new OpeningBook(game.getSideSize(), game);//
			this.firstMoves = new int[15];
		}
	}
	int[] firstMoves;
	public int playMove() {
		int move = 0;
		if(game.getMove(0) != -3 && openingBook.movesTaken < 15) {
			
			firstMoves[openingBook.movesTaken] = game.getMove(0);
			openingBook.movesTaken++;
		}
		if(openingBook.movesTaken < 15 && nodeRuleSet.openingBook && openingBook.playOpeningBookMove(firstMoves)) {
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
				//System.out.println(game.getMove(0));
				UCB ucbTracker = new UCB(nodeRuleSet);
				playNode = new TreeNode(game, game.getMove(0), 0, side, nodeRuleSet, ucbTracker);
			} else if(rememberTree) {
				TreeNode oldPlayNode = playNode;
				/* set the current node to the child of the previous last move played that matches the move last played */
		    	playNode = playNode.getChild(game.getMove(0));
		 //   	System.out.println("opponent move selected children values:");
		 //   	playNode.printList(playNode.getChildren());
		    	if(nodeRuleSet.clearMemory) {
		    		oldPlayNode.clearParentMemory(playNode, oldPlayNode.getChildren());
		    	}
		    	if(nodeRuleSet.pruneNodes > 0) {
		    		playNode.pruneNodes();
		    	}
		 //   	System.out.println("pruned children values:");
		 //   	playNode.printList(playNode.getChildren());
		    }
			
			/* if the player is on time or iterations */
			if(time > 0) {
				ElapsedTimer t = new ElapsedTimer();
				while(t.elapsed() < time) {
			        /* develop the tree in the node with the players current position recorded */
					playNode.developTree();
			       
			    }
			}
			if (iterations > 0) {
				for(int i=0;i<iterations;i++) {
			        /* develop the tree in the node with the players current position recorded */
					playNode.developTree();
			    }
			}
	//		System.out.println("Getting highest value move");
		    /* select the move from within the node with the developed tree, making use of the recorded position */
		    move = playNode.getHighestValueMove();
		    
	//	    System.out.println("Got it!");
		    /* if the opposing players move was a pass and the players current move is useless */
		    if(game.getMove(0) == -1 && getMoveValue(move) <= 0) {
		    	
		    	/* make the move a pass */
		    	move = -1;
		    	
		    }
		
		    
		    /* play the move on the board for this player */
		    game.play(move);
		    if(openingBook.movesTaken < 15) {
			    firstMoves[openingBook.movesTaken] = move;
			    openingBook.movesTaken++;
		    }
		    /* if remembering the tree */
		    if(rememberTree) {
	//	    	System.out.println("current children values:");
	//	    	playNode.printList(playNode.getChildren());
		    	TreeNode oldPlayNode = playNode;
				/* set the current node to the child of the previous last move played that matches the move last played */
		    	playNode = playNode.getChild(game.getMove(0));
		    	System.out.println("Weight of chosen node: " + playNode.ucbTracker.calculateWeight(playNode) + " "); //
	//	    	System.out.println("move selected children values:");
	//	    	playNode.printList(playNode.getChildren());
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
