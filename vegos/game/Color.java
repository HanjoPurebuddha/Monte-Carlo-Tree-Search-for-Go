package com.ideanest.vegos.game;

import com.ideanest.vegos.gtp.*;

/**
 * The color of a stone; also use to reprent the two sides playing.
 * @author Piotr Kaminski
 */
public class Color extends Point {
	
	public static final Color BLACK = new Color(0, "black");
	public static final Color WHITE = new Color(1, "white");

	protected Color(int index, String name) {
		super(index, name);
	}
	
	public Color inverse() {
		return this == BLACK ? WHITE : BLACK;
	}
	
	public static Color get(int index) {
		switch(index) {
			case 0: return BLACK;
			case 1: return WHITE;
			default: throw new IllegalArgumentException("no color with index " + index);
		}
	}
	
}
