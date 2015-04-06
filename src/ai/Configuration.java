package ai;


public class Configuration {
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
    
    public Configuration(boolean binaryScoring, boolean uct, boolean rave, boolean weightedRave, double initialWeight, double finalWeight, boolean heuristicRave, int raveHeuristic, int raveSkip, boolean dontExpandEyes) {
    	this.binaryScoring = binaryScoring;
    	this.uct = uct;
    	this.dontExpandEyes = dontExpandEyes;
    	this.rave = rave;
    	this.weightedRave = weightedRave;
    	this.initialWeight = initialWeight;
    	this.finalWeight = finalWeight;
    	this.heuristicRave = heuristicRave;
    	this.raveHeuristic = raveHeuristic;
    	this.raveSkip = raveSkip;
    }
}
