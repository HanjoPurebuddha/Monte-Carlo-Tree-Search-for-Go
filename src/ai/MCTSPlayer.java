package ai;


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
	
	public MCTSPlayer(int time, int iterations, 
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
    	
    	
	}

	public void setGame(Game game) {
		
		this.game = game;
	}
	

	public int playMove() {
		
		/* initialize the node that represents the players current position */
		TreeNode tn = new TreeNode(game, side, 1, 0,
				binaryScoring,  uct,  rave, null,  weightedRave,  weight,  heuristicRave,  raveHeuristic,  raveSkip);
		/* if the player is on time or iterations */
		if(time > 0) {
			ElapsedTimer t = new ElapsedTimer();
			while(t.elapsed() < time) {
		        /* develop the tree in the node with the players current position recorded */
				tn.developTree();
		        
		    }
		}
		if (iterations > 0) {
			for(int i=0;i<iterations;i++) {
		        /* develop the tree in the node with the players current position recorded */
				tn.developTree();
		        
		    }
		}
	        
	    /* select the move from within the node with the developed tree, making use of the recorded position */
	    int move = tn.getMove();

	    /* if the opposing players move was a pass and the players current move is useless */
	    if(game.getMove(0) == -1 && getMoveValue(move) <= 0) {
	    	
	    	/* make the move a pass */
	    	move = -1;
	    	
	    }
	    
	    /* play the move on the board for this player */
	    game.play(move);
	    //game.recordMove(move);
	    
	    /* return the move */
	    return move;
		
	}
	
}
