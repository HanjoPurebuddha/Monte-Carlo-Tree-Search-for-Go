package ai;


public class Configuration {
	/* pattern matching
near the second last move. If no move was selected so far,
a global capture moves are attempted next. */
	public boolean simulatePatternsSecondLastMove;
	public boolean globalCaptureMove;
	

	/*By default, the player chooses the most-simulated move at
the root. A few other rules such as highest mean value are
also implemented. */
	public boolean pickMostSimulated;
	public boolean pickHighestMean;
	public boolean pickUCB;
	
	/*The win/loss evaluation is modified by
small bonuses that favor shorter playouts and terminal posi-
tions with a larger score.*/
	public boolean humanLikeMoves;
	
	/* to implement....#
	 * uct changes */
	public boolean ucbTuned = false;
	public boolean simpleUcb = false;
	public boolean randomUcb = false;
	public boolean ucb = false;
	public double firstPlayUrgency;
	
	/* initialization changes */
	boolean heuristicInitialization;
	
	/* simulation changes */
	public boolean simulateAvoidEyes;
	public boolean simulateAtari;
	public boolean simulatePatterns;
	public boolean simulateTakePieces;
	public boolean simulateMercyRule;
	
	public boolean clearMemory;
	
	/* bonus changes */
	int bonusAtari;
	public double bonusPatterns;
    public double bonusAvoidEyes;
	int bonusTakePiece;
	int bonusLocalNeighbourhood;
	
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
    public int pruneNodes;
    public int developPruning;
    
    /* varying */
    public double varySimEyes;
    public double varySimAtari;
    public double varySimPatterns;
    public double varySimPieces;
    
    /* scoring */
    public boolean averageScoring;
    public boolean livingScoring;
    public boolean captureScoring;
    public int evenScoring;
    
    public Configuration(boolean binaryScoring, boolean uct, boolean rave, boolean weightedRave, double initialWeight, 
    		double finalWeight, int raveSkip, double firstPlayUrgency, double bonusPatterns, double bonusAvoidEyes,
    		boolean simulateAvoidEyes, boolean simulateAtari, boolean simulatePatterns, boolean simulateTakePieces, boolean simulateMercyRule,
    		double varySimEyes, double varySimAtari, double varySimPatterns, double varySimPieces,
    		boolean pickMostSimulated, boolean pickHighestMean, boolean pickUCB, boolean clearMemory,
    		int pruneNodes, int developPruning,
    		boolean ucb, boolean simpleUcb, boolean randomUcb, boolean ucbTuned,
    		boolean captureScoring, boolean livingScoring, boolean averageScoring, int evenScoring) {
    	this.binaryScoring = binaryScoring;
    	this.uct = uct;
    	this.bonusAvoidEyes = bonusAvoidEyes;
    	this.rave = rave;
    	this.weightedRave = weightedRave;
    	this.initialWeight = initialWeight;
    	this.finalWeight = finalWeight;
    	this.raveSkip = raveSkip;
    	this.simulateAvoidEyes = simulateAvoidEyes;
    	this.simulateAtari = simulateAtari;
    	this.simulatePatterns = simulatePatterns;
    	this.simulateTakePieces = simulateTakePieces;
    	this.firstPlayUrgency = firstPlayUrgency;
    	this.simulateMercyRule = simulateMercyRule;
    	this.pickMostSimulated = pickMostSimulated;
    	this.pickHighestMean = pickHighestMean;
    	this.pickUCB = pickUCB;
    	this.bonusPatterns = bonusPatterns;
    	this.clearMemory = clearMemory;
    	this.pruneNodes = pruneNodes;
    	this.simpleUcb = simpleUcb;
    	this.randomUcb = randomUcb;
    	this.developPruning = developPruning;
    	this.ucb = ucb;
    	this.ucbTuned = ucbTuned;
    	this.varySimEyes = varySimEyes;
    	this.varySimAtari = varySimAtari;
    	this.varySimPatterns = varySimPatterns;
    	this.varySimPieces = varySimPieces;
    	this.captureScoring = captureScoring;
    	this.livingScoring = livingScoring;
    	this.averageScoring = averageScoring;
    	this.evenScoring = evenScoring;
    }
    
    private int localPruneCheck;
    public boolean checkPruning() {
    	localPruneCheck++;
    	if(localPruneCheck == developPruning)
    		return true;
    	return false;
    }
    
    public double firstPlayUrgencyValue() {
    	if(firstPlayUrgency > 0)
    		return firstPlayUrgency;
    	return 0;
    }
}
