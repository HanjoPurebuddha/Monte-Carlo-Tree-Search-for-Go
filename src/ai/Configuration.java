package ai;


public class Configuration {
	/* begin values for adjusting different features */
    
	public boolean binaryScoring = false;
    public boolean uct = false;
    public boolean rave = false;
    public boolean weightedRave = false;
    public int weight = 0;
    public boolean heuristicRave = false;
    public int raveHeuristic = 0;
    public boolean raveSkip = false;
    
    public Configuration(boolean binaryScoring, boolean uct, boolean rave, boolean weightedRave, int weight, boolean heuristicRave, int raveHeuristic, boolean raveSkip) {
    	this.binaryScoring = binaryScoring;
    	this.uct = uct;
    	this.rave = rave;
    	this.weightedRave = weightedRave;
    	this.weight = weight;
    	this.heuristicRave = heuristicRave;
    	this.raveHeuristic = raveHeuristic;
    	this.raveSkip = raveSkip;
    }
}
