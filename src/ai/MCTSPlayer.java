package ai;


import mcts.ElapsedTimer;
import mcts.UCB;
import game.Color;
import game.Game;
import game.SimulateGame;
import gtp.Vertex;
import mcts.TreeNode;

/**
 * A player that uses MCTS.
 * @author Thomas Ager 
 */

public class MCTSPlayer extends Player {
	
	/* Time and iterations, used to determine how long the tree is developed for */
	int time;
	int iterations;
	
	/* A boolean used to check if subtrees will be re-used */
	boolean reuseSubtree;
	
	/* The configuration book specific to each player */
	Configuration nodeRuleSet;
	
	/* The root-node that a move will be selected from */
	TreeNode rootNode;
	
	/* The instance of the set opening book */
	OpeningBook openingBook = null;
	
	/* A boolean to check if the opening book will be used */
	boolean useOpeningBook;
	
	/* A boolean to check if the program will surrender */
	boolean surrender = false;
	
	/* A set of moves in the opening book, used to determine if an opening-book pattern is matched */
	int[] firstMoves;
	
	/* The amount of moves taken, used to determine when to stop attempting to use the opening book */
	private int movesTaken = 0;
	
	/* Initialization to setup the MCTSPlayer values and set the configuration for this player */
	public MCTSPlayer(int time, int iterations, boolean surrender, boolean reuseSubtree,  boolean useOpeningBook, boolean selectRandom,
			boolean binaryScoring, boolean uct, boolean amaf, boolean rave, double initialWeight, double aAmafWeight, double raveWeight,
			 int raveSkip, double bonusFpu, double firstPlayUrgency, double bonusPatterns, double bonusAvoidEyes, double explorationWeight,
			 boolean simulateAvoidEyes, boolean simulateAtari, boolean simulatePatterns, boolean simulateTakePieces, boolean simulateMercyRule,
			 double varySimEyes, double varySimAtari, double varySimPatterns, double varySimPieces,
			 boolean pickRobust, boolean pickMax, boolean pickSecure, boolean pickMaxRobust,  
			 boolean clearMemory, int pruneNodes, int developPruning,
			 boolean ucb, boolean simpleUcb, boolean singleLogUcb, boolean ucbTuned,
	    		boolean captureScoring, boolean livingScoring, boolean averageScoring, int evenScoring) {
		super("TimedPlayer");
		this.time = time;
		this.iterations = iterations;
		this.reuseSubtree = reuseSubtree;
		this.useOpeningBook = useOpeningBook;
		this.surrender = surrender;
		
		/* set the values for different features */
    	this.nodeRuleSet = new Configuration(binaryScoring, uct, amaf, rave, 
    			initialWeight, aAmafWeight, raveWeight, raveSkip, bonusFpu, firstPlayUrgency, bonusPatterns, bonusAvoidEyes, explorationWeight,
    			simulateAvoidEyes, simulateAtari, simulatePatterns, simulateTakePieces, simulateMercyRule,
    			varySimEyes, varySimAtari, varySimPatterns, varySimPieces,
    			pickRobust, pickMax, pickSecure, pickMaxRobust,
    			clearMemory, pruneNodes, developPruning,
    			ucb, simpleUcb, singleLogUcb, ucbTuned, captureScoring, livingScoring, averageScoring, evenScoring,
    			selectRandom);//
    	
    	 
	}
	
	/* Update the game state and set the opening book if it is not set */
	public void setGame(Game game) {
		this.game = game;
		if(openingBook == null && useOpeningBook) {
			this.openingBook = new OpeningBook(game.getSideSize(), game);//
			this.firstMoves = new int[40];
		}
	}
	
	/* Reset the game-related values */
	public void endGame() {
		movesTaken = 0;
		openingBook = null;
		initrootNode = false;
		rootNode = null;
		firstMoves = new int[40];
		super.endGame();
	}
	
	/* Play and return a move */
	public int playMove() {
		/* Initialize the move to be returned */
		int move = 0;
		
		/* Increase the amount of moves taken by two under the assumption that a move has been taken outside
		 * of this player instance by an opponent player. Slightly inaccurate, but it does not require
		 * accuracy to function efficiently.
		 */
		movesTaken +=2;
		
		/* If it is lategame and the opponent has a much higher score than us, just give up */
		if(surrender && movesTaken >= ((game.getSideSize() * game.getSideSize()) -1) && game.mercy()) {
			/* just end the game */
			return -2;
		}
		
		/* If using an opening book and no moves have been taken */
		if(useOpeningBook && game.getMove(0) != -3 && openingBook.movesTaken < 15) {
			
			/* Add a first move and increase the amount of moves taken in the openingBook */
			firstMoves[openingBook.movesTaken] = game.getMove(0);
			openingBook.movesTaken++;
		}
		
		/* If there are still possible moves left to be played, and the opening book matches the moves played */
		if(useOpeningBook && openingBook.movesTaken < 15 && openingBook.playOpeningBookMove(firstMoves)) {
			
			/* Set the move to the opening book move */
			move = openingBook.move;
			
			/* Play the move on the board for this player */
		    game.play(move);
		    
		    /* And add that move to the firstMoves */
		    firstMoves[openingBook.movesTaken] = move;
		    
		    /* Incrementing the amount of moves taken in the opening book, and setting the initrootNode variable to false,
		     * meaning that there is not a tree that has been built because an opening book move has been taken.
		     * This triggers code below to recreate the rootNode from scratch.
		     */
		    openingBook.movesTaken++;
		    initrootNode = false;
		    
		/* If the game is too many moves in, or no opening book move could be found */
		} else {
			
			/* Check if the rootNode requires initialization */
			if(!initrootNode || !reuseSubtree) {
				
				/* If it does, create a TreeNode that represents the players current position 
				 * And set the initrootNode value to true, meaning that the rootNode can be used
				 * Later to re-use subtrees. */
				initrootNode = true;
				UCB ucbTracker = new UCB(nodeRuleSet);
				rootNode = new TreeNode(game, game.getMove(0), 0, side, nodeRuleSet, ucbTracker);
			
			/* If the rootNode doesn't require initialization, then find the new root node
			 * Out of the old root nodes children, by matching the move taken from that node
			 * To the last move played by the opposing player.
			 */
			} else if(reuseSubtree) {
				TreeNode oldRootNode = rootNode;
				
				/* The root node is equal to the child that matches the last move played in the game.
				 * The game knows which move was played because setGame was called before a move was
				 * requested by the engine.
				 */
		    	rootNode = rootNode.getChild(game.getMove(0));
		    	
		    	/* Once the old root node has been discarded, there is no need to store any of its values
		    	 * or its children in memory. This algorithm iterates over the entire subtree, setting every
		    	 * child in the subtree of the root node that is not the new root node to null.
		    	 */
		    	if(nodeRuleSet.clearMemory) {
		    		oldRootNode.clearParentMemory(rootNode, oldRootNode.getChildren());
		    	}
		    }
			
			/* If the player wants to make use of time */
			if(time > 0) {
				
				/* Initialize a timer */
				ElapsedTimer t = new ElapsedTimer();
				
				/* Until that timer has elapsed its predefined limit */
				while(t.elapsed() < time) {
					
			        /* Develop the tree from the root node */
					rootNode.developTree();
					
					/* Tried pruning all nodes beyond a certain threshold of visits. 
					 * Ended up using up more memory than if it was disabled. */
					if(nodeRuleSet.checkPruning()) {
						rootNode.pruneNodes();
					}
			       
			    }
			}
			
			/* If the player wants to use the amount of iterations/simulations, then
			 * just run developtree the amount of times they've determined.
			 */
			if (iterations > 0) {
				for(int i=0;i<iterations;i++) {
					rootNode.developTree();
					if(nodeRuleSet.checkPruning()) {
						rootNode.pruneNodes();
					}
			    }
			}
			
		    /* Get the highest value move from the root node, based on the criteria set out in the config */
		    move = rootNode.getHighestValueMove();
		   
		    /* Play the move on the board for this player */
		    game.play(move);
		    
		    /* If the opening book still has moves to play, then add this move to the moves played
		     * and increment the amount of moves taken on the openingBook instance.
		     */
		    if(useOpeningBook && openingBook.movesTaken < 15) {
			    firstMoves[openingBook.movesTaken] = move;
			    openingBook.movesTaken++;
		    }
		    
		    /* If re-using the subtree, then the child node of the current root must be navigated to
		     * In order to get the child node of that node once the opponent takes their move */
		    if(reuseSubtree) {
		    	TreeNode oldRootNode = rootNode;
		    	rootNode = rootNode.getChild(game.getMove(0));
		    	if(nodeRuleSet.clearMemory) {
		    		oldRootNode.clearParentMemory(rootNode, oldRootNode.getChildren());
		    	}
		    }
		}
		
		/* Collect all of the parent memory that has been cleared */
		System.gc();
		
	    /* And return the move to the engine, so it can be converted into GTP */
	    return move;
		
	}
	
}
