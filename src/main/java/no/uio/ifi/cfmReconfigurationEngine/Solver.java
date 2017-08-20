/**
 * @author Magnus
 *
 */
package no.uio.ifi.cfmReconfigurationEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Solver{
	
	CFMwrapper FM;
	
	private int hillClimbAllowedPlateauIterations = 0;
	
	private int hillClimbTotalIterations = 0;
	private int[] hillClimbBestVector = null;
	private int hillClimbBestScore = Integer.MAX_VALUE;
	private long hillClimbsolvingTime;
	
	private int simAnnealTotalIterations = 0;
	private int[] simAnnealBestVector = null;
	private int simAnnealBestScore = Integer.MAX_VALUE;
	private long SimAnnealSolvingTime;
	
	private int geneticAlgTotalIterations = 0;
	private int[] geneticAlgBestVector = null;
	private int geneticAlgBestScore = Integer.MAX_VALUE;
	private long geneticAlgSolvingTime;
	
	private String hyvarrecResult = "";
	private boolean voidModel = false;
	
	Solver(CFMwrapper FM){
		this.FM = FM;
		nullifyLogValues();
	}
	
	public void setAllowedPlateauIterations(int allowedIterations){
		hillClimbAllowedPlateauIterations = allowedIterations;
	}
	
	private void nullifyLogValues(){
		hillClimbTotalIterations = 0;
		hillClimbBestVector = null;
		hillClimbBestScore = Integer.MAX_VALUE;
	}
	
	public int hillClimbing(int[] candidateVector){

		int plateauCounter = 0;
		ArrayList<Candidate> plateauVectors;
		
		Candidate candidate = new Candidate(FM, candidateVector);
		Candidate candS = candidate;
		int bestScore = candidate.score();
		
		Candidate trivial = new Candidate(FM, FM.generateTrivialCandidate());
		if(trivial.score() == 0) {
			System.out.println("Solution: trivial");
			candS = trivial;
			bestScore = trivial.score();
		}
		
		while (bestScore > 0 && plateauCounter <= hillClimbAllowedPlateauIterations){
			hillClimbTotalIterations++;
			plateauVectors = new ArrayList<Candidate>();
			ArrayList<Integer> shuffledNeighborhoodIndexes = FM.getShuffledNeighborhoodIndexes();
			for (int i = 0; i < shuffledNeighborhoodIndexes.size(); i++){
				
				Candidate candX = candidate.getNeighbor(shuffledNeighborhoodIndexes.get(i));
				if(candX != null){
					int scoreX = candX.score();
					
					if (scoreX < bestScore){
						candS = candX;
						bestScore = scoreX;
						plateauVectors = new ArrayList<Candidate>();
						break;
					}else if (candX.score() == bestScore){
						plateauVectors.add(candX);
					}
				}
			}
			if (bestScore == candidate.score()){
				if (!plateauVectors.isEmpty()){
					int randomIndex = ThreadLocalRandom.current().nextInt(0, plateauVectors.size());
					candidate = plateauVectors.get(randomIndex);
					plateauCounter++;
				}else{
					plateauCounter = hillClimbAllowedPlateauIterations+1;
				}
			}else{
				candidate = candS;
				plateauCounter = 0; 
			}
			
		}
		
		if (bestScore < hillClimbBestScore){
			hillClimbBestVector = candS.getCandidateVector();
			hillClimbBestScore = bestScore;
		}
		return bestScore;
	}
	
	public int simulatedAnnealing(int[] candidateVector, int maxIterations, double initialTemperature){
		double t = initialTemperature;
		int kMax = maxIterations; // max number of iterations
		double r = 1-(t/maxIterations);
		double k = 1.0;
				
		Candidate candidate = new Candidate(FM, candidateVector);
		Candidate best = candidate;
		
		Candidate trivial = new Candidate(FM, FM.generateTrivialCandidate());
		if(trivial.score() == 0) {
			best = trivial;
		}
		
		while(best.score() > 0 && k < kMax){
			Candidate neighbor = null;
			Candidate locallyBest = null;
			boolean neighborAccepted = false;
			ArrayList<Integer> shuffledNeighborhoodIndexes = FM.getShuffledNeighborhoodIndexes();
			for (int i = 0; i < shuffledNeighborhoodIndexes.size(); i++){
				neighbor = candidate.getNeighbor(shuffledNeighborhoodIndexes.get(i));
				if(neighbor != null){
					double acceptanceProb = acceptanceProbability(candidate.score(), neighbor.score(), t);
					double threshold = ThreadLocalRandom.current().nextDouble();
					if (acceptanceProb > threshold){
						candidate = neighbor;
						neighborAccepted = true;
						if (neighbor.score() < best.score()) {
							best = neighbor;
						}
						break;
					}else if(locallyBest == null || neighbor.score() < locallyBest.score()){
						locallyBest = neighbor;
					}
				}
			}
			if(!neighborAccepted) candidate = locallyBest;
			k++;
			t *= r;
		}
		simAnnealTotalIterations += k;
		if(best.score() < simAnnealBestScore){
			simAnnealBestVector = best.getCandidateVector();
			simAnnealBestScore = best.score();
		}
		
		return best.score();
	}
	
	private double acceptanceProbability(int currentScore, int neighbScore, double temp){
		if (neighbScore < currentScore) return 1.0;
		return Math.exp((currentScore - neighbScore)/temp);
	}
	
	
	public int geneticAlgorithm(int initPopSize, int crossOverBreakPoints, double mutationProbability, double randomSelectionRate){
		int popSize = initPopSize;
		if (initPopSize % 2 != 0) popSize--;
		
		Candidate best = new Candidate(FM, FM.generateCandidate());
		
		Candidate trivial = new Candidate(FM, FM.generateTrivialCandidate());
		if(trivial.score() == 0) {
			System.out.println("Solution: trivial");
			best = trivial;
		}
		
		ArrayList<Candidate> generation = FM.generateCandidates(popSize-1);
		generation.add(trivial);
		
		double generalFitness = Double.MAX_VALUE;
		double bestGeneralFitness = generalFitness;
		
		int termCount = 0;
		
		while(best.score() > 0 && popSize > 1 && termCount < 4){
			geneticAlgTotalIterations++;
			boolean globalOptFound = false;
			for(Candidate p : generation){
				if(p.score() < best.score()){
					best = p;
					if (best.score() == 0) {
						globalOptFound = true;
						break;		// No need to go through the rest of the iteration
					}
				}
			}
			if(!globalOptFound){
				generation = naturalSelection(generation, popSize, randomSelectionRate);
				double newGeneralFitness = calculateGeneralFitness(generation);
				if(newGeneralFitness >= bestGeneralFitness) {
					termCount++;
				}
				else bestGeneralFitness = newGeneralFitness;
				generalFitness = newGeneralFitness;
				
				ArrayList<Candidate> newGeneration = new ArrayList<Candidate>();
				for(int i = 0; i < popSize; i++){
					Candidate parent1 = tournamentSelectionWithoutReplacement(generation, 6);
					Candidate parent2 = tournamentSelectionWithoutReplacement(generation, 6);
					ArrayList<Candidate> children = crossover(parent1, parent2, crossOverBreakPoints, mutationProbability);
					newGeneration.addAll(children);
				}
				/* Elitism */
				int elites = 5;
				for(int i = 0; i < elites; i++){
					Candidate elite = generation.get(i);
					newGeneration.add(elite);
				}				
				generation = newGeneration;
			}
			
		}
		
		if(best.score() < geneticAlgBestScore){
			geneticAlgBestScore = best.score();
			geneticAlgBestVector = best.getCandidateVector();
		}
		//if(best.score() == 0) printEvolution(best, "0");
		return best.score();
	}
	
	
	@SuppressWarnings("unused")
	private void tournamentReplacement(ArrayList<Candidate> generation, Candidate newCand, int tournamentSize) {
		int worstScore = -1;
		Candidate worst = null;
		int worstIndex = 0;
		for(int i = 0; i < tournamentSize; i++){
			int compIndex = ThreadLocalRandom.current().nextInt(0, generation.size());
			Candidate competitor = generation.get(compIndex);
			if(competitor.score() > worstScore){
				worstScore = competitor.score();
				worst = competitor;
				worstIndex = compIndex;
			}
		}
		if(worst != null){
			generation.remove(worstIndex);
			generation.add(worstIndex, newCand);
		}else{
			System.err.println("Tournament replacement failed");
		}
		
	}

	private Candidate tournamentSelectionWithoutReplacement(ArrayList<Candidate> generation, int tournamentSize){
		int bestScore = Integer.MAX_VALUE;
		Candidate best = null;
		for(int i = 0; i < tournamentSize; i++){
			int compIndex = ThreadLocalRandom.current().nextInt(0, generation.size());
			Candidate competitor = generation.get(compIndex);
			if(competitor.score() < bestScore){
				bestScore = competitor.score();
				best = competitor;
			}
		}
		return best;
	}
	
	@SuppressWarnings("unused")
	private Candidate tournamentSelectionWithReplacement(ArrayList<Candidate> generation, int tournamentSize){
		int bestScore = Integer.MAX_VALUE;
		Candidate best = null;
		int bestIndex = 0;
		for(int i = 0; i < tournamentSize; i++){
			int compIndex = ThreadLocalRandom.current().nextInt(0, generation.size());
			Candidate competitor = generation.get(compIndex);
			if(competitor.score() < bestScore){
				bestScore = competitor.score();
				best = competitor;
				bestIndex = compIndex;
			}
		}
		if (best != null){
			generation.remove(bestIndex);
			generation.add(new Candidate(FM, FM.generateCandidate()));
		}else{
			System.err.println("Tournament with replacement failed");
		}
		return best;
	}
	
	private ArrayList<Candidate> crossover(Candidate parent1, Candidate parent2, int noBreakPoints, double mutationProbability){
		int vectLength = parent1.getCandidateVector().length;
		if(vectLength != parent2.getCandidateVector().length) {
			System.err.println("Genetic alg: Two parents cannot have different vector length: "+vectLength+" != "+parent2.getCandidateVector().length);
		}else if(noBreakPoints >= vectLength/2){
			System.err.println("Warning! There are too many breakpoints set in genetic algorithm");
		}
		int[] breakPoints = generateBreakPoints(noBreakPoints, vectLength);
		int k = 0;
		
		int[] childVector1 = new int[vectLength];
		int[] childVector2 = new int[vectLength];
		int[] parentToC1 = parent1.getCandidateVector();
		int[] parentToC2 = parent2.getCandidateVector();
		int c1Mutations = 0;
		int c2Mutations = 0;
		
		for(int i = 0; i < vectLength; i++){
			if(k < breakPoints.length && breakPoints[k] == i){
				int[] parentToCBuffer = parentToC1;
				parentToC1 = parentToC2;
				parentToC2 = parentToCBuffer;
				k++;
			}
			double mutation = ThreadLocalRandom.current().nextDouble();
			if(mutationProbability > mutation){
				childVector1[i] = FM.tweak(i, parentToC1[i]);
				c1Mutations++;
				//System.out.println("Mutation ("+mutationProbability+" > "+mutation+"): "+parentToC1[i]+" --> "+childVector1[i]);
			}else{
				childVector1[i] = parentToC1[i];
			}
			mutation = ThreadLocalRandom.current().nextDouble();
			if(mutationProbability > mutation){
				childVector2[i] = FM.tweak(i, parentToC2[i]);
				c2Mutations++;
				//System.out.println("Mutation ("+mutationProbability+" > "+mutation+"): "+parentToC2[i]+" --> "+childVector2[i]);
			}else{
				childVector2[i] = parentToC2[i];
			}
		}
		
		Candidate c1 = new Candidate(FM, childVector1);
		c1.parentsGA = new Candidate[]{parent1, parent2};
		c1.breakPoints = breakPoints;
		c1.mutations = c1Mutations;
		Candidate c2 = new Candidate(FM, childVector2);
		c2.parentsGA = new Candidate[]{parent1, parent2};
		c2.breakPoints = breakPoints;
		c2.mutations = c2Mutations;
		ArrayList<Candidate> result = new ArrayList<Candidate>();
		result.add(c1);
		result.add(c2);
		return result;
	}
	
	private double calculateGeneralFitness(ArrayList<Candidate> generation){
		double sumScore = 0.0;
		for(Candidate c : generation){
			sumScore += c.score();
		}
		return sumScore/generation.size();
	}
	
	private int[] generateBreakPoints(int noBreakPoints, int vectLength){
		int[] breakPoints = new int[noBreakPoints];
		for (int i = 0; i < noBreakPoints; i++){
			int newBP = ThreadLocalRandom.current().nextInt(1, vectLength);
			for(int j = 0; j < i; j++){
				if(newBP == breakPoints[j]){
					newBP = ThreadLocalRandom.current().nextInt(1, vectLength);
					j = 0;
				}
			}
			breakPoints[i] = newBP;
		}
		Arrays.sort(breakPoints);
		return breakPoints;
	}
	
	
	private ArrayList<Candidate> naturalSelection(ArrayList<Candidate> generation, int popSize, double selectionRate){
		generation.sort((c1, c2) -> c1.score() - c2.score());
		ArrayList<Candidate> newSelection = new ArrayList<Candidate>();
		
		int i = 0;
		while(i < popSize){
			Candidate c = generation.get(i);
			newSelection.add(c);
			i++;
		}
		if(generation.size() > i){
			int introductions = (int) (popSize*selectionRate);
			
			
			// Adding genes from previous generation
//			for(int j = 0; j < introductions; j++){
//				int toBeReplaced = ThreadLocalRandom.current().nextInt(5, i);
//				int toReplace = ThreadLocalRandom.current().nextInt(i, generation.size());
//				newSelection.remove(toBeReplaced);
//				newSelection.add(toBeReplaced, generation.get(toReplace));
//			}
			
			// Introducing foreign genes
			for(int j = 0; j < introductions; j++){
				int toBeReplaced = ThreadLocalRandom.current().nextInt(5, i);
				newSelection.remove(toBeReplaced);
				newSelection.add(toBeReplaced, new Candidate(FM, FM.generateCandidate()));
			}
		}
		
		//Collections.shuffle(newSelection);
		return newSelection;
	}
	
	public void printEvolution(Candidate s, String path){
		
		System.out.println("p: "+path);
		System.out.print(s.getConfigAsString()+"\t"+s.score()+"\t"+s.mutations+"\t");
		if(s.breakPoints != null){
			for(int i = 0; i < s.breakPoints.length; i++){
				System.out.print(s.breakPoints[i]+",");
			}
		}
		System.out.println();
		if(s.parentsGA != null){
			printEvolution(s.parentsGA[0], path+"-L");
			printEvolution(s.parentsGA[1], path+"-R");
		}
	}
	
	public int evaluate(int[] candidate){
		return FM.score(candidate);
	}
	
	public int getHillClimbIterations(){
		return hillClimbTotalIterations;
	}
	
	public int[] getHillClimbResultVector(){
		return hillClimbBestVector;
	}
	
	public int getHillClimbResultScore(){
		return hillClimbBestScore;
	}

	public long getHillClimbSolvingTime() {
		return hillClimbsolvingTime;
	}

	public void setHillClimbSolvingTime(long hillClimbSolvingTime) {
		this.hillClimbsolvingTime = hillClimbSolvingTime;
	}
	
	public int getSimAnnealIterations(){
		return simAnnealTotalIterations;
	}
	
	public int[] getSimAnnealResultVector(){
		return simAnnealBestVector;
	}
	
	public int getSimAnnealResultScore(){
		return simAnnealBestScore;
	}

	public long getSimAnnealSolvingTime() {
		return SimAnnealSolvingTime;
	}

	public void setSimAnnealSolvingTime(long simAnnealSolvingTime) {
		SimAnnealSolvingTime = simAnnealSolvingTime;
	}
	
	public int getGeneticAlgIterations(){
		return geneticAlgTotalIterations;
	}
	
	public int[] getGeneticAlgResultVector(){
		return geneticAlgBestVector;
	}
	
	public int getGeneticAlgResultScore(){
		return geneticAlgBestScore;
	}

	public long getGeneticAlgSolvingTime() {
		return geneticAlgSolvingTime;
	}

	public void setGeneticAlgSolvingTime(long geneticAlgSolvingTime) {
		this.geneticAlgSolvingTime = geneticAlgSolvingTime;
	}

	public String getHyvarrecResult() {
		return hyvarrecResult;
	}

	public void setHyvarrecResult(String hyvarrecResult) {
		this.hyvarrecResult = hyvarrecResult;
	}

	public boolean isVoid() {
		return voidModel;
	}

	public void setVoid(boolean voidModel) {
		this.voidModel = voidModel;
	}
	
//
//	public int hillClimbingOrig(int[] candidate){
//		long timeBreakA = System.nanoTime();				//
//		//nullifyLogValues();
//		
//		int iterations = 0;
//		int plateauCounter = 0;
//		ArrayList<int[]> plateauVectors;
//		
//		int[] vectS = candidate;
//		int bestScore = FM.score(candidate);
//		long timeBreakB1a = System.nanoTime();				//
//		//System.out.format("Init time:\t%012d%n", (timeBreakB1a - timeBreakA));		//
//		long timeBreakB1b = System.nanoTime();				//
//		while (bestScore > 0 && plateauCounter <= hillClimbAllowedPlateauIterations){
//			iterations++;
//			long timeBreakC1 = System.nanoTime();				//
//			plateauVectors = new ArrayList<int[]>();
//			long timeBreakE1 = System.nanoTime();				//
//			int[][] neighborhood = FM.N(candidate);
//			long timeBreakE2 = System.nanoTime();				//
//			//System.out.format("Generate N:\t%012d%n", (timeBreakE2 - timeBreakE1));		//
//			
//			for (int i = 0; i < neighborhood.length; i++){
//				long timeBreakD1 = System.nanoTime();				//
//				int[] vectX = neighborhood[i];
//				int scoreX = FM.score(vectX);
//				long timeBreakD2 = System.nanoTime();				//
//				if (scoreX < bestScore){
//					vectS = vectX;
//					bestScore = scoreX;
//					plateauVectors = new ArrayList<int[]>();
//					
//				}else if (scoreX == bestScore){
//					plateauVectors.add(vectX);
//				}
//				//System.out.format("Neighb %d score: \t%012d%n", i, (timeBreakD2 - timeBreakD1));
//			}
//			if (bestScore == FM.score(candidate)){
//				if (!plateauVectors.isEmpty()){
//					int randomIndex = ThreadLocalRandom.current().nextInt(0, plateauVectors.size());
//					candidate = plateauVectors.get(randomIndex);
//					plateauCounter++;
//				}else{
//					plateauCounter = hillClimbAllowedPlateauIterations;
//				}
//				//System.out.println(bestScore+" == "+FM.score(candidate)+" plat: "+plateauCounter);
//			}else{
//				//System.out.println(bestScore+" < "+FM.score(candidate));
//				candidate = vectS;
//				plateauCounter = 0; 
//			}
//			long timeBreakC2 = System.nanoTime();				//
//			//System.out.format("Iter %d: \t%012d%n", iterations, (timeBreakC2 - timeBreakC1));
//			
//		}
//		long timeBreakB2 = System.nanoTime();				//
//		//System.out.println("Init time:\t"+(timeBreakB1 - timeBreakA));
//		//System.out.format("Main loop:\t%012d%n", (timeBreakB2 - timeBreakB1b));
//		//System.out.println("Main loop:\t"+(timeBreakB2 - timeBreakB1));
//		
//		hillClimbTotalIterations += iterations;
//		if (bestScore < hillClimbBestScore){
//			hillClimbBestVector = vectS;
//			hillClimbBestScore = bestScore;
//		}
//		
///*		System.out.println("Iterations "+iterations+", score "+bestScore+".");		//
//		if (bestScore > 0) {
//			System.out.println("Global optimal not reached, score: "+bestScore);	//
//		}else {
//			System.out.println("Global optimal was reached! "+bestScore);			//
//		}*/
//		return bestScore;
//	}
//
//	// Differs from Orig in that it breaks the loop at once if it finds a cand with better score than current best
//	public int hillClimbingV2(int[] candidate){
//
//		long timeBreakA = System.nanoTime();				//
//		//nullifyLogValues();
//		
//		int iterations = 0;
//		int plateauCounter = 0;
//		ArrayList<int[]> plateauVectors;
//		
//		int[] vectS = candidate;
//		int bestScore = FM.score(candidate);
//		long timeBreakB1a = System.nanoTime();				//
//		//System.out.format("Init time:\t%012d%n", (timeBreakB1a - timeBreakA));		//
//		long timeBreakB1b = System.nanoTime();				//
//		while (bestScore > 0 && plateauCounter <= hillClimbAllowedPlateauIterations){
//			iterations++;
//			long timeBreakC1 = System.nanoTime();				//
//			plateauVectors = new ArrayList<int[]>();
//			long timeBreakE1 = System.nanoTime();				//
//			int[][] neighborhood = FM.N(candidate);
//			long timeBreakE2 = System.nanoTime();				//
//			//System.out.format("Generate N:\t%012d%n", (timeBreakE2 - timeBreakE1));		//
//			
//			for (int i = 0; i < neighborhood.length; i++){
//				long timeBreakD1 = System.nanoTime();				//
//				int[] vectX = neighborhood[i];
//				int scoreX = FM.score(vectX);
//				long timeBreakD2 = System.nanoTime();				//
//				if (scoreX < bestScore){
//					vectS = vectX;
//					bestScore = scoreX;
//					plateauVectors = new ArrayList<int[]>();
//					break;
//				}else if (scoreX == bestScore){
//					plateauVectors.add(vectX);
//				}
//				//System.out.format("Neighb %d score: \t%012d%n", i, (timeBreakD2 - timeBreakD1));
//			}
//			if (bestScore == FM.score(candidate)){
//				if (!plateauVectors.isEmpty()){
//					int randomIndex = ThreadLocalRandom.current().nextInt(0, plateauVectors.size());
//					candidate = plateauVectors.get(randomIndex);
//					plateauCounter++;
//				}else{
//					plateauCounter = hillClimbAllowedPlateauIterations;
//				}
//				//System.out.println(bestScore+" == "+FM.score(candidate)+" plat: "+plateauCounter);
//			}else{
//				//System.out.println(bestScore+" < "+FM.score(candidate));
//				candidate = vectS;
//				plateauCounter = 0; 
//			}
//			long timeBreakC2 = System.nanoTime();				//
//			//System.out.format("Iter %d: \t%012d%n", iterations, (timeBreakC2 - timeBreakC1));
//			
//		}
//		long timeBreakB2 = System.nanoTime();				//
//		//System.out.println("Init time:\t"+(timeBreakB1 - timeBreakA));
//		System.out.format("Main loop:\t%012d%n", (timeBreakB2 - timeBreakB1b));
//		//System.out.println("Main loop:\t"+(timeBreakB2 - timeBreakB1));
//		
//		hillClimbTotalIterations += iterations;
//		if (bestScore < hillClimbBestScore){
//			hillClimbBestVector = vectS;
//			hillClimbBestScore = bestScore;
//		}
//		
///*		System.out.println("Iterations "+iterations+", score "+bestScore+".");		//
//		if (bestScore > 0) {
//			System.out.println("Global optimal not reached, score: "+bestScore);	//
//		}else {
//			System.out.println("Global optimal was reached! "+bestScore);			//
//		}*/
//		return bestScore;
//	}
//
//	// Differs from V2 in that it goes through a shuffled set of the neighborhood (and only generates neighbor when it is needed)
//	public int hillClimbingV3(int[] candidate){
//		long timeBreakA = System.nanoTime();				//
//		//nullifyLogValues();
//		
//		int iterations = 0;
//		int plateauCounter = 0;
//		ArrayList<int[]> plateauVectors;
//		
//		int[] vectS = candidate;
//		int bestScore = FM.score(candidate);
//		
//		long timeBreakB1a = System.nanoTime();				//
//		
//		System.out.format("Init time:\t%012d%n", (timeBreakB1a - timeBreakA));		//
//		long timeBreakB1b = System.nanoTime();				//
//		while (bestScore > 0 && plateauCounter <= hillClimbAllowedPlateauIterations){
//			iterations++;
//			long timeBreakC1 = System.nanoTime();				//
//			plateauVectors = new ArrayList<int[]>();
//			long timeBreakE1 = System.nanoTime();				//
//			//int[][] neighborhood = FM.N(candidate);
//			ArrayList<Integer> shuffledNeighborhoodIndexes = FM.getShuffledNeighborhoodIndexes();
//			long timeBreakE2 = System.nanoTime();				//
//			//System.out.format("Generate N:\t%012d%n", (timeBreakE2 - timeBreakE1));		//
//			
//			for (int i = 0; i < shuffledNeighborhoodIndexes.size(); i++){
//				long timeBreakD1 = System.nanoTime();				//
//				int[] vectX = FM.getNeighbor(candidate, shuffledNeighborhoodIndexes.get(i));
//				if(vectX != null){
//					int scoreX = FM.score(vectX);
//					long timeBreakD2 = System.nanoTime();				//
//					if (scoreX < bestScore){
//						vectS = vectX;
//						bestScore = scoreX;
//						plateauVectors = new ArrayList<int[]>();
//						break;
//					}else if (scoreX == bestScore){
//						plateauVectors.add(vectX);
//					}
//					//System.out.format("Neighb %d score: \t%012d%n", shuffledNeighborhoodIndexes.get(i), (timeBreakD2 - timeBreakD1));
//				}
//			}
//			if (bestScore == FM.score(candidate)){
//				if (!plateauVectors.isEmpty()){
//					int randomIndex = ThreadLocalRandom.current().nextInt(0, plateauVectors.size());
//					candidate = plateauVectors.get(randomIndex);
//					plateauCounter++;
//				}else{
//					plateauCounter = hillClimbAllowedPlateauIterations;
//				}
//				//System.out.println(bestScore+" == "+FM.score(candidate)+" plat: "+plateauCounter);
//			}else{
//				//System.out.println(bestScore+" < "+FM.score(candidate));
//				candidate = vectS;
//				plateauCounter = 0; 
//			}
//			long timeBreakC2 = System.nanoTime();				//
//			//System.out.format("Iter %d: \t%012d%n", iterations, (timeBreakC2 - timeBreakC1));
//			
//		}
//		long timeBreakB2 = System.nanoTime();				//
//		//System.out.println("Init time:\t"+(timeBreakB1 - timeBreakA));
//		System.out.format("Main loop:\t%012d%n", (timeBreakB2 - timeBreakB1b));
//		//System.out.println("Main loop:\t"+(timeBreakB2 - timeBreakB1));
//		
//		hillClimbTotalIterations += iterations;
//		if (bestScore < hillClimbBestScore){
//			hillClimbBestVector = vectS;
//			hillClimbBestScore = bestScore;
//		}
//		
///*		System.out.println("Iterations "+iterations+", score "+bestScore+".");		//
//		if (bestScore > 0) {
//			System.out.println("Global optimal not reached, score: "+bestScore);	//
//		}else {
//			System.out.println("Global optimal was reached! "+bestScore);			//
//		}*/
//		return bestScore;
//	}
//
//	
//
	
//	
//	public int geneticAlgorithmOLD(int initPopSize, int crossOverBreakPoints, double mutationProbability){
//		int popSize = initPopSize;
//		if (initPopSize % 2 != 0) popSize--;
//		
//		ArrayList<Candidate> generation = FM.generateCandidates(popSize*4);
//		Candidate best = generation.get(ThreadLocalRandom.current().nextInt(generation.size()));
//		
//		double generalFitness = Double.MAX_VALUE;
//		double bestGeneralFitness = generalFitness;
//		
//		int termCount = 0;
//		
//		while(best.score() > 0 && popSize > 1 && termCount < 40){
//			geneticAlgTotalIterations++;
//			//System.out.println(geneticAlgTotalIterations+" GenerationSize: "+generation.size());
//			
//			boolean globalOptFound = false;
//			//System.out.println(geneticAlgTotalIterations+" size: "+generation.size());
//			for(Candidate p : generation){
//				//System.out.print(p.score()+",");
//				if(p.score() < best.score()){
//					best = p;
//					if (best.score() == 0) {
//						globalOptFound = true;
//						break;		// No need to go through the rest of the iteration
//					}
//				}
//			}
//			//System.out.println();
//			if(!globalOptFound){
//				generation = naturalSelection(generation, popSize, 0.03);
//				//System.out.println(geneticAlgTotalIterations+" size: "+generation.size());
//				double newGeneralFitness = calculateGeneralFitness(generation);
//				if(newGeneralFitness >= bestGeneralFitness) {
//					termCount++;
//				}
//				else bestGeneralFitness = newGeneralFitness;
//				generalFitness = newGeneralFitness;
//				
//				
//				//System.out.println(geneticAlgTotalIterations+" General fitness: "+generalFitness+", Best: "+best.score());
//				
//				ArrayList<Candidate> newGeneration = new ArrayList<Candidate>();
//				for(int i = 0; i < popSize; i++){
//					Candidate parent1 = generation.get(i);
//					Candidate parent2 = generation.get(++i);
//					ArrayList<Candidate> children = crossover(parent1, parent2, crossOverBreakPoints, mutationProbability);
//					newGeneration.addAll(children);
//					newGeneration.add(parent1);			// This improves result but makes a more homogeneous group
//					newGeneration.add(parent2);			// This improves result but makes a more homogeneous group
//				}
//				generation = newGeneration;
//				//popSize -= 2;
//			}
//			
//		}
//		
//		//
///*		if (best.score() > 0){
//			System.out.println("Score: "+best.score()+", Iterations: "+geneticAlgTotalIterations);
//			for(Candidate p: generation){
//				int[] config = p.getCandidateVector();
//				System.out.println(FMReconfigurer.resultAsString(config, FM.getNumberOfFeatures()));
//			}
//		}*/
//		//
//		
//		if(best.score() < geneticAlgBestScore){
//			geneticAlgBestScore = best.score();
//			geneticAlgBestVector = best.getCandidateVector();
//		}
//		
//		return best.score();
//	}
//	
//	public int geneticAlgorithmV2(int initPopSize, int crossOverBreakPoints, double mutationProbability){
//		int popSize = initPopSize;
//		if (initPopSize % 2 != 0) popSize--;
//		
//		Candidate best = new Candidate(FM, FM.generateCandidate());
//		ArrayList<Candidate> generation = FM.generateCandidates(popSize);
//		
//		
//		double generalFitness = Double.MAX_VALUE;
//		double bestGeneralFitness = generalFitness;
//		
//		int termCount = 0;
//		
//		while(best.score() > 0 && popSize > 1 && termCount < 40){
//			geneticAlgTotalIterations++;
//			//System.out.println(geneticAlgTotalIterations+" GenerationSize: "+generation.size());
//			
//			boolean globalOptFound = false;
//			//System.out.println(geneticAlgTotalIterations+" size: "+generation.size());
//			for(Candidate p : generation){
//				//System.out.print(p.score()+",");
//				if(p.score() < best.score()){
//					best = p;
//					if (best.score() == 0) {
//						globalOptFound = true;
//						break;		// No need to go through the rest of the iteration
//					}
//				}
//			}
//			//System.out.println();
//			if(!globalOptFound){
//				generation = naturalSelection(generation, popSize, 0.03);
//				//System.out.println(geneticAlgTotalIterations+" size: "+generation.size());
//				double newGeneralFitness = calculateGeneralFitness(generation);
//				if(newGeneralFitness >= bestGeneralFitness) {
//					termCount++;
//				}
//				else bestGeneralFitness = newGeneralFitness;
//				generalFitness = newGeneralFitness;
//				
//				
//				//System.out.println(geneticAlgTotalIterations+" General fitness: "+generalFitness+", Best: "+best.score());
//				
//				ArrayList<Candidate> newGeneration = new ArrayList<Candidate>();
//				for(int i = 0; i < popSize; i++){
//					int pIndex1 = ThreadLocalRandom.current().nextInt(0, generation.size());
//					int pIndex2 = ThreadLocalRandom.current().nextInt(0, generation.size());
//					while (pIndex1 == pIndex2) pIndex2 = ThreadLocalRandom.current().nextInt(0, generation.size());
//					Candidate parent1 = generation.get(pIndex1);
//					Candidate parent2 = generation.get(pIndex2);
//					ArrayList<Candidate> children = crossover(parent1, parent2, crossOverBreakPoints, mutationProbability);
//					newGeneration.addAll(children);
//					//newGeneration.add(parent1);			// This improves result but makes a more homogeneous group
//					//newGeneration.add(parent2);			// This improves result but makes a more homogeneous group
//				}
//				/* Elitism */
//				//int elites = 10;
//				//int elites = popSize/10;
//				int elites = 5;
//				for(int i = 0; i < elites; i++){
//					Candidate elite = generation.get(i);
//					//System.out.println(elite.score());
///*					while(newGeneration.contains(elite)){
//						elites++;
//						elite = generation.get(++i);
//					}*/
//					newGeneration.add(elite);
//					//System.out.println("I:"+i);
//				}
//				
//				generation = newGeneration;
//				//popSize -= 2;
//			}
//			
//		}
//		
//		//
///*		if (best.score() > 0){
//			System.out.println("Score: "+best.score()+", Iterations: "+geneticAlgTotalIterations);
//			for(Candidate p: generation){
//				int[] config = p.getCandidateVector();
//				System.out.println(FMReconfigurer.resultAsString(config, FM.getNumberOfFeatures()));
//			}
//		}*/
//		//
//		
//		if(best.score() < geneticAlgBestScore){
//			geneticAlgBestScore = best.score();
//			geneticAlgBestVector = best.getCandidateVector();
//		}
//		
//		return best.score();
//	}
//
//	
	
//	/** Time consuming and not better than V3
//	 */
//	public int geneticAlgorithmV4(int popSize, int crossOverBreakPoints, double mutationProbability){
//		
//		double crossoverProb = 0.75;
//		ArrayList<Candidate> generation = FM.generateCandidates(popSize);
//		Candidate best = generation.get(ThreadLocalRandom.current().nextInt(generation.size()));
//		double generalFitness = Double.MAX_VALUE;
//		double bestGeneralFitness = generalFitness;
//		
//		int termCount = 0;
//		while(best.score() > 0 && popSize > 1 && termCount < 100){
//			geneticAlgTotalIterations++;
//			boolean globalOptFound = false;
//			for(Candidate p : generation){
//				if(p.score() < best.score()){
//					best = p;
//					if (best.score() == 0) {
//						globalOptFound = true;
//						break;		// No need to go through the rest of the iteration
//					}
//				}
//			}
//			if(!globalOptFound){
//				double newGeneralFitness = calculateGeneralFitness(generation);
//				if(newGeneralFitness >= bestGeneralFitness) {
//					termCount++;
//				}
//				else {
//					bestGeneralFitness = newGeneralFitness;
//					termCount = 0;
//				}
//				generalFitness = newGeneralFitness;
//				
//				for(int i = 0; i < popSize; i++){
//					Candidate parent1 = tournamentSelectionWithoutReplacement(generation, 6);
//					Candidate parent2 = tournamentSelectionWithoutReplacement(generation, 6);
//					ArrayList<Candidate> children = crossover(parent1, parent2, crossOverBreakPoints, mutationProbability);
//					for(Candidate c: children){
//						tournamentReplacement(generation, c, 6); //Instant natural selection
//					}
//					i++;
//				}
//			}
//			
//		}
//		if(best.score() < geneticAlgBestScore){
//			geneticAlgBestScore = best.score();
//			geneticAlgBestVector = best.getCandidateVector();
//		}
//		
//		return best.score();
//	}
//	
	
	
}