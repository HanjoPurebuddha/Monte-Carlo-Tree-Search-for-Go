package ai;


public class Configuration {
	/* to implement....#
	 * uct changes */
	public boolean ucbTuned;
	public double firstPlayUrgency;
	
	/* initialization changes */
	boolean openBook;
	boolean heuristicInitialization;
	
	/* simulation changes */
	public boolean simulateAvoidEyes;
	public boolean simulateAtari;
	public boolean simulatePatterns;
	public boolean simulateTakePieces;
	public boolean simulateMercyRule;
	
	/* bonus changes */
	boolean bonusAtari;
	boolean bonusPatterns;
	boolean bonusTakePiece;
	boolean bonusLocalNeighbourhood;
	
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
    public int dynamicTree;
    
    public Configuration(boolean binaryScoring, boolean uct, boolean rave, boolean weightedRave, double initialWeight, 
    		double finalWeight, int raveSkip, boolean dontExpandEyes, int dynamicTree, double firstPlayUrgency,
    		boolean simulateAvoidEyes, boolean simulateAtari, boolean simulatePatterns, boolean simulateTakePieces, boolean simulateMercyRule) {
    	this.binaryScoring = binaryScoring;
    	this.uct = uct;
    	this.dontExpandEyes = dontExpandEyes;
    	this.rave = rave;
    	this.weightedRave = weightedRave;
    	this.initialWeight = initialWeight;
    	this.finalWeight = finalWeight;
    	this.raveSkip = raveSkip;
    	this.dynamicTree = dynamicTree;
    	this.simulateAvoidEyes = simulateAvoidEyes;
    	this.simulateAtari = simulateAtari;
    	this.simulatePatterns = simulatePatterns;
    	this.simulateTakePieces = simulateTakePieces;
    	this.firstPlayUrgency = firstPlayUrgency;
    	this.simulateMercyRule = simulateMercyRule;
    }
    
    public double firstPlayUrgencyValue() {
    	if(firstPlayUrgency > 0)
    		return firstPlayUrgency;
    	return -Double.MAX_VALUE;
    }
}
