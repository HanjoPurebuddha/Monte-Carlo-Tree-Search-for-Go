package mcts;

import ai.Configuration;

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
    Game currentGame;
     //TreeNode previousNode;
    TreeNode parent;
    List<TreeNode> children = new ArrayList<TreeNode>();
    double nVisits, totValue;
    Color playerColor;
    boolean testing = false;
    private final Random rnd = new Random();
    boolean amafVisited = false;
    Color[] amafMap;
    public int move;
    int amountOfNodes;
    Configuration nodeRuleSet;
    public TreeNode(Game currentGame, TreeNode parent, Color playerColor, int move, Configuration nodeRuleSet) {
    	/* set the game so each node represents a gamestate */
    	this.parent = parent;
    	this.currentGame = currentGame;
    	/* set the color to determine the move from */
    	this.playerColor = playerColor;
    	this.move = move;
    	this.nodeRuleSet = nodeRuleSet;
    	
    }

    /* return the current game */
    
    public Game getGame() {
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
    
    /* get the child node that matches the game state given (last move played, from MCTSPlayer) */
    
    public TreeNode getChild(int lastMove) {
    	//System.out.println("Amount of children on parent node: " + children.size());
    	for(TreeNode c : children) {
    		//System.out.println("Amount of children on child node: " + c.children.size());
    		if(c.move == lastMove) {
    			//System.out.println("Found child:" +c.move + " ");
    			return c;
    		}
    	}
    	//System.out.println("No children that match: " + lastMove + " out of " +children.size() + " children. ");
    	
    	/* if we couldn't find a matching expanded child, then create one with the move played out */
    	Game normalGame = currentGame.duplicate();
    	normalGame.play(lastMove);
    	TreeNode newChild = new TreeNode(normalGame, this, playerColor, lastMove, nodeRuleSet);
    	return newChild;
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
        TreeNode newNode = null;
        /* if it was possible to expand the node */
	        /* select the highest value child, adding it to the visited list */
	        print("selecting");
	        newNode = cur.select();
        if(newNode == null) {
        	/* just do a simulation from the current node */
        	newNode = cur;
        } else {
        	print("Selected" +newNode);
	        visited.add(newNode);
	        
        }
	        
	        /* get the value for the simulation for the expanded node */
	        print("simulating" + newNode);
	        double value = simulate(newNode);
	        print("got result" + value);
	        
	       
	        /* backpropogate the values of all visited nodes */
	        for (TreeNode node : visited) {
	            node.updateStats(value);
	            /* update the amaf value for all visited nodes */
		        if(nodeRuleSet.rave) {
		        	updateStatsRave(node, value);
		        }
	        }
    }
    
    public int getMove() {
    	/* get the highest uct value child node of the gamestate given */
    	TreeNode chosenNode = select();
    	if(chosenNode != null) {
    		return chosenNode.move;
    	} else {
    		return -1;
    	}
    	
    }
    
    public int nodeMove(TreeNode node) {
    	//Game nodeGame = node.getGame();
    	//int move = nodeGame.getMove(0);
    	//return move;
    	return node.move;
    }
    
    /* expand the tree node */
    
    public void expand() {
    	
    	/* get all of the empty points on the board */
    	PositionList emptyPoints = currentGame.board.getEmptyPoints();
    	int sizeOfPoints = emptyPoints.size();
    	print(sizeOfPoints);
    	/* for every empty point on the board */
        for (int i=0; i<emptyPoints.size(); i++) {
        	
        	/* duplicate the board */
        	SemiPrimitiveGame duplicateBoard = currentGame.copy();     
        	Game normalGame = currentGame.duplicate();
        	/* and play one of the empty points */
        	boolean canPlay = duplicateBoard.play(emptyPoints.get(i));
        	boolean canPlayDuplicate = normalGame.play(emptyPoints.get(i));
        	/* checking if it is possible to play that point, checking if we're playing into an eye */
        	if(canPlay && canPlayDuplicate) {
        		/* create a new child for that point */
        		TreeNode newChild = new TreeNode(normalGame, this, playerColor, emptyPoints.get(i), nodeRuleSet);
        		
        		/* and add it to the current nodes children */
        		children.add(newChild);
        	} else {
        		print("cant play");
        	}
        	
        }
       // System.out.println(amountOfChildren);
        /* add a pass move */
        Game normalGame = currentGame.duplicate();
        normalGame.play(-1);
		TreeNode newChild = new TreeNode(normalGame, this, playerColor, -1, nodeRuleSet);
		
		/* and add it to the current nodes children */
		children.add(newChild);
        
    }
    
    public void prune() {
    	//
    }

    private TreeNode select() {
    	
    	/* initialize the values, with the bestvalue put at its smallest possible value */
        TreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        /* for every child node of the current node being selected */
       //System.out.println(" children: " +children.size() + " ");
       
	        for (TreeNode c : children) {
	        	/* if we are using UCT at all calculate it */
	        	if (nodeRuleSet.uct) {
	        		/* calculate the uct value of that child */ // small random number to break ties randomly in unexpanded nodes
	
	        		double uctValue = getUctValue(c.totValue, c.nVisits);
	        		print("UCT value = " + uctValue);
	        		
	        		/* if we are just using UCT, or just using RAVE */
	                if (!nodeRuleSet.rave || nodeRuleSet.rave && !nodeRuleSet.weightedRave && !nodeRuleSet.heuristicRave) {
	    	            /* if the uctvalue is larger than the best value */
	                	print("current best value is: "+bestValue);
	    	            if (uctValue > bestValue) {
	    	            	
	    	            	/*the selected node is that child */
	    	                selected = c;
	    	                
	    	                /* and the best value is the current value */
	    	                bestValue = uctValue;
	    	                print("found new bestValue: " + uctValue);
	    	                
	    	            }
	                }
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
    
    public double getUctValue(double totalValue, double visits) {

        return totValue / (nVisits + epsilon) +
                Math.sqrt(Math.log(nVisits+1) / (nVisits + epsilon)) +
                r.nextDouble() * epsilon;
    }

    public void updateStatsRave(TreeNode tn, double simulationResult) {
    	/* for every child of this node */
    	for (TreeNode c : tn.children) {
    		/* if that child contains any move played in the simulation, that matches the players colour */
    		for (int i=0; i<amafMap.length; i++) {
    			/* if the move was played during the simulation */
    			
    			if(amafMap[i] != null) {
    				/* if that move is occupied on the childs board, in the nodes colour */
    				if(amafMap[i] != c.getGame().getNextToPlay()) {
    					/* update the total value of that node with the simulation result */
    					tn.updateStats(simulationResult);
    				}
    			}
    		}
    		/* recursively iterate through the whole subtree */
    		updateStatsRave(c, simulationResult);
    	}
    }

    public double simulate(TreeNode tn) {

    	/* initialize the map of who played where for this simulation */
		amafMap = new Color[tn.getGame().getSideSize() * tn.getGame().getSideSize()];

    	/* create a random player */
    	Randy randomPlayer = new Randy();
    	
    	/* create a duplicate of the game */
    	Game duplicateGame = currentGame.copy();
    	
    	/* initialize the game using the duplicate */
    	randomPlayer.startGame(duplicateGame, null);
    	
    	/* until the simulation has finished, play moves */
    	while(!duplicateGame.isOver()) {
    		
    		/* get the move, and play on the board */
    		int move = randomPlayer.playMove();
    		
    		/* record the move, updating to see if the game is over */
    		duplicateGame.recordMove(move);
    		/* if we are using any variation of rave */
    		if (nodeRuleSet.rave) {
	    		/* if the move isn't a pass */
	    		if(move != -1) {
		    		/* set the current moves colour on the amaf map */
		    		if(randomPlayer.game.getNextToPlay() == playerColor) {
		    			amafMap[move] = playerColor;
		    		} else {
		    			amafMap[move] = playerColor.inverse();
		    		}
	    		}
    		}
    	}
    	
    	/* get the score for the players color, positive or negative depending on colour */
    	float score = duplicateGame.score(playerColor);
    	
    	/* if using binary scoring */
    	if(nodeRuleSet.binaryScoring) {
    		/* return 0 for loss, 1 for win */
	    	if(score > 0)
	    		return 1;
	    	return 0;
    	} else {
    		/* return the score value */
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
        totValue = totValue + value;
        print("updated stats, visits: " + nVisits + " total value: " + totValue);
    }

    public int arity() {
        return children == null ? 0 : children.size();
    }
}