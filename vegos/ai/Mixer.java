package com.ideanest.vegos.ai;

/**
 * Mixes up moves in a sim player's sequence.
 * @author Piotr Kaminski
 */
public interface Mixer {
	/**
	 * The move at <code>index</code> will be played next.  Shake things around if
	 * necessary, so that a different move takes its place.  If the spot ends up holding
	 * <code>null</code>, a new move will be generated.
	 * @param seq the sequence to mix
	 * @param index the index of the next move that will be used by the sim player
	 */ 
	void mix(MoveSequence seq, int index);
	
	/**
	 * Reset the mixer to be used over the given number of refining iterations.
	 * @param numIterations the number of iterations that the mixer will be used for
	 */
	void reset(int numIterations);
	
	/**
	 * The next iteration is beginning.
	 */
	void nextIteration();
	
}
