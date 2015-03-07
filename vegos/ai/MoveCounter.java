package com.ideanest.vegos.ai;

import com.ideanest.vegos.game.Color;

/**
 * Count moves made in a game.
 * @author Piotr Kaminski
 */
public interface MoveCounter {
	int countMove(Color side, int move);
	void reset();
}
