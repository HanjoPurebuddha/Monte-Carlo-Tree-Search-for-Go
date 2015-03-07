package com.ideanest.vegos.ai;

/**
 * 
 * @author Piotr Kaminski
 */
public abstract class SimPlayer extends Player {

	public SimPlayer(String name) {
		super(name);
	}

	public abstract void influence(WeightedMoveList otherMoves);
	public abstract WeightedMoveList getFirstMoves();

}
