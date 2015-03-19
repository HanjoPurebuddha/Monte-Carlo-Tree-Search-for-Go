package gtp;

import game.*;

/**
 * A move in a game of Go, for GTP communication only.
 * @author Piotr Kaminski
 */
public class Move {
	public final Color color;
	public final Vertex vertex;
	public Move(Color color, Vertex vertex) {
		this.color = color;
		this.vertex = vertex;
	}
	public String toString() {
		return color + " " + vertex;
	}
}
