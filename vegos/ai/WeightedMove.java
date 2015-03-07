package com.ideanest.vegos.ai;

import java.util.Comparator;

import com.ideanest.util.UnexpectedException;

/**
 * A simulated move.
 * @author Piotr Kaminski
 */
class WeightedMove implements Cloneable {
	final int z;
	int numTotalPlays, numPlaysLastGame;
	float weight;
	
	public WeightedMove(int z) {this.z = z;}
	public WeightedMove(int z, float fixedWeight) {
		this.z = z;
		this.weight = fixedWeight;
	}
	
	public String toString() {
		return "" + z + "@" + weight + " (" + numTotalPlays + "/+" + numPlaysLastGame + ")";
	}
	
	public WeightedMove duplicate() {
		try {
			return (WeightedMove) clone();
		} catch (CloneNotSupportedException e) {
			throw new UnexpectedException(e);
		}
	}

	public void updateWeight(float score) {
		if (numPlaysLastGame > 0) {
			int newTotalPlays = numTotalPlays + numPlaysLastGame;
			weight = (weight * numTotalPlays + score * numPlaysLastGame) / newTotalPlays;
			numTotalPlays = newTotalPlays;
			numPlaysLastGame = 0;
		}
	}
	
	public void add(WeightedMove that) {
		int newTotalPlays = this.numTotalPlays + that.numTotalPlays;
		this.weight = (this.weight * this.numTotalPlays + that.weight * that.numTotalPlays) / newTotalPlays;
		this.numTotalPlays = newTotalPlays;
	}
	
	public void wasPlayed() {
//		numPlaysLastGame++;
		numPlaysLastGame = 1;
	}
	
	public static final Comparator WEIGHT_COMPARATOR = new Comparator() {
		public int compare(Object a, Object b) {
			return Float.compare(((WeightedMove) b).weight, ((WeightedMove) a).weight);
		}
	};
	
}
