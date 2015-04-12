package game;




//import com.ideanest.util.UnexpectedException;
import gtp.*;

/**
 * A game of Go.
 * @author Piotr Kaminski
 */
public abstract class Game implements Cloneable {
	
	public static final int MOVE_PASS = -1;
	public static final int MOVE_RESIGN = -2;
	
	protected float komi;
	protected int handicap;
	
	protected Color nextToPlay;
	public Board board;
	protected int numMoves;
	
	protected int lastMove;
	
	/* used to ignore eyes, or not */
	public boolean avoidEyes;
	
	public boolean equals(Object o) {
		Game that = (Game) o;
		return
			this.komi == that.komi &&
			this.handicap == that.handicap &&
			this.nextToPlay == that.nextToPlay &&
			this.board.equals(that.board) &&
			this.numMoves == that.numMoves &&
			this.lastMove == that.lastMove;
	}
	
	public SimulateGame semiPrimitiveCopy() {
		SimulateGame copyGame = new SimulateGame(this.getSideSize());
		copyGame.komi = this.komi;
		copyGame.handicap = this.handicap;
		copyGame.nextToPlay = this.nextToPlay;
		copyGame.board = this.board.duplicate();
		copyGame.numMoves = this.numMoves;
		copyGame.lastMove = this.lastMove;
		return copyGame;
	}
	
	public int hashCode() {
		return board.hashCode() ^ numMoves ^ handicap ^ getClass().hashCode();
	}
	

	/* code that checks if the opponent has over 30% of the board in captures */
	
	public boolean mercy() {
		return false;
	}
	
	/* code that recognizes atari states */
	
	public boolean atari(int z) {
		return false;
	}
	
	/* code that recognizes if stones can be saved from atari */
	
	public boolean saveStones(int z) {
		return false;
	}
	
	/* code that recognizes pieces it can take and takes them */
	
	public boolean takePiece(int z) {
		return false;
	}
	
	/* Some hard-coded pattern matching routines to match patterns used by MoGo.
    See <a href="http://hal.inria.fr/docs/00/11/72/66/PDF/MoGoReport.pdf">
    Modification of UCT with Patterns in Monte-Carlo Go</a>.

    The move is always in the center of the pattern or at the middle edge
    point (lower line) for edge patterns. The patterns are matched for both
    colors, unless specified otherwise. Notation:
    @verbatim
    O  White            x = Black or Empty
    X = Black           o = White or Empty
    . = Empty           B = Black to Play
    ? = Don't care      W = White to Play
    @endverbatim */
	
	public boolean matchPattern(int z) {
		int[][] surroundingPositions = populateSurroundingPositions(z);
	//	System.out.println(surroundingPositions);
		for(int i=0;i<3;i++) {
			for(int j=0;j<3;j++) {
			//	System.out.print("Populating surrounding positions:");
				int[][] patternMatchGrid = populateSurroundingPositions(surroundingPositions[i][j]);
			//	System.out.println(patternMatchGrid);
			//	System.out.print("Populating pattern types:");
				int[][][] patternTypes = populatePatternTypes(patternMatchGrid);
			//	System.out.println(patternTypes);
				for(int k=0;k<patternTypes.length;k++) {
				//	System.out.println("[[[[[[[[[[[[TYPE "+ k + "]]]]]]]]]]]]]]]]");
					if(matchesPatterns(patternTypes[k])) {
						return true;
					}
				}
			}
			
		}
		return false;
	}
	
	public boolean matchesPatterns(int[][] grid) {
		if(checkHane(grid) || checkCut1(grid) || checkCut2(grid) || checkEdge(grid)) {
	//		System.out.println("matched");
			boolean canPlay = play(grid[1][1]);
			if(canPlay) {
			//	System.out.println("Can play!");
				recordMove(grid[1][1]);
				return true;
				
			} else {
			//	System.out.println("Couldn't play this match...");
				return false;
			}
		}
	//	System.out.println("no match");
		return false;
	}
	
	public int[][][] populatePatternTypes(int[][] grid) {
		int[][][] patternTypes = new int[6][][];
		patternTypes[0] = grid;
		patternTypes[1] = rotate(grid);
		patternTypes[2] = rotate(patternTypes[1]);
		patternTypes[3] = rotate(patternTypes[2]);
		patternTypes[4] = verticalFlip(grid);
		patternTypes[5] = horizontalFlip(grid);
	//	patternTypes[6] = swapcolors(grid);
		return patternTypes;
	}
	
	static int[][] rotate(int[][] grid) {
	    int[][] out = new int[3][3];

	    for (int i = 0; i < 3; ++i) {
	        for (int j = 0; j < 3; ++j) {
	        	out[i][j] = grid[3 - j - 1][i];
	        }
	    }

	    return out;
	}
	
	public static int[][] horizontalFlip(int[][] grid) {
	    int[][] out = new int[3][3];
	    for (int i = 0; i < 3; i++) {
	        for (int j = 0; j < 3; j++) {
	            out[i][3 - j - 1] = grid[i][j];
	        }
	    }
	    return out;
	}
	
	public static int[][] verticalFlip(int[][] grid) {
	    int[][] out = new int[3][3];
	    for (int i = 0; i < 3; i++) {
	        for (int j = 0; j < 3; j++) {
	            out[3 - j - 1][j] = grid[i][j];
	        }
	    }
	    return out;
	}
	
	public int[][] populateSurroundingPositions(int z) {
		int[][] surroundingPositions = new int[3][3];
		surroundingPositions[0][0] = getGrid().upleft(z);
	//	System.out.println("1");
		surroundingPositions[1][0] = getGrid().left(z);
	//	System.out.println("2");
		surroundingPositions[2][0] = getGrid().downleft(z);
	//	System.out.println("3");
		surroundingPositions[0][1] = getGrid().up(z);
	//	System.out.println("4");
		surroundingPositions[0][2] = getGrid().upright(z);
	//	System.out.println("5");
		surroundingPositions[1][1] = z;
	//	System.out.println("6");
		surroundingPositions[2][2] = getGrid().downright(z);
	//	System.out.println("7");
		surroundingPositions[2][1] = getGrid().down(z);
	//	System.out.println("8");
		surroundingPositions[1][2] = getGrid().right(z);
	//	System.out.println("9");
		return surroundingPositions;
	}

    /* Patterns for Hane. <br>
    True is returned if any pattern is matched.
    In the right one, true is returned if and only if the eight positions around are matched and it is black to play.
    @verbatim
    X O X   X O .   X O ?   X O O
    . . .   . . .   X . .   . . .
    ? ? ?   ? . ?   ? . ?   ? . ? B
    @endverbatim */
	

    
    Point[][] hane1 = new Point[][]{
    	  { Color.BLACK, Color.BLACK, Color.EMPTY },
    	  { Color.EMPTY, Color.EMPTY, Color.EMPTY }
    	};
    
    Point[][] hane2 = new Point[][]{
      	  { Color.BLACK, Color.WHITE, Color.EMPTY },
      	  { Color.EMPTY, Color.EMPTY, Color.EMPTY },
      	  { Color.EMPTY, Color.EMPTY, Color.EMPTY }
      	};
    
    Point[][] hane3 = new Point[][]{
      	  { Color.BLACK, Color.WHITE, Color.EMPTY },
      	  { Color.BLACK, Color.EMPTY, Color.EMPTY },
      	  { Color.EMPTY, Color.EMPTY, Color.EMPTY }
      	};
    
    Point[][] hane4 = new Point[][]{
      	  { Color.BLACK, Color.WHITE, Color.WHITE },
      	  { Color.EMPTY, Color.EMPTY, Color.EMPTY },
      	  { Color.EMPTY, Color.EMPTY, Color.EMPTY }
      	};
    
    
    public boolean checkHane(int[][] grid) {
    //	printGrid(grid);
    	if(checkHane1(grid) || checkHane2(grid) || checkHane3(grid) || checkHane4(grid)) {
    		
    		
    		return true;  
    	}
    //	System.out.println("Didn't match hane.");
    	return false;
	}
    
    public boolean checkHane1(int[][] grid) {
    	for(int i=0;i<2;i++) {
			for(int j=0;j<3;j++) {
				if( getPoint(grid[i][j]) != hane1[i][j] )
					return false;
			}
    	}
    //	System.out.println("Matched hane1!");
    	return true;
    }
    
    public void printGrid(int[][] grid) {
    	for(int i=0;i<3;i++) {
    		System.out.println(";");
			for(int j=0;j<3;j++) {
				System.out.print(getPoint(grid[i][j]) + ", ");
			}
    	}
    	System.out.println();
    }
    
    public void printPointGrid(Point[][] grid) {
    	
    }
    
    public boolean checkHane2(int[][] grid) {
    	for(int i=0;i<3;i++) {
			for(int j=0;j<3;j++) {
				if(i==2 && j==0)
					continue;
				if(i==2 && j==2)
					continue;
				if( getPoint(grid[i][j]) != hane2[i][j] )
					return false;
			}
    	}
    //	System.out.println("Matched hane2!");
    	return true;
    }
    
    public boolean checkHane3(int[][] grid) {
    	for(int i=0;i<3;i++) {
			for(int j=0;j<3;j++) {
				if(i==2 && j==0)
					continue;
				if(i==0 && j==2)
					continue;
				if(i==2 && j==2)
					continue;
				if( getPoint(grid[i][j]) != hane3[i][j])
					return false;
			}
    	}
    //	System.out.println("Matched hane3!");
    	return true;
    }
    
    public boolean checkHane4(int[][] grid) {
    	if(nextToPlay == Color.BLACK) {
	    	for(int i=0;i<3;i++) {
				for(int j=0;j<3;j++) {
					if(i==2 && j==0)
						continue;
					if(i==2 && j==2)
						continue;
					if( getPoint(grid[i][j]) != hane4[i][j])
						return false;
				}
	    	}
	 //   	System.out.println("Matched hane4!");
	    	return true;
    	} else {
    		return false;
    	}
    }

    /* Patterns for Cut1. <br>
    True is returned if the first pattern is matched, but not the next two.
    @verbatim
    X O ?   X O ?   X O ?
    O . ?   O . O   O . .
    ? ? ?   ? . ?   ? O ?
    @endverbatim  */
    
    Point[][] cutTrue = new Point[][]{
      	  { Color.BLACK, Color.WHITE, },
      	  { Color.WHITE, Color.EMPTY, }
      	};
      
      Point[][] cutFalse1 = new Point[][]{
        	  { Color.BLACK, Color.WHITE, Color.EMPTY },
        	  { Color.WHITE, Color.EMPTY, Color.WHITE },
        	  { Color.EMPTY, Color.EMPTY, Color.EMPTY }
        	};
      
      Point[][] cutFalse2 = new Point[][]{
        	  { Color.BLACK, Color.WHITE, Color.EMPTY },
        	  { Color.WHITE, Color.EMPTY, Color.EMPTY },
        	  { Color.EMPTY, Color.WHITE, Color.EMPTY }
        	};
    
    public boolean checkCut1(int[][] grid) {
    	if(checkCutTrue(grid) && !checkCutFalse1(grid) && !checkCutFalse2(grid)) {
   // 		System.out.println("Matched cut1!");
    		return true;
    	}
		return false;
	}
    
    public boolean checkCutTrue(int[][] grid) {
    	for(int i=0;i<2;i++) {
			for(int j=0;j<2;j++) {
				if( getPoint(grid[i][j]) != cutTrue[i][j] )
					return false;
			}
    	}
    	return true;
    }
    
    public boolean checkCutFalse1(int[][] grid) {
    	for(int i=0;i<3;i++) {
			for(int j=0;j<3;j++) {
				if(i==2 && j==0)
					continue;
				if(i==0 && j==2)
					continue;
				if(i==2 && j==2)
					continue;
				if( getPoint(grid[i][j]) != cutFalse1[i][j])
					return true;
			}
    	}
    	return false;
    }
    
    public boolean checkCutFalse2(int[][] grid) {
    	for(int i=0;i<3;i++) {
			for(int j=0;j<3;j++) {
				if(i==2 && j==0)
					continue;
				if(i==0 && j==2)
					continue;
				if(i==2 && j==2)
					continue;
				if( getPoint(grid[i][j]) != cutFalse2[i][j])
					return true;
			}
    	}
    	return false;
    }

    /* Pattern for Cut2.
     True is returned when the 6 upper positions are matched and
	the 3 bottom positions are not white.
    @verbatim
    ? X ?
    O . O
    x x x
    @endverbatim  */
    
    Point[][] cut2 = new Point[][]{
      	  { Color.EMPTY, Color.BLACK, Color.EMPTY },
      	  { Color.WHITE, Color.EMPTY, Color.WHITE }
      	};
    
    public boolean checkCut2(int[][] grid) {
    	for(int i=0;i<2;i++) {
			for(int j=0;j<3;j++) {
				if(i==0 && j==0)
					continue;
				if(i==0 && j==2)
					continue;
				if( getPoint(grid[i][j]) != cut2[i][j] )
					return false;
			}
    	}
    	/* testing if the 3 bottom positions are white */
    	for(int j=0;j<3;j++) {
    		if( getPoint(grid[2][j]) == Color.WHITE )
    			return false;
    	}
    //	System.out.println("Matched cut2!");
    	return true;
	}

    /* Pattern for Edge. <br>
    True is returned if any pattern is matched.
    @verbatim
    X . ?   ? X ?   ? X O    ? X O    ? X O
    O . ?   o . O   ? . ? B  ? . o W  O . X W
    @endverbatim  */
    
    Point[][] edge1 = new Point[][]{
        	  { Color.BLACK, Color.EMPTY},
        	  { Color.WHITE, Color.EMPTY}
        	};
	
    Point[][] edge2 = new Point[][]{
      	  { Color.EMPTY, Color.BLACK, Color.EMPTY },
      	  { Color.EMPTY, Color.EMPTY, Color.WHITE }
      	};
    
    /* use this for edge4 too, with specific rules for other conditions */
    Point[][] edge3 = new Point[][]{
      	  { Color.EMPTY, Color.BLACK, Color.WHITE },
      	  { Color.EMPTY, Color.EMPTY, Color.EMPTY }
      	};
    Point[][] edge5 = new Point[][]{
        	  { Color.EMPTY, Color.BLACK, Color.WHITE },
        	  { Color.WHITE, Color.EMPTY, Color.BLACK }
        	};
    
    public boolean checkEdge(int[][] grid) {
    	if(checkEdge1(grid) || checkEdge2(grid) || checkEdge3(grid) || checkEdge4(grid) || checkEdge5(grid))
    		return true;
		return false;
	}
	
    public boolean checkEdge1(int[][] grid) {
    	for(int i=0;i<2;i++) {
			for(int j=0;j<2;j++) {
				if( getPoint(grid[i][j]) != edge1[i][j] )
					return false;
			}
    	}
    //	System.out.println("Matched edge1!");
    	return true;
    }
    
    public boolean checkEdge2(int[][] grid) {
    	if(getPoint(grid[1][0]) == Color.BLACK ) {
    		return false;
    	}
    	for(int i=0;i<2;i++) {
			for(int j=0;j<3;j++) {
				if(i==0 && j==0)
					continue;
				if(i==1 && j==2)
					continue;
				if( getPoint(grid[i][j]) != edge2[i][j] )
					return false;
			}
    	}
    //	System.out.println("Matched edge2!");
    	return true;
    }
    
    public boolean checkEdge3(int[][] grid) {
    	if(nextToPlay == Color.BLACK) {
	    	for(int i=0;i<2;i++) {
				for(int j=0;j<3;j++) {
					if(i==0 && j==0)
						continue;
					if(i==1 && j==0)
						continue;
					if(i==1 && j==2)
						continue;
					if( getPoint(grid[i][j]) != edge3[i][j] )
						return false;
				}
	    	}
    	} else {
    		return false;
    	}
    //	System.out.println("Matched edge3!");
    	return true;
    }
    
    public boolean checkEdge4(int[][] grid) {
    	if(nextToPlay == Color.WHITE) {
    		if(getPoint(grid[1][2]) == Color.BLACK ) {
	    		return false;
	    	}
	    	for(int i=0;i<2;i++) {
				for(int j=0;j<3;j++) {
					if(i==0 && j==0)
						continue;
					if(i==1 && j==0)
						continue;
					if(i==1 && j==2)
						continue;
					if( getPoint(grid[i][j]) != edge3[i][j] )
						return false;
				}
	    	}
	    	
    	} else {
    		return false;
    	}
    //	System.out.println("Matched edge4!");
    	return true;
    }
    
    public boolean checkEdge5(int[][] grid) {
    	if(nextToPlay == Color.WHITE) {
	    	for(int i=0;i<2;i++) {
				for(int j=0;j<3;j++) {
					if(i==0 && j==0)
						continue;
					if( getPoint(grid[i][j]) != edge5[i][j] )
						return false;
				}
	    	}
    	} else {
    		return false;
    	}
    //	System.out.println("Matched edge5!");
    	return true;
    }	
	/**
	 * Used by many methods to efficiently store lists of board positions.  Allocated only once,
	 * to the number of points on the board.
	 */
	protected Board.PositionList temp1, temp2;
	public static boolean gameOver;
		
	public Game(int sideSize) {
		board = new Board(sideSize);
		temp1 = board.createPositionList();
		temp2 = board.createPositionList();
		nextToPlay = Color.BLACK;
	}
	
	public abstract String getName();
	
	public String toString() {
		return getName() + " " + getSideSize() + "x" + getSideSize() + " handicap=" + handicap + " komi=" + komi;
	}
		
	public Object clone() throws CloneNotSupportedException {
		Game that = (Game) super.clone();
		that.board = this.board.duplicate();
		that.temp1 = that.board.createPositionList();
		that.temp2 = that.board.createPositionList();
		return that;
	}
	
	public Object createSimulationGame() {
		try {
			return clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	public Game duplicate() {
		try {
			return (Game) clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	public Board.Grid getGrid() {return board.getGrid();}
	
	public void setKomi(float komi) {
		if (numMoves > 0) System.out.println("game already started");
		this.komi = komi;
	}

	public void setFixedHandicap(int handicap) {
		if (numMoves > 0) System.out.println("game already started");
		if (this.handicap > 0) board.clear();
		this.handicap = handicap;
		placeFixedHandicapStones();
	}
	
	/**
	 * Place fixed handicap stones on the board, and change to White's turn.
	 * @throws IllegalArgumentException if the combination of handicap and board size is illegal
	 */
	protected void placeFixedHandicapStones() {
		if (handicap == 0) return;
		final int ss = board.getSideSize();
		if (
			handicap < 0
			|| handicap == 1
			|| handicap > (ss % 2 == 0 ? 4 : 9)
			|| ss < 7
			|| ss == 7 && handicap > 4
		) System.out.println("illegal handicap " + handicap);
		
		final int ho = ss < 13 ? 2 : 3;  // handicap offset
		final int s = 1 + ho, b = ss - ho, m = (ss+1)/2;
		
		for (int i=1; i <= handicap; i++) {
			switch(i) {
				case 1:  board.setPoint(getGrid().at(s, s), Color.BLACK); break;
				case 2:  board.setPoint(getGrid().at(b, b), Color.BLACK); break;
				case 3:  board.setPoint(getGrid().at(s, b), Color.BLACK); break;
				case 4:  board.setPoint(getGrid().at(b, s), Color.BLACK); break;
				case 5:
					if (handicap == 5) board.setPoint(getGrid().at(m, m), Color.BLACK);
					else board.setPoint(getGrid().at(s, m), Color.BLACK);
					break;
				case 6:  board.setPoint(getGrid().at(b, m), Color.BLACK); break;
				case 7:
					if (handicap == 7) board.setPoint(getGrid().at(m, m), Color.BLACK);
					else board.setPoint(getGrid().at(m, s), Color.BLACK);
					break;
				case 8:  board.setPoint(getGrid().at(m, b), Color.BLACK); break;
				case 9:  board.setPoint(getGrid().at(m, m), Color.BLACK); break;
				default:  assert false;
			}
		}
		nextToPlay = Color.WHITE;
	}
	
	/**
	 * Check if a point reaches another color.  The method uses both <code>temp1</code> and
	 * <code>temp2</code> to do its work.  If the point does not reach the color, then <code>temp2</code>
	 * will hold all the points traversed in an attempt to reach the other color.  All those points
	 * will be of the same color as the original point.
	 * @return whether the given point reaches another color
	 * @throws IllegalArgumentException if <code>p</code> is the same as the color, or <code>z</code> out of bounds
	 */
	protected boolean reaches(int z, Point p) {
		Point color = getPoint(z);
		if (color == Point.OUT_OF_BOUNDS || color == p) throw new IllegalArgumentException();
		temp1.clear();  temp2.clear();
		temp1.add(z);
		for (int i = 0; i < temp1.size(); i++) {
			z = temp1.get(i);
			if (getPoint(z) == p) return true;
			if (getPoint(z) == color) {
				// string continues
				temp2.add(z);  // save string member
				temp1.addNeighbors(z);  // expand search
			}
		}
		return false;
	}
	
	/**
	 * Verify that the string connected to the stone at the given position is still alive,
	 * and remove it if it's not.  If the position given is empty or out of bounds, do nothing.
	 * @param z the position of a stone that's a member of the string to verify
	 * @return true if the string was there, alive and remains on the board, false otherwise
	 */
	protected boolean verifyString(int z) {
		Point color = getPoint(z);
		if (color == Point.EMPTY || color == Point.OUT_OF_BOUNDS) return false;
		if (reaches(z, Point.EMPTY)) return true;
		// string is dead!
		for (int i=0; i < temp2.size(); i++) {
			board.setPoint(temp2.get(i), Point.EMPTY);
		}
		return false;
	}
	
	/**
	 * Play at the given position.  The move is automatically assigned to the player
	 * whose turn it is.  If the move is not legal, the board position is not changed
	 * and it's still the same player's move.  If the move is legal, the board reflects
	 * the effects of the move, the number of moves has increased by 1, the move
	 * is recorded, and it's the other player's move.
	 * @param z the position to play at, or MOVE_PASS to pass
	 * @return true if the play was successful, false if it was illegal and rejected
	 */
	public abstract boolean play(int z);
	
	/**
	 * Return whether this game is over.  When the game is over, no further moves
	 * can be made.  Furthermore, for some games, the score can only be counted
	 * once the game is over.
	 * @return true if the game is over, false otherwise
	 */
	public abstract boolean isOver();
	
	/**
	 * Return whether passing is allowed.
	 * @return true if passing can be a valid move, false otherwise; if passing is not allowed,
	 * there must exist another mechanism for ending the game
	 */
	public boolean isPassingAllowed() {return true;}
	
	/**
	 * Calculate the final score of the game.  Return the score in favor of black,
	 * adjusted by the komi.
	 * @return the final score; positive if black won, negative if white won
	 * @throws IllegalStateException if unable to calculate the score for some reason
	 */
	public abstract float score();
	
	/**
	 * Calculate the final score of the game, from the point of view of the given side.
	 * Useful to always get a positive value for a "good" score.
	 * @param side the player from whose perspective the score is returned
	 * @return the final score; positive if the given side won, negative otherwise
	 */
	public float score(Color side) {
		return side == Color.BLACK ? score() : -score();
	}
	
	
	/**
	 * Return a move played previously.  If the offset is positive, it starts counting from the beginning,
	 * the first move being 1.  If the offset is negative, it starts counting from the current position in
	 * the game, with the last move played being -1.  Moves never include fixed handicap placement.
	 * @param offset the offset of the move to retrieve
	 * @return the move played at the given offset
	 * @throws IndexOutOfBoundsException if the offset specified is outside the list of moves played, or the history horizon of this game
	 */
	public int getMove(int offset) {
		if (offset == 0) return lastMove; //changed from throwing an exception to get this easily
		if (offset < 0) offset = numMoves + offset + 1;
		if (offset == numMoves) return lastMove;
		else System.out.println("requested move is beyond history horizon");
		return 0;
	}
	
	public void recordMove(int move) {
		lastMove = move;
		numMoves++;
	}

	public double getKomi() {return komi;}

	public int getNumPoints() {
		return board.getNumPoints();
	}
	
	public Board.PositionList getPotentiallyPlayablePoints() {
		return board.getEmptyPoints();
	}
	
	public Board.PositionList getNonEmptyPoints() {
		return board.getNonEmptyPoints();
	}

	public Point getPoint(int z) {
		return board.getPoint(z);
	}

	public int getSideSize() {
		return board.getSideSize();
	}
	
	/**
	 * Returns the handicap.
	 */
	public int getHandicap() {
		return handicap;
	}

	/**
	 * Returns the next color to play.
	 */
	public Color getNextToPlay() {
		return nextToPlay;
	}

	/**
	 * Returns the number of moves played so far.  Placing fixed handicap stones
	 * does not count as moves, but placing free ones does.
	 * @return int the number of moves played in the game so far
	 */
	public int getNumMoves() {
		return numMoves;
	}



}
