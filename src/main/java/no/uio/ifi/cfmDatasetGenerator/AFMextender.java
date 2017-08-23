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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class AFMextender implements VarModelExtender{

	private int choice = RELATION;
	
	private String originPath;
	private Context context;
	private HashMap<Integer, String> relations;
	private int relationCounter;
	private HashMap<String, String> attributes;
	
	private HashMap<Integer, String> crossTreeConstraints;
	private int ctcCounter;
	private HashMap<Integer, String> extendedConstraints;
	private int extcCounter;
	
	private int incrementingID;
	private int noAttributes = 0;
	
	public AFMextender(int highestID, int contextMaxSize, int contextMaxRange){
		context = new Context(1, contextMaxSize, contextMaxRange);
		relations = new HashMap<Integer, String>();
		relationCounter = 0;
		attributes = new HashMap<String, String>();
		crossTreeConstraints = new HashMap<Integer, String>();;
		ctcCounter = 0;
		extendedConstraints = new HashMap<Integer, String>();;
		extcCounter = 0;
		
		incrementingID = highestID;
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
		addContext(afmc);
		addRelations(afmc);
		addCTCs(afmc);
		addVFs(afmc);
		return true;
	}
	
	private void addVFs(AFMWithContext afmc){
		HashMap<Integer, int[]> attributeList = new HashMap<Integer, int[]>();
		HashMap<Integer, Integer> addedAttributes = new HashMap<Integer, Integer>();
		for (int i = 0; i < extcCounter; i++){
			
			String extConstString = extendedConstraints.get(i);
			String[] extConstArr = extConstString.split("\\s+");
			
			int idf = extractFirstIntFromString(extConstArr[0]);
			int idc = ThreadLocalRandom.current().nextInt(0, context.size());
			String contextEQ = extConstArr[1];
			int cValue = ThreadLocalRandom.current().nextInt(context.getRangeMin(idc), context.getRangeMax(idc)+1);
			
			if ( !isValidEqualityTop(cValue, contextEQ, context.getRangeMax(idc)) ){
				if (context.getRangeMax(idc)-1 >= context.getRangeMin(idc)) cValue = context.getRangeMax(idc) - 1;
				else contextEQ = "=";
			}
			if ( !isValidEqualityBottom(cValue, contextEQ, context.getRangeMin(idc)) ){
				if (context.getRangeMin(idc)+1 <= context.getRangeMax(idc)) cValue = context.getRangeMin(idc) + 1;
				else contextEQ = "=";
			}
			if(contextEQ.equals("==")) contextEQ = "=";
			
			boolean VFwithAttribute = Math.random() > 0.5;
			if (VFwithAttribute || isMandatoryFromRoot(idf, afmc.constraints)){
				int attr_idf = extractFirstIntFromString(extConstArr[0]);
				int ida = incrementingID;
				if(addedAttributes.containsKey(attr_idf)){
					ida = addedAttributes.get(attr_idf);
				}else{
					ida = ++incrementingID;
					noAttributes++;
				}
				int attrMin;
				int attrMax;
				int attrValue;
				
				if (attributeList.containsKey(ida)){
					int[] attr = attributeList.get(ida);
					attrMin = attr[0];
					attrMax = attr[1];
					attrValue = attr[2];
				}else{
					String[] attributeConfig = attributes.get(extConstArr[0]).split(",");
					attrMin = extractFirstIntFromString(attributeConfig[0]);
					int origMax = extractSecondIntFromString(attributeConfig[0]);
					int origValue = extractFirstIntFromString(attributeConfig[1]);
					attrMax = ThreadLocalRandom.current().nextInt(attrMin+1, origMax);
					attrValue = (int) ( (origValue - attrMin) * ( (1.0 * attrMax - attrMin) / (origMax - attrMin) ) ) + attrMin;
					afmc.addAttribute(ida, attrMin, attrMax, attrValue, attr_idf);
					attributeList.put(ida, new int[]{attrMin, attrMax, attrValue, attr_idf});
					addedAttributes.put(attr_idf, ida);
				}
				
				String attributeEQ = extConstArr[5];
				attrValue = ThreadLocalRandom.current().nextInt(attrMin, attrMax+1);
				
				if ( !isValidEqualityTop(attrValue, attributeEQ, attrMax) ){
					if (attrMax - 1 >= attrMin) attrValue = attrMax - 1;
					else attributeEQ = "=";
				}
				if ( !isValidEqualityBottom(attrValue, attributeEQ, attrMin) ){
					if (attrMin + 1 <= attrMax) attrValue = attrMax + 1;
					else attributeEQ = "=";
				}
				if(attributeEQ.equals("==")) attributeEQ = "=";
				afmc.addVF(attr_idf, idc, contextEQ, cValue, ida, attributeEQ, attrValue);
			}else{
//				System.out.println("Add VF: F"+idf+" impl (C"+idc+" "+contextEQ+" "+cValue+")");
				afmc.addVF(idf, idc, contextEQ, cValue);
//				System.out.println("Context: ("+idc+") "+cValue+" in ["+context.getRangeMin(idc)+", "+context.getRangeMax(idc)+"]");
//				System.out.println("-");
			}
			//System.out.println("Iter: "+i+" < "+extcCounter);
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
	
	private boolean isValidEqualityTop(int value, String eq, int max){
		if (value < max) return true;
		else if (value > max) return false;
		else if (eq.equals(">")) return false;
		else return true;
	}
	
	private boolean isValidEqualityBottom(int value, String eq, int min){
		if (value > min) return true;
		else if (value < min) return false;
		else if (eq.equals("<")) return false;
		else return true;
	}
	
	private void addCTCs(AFMWithContext afmc){
		for (int i = 0; i < ctcCounter; i++){
			String ctcString = crossTreeConstraints.get(i);
			String[] ctcArr = ctcString.split("\\s+");
			
			int lhs = extractFirstIntFromString(ctcArr[0]);
			int rhs = extractFirstIntFromString(ctcArr[2]);
			
			afmc.setCrossTreeConstraint(lhs, ctcArr[1], rhs);
		}
	}
	
	private void addRelations(AFMWithContext afmc){
		for (int i = 0; i < relationCounter; i++){
			String relationString = relations.get(i);
			//System.out.println(relationString);
			String[] relArr = relationString.split(" : ");
			String parentString = relArr[0];
			String childrenString = relArr[1];
			String[] children = childrenString.split("\\s+");

			ArrayList<Integer> mandatoryChildren = new ArrayList<Integer>();
			ArrayList<Integer> optionalChildren = new ArrayList<Integer>();
			ArrayList<Integer> group = new ArrayList<Integer>();
			boolean orGroup = false;
			boolean altGroup = false;
			
			int parent;
			if (parentString.equals("root")) {
				parent = 0;
				afmc.setRoot(0);
			}
			else parent = extractFirstIntFromString(parentString);
			
			for (int j = 0; j < children.length; j++){
				if (!children[j].isEmpty()){
					String element = children[j];					
					// End of a group
					if (element.charAt(0) == '}'){
						int[] groupInt = convertIntegersToInt(group);
						if (orGroup){
							try {
								afmc.setOrConstraint(parent, groupInt);
							} catch (Exception e) {
								e.printStackTrace();
							}
							orGroup = false;
						}else if (altGroup){
							try {
								afmc.setAlternativeConstraint(parent, groupInt);
							} catch (Exception e) {
								e.printStackTrace();
							}
							altGroup = false;
						}
						group = new ArrayList<Integer>();
						element = element.substring(1);
					}
					
					//Either a mandatory, or a feature inside a group
					if(children[j].charAt(0) == 'F'){
						int c = extractFirstIntFromString(element);
						if (orGroup || altGroup) {
							group.add(c);
						}else {
							mandatoryChildren.add(c);
						}
						
					//Beginning of a group
					}else if(element.charAt(0) == '{'){
						int c = extractFirstIntFromString(element);
						group.add(c);
						
					//Either optional or interval
					}else if(element.charAt(0) == '['){
						if(element.charAt(1) == 'F'){
							int c = extractFirstIntFromString(element);
							optionalChildren.add(c);
						}else{
							int intervalRoof = extractLastIntFromString(element);
							if (intervalRoof == 1) {
								altGroup = true;
							}else if (intervalRoof > 1) {
								orGroup = true;
							}
						}
					}
				}
			}
			int[] mandatoryChildrenInt = convertIntegersToInt(mandatoryChildren);
			afmc.setMandatoryConstraints(parent, mandatoryChildrenInt);
			int[] optionalChildrenInt = convertIntegersToInt(optionalChildren);
			afmc.setOptionalConstraints(parent, optionalChildrenInt);
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
	
	private int extractSecondIntFromString(String s){
		Scanner sc = new Scanner(s);
		sc.useDelimiter("[^0-9]+");
		sc.nextInt();
		int integer = sc.nextInt();
		sc.close();
		return integer;
	}
	
	private int extractLastIntFromString(String s){
		Scanner sc = new Scanner(s);
		sc.useDelimiter("[^0-9]+");
		int integer = -1;
		while (sc.hasNextInt()){
			integer = sc.nextInt();
		}
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
		//System.out.println(filePath);
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
				relations.put(relationCounter, l);
				relationCounter++;
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
	
}