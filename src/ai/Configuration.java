package ai;


public class Configuration {
	/* to implement....#
	 * uct changes */
	boolean ucbTuned = false;
	
	/* initialization changes */
	boolean openBook = false;
	boolean heuristicInitialization = false;
	
	/* simulation changes */
	boolean simulateAtari = false;
	boolean simulatePatterns = false;
	boolean simulateTakePiece = false;
	public boolean simulateInEyes = false;
	
	/* bonus changes */
	boolean bonusAtari = false;
	boolean bonusPatterns = false;
	boolean bonusTakePiece = false;
	boolean bonusLocalNeighbourhood = false;
	
	/* begin values for adjusting different features */
	public boolean binaryScoring;
    public boolean uct;
    public boolean rave;
    public boolean weightedRave;
    public double initialWeight;
    public double finalWeight;
    public boolean heuristicRave;
    public int raveHeuristic;
    public int raveSkip;
    public boolean dontExpandEyes;
    public boolean dynamicTree;
    
    public Configuration(boolean binaryScoring, boolean uct, boolean rave, boolean weightedRave, double initialWeight, 
    		double finalWeight, int raveSkip, boolean dontExpandEyes, boolean dynamicTree, boolean simulateInEyes) {
    	this.binaryScoring = binaryScoring;
    	this.uct = uct;
    	this.dontExpandEyes = dontExpandEyes;
    	this.rave = rave;
    	this.weightedRave = weightedRave;
    	this.initialWeight = initialWeight;
    	this.finalWeight = finalWeight;
    	this.raveSkip = raveSkip;
    	this.dynamicTree = dynamicTree;
    	this.simulateInEyes = simulateInEyes;
    }
}
