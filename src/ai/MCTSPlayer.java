package ai;


import java.util.ArrayList;

import mcts.ElapsedTimer;
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
	
	public MCTSPlayer(int time, int iterations, boolean rememberTree,
			boolean binaryScoring, boolean uct, boolean rave, boolean weightedRave, double initialWeight, double finalWeight, 
			 int raveSkip, boolean dontExpandEyes, int dynamicTree, double firstPlayUrgency, 
			 boolean simulateAvoidEyes, boolean simulateAtari, boolean simulatePatterns, boolean simulateTakePieces, boolean simulateMercyRule,
			 boolean pickMostSimulated, boolean pickHighestMean, boolean pickUCB) {
		super("TimedPlayer");
		this.time = time;
		this.iterations = iterations;
		this.rememberTree = rememberTree;
		/* set the values for different features */
    	this.nodeRuleSet = new Configuration(binaryScoring, uct, rave, weightedRave, 
    			initialWeight, finalWeight, raveSkip, dontExpandEyes, dynamicTree, firstPlayUrgency,
    			simulateAvoidEyes, simulateAtari, simulatePatterns, simulateTakePieces, simulateMercyRule,
    			pickMostSimulated, pickHighestMean, pickUCB);
    	
	}

	public int playMove() {
		
		/* if the tree hasn't been initialized or we aren't remembering the tree */
		if(!noTree || !rememberTree) {
		/* initialize the node that represents the players current position */
			noTree = true;
			//System.out.println(game.getMove(0));
			playNode = new TreeNode(null, game, game.getMove(0), 0, nodeRuleSet.firstPlayUrgencyValue(), side, false, nodeRuleSet);
		} else if(rememberTree) {
			
			/* set the current node to the child of the previous last move played that matches the move last played */
	    	playNode = playNode.getChild(game.getMove(0));
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
	    System.out.println(playNode.calculateWeight(playNode)); 
	    /* select the move from within the node with the developed tree, making use of the recorded position */
	    int move = playNode.getHighestValueMove();

	    /* if the opposing players move was a pass and the players current move is useless */
	    if(game.getMove(0) == -1 && getMoveValue(move) <= 0) {
	    	
	    	/* make the move a pass */
	    	move = -1;
	    	
	    }
	    
	    /* play the move on the board for this player */
	    game.play(move);
	    
	    /* if we are remembering the tree */
	    if(rememberTree) {
	    	/* set the child node to the child of the current node that matches the move */
	    	playNode = playNode.getChild(game.getMove(0));
	    }
	    
	    /* return the move */
	    return move;
		
	}
	
}
