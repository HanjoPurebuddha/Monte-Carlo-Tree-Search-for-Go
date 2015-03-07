package com.ideanest.vegos.ai;

/**
 * 
 * @author Piotr Kaminski
 */
public abstract class WeightedAnnealingPlayer extends SimulatedAnnealingPlayer {

	protected MoveCounter moveCounter = new NullMoveCounter();

	public WeightedAnnealingPlayer(String name) {
		super(name);
	}
	
	public void setMoveCounter(MoveCounter moveCounter) {this.moveCounter = moveCounter;}

	protected void prepRefinement() {
		super.prepRefinement();
		moveCounter.reset();
	}
	
	public String toString() {
		return super.toString() + " move counts: " + moveCounter;
	}

}
