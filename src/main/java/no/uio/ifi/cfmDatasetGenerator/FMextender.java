/**
 * @author Magnus
 *
 */
package no.uio.ifi.cfmDatasetGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class FMextender implements VarModelExtender{

	private int choice = RELATION;
	
	private String originPath;
	private Context context;
	private int attrMinRange;
	private int attrMaxRange;
	private int maxNumberOfVFs;
	private HashMap<Integer, Attribute> featAttributes = new HashMap<Integer, Attribute>();
	private ArrayList<String> featContext = new ArrayList<String>();
	private HashMap<Integer, ArrayList<Integer>> mandRelations = new HashMap<Integer, ArrayList<Integer>>();
	private HashMap<Integer, ArrayList<Integer>> optRelations = new HashMap<Integer, ArrayList<Integer>>();
	private HashMap<Integer, ArrayList<int[]>> altRelations = new HashMap<Integer, ArrayList<int[]>>();
	private HashMap<Integer, ArrayList<int[]>> orRelations = new HashMap<Integer, ArrayList<int[]>>();
	private HashMap<String, String> attributes;
	private ArrayList<Integer> leafFeatures;
	
	private HashMap<Integer, String> crossTreeConstraints;
	private int ctcCounter;
	private HashMap<Integer, String> extendedConstraints;
	private int extcCounter;
	
	private int incrementingID;
	private int noAttributes = 0;
	
	private class Attribute{
		protected int attrID;
		protected int minRange;
		protected int maxRange;
		protected int[] contextAffiliation = new int[context.size()];
		
		Attribute(int attrID, int minRange, int maxRange){
			this.attrID = attrID;
			this.minRange = minRange;
			this.maxRange = maxRange;
		}
	}
	
	public FMextender(int highestID, int contextMaxSize, int contextMaxRange, int maxNumberOfVFs, int attrMinRange, int attrMaxRange){
		context = new Context(1, contextMaxSize, contextMaxRange);
		attributes = new HashMap<String, String>();;
		crossTreeConstraints = new HashMap<Integer, String>();;
		ctcCounter = 0;
		extendedConstraints = new HashMap<Integer, String>();;
		extcCounter = 0;
		
		this.maxNumberOfVFs = maxNumberOfVFs;
		this.attrMinRange = attrMinRange;
		this.attrMaxRange = attrMaxRange;
		this.incrementingID = highestID;
	}
	
	public int getNumberOfAttributes(){
		return noAttributes;
	}
	
	public int getContextSize(){
		return context.size();
	}
	
	public int getHighestID(){
		return incrementingID;
	}
	
	public String getOriginPath(){
		return originPath;
	}
	
	public JSONObject generateAFMwithContext(String FilePath){
		parse(FilePath);
		AFMWithContext afmc = new AFMWithContext();
		addProperties(afmc);
		JSONObject afmcJSON = afmc.retrieveModel();
		
		return afmcJSON;
	}
	
	private boolean addProperties(AFMWithContext afmc){
		addRelations(afmc);
		addContext(afmc);
		addCTCs(afmc);
		addVFs(afmc);
		return true;
	}
	
	private String getRandomEqualityOperator(){
		int pos = ThreadLocalRandom.current().nextInt(eqOperators.length);
		return eqOperators[pos];
	}
	
	private Integer getRandomLeafFeature(){
		int pos = ThreadLocalRandom.current().nextInt(leafFeatures.size());
		return leafFeatures.get(pos);
	}
	
	private int getRandomValueInRange(String eqOperator, int minRange, int maxRange){
		int minVal = minRange;
		int maxVal = maxRange;
		if (eqOperator.equals("<")) minVal +=1;
		else if(eqOperator.equals(">")) maxVal -= 1;
		if(maxVal < minVal) return -1;
		return ThreadLocalRandom.current().nextInt(minVal, maxVal+1);
	}
	
	private Attribute getAttributeForFeature(int featId){
		if(featAttributes.containsKey(featId)){
			return featAttributes.get(featId);
		}else{
			int ida = ++incrementingID;
			int attrMin = attrMinRange;
			int attrMax = ThreadLocalRandom.current().nextInt(attrMin, attrMaxRange+1);
			Attribute at = new Attribute(ida, attrMin, attrMax);
			featAttributes.put(featId, at);
			noAttributes++;
			return at;
		}
	}
	
	private int getNewRandomContextVarForFeature(int featID){
		int idc = ThreadLocalRandom.current().nextInt(context.size());
		String key = "<"+featID+","+idc+">";
		int i = 0;
		while(featContext.contains(key) && i < context.size()*4){
			idc = ThreadLocalRandom.current().nextInt(context.size());
			key = "<"+featID+","+idc+">";
			i++;
		}
		featContext.add(key);
		return idc;
	}
	
	private int getNewRandomContextVarForAttribute(Attribute at){
		int idc = ThreadLocalRandom.current().nextInt(context.size());
		int i = 0;
		while(at.contextAffiliation[idc] == 0 && i < context.size()*4){
			idc = ThreadLocalRandom.current().nextInt(context.size());
			i++;
		}
		at.contextAffiliation[idc] = 1;
		return idc;
	}
	
	private void addVFs(AFMWithContext afmc){
		// Assumptions:
		// - only on leaf features
		// - not more likely to involve features from alternative groups than any other
		// - equally likely to be involving an attribute as not
		// - VFs with attr has equal likeliness to have 1-5 parts
		
		int numberOfVFs;
		if (maxNumberOfVFs <= context.size()) numberOfVFs = maxNumberOfVFs;
		else numberOfVFs = ThreadLocalRandom.current().nextInt(context.size(), maxNumberOfVFs+1);
		
		for(int i = 0; i < numberOfVFs; i++){
			int homeFeature = getRandomLeafFeature();
			boolean withAttribute = ThreadLocalRandom.current().nextBoolean();
			
			if(withAttribute || isMandatoryFromRoot(homeFeature, afmc.constraints)){
				// Only = is used as eq operator for context values
				// Possibly several VFs added per context variable (max one per value, half of max value on average)
				// It is possible that no VF will be added in this procedure
				Attribute at = getAttributeForFeature(homeFeature);
				int ida = at.attrID;
				afmc.addAttribute(ida, at.minRange, at.maxRange, at.minRange, homeFeature);
				int idc = getNewRandomContextVarForAttribute(at);
				for (int j = context.getRangeMin(idc); j <= context.getRangeMax(idc); j++){
					boolean addForCurrentContextVal = ThreadLocalRandom.current().nextBoolean();
					if(addForCurrentContextVal){
						String attrEq = getRandomEqualityOperator();
						int attrVal = getRandomValueInRange(attrEq, at.minRange, at.maxRange);
						if(attrVal < 0){
							attrEq = "=";
							attrVal = getRandomValueInRange(attrEq, at.minRange, at.maxRange);
						}
						afmc.addVF(homeFeature, idc, "=", j, ida, attrEq, attrVal);
					}
				}
			}else{
				int contIndex = getNewRandomContextVarForFeature(homeFeature);
				String eqOp = getRandomEqualityOperator();
				int contVal = getRandomValueInRange(eqOp, context.getRangeMin(contIndex), context.getRangeMax(contIndex));
				if(contVal < 0){
					eqOp = "=";
					contVal = getRandomValueInRange(eqOp, context.getRangeMin(contIndex), context.getRangeMax(contIndex));
				}
				afmc.addVF(homeFeature, contIndex, eqOp, contVal);
			}
		}
	}
	
	private boolean isMandatoryFromRoot(int idf, JSONArray constraints){
		HashSet<Integer> checked = new HashSet<Integer>();
		return recursiveIsMandatoryFromRoot(idf, constraints, checked);
	}
	
	@SuppressWarnings("unchecked")
	private boolean recursiveIsMandatoryFromRoot(int idf, JSONArray constraints, HashSet<Integer> checked){
		Iterator<String> constrIter = constraints.iterator();
		if (idf == 0) return true;
		while(constrIter.hasNext()){
			String c = constrIter.next();
			if (c.matches("\\(?feature\\[_id\\d+\\] = [01] impl \\(?(feature\\[_id\\d+\\] = [01] and )*feature\\[_id"+idf+"\\] = [01]( and feature\\[_id\\d+\\] = [01])*\\){0,2}")){
				int idflhs = extractFirstIntFromString(c);
				checked.add(idf);
				if (!checked.contains(idflhs) && recursiveIsMandatoryFromRoot(idflhs, constraints, checked)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private int findMandatoryPredecessorOf(int f){
		int p = f;
		for(int key : mandRelations.keySet()){
			if(mandRelations.get(key).contains(p)){
				p = key;
				break;
			}
		}
		if(p == f) return f;
		else return findMandatoryPredecessorOf(p);
	}
	
	private boolean sharesAltGroup(int a, int b){;
		for(int key : altRelations.keySet()){
			ArrayList<int[]> altGroups = altRelations.get(key);
			for(int[] group: altGroups){
				boolean aFound = false;
				boolean bFound = false;
				for(int j = 0; j < group.length; j++){
					if(isPredecessorOf(group[j], a)) aFound = true;
					else if(isPredecessorOf(group[j], b)) bFound = true;
				}
				if(aFound && bFound) {
//					System.err.println("Features "+a+" and "+b+" share alt-group");
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isPredecessorOf(int a, int b){
		//System.out.println("IsPredecessorOf: "+a+", "+b);
		if(a == b) return true;
		boolean result = false;
		ArrayList<Integer> children = new ArrayList<Integer>();
		if(mandRelations.containsKey(a)) children.addAll(mandRelations.get(a));
		if(optRelations.containsKey(a)) children.addAll(optRelations.get(a));
		if(altRelations.containsKey(a)){ 
			ArrayList<int[]> altGroups = altRelations.get(a);
			for(int[] g : altGroups){
				for(int m: g) children.add(m);
			}
		}
		if(orRelations.containsKey(a)){
			ArrayList<int[]> orGroups = orRelations.get(a);
			for(int[] g : orGroups){
				for(int m: g) children.add(m);
			}
		}
		for(Integer c : children){
			result = result || isPredecessorOf(c, b);
			if(result) return result;
		}
		return result;
	}
	
	private void addCTCs(AFMWithContext afmc){
		int CTCsRemoved = 0;
		for (int i = 0; i < ctcCounter; i++){
			String ctcString = crossTreeConstraints.get(i);
			String[] ctcArr = ctcString.split("\\s+");
			
			int lhs = extractFirstIntFromString(ctcArr[0]);
			int rhs = extractFirstIntFromString(ctcArr[2]);
			
			int lhsp = findMandatoryPredecessorOf(lhs);
			int rhsp = findMandatoryPredecessorOf(rhs);
			if(!isPredecessorOf(lhsp, rhsp) && !sharesAltGroup(lhsp, rhsp)){
				afmc.setCrossTreeConstraint(lhs, ctcArr[1], rhs);
			}else{
//				System.err.println("CTC removed due to shared sub-tree: "+lhs+" and "+rhs);
				CTCsRemoved++;
			}
		}
		System.out.println("Removed "+CTCsRemoved+" CTCs");
	}
	
	private ArrayList<Integer> addAllFeatureIds(ArrayList<Integer> set){
		int highestId = incrementingID - noAttributes;
		for (int i = 0; i <= highestId; i++){
			set.add(i);
		}
		return set;
	}
	
	private void addRelations(AFMWithContext afmc){
		leafFeatures = addAllFeatureIds(new ArrayList<Integer>());
		
		afmc.setRoot(0);
		
		for(int p : mandRelations.keySet()){
			leafFeatures.remove((Integer) p);
			afmc.setMandatoryConstraints(p, convertIntegersToInt(mandRelations.get(p)));
		}
		
		for(int p : optRelations.keySet()){
			leafFeatures.remove((Integer) p);
			afmc.setOptionalConstraints(p, convertIntegersToInt(optRelations.get(p)));
		}
		
		for(int p : altRelations.keySet()){
			leafFeatures.remove((Integer) p);
			for(int[] group : altRelations.get(p)){
				try {
					afmc.setAlternativeConstraint(p, group);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		for(int p : orRelations.keySet()){
			leafFeatures.remove((Integer) p);
			for(int[] group : orRelations.get(p)){
				try {
					afmc.setOrConstraint(p, group);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private int[] convertIntegersToInt(ArrayList<Integer> integers){
		int[] ints = new int[integers.size()];
		for (int i = 0; i < integers.size(); i++){
			ints[i] = integers.get(i);
		}
		return ints;
	}
	
	private int extractFirstIntFromString(String s){
		Scanner sc = new Scanner(s);
		sc.useDelimiter("[^0-9]+");
		int integer = sc.nextInt();
		sc.close();
		return integer;
	}
	
	private void addContext(AFMWithContext afmc){
		int contextSize = context.size();
		for (int i = 0; i < contextSize; i++){
			afmc.addContext(i, context.getRangeMin(i), context.getRangeMax(i), context.getValue(i));
			
		}
	}
	
	private boolean parse(String filePath){
		originPath = filePath;
		System.out.println(filePath);
        try {
            Scanner in = new Scanner(new File(filePath));
        	while (in.hasNext()){
        		String next = in.nextLine();
        		if (next.length() > 0) processLine(next);
        	}
        	in.close();

        } catch (FileNotFoundException e){ 
        	System.out.println(e.getMessage());
        	return false;
        }
		return true;
	}
	
	private boolean processLine(String l){
		if (l.charAt(0) == '%'){
			if (l.compareTo("%Relationships") == 0) choice = RELATION;
			else if (l.compareTo("%Attributes") == 0) choice = ATTRIBUTE;
			else if (l.compareTo("%Constraints") == 0) choice = CTC;
			else return false;
			return true;
		}
		
		switch (choice){
			case RELATION:
				processRelations(l);
				break;
			case ATTRIBUTE:
				String[] att = l.split(": ");
				attributes.put(att[0], att[1]);
			break;
			case CTC:
				if (l.contains("REQUIRES") || l.contains("EXCLUDES")){
					crossTreeConstraints.put(ctcCounter, l);
					ctcCounter++;
				}else{
					choice = EXTENDEDCONST;
					extendedConstraints.put(extcCounter, l);
					extcCounter++;
				}
				break;
			case EXTENDEDCONST:
				extendedConstraints.put(extcCounter, l);
				extcCounter++;
				break;
			default:
				return false;
		}
		return true;
	}
	
	private void processRelations(String relation){
		String[] rel = relationSplit.split(relation);
		int parent;
		if (rel[0].equals("\'root\'")) {
			parent = 0;
		}
		else parent = extractFirstIntFromString(rel[0]);
		
		Matcher mandMatcher = mandPattern.matcher(rel[1]);
		Matcher optMatcher = optPattern.matcher(rel[1]);
		Matcher altMatcher = altPattern.matcher(rel[1]);
		Matcher orMatcher = orPattern.matcher(rel[1]);
		ArrayList<Integer> children;
		ArrayList<int[]> groups;
		if(mandMatcher.find()){
			//System.out.println(rel[1]+" is mandatory");
			if(mandRelations.containsKey(parent)){
				children = mandRelations.get(parent);
			}else{
				children = new ArrayList<Integer>();
				mandRelations.put(parent, children);
			}
			children.add(extractFirstIntFromString(rel[1]));
		}else if(optMatcher.find()){
			//System.out.println(rel[1]+" is optional");
			if(optRelations.containsKey(parent)){
				children = optRelations.get(parent);
			}else{
				children = new ArrayList<Integer>();
				optRelations.put(parent, children);
			}
			children.add(extractFirstIntFromString(rel[1]));
		}else if(altMatcher.find()){
			//System.out.println(rel[1]+" is alt");
			Matcher featureMatcher = featPattern.matcher(rel[1]);
			ArrayList<Integer> altGroup = new ArrayList<Integer>();
			while(featureMatcher.find()){
				altGroup.add(extractFirstIntFromString(featureMatcher.group()));
			}
			if(altRelations.containsKey(parent)){
				groups = altRelations.get(parent);
			}else{
				groups = new ArrayList<int[]>();
				altRelations.put(parent, groups);
			}
			groups.add(convertIntegersToInt(altGroup));
		}else if(orMatcher.find()){
			//System.out.println(rel[1]+" is or");
			Matcher featureMatcher = featPattern.matcher(rel[1]);
			ArrayList<Integer> orGroup = new ArrayList<Integer>();
			while(featureMatcher.find()){
				orGroup.add(extractFirstIntFromString(featureMatcher.group()));
			}
			if(orRelations.containsKey(parent)){
				groups = orRelations.get(parent);
			}else{
				groups = new ArrayList<int[]>();
				orRelations.put(parent, groups);
			}
			groups.add(convertIntegersToInt(orGroup));
		}else{
			System.err.println(rel[1]+" does not match anything");
		}
		
	}
	
}