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
	
	SemiPrimitiveGame game;
	
	public Tom() {
		super("Tom");
		
	}

	public void setGame(SemiPrimitiveGame game) {
		this.game = game;
		
	}

	public int playMove() {
		
		/* initialize the node that represents the players current position */
		TreeNode tn = new TreeNode(game, null, side);
		
		/* create a node that has the players current position recorded */
		TreeNode withPlayerNode = new TreeNode(game, tn, side);
		
		/* develop the tree in the node with the players current position recorded */
        withPlayerNode.developTree();
        
        /* select the move from within the node with the developed tree, making use of the recorded position */
        int move = withPlayerNode.getMove();
        
        /* play the move on the internal game board */
        withPlayerNode.getGame().play(move);
        
        /* return the move */
		return move;
		
	}
	
}
