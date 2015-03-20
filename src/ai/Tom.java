package ai;


import game.Color;
import game.Game;
import game.SemiPrimitiveGame;
import gtp.Vertex;
import mcts.TreeNode;

/**
 * A player that uses MCTS.
 * @author Thomas Ager (with thanks to mcts.ai)
 */

public class Tom extends Player {
	
	Game game;
	
	public Tom() {
		super("Tom");
		
	}

	public void setGame(Game game) {
		this.game = game;
		
	}

	public int playMove() {
		
		SemiPrimitiveGame semiPrimitiveGame = new SemiPrimitiveGame(5);
		semiPrimitiveGame.equals(game);

		/* initialize the node that represents the players current position */
		TreeNode tn = new TreeNode(semiPrimitiveGame, null, side);
			
		/* create a node that has the players current position recorded */
		TreeNode withPlayerNode = new TreeNode(semiPrimitiveGame, tn, side);
			
		int n = 100;
	    for (int i=0; i<n; i++) {
	        /* develop the tree in the node with the players current position recorded */
	        withPlayerNode.developTree();
	    }
	        
	    /* select the move from within the node with the developed tree, making use of the recorded position */
	    int move = withPlayerNode.getMove();
	        
	    /* if the person playing the AI didn't play a pass */
	    if(game.getMove(0) > 0) {
		    /* play the move on the internal game board */
		    withPlayerNode.getGame().play(move);
		        
		    /* return the move */
		    return move;
				
		/* if they did play the pass */
	    } else {
	        	
	    	/* check if that move has any value */
	        if(getMoveValue(move) > 0) {
	        		
	        	/*if it does then return it*/
				return move;
			} else {
					
				/*if it doesn't then return pass*/
				return -1;
			}
	    }
		
	}
	
}
