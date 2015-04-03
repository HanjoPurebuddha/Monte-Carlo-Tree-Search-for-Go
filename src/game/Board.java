package game;

import java.util.Arrays;
import java.util.Random;

//import com.ideanest.util.UnexpectedException;
import gtp.*;
import gtp.Vertex;

/**
 * A board for playing Go.  Conceptually, a board is a square composed of
 * points.  Each point may be empty or hold a black or a white stone.  The
 * points can be addressed either by pairs of x/y coordinates, ranging from
 * 1 to the width of the board, or by a single z index, ranging from 0 to one
 * less than the number of points on the board.  The z index goes across rows
 * first, so, for a board of size 5x5, z=7 is equivalent to x=3, y=2.
 * 
 * @author Piotr Kaminski
 */

public class Board implements Cloneable {
	
	protected final int sideSize;
	protected final Grid grid;
	protected final int numPoints;
	protected Point[] points;
	protected int[] pointCounts;
	protected Color nextToPlay;
	protected boolean frozen;
	
	protected static final Random rnd = new Random();
	
	public Board(int sideSize) {
		this.sideSize = sideSize;
		numPoints = sideSize * sideSize;
		points = new Point[numPoints];
		pointCounts = new int[3];
		clear();
		grid = new Grid();
	}
	
	public Grid getGrid() {return grid;}
	
	/**
	 * Freeze the board, so further mutations are refused.
	 */
	public void freeze() {frozen = true;}
	public boolean isFrozen() {return frozen;}
	protected void checkFrozen() {
		if (frozen) throw new IllegalStateException("board frozen");
	}
	
	/**
	 * Return the number of points on each side of this square board.
	 */
	public int getSideSize() {return sideSize;}
	
	/**
	 * Return the total number of points on this board.
	 */
	public int getNumPoints() {return numPoints;}
		
	/**
	 * Return the contents of the point at coordinate z.  If the point
	 * is out of bounds, return <code>Point.OUT_OF_BOUNDS</code>.
	 */
	public Point getPoint(int z) {
		if (z < 0 || z >= getNumPoints()) return Point.OUT_OF_BOUNDS;
		else return points[z];
	}
	
	public void clear() {
		checkFrozen();
		Arrays.fill(points, Point.EMPTY);
		pointCounts[Point.EMPTY.getIndex()] = numPoints;
		pointCounts[Color.BLACK.getIndex()] = 0;
		pointCounts[Color.WHITE.getIndex()] = 0;
	}
		
	public Point setPoint(int z, Point p) {
		checkFrozen();
		Point oldp = points[z];
		points[z] = p;
		pointCounts[oldp.getIndex()]--;
		pointCounts[p.getIndex()]++;
		return oldp;
	}
	

	/**
	 * Return a list of all empty points on the board.
	 */
	public PositionList getEmptyPoints() {
		PositionList list = new PositionList();
		for (int i = 0; i < numPoints; i++) {
			if (getPoint(i) == Point.EMPTY) list.add(i);
		}
		assert list.size() == pointCounts[Point.EMPTY.getIndex()];
		return list;
	}
	
	/**
	 * Return a list of all non-empty points on the board.
	 */
	public PositionList getNonEmptyPoints() {
		PositionList list = new PositionList();
		for (int i = 0; i < numPoints; i++) {
			if (getPoint(i) != Point.EMPTY) list.add(i);
		}
		assert list.size() == pointCounts[Color.BLACK.getIndex()] + pointCounts[Color.WHITE.getIndex()];
		return list;
	}
	
	public Object clone() throws CloneNotSupportedException {
		Board that = (Board) super.clone();
		that.points = new Point[numPoints];
		System.arraycopy(this.points, 0, that.points, 0, numPoints);
		that.pointCounts = new int[this.pointCounts.length];
		System.arraycopy(this.pointCounts, 0, that.pointCounts, 0, this.pointCounts.length);
		return that;
	}
	
	public Board duplicate() {
		try {
			return (Board) clone();
		} catch (CloneNotSupportedException e) {
		//	throw new UnexpectedException(e);
			return null;
		}
	}

	public int hashCode() {
		return pointCounts[0] << 10 + pointCounts[1];
	}
	
	public boolean equals(Object o) {
		if (o.getClass() != Board.class) return false;
		Board that = (Board) o;
		return
			Arrays.equals(this.pointCounts, that.pointCounts) &&
			Arrays.equals(this.points, that.points) &&
			this.nextToPlay == that.nextToPlay;
	}
	
	public Board copy() {
		Board copyBoard = new Board(this.sideSize);
		
		System.arraycopy(this.points, 0, copyBoard.points, 0, points.length);
		System.arraycopy(this.pointCounts, 0, copyBoard.pointCounts, 0, pointCounts.length);
		
		copyBoard.nextToPlay = this.nextToPlay;
		
		return copyBoard;
	}

	public Color getNextToPlay() {
		return nextToPlay;
	}

	public void setNextToPlay(Color nextToPlay) {
		checkFrozen();
		this.nextToPlay = nextToPlay;
	}
	
	/**
	 * A raw list of positions on a board, initialized to the number of points on the board plus one (for pass, if needed).
	 * @author Piotr Kaminski
	 */
	public class PositionList {
		private int[] pos = new int[getNumPoints()+1];
		private int len;
		public PositionList() {}
		
		/**
		 * Add the given position to the list if it's not already present (and not out of bounds).
		 * @return whether the position list changed as a result of the operation
		 */
		public boolean add(int z) {
			if (z < 0 || z >= getNumPoints()) return false;
			return addInternal(z);
		}

		/**
		 * Add a pass move to the list if it's not already present.  This overrides the usual
		 * bounds-checking.  Although moves don't really belong in this list (since it's a list
		 * of positions) it's just too convenient to be able to add as pass move in.
		 * @return whether the position list changed as a result of the operation
		 */
		public boolean addPass() {
			return addInternal(Game.MOVE_PASS);
		}
		
		/**
		 * Add an int to the list, without checking validity as a position.
		 * @param z the int to add, representing a position or a move
		 * @return whether the list changed
		 */
		private boolean addInternal(int z) {
			for (int i=0; i<len; i++) if (pos[i] == z) return false;
			assert len < pos.length;	// should never exceed array length, by definition!
//			if (len == pos.length) {
//				// expand array
//				int[] temp = new int[pos.length+20];
//				System.arraycopy(pos, 0, temp, 0, pos.length);
//				pos = temp;
//			}
			pos[len++] = z;
			return true;
		}
		
		/**
		 * Return the position at the given index.
		 */
		public int get(int i) {
			assert i < len;
			return pos[i];
		}
		/**
		 * Return the number of positions currently in the list.
		 */
		public int size() {return len;}
		/**
		 * Clear the list.
		 */
		public void clear() {len = 0;}
		/**
		 * Add the neighbours of z to the list.
		 * @return true if the list was modified (new neighbors were added), false otherwise
		 */
		public boolean addNeighbors(int z) {
			return add(grid.left(z)) | add(grid.right(z)) | add(grid.up(z)) | add(grid.down(z));
		}
		
		public boolean addDiagonalNeighbors(int z) {
			return add(grid.upleft(z)) | add(grid.upright(z)) | add(grid.downleft(z)) | add(grid.downright(z));
		}
		
		public void shuffle() {
			for (int i=len; i>1; i--) {
				int j = rnd.nextInt(i);
				int t = pos[i-1];
				pos[i-1] = pos[j];
				pos[j] = t;
			}
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append('[');
			for (int i=0; i<len; i++) {
				if (i != 0) buf.append(",");
				if (pos[i] == Game.MOVE_PASS) buf.append("pass");
				else buf.append(new Vertex(grid.x(pos[i]), grid.y(pos[i])));
			}
			return buf.toString();
		}
}

	public PositionList createPositionList() {
		return new PositionList();
	}
	
	public PositionList createPositionList(PositionList orig) {
		PositionList plist = new PositionList();
		for (int i=0; i<orig.size(); i++) {
			int z = orig.get(i);
			if (z == -1) plist.addPass(); else plist.add(z);
		}
		return plist;
	}
	
	public class Grid {
		/**
		 * Convert (x,y) coordinates to z.
		 * @throws IndexOutOfBoundsException if x or y is out of bounds
		 */	
		public int at(int x, int y) {
			if (x < 1 || x > sideSize || y < 1 || y > sideSize) throw new IndexOutOfBoundsException("board coordinates out of bounds: (" + x + "," + y + "), board size " + sideSize);
			else return (y-1) * sideSize + (x-1);
		}
		
		public int x(int z) {
			if (z < 0 || z >= numPoints) throw new IndexOutOfBoundsException("board coordinates out of bounds");
			return (z % sideSize) + 1;
		}

		public int y(int z) {
			if (z < 0 || z >= numPoints) throw new IndexOutOfBoundsException("board coordinates out of bounds");
			return (z / sideSize) + 1;
		}
				
		public int left(int z) {return z-- % sideSize == 0 ? -1 : z;}
		public int right(int z) {return ++z % sideSize == 0 ? -1 : z;}
		public int up(int z) {z -= sideSize;  return z < 0 ? -1 : z;}
		public int down(int z) {z += sideSize;  return z >= numPoints ? -1 : z;}
		public int upleft(int z) {z = left(z);  return z == -1 ? -1 : up(z);}
		public int upright(int z) {z = right(z);  return z == -1 ? -1 : up(z);}
		public int downleft(int z) {z = left(z);  return z == -1 ? -1 : down(z);}
		public int downright(int z) {z = right(z);  return z == -1 ? -1 : down(z);}
	}	


}
