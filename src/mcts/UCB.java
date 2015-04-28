package mcts;

import java.util.Random;

import ai.Configuration;

public class UCB {
	Configuration nodeRuleSet;
	int totalActions = 0;
	/* initialize the random generation */
    static Random r = new Random();
    
    /* initialize epsilon for use in uct */
    static double epsilon = 1e-6;
    
	public UCB(Configuration nodeRuleSet) {
		this.nodeRuleSet = nodeRuleSet;
	}
	
	/* perform the calculation required for uct */
    private double calculateUctValue(int type, TreeNode parent, TreeNode tn) {
    	double mean = tn.totValue[type] / (tn.nVisits[type] + epsilon);
    	if(nodeRuleSet.ucbTuned) {
    		/* ((this nodes total value) / (visits + e)) + sqrt(log(visits) / (visits + e) + random number) */
    		return mean +
    				getBonus(tn) * (Math.sqrt((Math.log(parent.nVisits[0]+1)) / (tn.nVisits[0] + epsilon )) *
                    getVariance(type, mean, parent, tn)) + r.nextDouble() * epsilon;
    	}
    	if(nodeRuleSet.randomUcb) {
    		/* ((this nodes mean value) / (visits + e)) + sqrt(log(visits) / (visits + e) + random number) */
	    	return mean +
	    			getBonus(tn) * (Math.sqrt(Math.log(parent.nVisits[0]+1) / (tn.nVisits[0] + epsilon)) +
                    r.nextDouble() * epsilon);
    	}
    	if(nodeRuleSet.ucb) {
    		/* (average value + sqrt(2 log parentN / childN)) */
    		return mean +
    				getBonus(tn) * (Math.sqrt((2 * Math.log(parent.nVisits[0]+1)) / (tn.nVisits[0])));
    	}
    	if(nodeRuleSet.simpleUcb) {
    		/* (value/visits + random number) */
	    	return getBonus(tn) * (mean + epsilon +
                    r.nextDouble() * epsilon);
    	}
    	return 0;
    }
    
    public double getVariance(int type, double mean, TreeNode parent, TreeNode tn) {
    	double value = 0.25;
    	/* estimate of the variance + sqrt(2 log(n) / n) 
    	 * variance computed by subtracting the mean from each value of the reward, and then getting the mean
    	 * of those subtracted values squared*/
    	double variance = 0;
    	for(double result : tn.normalValues) {
    		variance = variance + Math.sqrt((result - mean));
    	}
    	variance = variance / (tn.nVisits[type]);
    	variance = variance + Math.sqrt(2*Math.log(parent.nVisits[0]+1) / tn.nVisits[0]);
    	if(variance < value)
    		value = variance;
    	/* if overall V is less than 0.25, return 0.25 */
    	return value;
    }
    Random rand = new Random();
    public int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
    
    /* perform the calculation needed to weight the node, in order to balance rave and uct */
    public double calculateWeight(TreeNode tn) {
    	
    	/* when only a few simulations have been seen, the weight is closer to 1, weighting the RAVE value more highly
    	 * when many simulations have been seen, the weight is closer to 0, weighting the MC value more highly
    	 */
    	if(tn.nVisits[0] == 0) {
    		return 1;
    	}
    	double weight = 1 - (nodeRuleSet.initialWeight * (tn.nVisits[0] / nodeRuleSet.finalWeight));
    	if(weight < 0) {
    		return 0;
    	}
    	return weight;
    }
    
    /* perform the calculation needed to balance exploration and exploitation */
    public double getBonus(TreeNode tn) {
    	
    	/* reduce the value of nodes the deeper we go, allowing more exploration of alternate subtrees, etc
    	 */
    	if(nodeRuleSet.explorationWeight == 0) {
    		return 1;
    	}
    	if(tn.nVisits[0] == 0) {
    		/* first play urgency! */
    		return 1 + nodeRuleSet.firstPlayUrgency;
    	}
    	double weight = 1 - (nodeRuleSet.initialWeight * (tn.nVisits[0] / nodeRuleSet.explorationWeight));
    	if(weight < 0) {
    		return 0.1;
    	}
    	return weight;
    }
    
    /* get the uct value for a node with a small random number to break ties randomly in unexpanded nodes  */
    public double getUctValue(TreeNode parent, TreeNode tn) {

    	/* if using UCT no rave */
        if (nodeRuleSet.uct) {
        	
        	/* get the uct value only */
        	return calculateUctValue(0, parent, tn);
    		
        } else if (nodeRuleSet.rave) {
        	
        	/* get the rave value only */
        	return calculateUctValue(1, parent, tn);
    		
        } else if (nodeRuleSet.weightedRave) {
        	
        	/* calculate it using the weight */
        	double weight = calculateWeight(tn);
    		return ((1 - weight) * calculateUctValue(0, parent, tn)) + (weight * calculateUctValue(1, parent, tn));
	        
    	}
        return 0;
    }
}
