package game;

/**
 * The contents of a point on a board.  A superset of the colors.
 * 
 * @author Piotr Kaminski
 */

public class Point {

	public static final Point EMPTY = new Point(2, "empty");
	public static final Point OUT_OF_BOUNDS = new Point(3, "out of bounds");
	
	public static int size() {return 4;}
	
	protected final String name;
	protected final int index;
	protected Point(int index, String name) {
		this.index = index;
		this.name = name;
	}
	public String toString() {return name;}
	public int getIndex() {return index;}
	
}
