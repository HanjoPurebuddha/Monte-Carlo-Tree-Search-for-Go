package ai;


public class Configuration {
	/* pattern matching
near the second last move. If no move was selected so far,
a global capture moves are attempted next. */
	public boolean simulatePatternsSecondLastMove;
	public boolean globalCaptureMove;
	
	/*he policy for 2-liberty blocks is applied both to the
last opponent move and to adjacent blocks of the player.
It generates moves on good liberties, which are points that
would gain liberties for that block and are not self-atari.
However, moves within simple chains such as bamboo and
diagonal connections are skipped.*/
	public boolean twoLibertyBlock;
	
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
	public double firstPlayUrgency;
	
	/* initialization changes */
	boolean openingBook;
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
    		boolean simulateAvoidEyes, boolean simulateAtari, boolean simulatePatterns, boolean simulateTakePieces, boolean simulateMercyRule,
    		boolean pickMostSimulated, boolean pickHighestMean, boolean pickUCB) {
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
    	this.pickMostSimulated = pickMostSimulated;
    	this.pickHighestMean = pickHighestMean;
    	this.pickUCB = pickUCB;
    }
    
    public double firstPlayUrgencyValue() {
    	if(firstPlayUrgency > 0)
    		return firstPlayUrgency;
    	return -Double.MAX_VALUE;
    }
}
