package com.ideanest.vegos.ai;

import com.ideanest.vegos.game.Color;

/**
 * 
 * @author Piotr Kaminski
 */
public class Stanley extends WeightedAnnealingPlayer {
	protected Mixer mixer = new FarSwapMixer(new LinearCoolingSchedule(0.99f, 10));
	
	public Stanley() {
		super("Stanley");
	}

	public Stanley(String name) {
		super(name);
	}
	
	public String toString() {
		return super.toString() + " mix: {" + mixer + "}";
	}

	public void setMixer(Mixer mixer) {this.mixer = mixer;}

	public void startCycle(int numRefinements) {
		for (int j=0; j<2; j++) {
			sims[j] = new Sam(game, Color.get(j), mixer, moveCounter);
		}
		mixer.reset(numRefinements);
	}
	
	protected void prepRefinement() {
		super.prepRefinement();
		mixer.nextIteration();
	}

}
