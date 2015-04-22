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
    	double mean = (tn.totValue[type] + getBonus(tn) / (tn.nVisits[type]));
    	if(nodeRuleSet.ucbTuned) {
    		/* ((this nodes total value) / (visits + e)) + sqrt(log(visits) / (visits + e) + random number) */
    		return mean + epsilon +
    				Math.sqrt((Math.log(parent.nVisits[0]+1)) / (tn.nVisits[0] )) *
                    getVariance(type, mean, parent, tn);
    	}
    	if(nodeRuleSet.randomUcb) {
    		/* ((this nodes mean value) / (visits + e)) + sqrt(log(visits) / (visits + e) + random number) */
	    	return  mean + epsilon +
                    Math.sqrt((2 * Math.log(parent.nVisits[0]+1)) / (tn.nVisits[0] + epsilon)) +
                    r.nextDouble() * epsilon;
    	}
    	if(nodeRuleSet.ucb) {
    		/* (average value + sqrt(2 log parentN / childN)) */
    		return mean +
    				Math.sqrt((2 * Math.log(parent.nVisits[0]+1)) / (tn.nVisits[0]));
    	}
    	if(nodeRuleSet.simpleUcb) {
    		/* (value/visits + random number) */
	    	return mean + epsilon +
                    r.nextDouble() * epsilon;
    	}
    	return 0;
    }
    
    public double getVariance(int type, double mean, TreeNode parent, TreeNode tn) {
    	double value = 0.25;
    	/* estimate of the variance + sqrt(2 log(n) / n) 
    	 * variance computed by maintaing the sum of squares of the reward, as well as the mean */
    	double variance = 0;
    	for(double result : tn.normalValues) {
    		variance = variance + (result - mean);
    	}
    	variance = variance / (tn.nVisits[type]);
    	variance = variance + Math.sqrt(2*Math.log(parent.nVisits[0]+1) / tn.nVisits[0]);
    	if(variance > value)
    		value = variance;
    	/* if overall V is less than 0.25, return 0.25 */
    	return value;
    }
    
    /* perform the calculation needed to weight the node, in order to balance rave and uct */
    public double calculateWeight(TreeNode tn) {
    	
    	/* when only a few simulations have been seen, the weight is closer to 1, weighting the RAVE value more highly
    	 * when many simulations have been seen, the weight is closer to 0, weighting the MC value more highly
    	 */
    	double weight = 1 - (nodeRuleSet.initialWeight * (tn.nVisits[0] / nodeRuleSet.finalWeight));
    	if(tn.nVisits[0] == 0) {
    		return 1;
    	}
    	if(weight < 0) {
    		return 0;
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
    /* get the bonus value for this node */
    public int getBonus(TreeNode tn) {
    	int bonusValue = 0;
    	if(nodeRuleSet.firstPlayUrgency > 0) {
		    if(tn.nVisits[0] == 0) //if this node hasnt been visited
		    	bonusValue += nodeRuleSet.firstPlayUrgencyValue(); //make sure it is!
	    }
    	if(nodeRuleSet.bonusPatterns > 0) {
    		if(tn.currentGame.lastMoveMatchesPatterns()) {
    			bonusValue += nodeRuleSet.bonusPatterns;

    		}
    	}
    	if(nodeRuleSet.bonusAvoidEyes != 0) {
    		if(tn.currentGame.checkEye(tn.currentGame.getMove(0)) == false) {
    			bonusValue -= nodeRuleSet.bonusAvoidEyes;
    		}
    	}
    	return bonusValue;
    }
}
