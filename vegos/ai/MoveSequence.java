package com.ideanest.vegos.ai;

import java.util.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * Holds a conceptually infinite sequence of weighted moves.  Optimized for random access and sparseness.
 * @author Piotr Kaminski
 */
class MoveSequence {
	private WeightedMove[] moves;
	private int size;
	private SortedMap sparseExtension;
	private boolean sparseExtensionInUse, arrayContiguous = true;
	
	public static final int SPARSE_RATIO = 5;
	
	public MoveSequence() {
		moves = new WeightedMove[100];
	}
	
	public MoveSequence(WeightedMove[] moves, int size) {
		if (moves.length == 0) {
			this.moves = new WeightedMove[100];
		} else {
			this.moves = moves;
			this.size = size;
		}
	}
	
	public void set(int index, WeightedMove wmove) {
		if (wmove != null && !sparseExtensionInUse && index >= moves.length && SPARSE_RATIO*(size+1) >= index+1) {
			// expand array by 50%, index is close enough to stay at or below min sparseness ratio for desired index
			WeightedMove[] temp = new WeightedMove[moves.length*3/2 + 1];
			System.arraycopy(moves, 0, temp, 0, moves.length);
			moves = temp;
		}
		if (index < moves.length) {
			count(moves[index], wmove);
			moves[index] = wmove;
			if (arrayContiguous) {
				if (wmove == null) arrayContiguous = index == moves.length-1 || moves[index+1] == null;
				else arrayContiguous = index == 0 || moves[index-1] != null;
			}
		} else if (wmove == null) {
			if (!sparseExtensionInUse) return;
			WeightedMove prev = (WeightedMove) sparseExtension.remove(new Integer(index));
			count(prev, wmove);
			if (sparseExtension.size() == 0) sparseExtensionInUse = false;
		} else { // wmove != null
			if (sparseExtension == null) sparseExtension = new TreeMap();
			WeightedMove prev = (WeightedMove) sparseExtension.put(new Integer(index), wmove);
			count(prev, wmove);
			sparseExtensionInUse = true;
		}
	}
	
	private void count(WeightedMove prev, WeightedMove curr) {
		if (prev == null && curr != null) size++;
		else if (prev != null && curr == null) size--;
	}
	
	public WeightedMove get(int index) {
		if (index < moves.length) return moves[index];
		else if (sparseExtensionInUse) return (WeightedMove) sparseExtension.get(new Integer(index));
		else return null;
	}
	
	public void swap(int index1, int index2) {
		WeightedMove wmove1 = get(index1), wmove2 = get(index2);
		set(index2, wmove1);
		set(index1, wmove2);
	}
	
	public int size() {return size;}
	
	public void compact() {
		if (!sparseExtensionInUse && arrayContiguous) return;
		WeightedMove[] temp = new WeightedMove[size()*3/2];
		int j = 0;
		for (int i=0; i<moves.length; i++) if (moves[i] != null) temp[j++] = moves[i];
		if (sparseExtensionInUse) {
			for (Iterator it = sparseExtension.values().iterator(); it.hasNext(); ) {
				temp[j++] = (WeightedMove) it.next();
			}
		}
		moves = temp;
		sparseExtension = null;
		sparseExtensionInUse = false;
		arrayContiguous = true;
	}
	
	public void updateWeightsAndSort(float score) {
		compact();
		for (int i = 0; i < size; i++) {
			moves[i].updateWeight(score);
		}
		if (size() > 0) Arrays.sort(moves, 0, size(), WeightedMove.WEIGHT_COMPARATOR);
	}
	
	public void sortByWeight() {
		compact();
		if (size() > 0) Arrays.sort(moves, 0, size(), WeightedMove.WEIGHT_COMPARATOR);
	}
	
	public void improve(WeightedMoveList wmoves) {
		compact();
		for (int i = 0; i < size; i++) {
			wmoves.putMax(moves[i]);
		}
	}
}
