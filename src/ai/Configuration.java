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
    public boolean amaf;
    public boolean rave;
    public double initialWeight;
    public double raveWeight;
    public boolean heuristicamaf;
    public int amafHeuristic;
    public int amafSkip;
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
    public int explorationWeight;
    public boolean selectRandom;
    public double bonusFpu;
    public double aAmafWeight;
    public Configuration(boolean binaryScoring, boolean uct, boolean amaf, boolean rave, double initialWeight, double aAmafWeight, 
    		double raveWeight, int amafSkip, double bonusFpu, double firstPlayUrgency, double bonusPatterns, double bonusAvoidEyes, int explorationWeight,
    		boolean simulateAvoidEyes, boolean simulateAtari, boolean simulatePatterns, boolean simulateTakePieces, boolean simulateMercyRule,
    		double varySimEyes, double varySimAtari, double varySimPatterns, double varySimPieces,
    		boolean pickMostSimulated, boolean pickHighestMean, boolean pickUCB, boolean clearMemory,
    		int pruneNodes, int developPruning,
    		boolean ucb, boolean simpleUcb, boolean randomUcb, boolean ucbTuned,
    		boolean captureScoring, boolean livingScoring, boolean averageScoring, int evenScoring, boolean selectRandom) {
    	this.binaryScoring = binaryScoring;
    	this.uct = uct;
    	this.bonusAvoidEyes = bonusAvoidEyes;
    	this.amaf = amaf;
    	this.rave = rave;
    	this.aAmafWeight = aAmafWeight;
    	this.initialWeight = initialWeight;
    	this.raveWeight = raveWeight;
    	this.amafSkip = amafSkip;
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
    	this.explorationWeight = explorationWeight;
    	this.selectRandom = selectRandom;
    	this.bonusFpu = bonusFpu;
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
