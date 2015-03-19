package gtp;

import game.Board;
import game.Game;

/**
 * A vertex on a go board.  Used only as part of the GTP protocol, since
 * the actual board implementation uses a more efficient format (a plain
 * int index).  However, a separate type is needed to correctly map GTP
 * commands.
 * 
 * @author Piotr Kaminski
 */
public class Vertex {
	private static final String PASS_STRING = "pass";
	private static final String RESIGN_STRING = "resign";		
	
	public static final Vertex PASS = new Vertex() {
		public String toString() {return PASS_STRING;}
		public int toPosition(Board.Grid grid) {return Game.MOVE_PASS;}
	};
	public static final Vertex RESIGN = new Vertex() {
		public String toString() {return RESIGN_STRING;}
		public int toPosition(Board.Grid grid) {return Game.MOVE_RESIGN;}
	};
	private final int x, y;
	private Vertex() {
		this.x = -1;
		this.y = -1;
	}
	public Vertex(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public static Vertex get(int z, Board.Grid grid) {
		if (z == Game.MOVE_PASS) return PASS;
		if (z == Game.MOVE_RESIGN) return RESIGN;
		return new Vertex(grid.x(z), grid.y(z));
	}
	public static Vertex get(String s) {
		if (s.toLowerCase().equals(PASS_STRING)) return PASS;
		if (s.toLowerCase().equals(RESIGN_STRING)) return RESIGN;
		char col = Character.toUpperCase(s.charAt(0));
		// adjust for skipping capital I
		if (col > 'I') col--;
		return new Vertex(col - 'A' + 1, Integer.parseInt(s.substring(1)));
	}
	public String toString() {
		StringBuffer buf = new StringBuffer();
		char col = (char) ('A' + x - 1);
		// adjust for skipping capital I
		if (col >= 'I') col++;
		buf.append(col);
		buf.append(y);
		return buf.toString();
	}
	public int toPosition(Board.Grid grid) {
		return grid.at(x, y);
	}
}
