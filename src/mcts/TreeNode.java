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

public class TreeNode extends Configuration {
    static Random r = new Random();
    static int nActions = 5;
    static double epsilon = 1e-6;
    Game currentGame;
     //TreeNode previousNode;
    List<TreeNode> children = new ArrayList<TreeNode>();
    double nVisits, totValue;
    Color playerColor;
    boolean testing = false;
    private final Random rnd = new Random();
    boolean amafVisited = false;
    Color[] amafMap;
    public int move;
    int amountOfNodes;
    public TreeNode(Game game, Color playerColor, int move, int amountOfNodes,
    		boolean binaryScoring, boolean uct, boolean rave, Color[] amafMap, boolean weightedRave, int weight, 
    		boolean heuristicRave, int raveHeuristic, boolean raveSkip) {
    	//System.out.println(game);
    	/* set the game so each node represents a gamestate */
    	this.currentGame = game;
    	
    	/* set the color to determine the move from */
    	this.playerColor = playerColor;
    	this.amountOfNodes = 0;
    	this.move = move;
    	
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
    	//System.out.println("children: " +children.size());
    	for(TreeNode c : children) {
    		//System.out.println(c.move + ":" + lastMove + " ");
    		if(c.move == lastMove) {
    			//System.out.println("found child" + c.move + c);
    			return c;
    		}
    	}
    	return this;
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
		        if(rave) {
		        	updateStatsRave(node, value);
		        }
	        }
        
	        //System.out.println("tree developed, size " + visited.size());
        //System.out.println(amountOfNodes + " ");
    }
    
    public int getMove() {
    	/* get the highest uct value child node of the gamestate given */
    	//System.out.println("getting move");
    	TreeNode chosenNode = select();
    	if(chosenNode != null) {
    		return nodeMove(chosenNode);
    	} else {
    		return -1;
    	}
    	
    }
    
    public int nodeMove(TreeNode node) {
    	Game nodeGame = node.getGame();
    	int move = nodeGame.getMove(0);
    	return move;
    }
    
    /* expand the tree node */
    
    public void expand() {
    	
    	/* get all of the empty points on the board */
    	PositionList emptyPoints = currentGame.board.getEmptyPoints();
    	//System.out.println(emptyPoints.size());
    	int sizeOfPoints = emptyPoints.size();
    	print(sizeOfPoints);
    	int amountOfChildren = 0;
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
        		amountOfNodes++;
        		/* create a new child for that point */
        		TreeNode newChild = new TreeNode(normalGame, playerColor, amountOfNodes, emptyPoints.get(i)
        				, binaryScoring,  uct,  rave, amafMap, weightedRave,  weight,  heuristicRave,  raveHeuristic,  raveSkip);
        		
        		/* and add it to the current nodes children */
        		children.add(newChild);
        		amountOfChildren++;
        	} else {
        		print("cant play");
        	}
        	
        }
       // System.out.println(amountOfChildren);
        /* add a pass move */
		//TreeNode newChild = new TreeNode(this.getGame().duplicate(), playerNode, playerColor);
		
		/* and add it to the current nodes children */
		//children.add(newChild);
        
    }
    
    public void prune() {
    	//
    }

    private TreeNode select() {
    	
    	/* initialize the values, with the bestvalue put at its smallest possible value */
        TreeNode selected = null;
        double bestValue = -1;
        /* for every child node of the current node being selected */
       //System.out.println(" children: " +children.size() + " ");
       
	        for (TreeNode c : children) {
	        	/* if we are using UCT at all calculate it */
	        	if (uct) {
	        		/* calculate the uct value of that child */ // small random number to break ties randomly in unexpanded nodes
	
	        		double uctValue = getUctValue(c.totValue, c.nVisits);
	        		//print("UCT value = " + uctValue);
	        		
	        		/* if we are just using UCT, or just using RAVE */
	                if (!rave || rave && !weightedRave && !heuristicRave) {
	    	            /* if the uctvalue is larger than the best value */
	    	            if (uctValue > bestValue) {
	    	            	
	    	            	/*the selected node is that child */
	    	                selected = c;
	    	                /* and the best value is the current value */
	    	                bestValue = uctValue;
	    	                
	    	            }
	                } else { /* if we are using RAVE and UCT */
	                	/* if we arent using the weightedrave or heuristic rave calculate the bestValue using both uct and RAVE */
	                	if(!weightedRave && !heuristicRave) {
	                	}
	                	
	                	/* if we are using weightedRave */
	                	if(weightedRave) {
	                		/* calculate the bestValue using rave, uct and a weight to make rave more effective early on */
	                	}
	                	
	                	/* if we are using heuristicRave */
	                	if(heuristicRave) {
	                		/* calculate the bestValue using rave, uct, and an additional heuristic value */
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
    	//if(tn.amafVisited)
    	for (TreeNode c : tn.children) {
    		//c.amafVisited = true;
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
		this.amafMap = new Color[tn.getGame().getSideSize() * tn.getGame().getSideSize()];

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
    		if (rave) {
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