package mcts;

import ai.Configuration;
import ai.SimulatePlayer;

import java.util.LinkedList;

import game.*;
import game.Board.PositionList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeNode {

    /* each node has a game with the current move taken */
    Game currentGame;

    /* each node has children */
    List<TreeNode> children = new ArrayList<TreeNode>();
    
    /* initialize the values used in uct, one for uct standard and one for rave */
    double[] nVisits = new double[2];
    double[] totValue = new double[2];
    
    /* record every value to be used for UCB1-Tuned */
    List<Double> normalValues;
    List<Double> raveValues;
    
    /* the counter for skipping rave */
    int raveSkipCounter;
    
    /* the playerColor for use in enforcing positive values for the player in the amaf map */
    Color playerColor;
    
    /* setup the testing variable to allow prints or disallow them */
    boolean testing = false;
    
    Color[] amafMap;
    
    /* each node has the move taken to get to that move */
    public int move;
    
    /* each node has a ruleset defined by the user */
    Configuration nodeRuleSet;
    
    public UCB ucbTracker;
    
    /* set all the values that change for every node */
    public TreeNode(Game currentGame, int move, int raveSkipCounter, //double weight, // NOTE // Might not need to m
    		Color playerColor, Configuration nodeRuleSet, UCB ucbTracker) {
    	this.currentGame = currentGame;
    	this.move = move;
    	this.nodeRuleSet = nodeRuleSet;
    	this.playerColor = playerColor;
    	this.raveSkipCounter = raveSkipCounter;
    	this.ucbTracker = ucbTracker;
    	if(nodeRuleSet.ucbTuned) {
    		this.normalValues = new ArrayList<Double>();
    		this.raveValues = new ArrayList<Double>();
    	}
    	/* a map of colours used to record if a move was in the players color for amaf */
        this.amafMap = new Color[currentGame.getSideSize()*currentGame.getSideSize()];
    }
    
    /* prune the tree of every node that will never be used again */
    public void clearParentMemory(TreeNode avoidClear, List<TreeNode> children) {
    	for(TreeNode c : children) {
    		if(c != avoidClear) {
    			if(c.children.size() > 0) {
    				//System.out.println("Found more children...");
    				clearParentMemory(avoidClear, c.children);
    			}
    			c = null;
    			
    		} else {
    			//System.out.println("not removing this");
    		}
    	}
    }
    
    /* for every child node that has been inactive, prune it and all its children from the tree
     * to economize memory and time */
    public void pruneNodes() {
    	
    	if(children.size() > 0) {
	    	/* for every child of the starting node */
	    	for(TreeNode c : children) {
	    		/* if this node has been visited less than the allowed amount of times */
	    		if(c.nVisits[0] < nodeRuleSet.pruneNodes) {
	    			/* prune it and all of its children */
	    			clearSubTree(c, c.getChildren());
	    		} else {
	    			
	    		}
	    	}
	    	for(int c = 0; c<children.size();c++) {
	    		if(children.get(c) == null) {
		    		/* and remove it from the children */
	    			children.remove(c);
	    		}
	    	}
    	} else {
    		System.out.println("Opponent chose a move that wasn't explored.");
    	}
    }
    
    public void clearSubTree(TreeNode node, List<TreeNode> children) {
    	for(TreeNode c : children) {
			if(c.children.size() > 0) {
				clearSubTree(c, c.children);
			}
			c = null;
    	}
    	node = null;
    }
    
    /* get the child node that matches the game state given (last move played, from MCTSPlayer) */
    public TreeNode getChild(int lastMove) {
		/* for every child, check if the move matches the last move */
    	for(TreeNode c : children) {
    		if(c.move == lastMove) {
    			return c;
    		}
    	}
    	/* if we couldn't find a matching expanded child, then create one with the move played out */
    	Game normalGame = currentGame.duplicate();
    	normalGame.play(lastMove);
    	TreeNode newChild = new TreeNode(normalGame, lastMove, raveSkipCounter, playerColor, nodeRuleSet, ucbTracker);
    	
    	/* and return that one instead */
    	return newChild;
    }
    
    /* develop the tree */
    public void developTree() {
    	/* create a list of all visited nodes to backpropogate later */
        List<TreeNode> visited = new LinkedList<TreeNode>();
        
        /* set the current node to this node, and add it to the visited list */
        TreeNode cur = this;
        visited.add(this);

        while (!cur.isLeaf()) {
        	/* follow the highest uct value node, and add it to the visited list */
        	cur = cur.select();
   //     	System.out.print("selecting1");
            visited.add(cur);
        }
        
        
        /* at the bottom of the tree expand the children for the node, including a pass move
         * but only if it hasn't been expanded before */
   //     System.out.println("expanding");
        
        cur.expand();

        /* get the best child from the expanded nodes, even if it's just passing */
   //     System.out.println("selecting2" );
	    TreeNode newNode = cur.select();
	    visited.add(newNode);
	    
	    /* simulate from the node, and get the value from it */
	//    System.out.println("simulating");
	    double value = simulate(newNode);
	//    System.out.println("got result, updating" + value);
	    
	    /* backpropogate the values of all visited nodes */
	    for (TreeNode node : visited) {
	    	
	    	/* type 0 for just uct updating */
	        node.updateStats(0, value);
	        
	    }
	//    System.out.println("Updating rave");
	    /* and if using rave update the subtree of the parent of the simulated node */
	    if(nodeRuleSet.rave || nodeRuleSet.weightedRave) {
	    	/* based on the amaf map of the node that was just simulated */
	    	updateStatsRave( cur.getChildren(), value);
	    }
    }
    
    
    
    public void printList(List<TreeNode> list) {
    	System.out.println("[");
    	for(TreeNode node : list) {
    		System.out.println(node.nVisits[0] + ", ");
    	}
    	System.out.println("]");
    }
    
    /* get the highest uct value child node of the node given */
    public int getHighestValueMove() {
    	TreeNode highestValueNode = null;
    	if(nodeRuleSet.pickHighestMean) {
    		highestValueNode = getHighestMean();
    	}
    	if(nodeRuleSet.pickMostSimulated) {
    		highestValueNode = getMostSimulated();
    	}
    	if(nodeRuleSet.pickUCB) {
    		System.out.println("size:" + getChildren().size());
    		System.out.println("children size:" + children.size());
    		highestValueNode = findBestValueNode(getChildren());
    	}
    	print("NODE SELECTED:" +highestValueNode);
    	return highestValueNode.move;
    }
    
    public TreeNode getHighestMean() {
    	/* initialize the values, with the bestvalue put at the smallest possible value */
    	TreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        for (TreeNode c : children) {
        	double currentValue = 0;
        	
        	currentValue = c.totValue[0]; // NOTE // Investigate avoiding recalculation if the node stats have not been updated // NOTE //
            if (currentValue > bestValue) {
            	
            	/*the selected node is that child */
                selected = c;
                
                /* and the best value is the current value */
                bestValue = currentValue;
                print("found new bestValue: " + currentValue);
                
            }
        }
        return selected;
    }
    
    public TreeNode getMostSimulated() {
    	/* initialize the values, with the bestvalue put at the smallest possible value */
    	TreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        System.out.println(children.size() + " ");
        for (TreeNode c : children) {
        	double currentValue = 0;
        	System.out.println(c.nVisits[0] + " ");
        	currentValue = c.nVisits[0]; // NOTE // Investigate avoiding recalculation if the node stats have not been updated // NOTE //
    		
            if (currentValue > bestValue) {
            	
            	/*the selected node is that child */
                selected = c;
                
                /* and the best value is the current value */
                bestValue = currentValue;
                
            }
        }
        return selected;
    }
    
    /* expand the children of the node */
    public void expand() {
    	
    	/* get all of the empty points on the board */
    	PositionList emptyPoints = currentGame.board.getEmptyPoints();
    	int emptyPointsSize = emptyPoints.size();
    	int childrenCounter = 0;
    	/* for every empty point on the board */
        for (int i=0; i<emptyPointsSize; i++) {

        	/* just try and play on it normally, ensuring the rules of the game are followed */
        	Game normalGame = currentGame.duplicate();
        	boolean canPlayNormal = normalGame.play(emptyPoints.get(i));
        	
        	/* checking if it is possible to play that point, checking if we're playing into an eye */
        	if(canPlayNormal) {
        		
        		/* create a new child for that point */
        		TreeNode newChild = new TreeNode(normalGame, emptyPoints.get(i), raveSkipCounter, playerColor, nodeRuleSet, ucbTracker);
        		
        		/* and add it to the current nodes children */
        		children.add(newChild);
        		childrenCounter++;
        	} 
        	
        }
        
        /* passing isn't something that should be done unless requesting an end to the game
         * meaning that there is a clear winner, one way or another. with this in mind, passes
         * are only added to the tree when there are few spaces on the board left
         */
       // System.out.println("out");
        if(emptyPointsSize < 7 || childrenCounter == 0) {
        	System.out.println("in");
        	/* add a pass move as well as playing on every allowable empty point */
	        Game passGame = currentGame.duplicate();
	        passGame.play(-1);
			TreeNode passChild = new TreeNode(passGame, -1, raveSkipCounter, playerColor, nodeRuleSet, ucbTracker);
			/* and add it to the current nodes children */
			children.add(passChild);
        }
	//	System.out.println("done");
        
    }
    
    /* get the highest value node according to the rules selected */
    private TreeNode select() {
        /* get the best value child node from the tree */
	    TreeNode selected = findBestValueNode(children);
        /* and then it is returned, with a value always selected thanks to randomisation */
        return selected;
        
    }
    
    public List<TreeNode> getChildren() {
    	return children;
    }
    
    private TreeNode findBestValueNode(List<TreeNode> children) {
    	/* initialize the values, with the bestvalue put at the smallest possible value */
    	TreeNode selected = children.get(0);
        double bestValue = ucbTracker.getUctValue(this, children.get(0));
        //System.out.println("amount of children: " + children.size());
        for (TreeNode c : children) {
        	double uctValue = 0;
        	/* if the rave skip counter has reached the amount of times to wait until skipping rave,
        	 * or if it is not enabled */
    		if(raveSkipCounter < nodeRuleSet.raveSkip || nodeRuleSet.raveSkip == -1) {
    		    /* get the uct value using standard rules */
    			
    		    uctValue = ucbTracker.getUctValue(this, c); // NOTE // Investigate avoiding recalculation if the node stats have not been updated // NOTE //
    		    
    		    /* and increment the counter */
    		    
    		    raveSkipCounter++;
    		    
    		} else if(raveSkipCounter == nodeRuleSet.raveSkip) {

    			/* otherwise disable rave */
    			if(nodeRuleSet.rave == true) {
    				nodeRuleSet.rave = false;
        			uctValue = ucbTracker.getUctValue(this, c);
        			nodeRuleSet.rave = true;
    			}
    			if(nodeRuleSet.weightedRave == true) {
    				nodeRuleSet.weightedRave = false;
        			uctValue = ucbTracker.getUctValue(this, c);
        			nodeRuleSet.weightedRave = true;
    			}
    			if(nodeRuleSet.heuristicRave == true) {
    				nodeRuleSet.heuristicRave = false;
        			uctValue = ucbTracker.getUctValue(this, c);
        			nodeRuleSet.heuristicRave = true;
    			}
    			
    		    /* and set the counter to 0 */
    		    raveSkipCounter = 0;
    		}

            /* if the uctvalue is larger than the best value */
            if (uctValue > bestValue) {

            	/*the selected node is that child */
                selected = c;
                
                /* and the best value is the current value */
                bestValue = uctValue;
                
            }
        }
        return selected;
    }

    /* check if the current node is a leaf */ 
    private boolean isLeaf() {
    	
    	/* if the size of the children of the node is 0, its a leaf */
    	if(children.size() == 0)
    		return true;
    	return false;
    }
    
    

    /* share the updated values across the subtree of the move too */
    private void updateStatsRave(List<TreeNode> children, double simulationResult) {
    	
    	/* for every child of this node */
    	for (TreeNode c : children) {

    		/* for every move on the amafmap */
    		for(int i =0; i<amafMap.length;i++) {
    			
				/* if that part is filled in on the amafMap and matches the childs most recently taken move 
				 * and is the right colour */
    			if(amafMap[i] != null && i == c.currentGame.getMove(0) 
    					&& c.currentGame.getNextToPlay().inverse() == amafMap[i]) {

    				/* update the total value of that node with the simulation result */
					c.updateStats(1, simulationResult);
					
					/* and quit out of the loop, no need to update multiple times for multiple matches */
					break;
    			}
			}
		
    		/* if there is more subtree to explore */
    		if(c.children.size() > 0) {
    			
	    		/* recursively iterate through the whole subtree */
    			updateStatsRave(c.getChildren(), simulationResult);
    		}
    	}
    }

    /* simulate a random game from a treenode */
    private double simulate(TreeNode tn) {	
    	/* create a random player that acts according to the ruleset */
		SimulatePlayer randomPlayer = new SimulatePlayer(
				nodeRuleSet.simulateAvoidEyes, nodeRuleSet.simulateAtari, nodeRuleSet.simulatePatterns, nodeRuleSet.simulateTakePieces, 
				nodeRuleSet.simulateMercyRule, nodeRuleSet.varySimEyes, nodeRuleSet.varySimAtari, nodeRuleSet.varySimPatterns, 
				nodeRuleSet.varySimPieces);

    	/* create a duplicate of the game */
		TreeNode simulateNode = tn;
		Game simulateGame = simulateNode.currentGame;
		SimulateGame duplicateGame = simulateGame.semiPrimitiveCopy();
		
    	/* initialize the game using the duplicate */
    	randomPlayer.startGame(duplicateGame, null);

    	/* until the simulation has finished, play moves */
    	while(!duplicateGame.isOver()) {

    		/* get the move, and play on the board */
    		int move = randomPlayer.playMove();
    		duplicateGame.recordMove(move);
    		
    		/* if we are using any variation of rave */
    		if (nodeRuleSet.rave || nodeRuleSet.weightedRave) {
    			
	    		/* if the move isn't a pass */
	    		if(move != -1) {
	    			
		    		/* set the last move takens colour on the amaf map */
	    			tn.amafMap[move] = randomPlayer.game.getNextToPlay().inverse();
	    		}
    		}
    	}
    	
    	/* get the score for the players color, from the perspective of black */
    	float score = duplicateGame.score(nodeRuleSet.captureScoring, nodeRuleSet.livingScoring, nodeRuleSet.averageScoring);
    	/* if we want the score from the perspective of white */
    	if(playerColor == Color.WHITE) {
    		score = -score;
    	}
    	//System.out.println(score + " ");
    	/* if using binary scoring */
    	if(nodeRuleSet.binaryScoring) {
    		/* if we are using even scoring, and the scores end similarly */
    		if(score < nodeRuleSet.evenScoring && score > -nodeRuleSet.evenScoring) {
    			//System.out.println("Even score!");
    			return 0.5;
    		}
    		/* return 0 for loss, 1 for win */
	    	if(score > 0)
	    		return 1;
	    	return 0;
	    
	    /* if scoring using our own system return the score value */
    	} else {
    		return score;
    	}
    	
    }

    /* methods to print things when explicitly allowed to */
    private void print(String line) {
    	if(testing)
    		System.out.println(line);
    }

    /* update the stats for this node */
    private void updateStats(int type, double value) {
    	
    	/* for the uct value or rave value, dependent on the type input */
    	if(nodeRuleSet.ucbTuned) {
	    	if(type == 0) {
	    		normalValues.add(value);
	    	} else {
	    		raveValues.add(value);
	    	}
    	}
    		nVisits[type]++;
    	ucbTracker.totalActions++;
        totValue[type] += value;
        //System.out.println("updated stats, visits: " + nVisits[type] + " total value: " + totValue[type] + " ");
    }
    
}