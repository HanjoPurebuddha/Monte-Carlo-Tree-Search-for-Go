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
	
	/* initialize the random generation */
    static Random r = new Random();
    
    /* initialize epsilon for use in uct */
    static double epsilon = 1e-6;
    
    /* each node has a game with the current move taken */
    Game currentGame;

    /* each node has children */
    List<TreeNode> children = new ArrayList<TreeNode>();
    
    /* and a dynamic tree is maintained of only the children who have been visited more than once */
    List<TreeNode> dynamicTreeChildren = new ArrayList<TreeNode>();
    
    /* initialize the values used in uct, one for uct standard and one for rave */
    double[] nVisits = new double[2];
    double[] totValue = new double[2];
    
    /*used to check if the node has been added to the dynamic tree or not */
    boolean addedToDynamicTree = false;
    
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
    
    /* set all the values that change for every node */
    public TreeNode(Game currentGame, int move, int raveSkipCounter, double uctValue, //double weight, // NOTE // Might not need to m
    		Color playerColor, Configuration nodeRuleSet) {
    	this.currentGame = currentGame;
    	this.move = move;
    	this.nodeRuleSet = nodeRuleSet;
    	this.playerColor = playerColor;
    	this.raveSkipCounter = raveSkipCounter;
    	/* a map of colours used to record if a move was in the players color for amaf */
        this.amafMap = new Color[currentGame.getSideSize()*currentGame.getSideSize()];
    }
    
    /* prune the tree of every node that will never be used again */
    public void clearParentMemory(TreeNode avoidClear, List<TreeNode> children) {
    	for(TreeNode c : children) {
    		if(c != avoidClear) {
    			if(c.children.size() > 0) {
    				System.out.println("Found more children...");
    				clearParentMemory(avoidClear, c.children);
    			}
    			c = null;
    			
    		}
    	}
    }
    
    /* get the child node that matches the game state given (last move played, from MCTSPlayer) */
    public TreeNode getChild(int lastMove) {
    	if(dynamicTreeChildren.size() > 0) { 
	    	/* for every child, check if the move matches the last move */
	    	for(TreeNode c : dynamicTreeChildren) {
	    		if(c.move == lastMove) {
	    			return c;
	    		}
	    	}
    	} else {
    		/* for every child, check if the move matches the last move */
	    	for(TreeNode c : children) {
	    		if(c.move == lastMove) {
	    			return c;
	    		}
	    	}
    	}
    	
    	/* if we couldn't find a matching expanded child, then create one with the move played out */
    	Game normalGame = currentGame.duplicate();
    	normalGame.play(lastMove);
    	TreeNode newChild = new TreeNode(normalGame, lastMove, raveSkipCounter, nodeRuleSet.firstPlayUrgencyValue(), playerColor, nodeRuleSet);
    	
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

        
        
        
        /* until the bottom of the tree is reached
         * in either dynamic or non-dynamic tree modes */
        boolean atLeafNode = false;
        if(nodeRuleSet.dynamicTree > 0)
        	atLeafNode = cur.isDynamicTreeLeaf();
        else 
        	atLeafNode = cur.isLeaf();
        while (!atLeafNode) {
        	/* follow the highest uct value node, and add it to the visited list */
        	if(nodeRuleSet.dynamicTree > 0) {
        		cur = cur.dynamicTreeSelect();
        	} else {
        		cur = cur.select();
        	}
            print("Adding: " + cur);
            visited.add(cur);
            if(nodeRuleSet.dynamicTree > 0)
            	atLeafNode = cur.isDynamicTreeLeaf();
            else 
            	atLeafNode = cur.isLeaf();
        }
        
        
        /* at the bottom of the tree expand the children for the node, including a pass move
         * but only if it hasn't been expanded before */
        print("found leaf node, expanding from: " + cur);
        if(cur.children.size() == 0)
        	cur.expand();

        /* get the best child from the expanded nodes, even if it's just passing */
	    print("selecting from: " + cur);
	    TreeNode newNode = cur.select();
	    visited.add(newNode);
	    
	    /* simulate from the node, and get the value from it */
	    print("simulating" + newNode);
	    double value = simulate(newNode);
	    print("got result" + value);
	    
	    /* backpropogate the values of all visited nodes */
	    for (TreeNode node : visited) {
	    	
	    	/* type 0 for just uct updating */
	        node.updateStats(0, value);
	        
	    }
	    /* and if using rave update the subtree of the parent of the simulated node */
	    if(nodeRuleSet.rave || nodeRuleSet.weightedRave) {
	    	/* based on the amaf map of the node that was just simulated */
	    	updateStatsRave( cur.getChildren(), value);
	    }
	    
	    /* if we are using the dynamic tree, update that */
        if(cur != null) {
		    if(nodeRuleSet.dynamicTree > 0) {
		    	if(newNode.nVisits[1] > (nodeRuleSet.dynamicTree - 1) && newNode.addedToDynamicTree == false) {
		    		cur.dynamicTreeChildren.add(newNode);
		    		newNode.addedToDynamicTree = true;
		    	}
		    }
        }
    }
    
    /* get the bonus value for this node */
    public int getBonus() {
    	if(nodeRuleSet.bonusPatterns > 0) {
    		if(currentGame.lastMoveMatchesPatterns()) {
    			return nodeRuleSet.bonusPatterns;

    		}
    	}
    	return 0;
    }
    
    public void printList(List<TreeNode> list) {
    	System.out.println("[");
    	for(TreeNode node : list) {
    		System.out.println(node + ", ");
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
        List<TreeNode> children;
        if(nodeRuleSet.dynamicTree > 0)
        	children = this.dynamicTreeChildren;
        else
        	children = this.children;
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
        List<TreeNode> children;
        if(nodeRuleSet.dynamicTree > 0)
        	children = this.dynamicTreeChildren;
        else
        	children = this.children;
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

    	/* for every empty point on the board */
        for (int i=0; i<emptyPoints.size(); i++) {
        	
        	/* if disallowing playing in eyes, create a semiprimitive board with that rule and test playing on it */
        	boolean canPlay = true;
        	if(nodeRuleSet.dontExpandEyes) {
	        	SimulateGame duplicateBoard = currentGame.semiPrimitiveCopy();    
	        	canPlay = duplicateBoard.play(emptyPoints.get(i));
        	}
        	
        	/* otherwise just try and play on it normally, ensuring the rules of the game are followed */
        	Game normalGame = currentGame.duplicate();
        	boolean canPlayNormal = normalGame.play(emptyPoints.get(i));
        	
        	/* checking if it is possible to play that point, checking if we're playing into an eye */
        	if(canPlay && canPlayNormal) {
        		
        		/* create a new child for that point */
        		TreeNode newChild = new TreeNode(normalGame, emptyPoints.get(i), raveSkipCounter, nodeRuleSet.firstPlayUrgencyValue(), playerColor, nodeRuleSet);
        		
        		/* and add it to the current nodes children */
        		children.add(newChild);
        	} 
        	
        }
        
        /* add a pass move as well as playing on every allowable empty point */
        Game passGame = currentGame.duplicate();
        passGame.play(-1);
		TreeNode passChild = new TreeNode(passGame, -1, raveSkipCounter, nodeRuleSet.firstPlayUrgencyValue(), playerColor, nodeRuleSet);
		
		/* and add it to the current nodes children */
		children.add(passChild);
        
    }
    
    /* get the highest value node according to the rules selected */
    private TreeNode select() {
        /* get the best value child node from the tree */
	    TreeNode selected = findBestValueNode(children);
        /* and then it is returned, with a value always selected thanks to randomisation */
        return selected;
        
    }
    
    private TreeNode dynamicTreeSelect() {
    	/* get the best value child node from the dynamic tree children */
	    TreeNode selected = findBestValueNode(dynamicTreeChildren);
        /* and then it is returned, with a value always selected thanks to randomisation */
        return selected;
    }
    
    public List<TreeNode> getChildren() {
    	if(nodeRuleSet.dynamicTree > 0)
    		return dynamicTreeChildren;
    	return children;
    }
    
    public List<TreeNode> getNonDynamicChildren() {
    	return children;
    }
    
    private TreeNode findBestValueNode(List<TreeNode> children) {
    	/* initialize the values, with the bestvalue put at the smallest possible value */
    	TreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        //System.out.println("amount of children: " + children.size());
        for (TreeNode c : children) {
        	double uctValue = 0;
        	/* if the rave skip counter has reached the amount of times to wait until skipping rave,
        	 * or if it is not enabled */
    		if(raveSkipCounter < nodeRuleSet.raveSkip || nodeRuleSet.raveSkip == -1) {
    		    /* get the uct value using standard rules */
    			
    		    uctValue = getUctValue(c); // NOTE // Investigate avoiding recalculation if the node stats have not been updated // NOTE //
    		    if(nodeRuleSet.firstPlayUrgency > 0) {
	    		    if(c.nVisits[0] == 0) //if this node hasnt been visited
	    				uctValue = nodeRuleSet.firstPlayUrgencyValue(); //make sure it is!
    		    }
    		    /* and increment the counter */
    		    
    		    raveSkipCounter++;
    		    
    		} else if(raveSkipCounter == nodeRuleSet.raveSkip) {

    			/* otherwise disable rave */
    			if(nodeRuleSet.rave == true) {
    				nodeRuleSet.rave = false;
        			uctValue = getUctValue(c);
        			nodeRuleSet.rave = true;
    			}
    			if(nodeRuleSet.weightedRave == true) {
    				nodeRuleSet.weightedRave = false;
        			uctValue = getUctValue(c);
        			nodeRuleSet.weightedRave = true;
    			}
    			if(nodeRuleSet.heuristicRave == true) {
    				nodeRuleSet.heuristicRave = false;
        			uctValue = getUctValue(c);
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
    private boolean isDynamicTreeLeaf() {
    	if(dynamicTreeChildren.size() == 0)
    		return true;
    	return false;
    }
    
    /* perform the calculation required for uct */
    private double calculateUctValue(int type, TreeNode tn) {

	    	/* ((total value) / (visits + e)) + (log(visits) / (visits + e) + random number) */
	    	return tn.totValue[type] + getBonus() / (tn.nVisits[type] + epsilon) +
                    Math.sqrt(Math.log(nVisits[type]+1) / (tn.nVisits[0] + epsilon)) +
                    r.nextDouble() * epsilon;

    }
    
    /* perform the calculation needed to weight the node, in order to balance rave and uct */
    private double calculateWeight(TreeNode tn) {
    	
    	/* when only a few simulations have been seen, the weight is closer to 1, weighting the RAVE value more highly
    	 * when many simulations have been seen, the weight is closer to 0, weighting the MC value more highly
    	 */
    	double weight = 1 - (nodeRuleSet.initialWeight * (tn.nVisits[0] / nodeRuleSet.finalWeight));
    	if(tn.nVisits[0] == 0) {
    		return 1;
    	}
    	if(weight < 0) {
    		return 0;
    	}
    	return weight;
    }
    
    /* get the uct value for a node with a small random number to break ties randomly in unexpanded nodes  */
    private double getUctValue(TreeNode tn) {

    	/* if we are using UCT no rave */
        if (nodeRuleSet.uct) {
        	
        	/* get the uct value only */
        	return calculateUctValue(0, tn);
    		
        } else if (nodeRuleSet.rave) {
        	
        	/* get the rave value only */
        	return calculateUctValue(1, tn);
    		
        } else if (nodeRuleSet.weightedRave) {
        	
        	/* calculate it using the weight */
        	double weight = calculateWeight(tn);
    		return ((1 - weight) * calculateUctValue(0, tn)) + (weight * calculateUctValue(1, tn));
	        
    	}
        return 0;
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
				nodeRuleSet.simulateMercyRule);

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
    	
    	/* get the score for the players color, positive or negative depending on colour */
    	float score = duplicateGame.score(playerColor);

    	/* if using binary scoring */
    	if(nodeRuleSet.binaryScoring) {
    		
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
    	
    		nVisits[type]++;
    	
        totValue[type] += value;
        print("updated stats, visits: " + nVisits[type] + " total value: " + totValue[type]);
    }
    
}