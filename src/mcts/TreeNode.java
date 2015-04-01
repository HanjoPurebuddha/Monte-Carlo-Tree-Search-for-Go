package mcts;

import java.util.LinkedList;

import game.*;
import game.Board.PositionList;
import gtp.Vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ai.Player;
import ai.Randy;

public class TreeNode {
    static Random r = new Random();
    static int nActions = 5;
    static double epsilon = 1e-6;
    SemiPrimitiveGame currentGame;
     //TreeNode previousNode;
    List<TreeNode> children = new ArrayList<TreeNode>();
    double nVisits, totValue;
    TreeNode playerNode;
    Color playerColor;
    boolean testing = false;
    private final Random rnd = new Random();
    
    /* begin values for adjusting different features */
    
    boolean binaryScoring = false;
    boolean RAVE = false;
    int raveParameter = 0;
    boolean RAVESkip = false;
    
    public TreeNode(Game game, TreeNode playerNode, Color playerColor, 
    		boolean binaryScoring, boolean RAVE, int raveParameter, boolean RAVESkip) {
    	//System.out.println(game);
    	/* set the game so each node represents a gamestate */
    	this.currentGame = game.createSimulationGame();
    	
    	/* set the node to determine the move from */
    	this.playerNode = playerNode;
    	
    	/* set the color to determine the move from */
    	this.playerColor = playerColor;
    	
    	/* set the values for different features */
    	this.binaryScoring = binaryScoring;
    	this.RAVE = RAVE;
    	this.raveParameter = raveParameter;
    	this.RAVESkip = RAVESkip;
    	
    }
    
   /* public List<GameState> getPath() {
        List<GameState> res;
        if (previousNode != null)
            res = previousNode.getPath(); //gets state objects for all previous nodes
        else
            res = new ArrayList();
        res.add(st);  //adds current state to end of object list
        return res;
    }       */ 
    
   /* public List<Action> getActions() {
        List<Action> res;
        if (previousNode != null){
            res = previousNode.getActions();
            res.add(lastAction);
        }
        else
            res = new ArrayList();       
        return res;
    }  */
    
    /* return the current game */
    
    public SemiPrimitiveGame getGame() {
    	return currentGame;
    }
    
    /* return the size of the board for the node's game */
    
    public int boardSize() {
    	return this.getGame().board.getSideSize();
    }

    /* print all the moves of the children of the current node */
    
    public void printChildren() {
    	for(TreeNode c : children) {
    		print(c.getGame().getMove(-1));
    	}
    }
    
    /* develop the tree */
    
    public void developTree() {
    	
    	/* create a list of all visited nodes to backpropogate later */
        List<TreeNode> visited = new LinkedList<TreeNode>();
        
        /* set the current node to this node, and add it to the visited list */
        TreeNode cur = this;
        visited.add(this);
        
        /*while we aren't at a leaf node */
        while (!cur.isLeaf()) {
        	/* select the next node, and add it to the visited list */
            cur = cur.select();
            print("Adding: " + cur);
            visited.add(cur);
        }
        
        /*once we've reached a leaf node, expand it */
        print("expanding" + cur);
        cur.expand();
        
        try {
	        /* and select the expanded node, adding it to the visited list */
	        print("selecting");
	        TreeNode newNode = cur.select();
	        print("Selected" +newNode);
	        visited.add(newNode);
	        
	        /* get the value for the simulation for the expanded node */
	        print("simulating" + newNode);
	        double value = simulate(newNode);
	        print("got result" + value);
	        
	        /* backpropogate the values of all visited nodes */
	        for (TreeNode node : visited) {
	            node.updateStats(value);
	        }
	        print("tree developed, size " + visited.size());
        } catch(NullPointerException e) {
            return;
        } 
        
    }
    
    public int getMove() {
    	/* get the highest uct value child node of the gamestate given */
    	print("getting move");
    	TreeNode chosenNode = select();
    	Game nodeGame = chosenNode.getGame();
    	int move = nodeGame.getMove(0);
    	return move;
    	
    }
    
    /* expand the tree node */
    
    public void expand() {
    	
    	/* get all of the empty points on the board */
    	PositionList emptyPoints = this.getGame().board.getEmptyPoints();
    	int sizeOfPoints = emptyPoints.size();
    	print(sizeOfPoints);
    	
    	/* for every empty point on the board */
        for (int i=0; i<emptyPoints.size(); i++) {
        	
        	/* duplicate the board */
        	SemiPrimitiveGame replacementGame = this.getGame().duplicate();
        	
        	/* and play one of the empty points */
        	boolean canPlay = replacementGame.play(emptyPoints.get(i));
        	
        	/* checking if it is possible to play that point, checking if we're playing into an eye */
        	if(canPlay && !replacementGame.validateBoard(emptyPoints.get(i), replacementGame.duplicate().board)) {
        		
        		/* create a new child for that point */
        		TreeNode newChild = new TreeNode(replacementGame, playerNode, playerColor
        				, binaryScoring, RAVE, raveParameter, RAVESkip);
        		
        		/* and add it to the current nodes children */
        		children.add(newChild);
        	} else {
        		print("cant play");
        	}
        	
        }
        
        /* add a pass move */
		//TreeNode newChild = new TreeNode(this.getGame().duplicate(), playerNode, playerColor);
		
		/* and add it to the current nodes children */
		//children.add(newChild);
        
    }


    private TreeNode select() {
    	
    	/* initialize the values, with the bestvalue put at its smallest possible value */
    	printChildren();
        TreeNode selected = null;
        double bestValue = Double.MIN_VALUE;
        
        /* for every child node of the current node being selected */
        for (TreeNode c : children) {
        	
        	/* calculate the uct value of that child */ // small random number to break ties randomly in unexpanded nodes
            double uctValue =
                    c.totValue / (c.nVisits + epsilon) +
                            Math.sqrt(Math.log(nVisits+1) / (c.nVisits + epsilon)) +
                            r.nextDouble() * epsilon;
            print("UCT value = " + uctValue);
            
            /* if the uctvalue is larger than the best value */
            if (uctValue > bestValue) {
            	
            	/*the selected node is that child */
                selected = c;
                /* and the best value is the current value */
                bestValue = uctValue;
                
            }
        }
        
        /* and then it is returned */
        return selected;
        
    }

    /* check if the current node is a leaf */ //rewrote this to accomodate the new list format, probably a better way to do it
    
    public boolean isLeaf() {
    	
    	/* if the size of the children of the node is 0, its a leaf */
    	if(children.size() == 0)
    		return true;
    	return false;
        //return children == null;
    	
    }
    
    

    public double simulate(TreeNode tn) {

    	/* create a random player */
    	Randy randomPlayer = new Randy();
    	
    	/* create a duplicate of the game */
    	TreeNode simulateNode = tn;
    	Game simulateGame = simulateNode.getGame();
    	Game duplicateGame = simulateGame.duplicate();
    	
    	/* initialize the game using the duplicate */
    	randomPlayer.startGame(duplicateGame, null);
    	
    	/* until the simulation has finished, play moves */
    	while(!duplicateGame.isOver()) {
    		
    		/* get the move, and play on the board */
    		int move = randomPlayer.playMove();
    		
    		/* record the move, updating to see if the game is over */
    		duplicateGame.recordMove(move);
    		
    	}
    	
    	/* get the score for the players color, positive or negative depending on colour */
    	float score = duplicateGame.score(playerColor);
    	
    	/* if using binary scoring */
    	if(binaryScoring) {
    		/* return 0 for loss, 1 for win */
	    	if(score > 0)
	    		return 1;
	    	return 0;
    	} else {
    		/* return the score value, positive for  */
    		return score;
    	}
    	
    }
    
    public void print(String line) {
    	if(testing)
    		System.out.println(line);
    }
    
    public void print(int line) {
    	if(testing)
    		System.out.println(line);
    }

    public void updateStats(double value) {
        nVisits++;
        totValue += value;
    }

    public int arity() {
        return children == null ? 0 : children.size();
    }
}