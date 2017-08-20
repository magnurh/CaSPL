package no.uio.ifi.cfmReconfigurationEngine;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CFMwrapper{
	
	private ContextDepFeatureModel fm;
	private String modelName;
	public ExpressionEvaluator eval;
	private int numberOfFeatures;
	private int numberOfAttributes;
	private int candidateLength;
	private int contextSize;
	private HashSet<Integer> alwaysSelected;
	private HashMap<String, Integer> savedScores = new HashMap<String, Integer>();
	
	ArrayList<int[]> neighborhoodChangeIndex = new ArrayList<int[]>();
	ArrayList<Integer> shuffledNeighborhoodIndexes = new ArrayList<Integer>();
	
	CFMwrapper(String dir, String modelName, int numberOfFeatures, int numberOfAttributes, int size, int contextSize){
		fm = importFM(dir+"/"+modelName, size);
		this.modelName = modelName;
		eval = new ExpressionEvaluator(fm);
		this.numberOfFeatures = numberOfFeatures;
		fm.setNumberOfFeatures(numberOfFeatures);
		this.numberOfAttributes = numberOfAttributes;
		fm.setNumberOfAttributes(numberOfAttributes);
		this.candidateLength = size;
		fm.setSize(size);
		this.contextSize = contextSize;
		fm.setContextSize(contextSize);
		alwaysSelected = fm.getSelectedFeatures();
	}
	
	// Returns number of constraints falsified by vector
	public int score(int[] vect){
		String vectStr = eval.getVectorAsString(vect);
		if (savedScores.containsKey(vectStr)){
			return savedScores.get(vectStr);
		}
		HashSet<String> unsatConstraints = new HashSet<String>();
		ArrayList<String> constraints = fm.getConstraints();
		int notSatConstraints = 0;
		for (String r : constraints){
			if(!eval.evaluate(r, vect)) {
				unsatConstraints.add(r);
				notSatConstraints += 1;
			}
		}
		savedScores.put(vectStr, notSatConstraints);
		return notSatConstraints;
	}
	
	public HashSet<String> scoreAndReturnUnsatConstraints(int[] vect){
		HashSet<String> unsatisfiedConstraints = new HashSet<String>();
		ArrayList<String> constraints = fm.getConstraints();
		for (String r : constraints){
			if(!eval.evaluate(r, vect)) unsatisfiedConstraints.add(r);
		}
		return unsatisfiedConstraints;
	}
	
	public void printAllConstraintsWithAssignments(int[] vect){
		for (int i = 0; i < vect.length; i++){
			System.out.println(i+": "+vect[i]);
		}
		ArrayList<String> constraints = fm.getConstraints();
		for (String r : constraints){
			eval.printConstraintWithAssignment(r, vect);
		}
	}
	
	public HashSet<String> dynamicScoring(int[] vect, HashSet<String> parentUnsatisfiedConstraints, int neighborhoodNumber){
		String vectStr = eval.getVectorAsString(vect);
		if (savedScores.containsKey(vectStr)){
			//TODO: add storing and retrieving of unsatisfied constraints
			//return savedScores.get(vectStr);
		}
		HashSet<String> currentUnsatisfiedConstraints = new HashSet<String>(parentUnsatisfiedConstraints);
		int changeIndex = getChangeIndex(neighborhoodNumber);
		ArrayList<String> constraintsToBeEvaluated = fm.getConstraintsContainingId(changeIndex);
		for (String constraint : constraintsToBeEvaluated){
			boolean evaluation = eval.evaluate(constraint, vect);
			if(evaluation){
				if(currentUnsatisfiedConstraints.contains(constraint)){
					currentUnsatisfiedConstraints.remove(constraint);
				}
			}else{
				currentUnsatisfiedConstraints.add(constraint);
			}
		}
		//System.out.println("Score: "+currentUnsatisfiedConstraints.size());
		return currentUnsatisfiedConstraints;
	}
	
	public int score(int[] vect, int maxScore){
		String vectStr = eval.getVectorAsString(vect);
		if (savedScores.containsKey(vectStr)){
			return savedScores.get(vectStr);
		}
		
		ArrayList<String> constraints = fm.getConstraints();
		int notSatConstraints = 0;
		for (String r : constraints){
			if(!eval.evaluate(r, vect)) notSatConstraints += 1;
			if(notSatConstraints > maxScore) return notSatConstraints;
		}
		savedScores.put(vectStr, notSatConstraints);
		return notSatConstraints;
	}
	
	public boolean evaluate(String exp, int[] vect){
		return eval.evaluate(exp, vect);
	}
	
	public int[] generateCandidate(){
		// TODO: move filling of neighborhoodChangeIndex to separate function
		//neighborhoodChangeIndex = new ArrayList<int[]>();		
		boolean changeIndexNotFilled = neighborhoodChangeIndex.size() == 0;
		int[] newCandidate = new int[candidateLength];
		
		for (int i = 0; i < candidateLength; i++){
			if (alwaysSelected.contains(i)){
				newCandidate[i] = 1;
			}else if (i < numberOfFeatures){
				newCandidate[i] = ThreadLocalRandom.current().nextInt(0, 2);
				if (changeIndexNotFilled) neighborhoodChangeIndex.add(new int[]{i, 0});
			}else{
				int[] attrRange = fm.getAttributeRange(i);
				int valIndex = ThreadLocalRandom.current().nextInt(0, attrRange.length);
				newCandidate[i] = attrRange[valIndex];
				if (changeIndexNotFilled) neighborhoodChangeIndex.add(new int[]{i, 1});
				if (changeIndexNotFilled) neighborhoodChangeIndex.add(new int[]{i, -1});
			}
		}
		return newCandidate;
	}
	
	public int[] generateTrivialCandidate() {
		int[] newCandidate = new int[candidateLength];
		Iterator<Integer> select = alwaysSelected.iterator();
		while(select.hasNext()){
			newCandidate[select.next()] = 1;
		}
		return newCandidate;
	}
	
	public ArrayList<Candidate> generateCandidates(int size){
		ArrayList<Candidate> candidates = new ArrayList<Candidate>();
		int i = 0;
		while(i < size){
			Candidate c = new Candidate(this, generateCandidate());
			candidates.add(c);
			i++;
		}
		return candidates;
	}
	
	// Returns neighborhood of input vector v 
	public int[][] N(int[] v){
		// The maximum size of a neighborhood will be the number of binary variables (features that can vary) 
		// plus two times nonBinary variables (attributes - they may change in either direction)
		int nSize = numberOfFeatures - alwaysSelected.size() + (numberOfAttributes*2);
		int cutOffNeighborhood = 0;
		int[][] neighborhood = new int[nSize][candidateLength];
		
		int j = 0;
		
		for (int i = 0; i < nSize; i++){
			while(alwaysSelected.contains(j)){
				j++;
			}
			if(j < numberOfFeatures){
				System.arraycopy(v, 0, neighborhood[i], 0, candidateLength);
				neighborhood[i][j] = negate(neighborhood[i][j]);
			}else if(j >= numberOfFeatures && j < candidateLength){
				boolean increaseSuccessfull = false;
				System.arraycopy(v, 0, neighborhood[i], 0, candidateLength);
			
				int attInc = inc(j, v[j]);
				if (attInc == -1){
					cutOffNeighborhood++;
				}else{
					neighborhood[i][j] = attInc;
					increaseSuccessfull = true;
				}
				
				int attDec = dec(j, v[j]);
				if (attDec == -1){
					cutOffNeighborhood++;
				}else{
					if (increaseSuccessfull){
						i++;
						System.arraycopy(v, 0, neighborhood[i], 0, candidateLength);
					}
					neighborhood[i][j] = attDec;
				}
			}
			j++;
							
		}
		
		int[][] finalNeighborhood;
		
		if (cutOffNeighborhood > 0){
			finalNeighborhood = new int[nSize-cutOffNeighborhood][candidateLength];
			for (int i = 0; i < finalNeighborhood.length; i++){
				finalNeighborhood[i] = neighborhood[i];																	
			}
		}else{
			finalNeighborhood = neighborhood;
		}
		return finalNeighborhood;
	}
	
	
	public int tweak(int id, int value){
		if (alwaysSelected.contains(id)) return 1;
		else if(id < numberOfFeatures) return negate(value);
		else if(id < numberOfFeatures + numberOfAttributes){
			boolean increase = ThreadLocalRandom.current().nextBoolean();
			if(increase){
				int res = inc(id, value);
				if (res != -1) return res;
				else return dec(id, value);
			}else{
				int res = dec(id, value);
				if (res != -1) return res;
				else return inc(id, value);
			}
		}else{
			System.err.println(id+" >= "+(numberOfFeatures + numberOfAttributes));
			return -1;
		}
	}
	
	private int inc(int attId, int v){
		Attribute a = fm.getAttribute(attId);
		int[] attRange = a.getRange();
		
		if (a.isRangeComplete()) {
			return incByInterval(attRange, v);
		}
		
		int max = attRange[attRange.length-1];
		if (v >= max) {
			return -1;
		}
		return ++v;
	}
	
	private int incByInterval(int[] interval, int v){
		for (int i = 0; i < interval.length-1; i++){
			if (v >= interval[i] && v < interval[i+1]){
				return interval[i+1];
			}
		}
		return -1;
	}
	
	private int dec(int attId, int v){
		Attribute a = fm.getAttribute(attId);
		int[] attRange = a.getRange();
		if (a.isRangeComplete()) {
			return decByInterval(attRange, v);
		}
		
		int min = attRange[0];
		if (v <= min) {
			return -1;
		}
		
		return --v;
	}
	
	private int decByInterval(int[] interval, int v){
		for (int i = interval.length-1; i > 0; i--){
			if (v <= interval[i] && v > interval[i-1]){
				return interval[i-1];
			}
		}
		return -1;
	}
	
	private int negate(int v){
		if (v == 0) return 1;
		else if (v == 1) return 0;
		else {
			System.err.println("Non-binary value "+v+" cannot be negated.");
			return -1;
		}
	}
	
	
	private ContextDepFeatureModel importFM(String dir, int size){
		ContextDepFeatureModel fm = null;
		
		JSONParser parser = new JSONParser();
		try {
			JSONObject fmJSON = (JSONObject) parser.parse(new FileReader(dir));
			fm = new ContextDepFeatureModel(fmJSON, size);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return fm;
		
	}
	
	public int getChangeIndex(int neighborIndex){
		int[] positionToChange = neighborhoodChangeIndex.get(neighborIndex);
		return positionToChange[0];
	}

	public int[] getNeighbor(int[] candidate, int neighborIndex) {
		int[] neighbor = new int[candidate.length];
		System.arraycopy(candidate, 0, neighbor, 0, candidate.length);
		int[] positionToChange = neighborhoodChangeIndex.get(neighborIndex);
		int pos = positionToChange[0];
				
		int direction = positionToChange[1];
		if(pos >= numberOfFeatures){
			if (direction > 0){
				neighbor[pos] = inc(pos, candidate[pos]);
			}else if (direction < 0){
				neighbor[pos] = dec(pos, candidate[pos]);
			}else{
				System.err.println("Attribute changeIndex with direction 0: "+pos+" >= Features: "+numberOfFeatures+" (neighbor "+neighborIndex+")");
			}
			if (neighbor[pos] == -1) return null;
		}else{
			neighbor[pos] = negate(candidate[pos]);
		}
		return neighbor;
	}

	public ArrayList<Integer> getShuffledNeighborhoodIndexes() {
		if (shuffledNeighborhoodIndexes.size() != neighborhoodChangeIndex.size()){
			shuffledNeighborhoodIndexes = new ArrayList<Integer>();
			for (int i = 0; i < neighborhoodChangeIndex.size(); i++){
				shuffledNeighborhoodIndexes.add(i);
			}
		}
		Collections.shuffle(shuffledNeighborhoodIndexes);
		return shuffledNeighborhoodIndexes;
	}
	
	public int getNumberOfAttributes(){
		return numberOfAttributes;
	}
	
	public int getNumberOfFeatures(){
		return numberOfFeatures;
	}
	
	public int getCandidateLength(){
		return candidateLength;
	}
	
	public int getContextSize(){
		return contextSize;
	}
	
	public String getModelName() {
		return modelName;
	}
	
}
