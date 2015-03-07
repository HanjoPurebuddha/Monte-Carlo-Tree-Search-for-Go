package com.ideanest.vegos.ai;

import com.ideanest.vegos.game.Color;

/**
 * 
 * @author Piotr Kaminski
 */
public class Ruby extends WeightedAnnealingPlayer {

	public Ruby(String name) {
		super(name);
	}
	
	public Ruby() {
		this("Ruby");
	}

	public void startCycle(int numRefinements) {
		for (int j=0; j<2; j++) {
			sims[j] = new Rob(game, Color.get(j), moveCounter);
		}
	}
	

}
