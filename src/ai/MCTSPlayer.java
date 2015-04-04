package ai;


import java.util.ArrayList;

import mcts.ElapsedTimer;
import game.Color;
import game.Game;
import game.SemiPrimitiveGame;
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
	
	public MCTSPlayer(int time, int iterations, boolean rememberTree,
			boolean binaryScoring, boolean uct, boolean rave, boolean weightedRave, int weight, boolean heuristicRave, int raveHeuristic, boolean raveSkip) {
		super("TimedPlayer");
		this.time = time;
		this.iterations = iterations;
		/* set the values for different features */
    	this.binaryScoring = binaryScoring;
    	this.uct = uct;
    	this.rave = rave;
    	this.weightedRave = weightedRave;
    	this.weight = weight;
    	this.heuristicRave = heuristicRave;
    	this.raveHeuristic = raveHeuristic;
    	this.raveSkip = raveSkip;
    	this.rememberTree = rememberTree;
    	
	}

	public void setGame(Game game) {
		//System.out.println(game);
		this.game = game;
		//System.out.println(this.game);
	}
	boolean noTree;
	TreeNode playNode;
	TreeNode childNode;
	public int playMove() {
		
		/* if the tree hasn't been initialized or we aren't remembering the tree */
		if(!noTree || !rememberTree) {
		/* initialize the node that represents the players current position */
			noTree = true;
			playNode = new TreeNode(this.game, side, 1, 0, new ArrayList<TreeNode>(),
				binaryScoring,  uct,  rave, null,  weightedRave,  weight,  heuristicRave,  raveHeuristic,  raveSkip);
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
	        
	    /* select the move from within the node with the developed tree, making use of the recorded position */
	    int move = playNode.getMove();

	    /* if the opposing players move was a pass and the players current move is useless */
	    if(game.getMove(0) == -1 && getMoveValue(move) <= 0) {
	    	
	    	/* make the move a pass */
	    	move = -1;
	    	
	    }
	    
	    /* play the move on the board for this player */
	    game.play(move);
	    //game.recordMove(move);
	    
	    /* if we are remembering the tree */
	    if(rememberTree) {
	    	/* set the child node to the child of the current node that matches the move */
	    	playNode = playNode.getChild(game.getMove(0));
	    }
	    
	    /* return the move */
	    return move;
		
	}
	
}
