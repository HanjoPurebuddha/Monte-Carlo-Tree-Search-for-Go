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
	
	/* initialize the random generation */
    static Random r = new Random();
    
    /* initialize epsilon for use in uct */
    static double epsilon = 1e-6;
    
    /* each node has a game with the current move taken */
    Game currentGame;
    
    /* each node has a parent */
    TreeNode parent;
    
    /* each node has children */
    List<TreeNode> children = new ArrayList<TreeNode>();
    
    /* initialize the values used in uct, one for uct standard and one for rave */
    double[] nVisits = new double[2], totValue = new double[2];
    
    /* initialize the playerColor for use in enforcing positive values for the player in the amaf map */
    Color playerColor;
    
    /* setup the testing variable to allow prints or disallow them */
    boolean testing = false;
    
    /* initialize a map of colours used to record if a move was in the players color for amaf */
    Color[] amafMap;
    
    /* each node has the move taken to get to that move */
    public int move;
    
    /* each node has a ruleset defined by the user */
    Configuration nodeRuleSet;
    
    /* set all the values that change for every node */
    public TreeNode(TreeNode parent, Game currentGame, int move,
    		Color playerColor, Configuration nodeRuleSet) {
    	this.parent = parent;
    	this.currentGame = currentGame;
    	this.move = move;
    	this.nodeRuleSet = nodeRuleSet;
    	this.playerColor = playerColor;
    	/* and set the default values of the visits and total value for the node */
    	for(int i=0; i<nVisits.length; i++) {
    		nVisits[i] = 0;
    		totValue[i] = 0;
    	}
    }
    
    /* set the values that are persistent across nodes, but given by the user/player */
    public void setRuleSet(Configuration ruleSet) {
    	this.nodeRuleSet = ruleSet;
    }
    public void setPlayerColor(Color playerColor) {
    	this.playerColor = playerColor;
    }

    /* return the size of the board for the node's game */
    public int boardSize() {
    	return this.currentGame.board.getSideSize();
    }

    /* print all the moves of the children of the current node */
    public void printChildren() {
    	for(TreeNode c : children) {
    		print(c.currentGame.getMove(-1));
    	}
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
    	TreeNode newChild = new TreeNode(this, normalGame, lastMove, playerColor, nodeRuleSet);
    	
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
        
        /* until the bottom of the tree is reached */
        while (!cur.isLeaf()) {
        	/* follow the highest uct value node, and add it to the visited list */
        	print("navigating to leaf node");
            cur = cur.select();
            print("Adding: " + cur);
            visited.add(cur);
        }
        
        /* at the bottom of the tree expand the children for the node, including a pass move */
        print("found leaf node, expanding from: " + cur);
        cur.expand();

        /* get the best child from the expanded nodes, even if it's just passing */
	    print("selecting from: " + cur);
	    TreeNode newNode = cur.select();
	        
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
	    if(nodeRuleSet.rave) {
	    	
	    	/* based on the amaf map of the node that was just simulated */
	    	updateStatsRave(newNode.amafMap, cur, value);
	    }
    }
    
    /* get the highest uct value child node of the node given */
    public int getHighestValueMove() {
    	TreeNode highestValueNode = select();
    	print("NODE SELECTED:" +highestValueNode);
    	return highestValueNode.move;
    }
    
    /* expand the children of the node */
    public void expand() {
    	
    	/* get all of the empty points on the board */
    	PositionList emptyPoints = currentGame.board.getEmptyPoints();
    	int sizeOfPoints = emptyPoints.size();
    	print("There are currently this many empty points:" +sizeOfPoints + " ");
    	
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
        		TreeNode newChild = new TreeNode(this, normalGame, emptyPoints.get(i), playerColor, nodeRuleSet);
        		
        		/* and add it to the current nodes children */
        		children.add(newChild);
        		print("added child " + newChild.move);
        	} else {
        		print("cant play");
        	}
        	
        }
        
        /* add a pass move as well as playing on every allowable empty point */
        Game passGame = currentGame.duplicate();
        passGame.play(-1);
        /* and retain the possible moves from that pass move, allowing this move to be skipped over for expansion,
         * but still updated if its child nodes have potential value */
		TreeNode passChild = new TreeNode(this, passGame, -1, playerColor, nodeRuleSet);
		
		/* and add it to the current nodes children */
		children.add(passChild);
        
    }
    
    /* prune nodes playing on the first line of the board without surrounding nodes */
    public void prune() {
    	//
    }
    
    /* get the highest value node according to the rules selected */
    private TreeNode select() {
    	
    	/* initialize the values, with the bestvalue put at the smallest possible value */
        TreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;

        /* for every child node of the current node being selected */
        for (TreeNode c : children) {

        	/* if we are using UCT at all calculate it */
    		double uctValue = 0;
    		/* if we are using UCT no rave */
            if (nodeRuleSet.uct) {
            	/* get the uct value just using the uct values */
        		uctValue = getUctValue(0, c);
        		
            }
            
            /* if we are using rave */
            if (nodeRuleSet.rave) {
            	
            	/* get the uct value for the rave values */
        		uctValue = getUctValue(1, c);
            }
    		print("UCT value = " + uctValue);
    		
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
        
        /* and then it is returned, with a value always selected thanks to randomisation */
        return selected;
        
    }

    /* check if the current node is a leaf */ 
    public boolean isLeaf() {
    	
    	/* if the size of the children of the node is 0, its a leaf */
    	if(children.size() == 0)
    		return true;
    	return false;
    }
    
    /* get the uct value for a node with a small random number to break ties randomly in unexpanded nodes  */
    public double getUctValue(int type, TreeNode tn) {
    	
    	/* ((total value) / (visits + e)) + (log(visits) / (visits + e) + random number) */
        return tn.totValue[type] / (tn.nVisits[type] + epsilon) +
                Math.sqrt(Math.log(tn.nVisits[type]+1) / (tn.nVisits[type] + epsilon)) +
                r.nextDouble() * epsilon;
    }

    /* update the subtrees using RAVE */
    public void updateStatsRave(Color[] amafMap, TreeNode tn, double simulationResult) {
    	
    	/* for every child of this node */
    	for (TreeNode c : tn.children) {
    		
    		/* for every move on the amafmap */
    		for(int i =0; i<amafMap.length;i++) {
    			
				/* that matches any moves on the board */
				if(amafMap[i] != null && i != c.currentGame.board.getEmptyPoints().get(i)) {
					
					/* and the colour matches */
					if(amafMap[c.currentGame.getMove(0)] != tn.currentGame.getNextToPlay()) {
						
						/* update the total value of that node with the simulation result */
						tn.updateStats(1, simulationResult);
					}
					
					/* and quit out of the loop, no need to update multiple times for multiple matches */
					return;
				}
    		}
    		
    		/* if there is more subtree to explore */
    		if(c.children.size() > 0) {
    			
	    		/* recursively iterate through the whole subtree */
	    		updateStatsRave(amafMap, c, simulationResult);
    		}
    	}
    }

    /* simulate a random game from a treenode */
    public double simulate(TreeNode tn) {

    	/* initialize the map of who played where for this simulation
    	 * on the node that is to be simulated from */
		tn.amafMap = new Color[tn.currentGame.getSideSize() * tn.currentGame.getSideSize()];

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
		    			tn.amafMap[move] = playerColor;
		    		} else {
		    			tn.amafMap[move] = playerColor.inverse();
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
	    
	    /* if scoring using our own system return the score value */
    	} else {
    		return score;
    	}
    	
    }
    
    /* methods to print things when explicitly allowed to */
    public void print(String line) {
    	if(testing)
    		System.out.println(line);
    }
    public void print(int line) {
    	if(testing)
    		System.out.println(line);
    }

    /* update the stats for this node */
    public void updateStats(int type, double value) {
    	
    	/* for the uct value or rave value, dependent on the type input */
    	nVisits[type]++;
        totValue[type] += value;
        print("updated stats, visits: " + nVisits[type] + " total value: " + totValue[type]);
    }
    
}