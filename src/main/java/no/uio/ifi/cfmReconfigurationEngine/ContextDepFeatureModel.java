/**
 * @author Magnus
 *
 */
package no.uio.ifi.cfmReconfigurationEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class ContextDepFeatureModel{
		
	private HashMap<String, Attribute> attributes;
	private HashMap<String, ContextVar> context;
	private ArrayList<String> constraints;
	private ArrayList<ArrayList<Integer>> idsInConstraints;
	private HashSet<Integer> selectedFeatures;
	private int size;
	private int numberOfFeatures;
	private int numberOfAttributes;
	private int contextSize;
	
	private Pattern assignedFeatPattern = Pattern.compile("feature\\[_id\\d+\\] = [01]");
	
	ContextDepFeatureModel(JSONObject FM, int size){
		attributes = new HashMap<String, Attribute>();
		context = new HashMap<String, ContextVar>();
		constraints = new ArrayList<String>();
		idsInConstraints = new ArrayList<ArrayList<Integer>>();
		initializeIdsInConstraints(size);
		selectedFeatures = new HashSet<Integer>();
		new HashSet<String>();
		populate(FM);
	}
	
	private void initializeIdsInConstraints(int size){
		for (int i = 0; i < size; i++){
			idsInConstraints.add(i, new ArrayList<Integer>());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void populate(JSONObject FM){
		JSONObject configJSON = (JSONObject) FM.get("configuration");
		
		JSONArray attributesJSON = (JSONArray) FM.get("attributes");
		Iterator<JSONObject> attributeIter = attributesJSON.iterator();
		while (attributeIter.hasNext()){
			JSONObject a = attributeIter.next();
			String id = (String) a.get("id");
			Long min = (Long) a.get("min");
			Long max = (Long) a.get("max");
			String featureId = (String) a.get("featureId");
			Attribute attribute = new Attribute(id, min.intValue(), max.intValue(), featureId);
			attributes.put(id, attribute);
		}
		
		JSONArray attributeValuesJSON = (JSONArray) configJSON.get("attribute_values");
		Iterator<JSONObject> attributeValueIter = attributeValuesJSON.iterator();
		while (attributeValueIter.hasNext()){
			JSONObject av = attributeValueIter.next();
			String id = (String) av.get("id");
			Long value = (Long) av.get("value");
			Attribute attribute = attributes.get(id);
			attribute.setValue(value.intValue());
		}
		
		JSONArray contextJSON = (JSONArray) FM.get("contexts");
		Iterator<JSONObject> contextIter = contextJSON.iterator();
		while (contextIter.hasNext()){
			JSONObject c = contextIter.next();
			String id = (String) c.get("id");
			Long min = (Long) c.get("min");
			Long max = (Long) c.get("max");
			ContextVar contextvar = new ContextVar(id, min.intValue(), max.intValue());
			context.put(id, contextvar);
		}
		
		JSONArray contextValuesJSON = (JSONArray) configJSON.get("context_values");
		Iterator<JSONObject> contextValueIter = contextValuesJSON.iterator();
		while (contextValueIter.hasNext()){
			JSONObject cv = contextValueIter.next();
			String id = (String) cv.get("id");
			Long value = (Long) cv.get("value");
			ContextVar contextvar = context.get(id);
			contextvar.setValue(value.intValue());
		}
		
		
		JSONArray constraintsJSON = (JSONArray) FM.get("constraints");
		Iterator<String> constraintsIter = constraintsJSON.iterator();
		while (constraintsIter.hasNext()){
			String constraint = constraintsIter.next();
			constraints.add(constraint);

		}
		
		JSONArray selectedFeaturesJSON = (JSONArray) configJSON.get("selectedFeatures");
		Iterator<String> selectedFeaturesIter = selectedFeaturesJSON.iterator();
		while (selectedFeaturesIter.hasNext()){
			Integer selFeat = getIdAsInteger(selectedFeaturesIter.next());
			if (selFeat >= 0) selectedFeatures.add(selFeat);
		}
		
		locateSelectedFeatures();
		removeRulesAffectedBySelectedFeatures();
		populateIndexOfIdsBelongingToConstraints();
		
	}
	
	
	public int getContextValue(String id){
		ContextVar c = context.get(id);
		return c.getValue();
	}
	
	public Attribute getAttribute(int attIndex){
		String attId = "attribute[_idatt"+attIndex+"]";
		return attributes.get(attId);
	}
	
	public int getAttributeValue(String id){
		Attribute a = attributes.get(id);
		return a.getValue();
	}
	
	public int[] getAttributeRange(int attIndex){
		String attId = "attribute[_idatt"+attIndex+"]";
		Attribute a = attributes.get(attId);
		
		if (!a.isRangeComplete()){
			//System.out.println("Find range values for "+attId);		//
			setMultiRange(attIndex, a);
		}
		
		return a.getRange();
	}
	
	private void setMultiRange(int index, Attribute a){
		String attributeConstraintPattern = ".*attribute\\[_idatt"+index+"\\] ([!><]=|[=><])  \\d+.*";
		
		for (String constraint : constraints){
			//System.out.println(constraint+" matches? "+attributeConstraintPattern);
			if (constraint.matches(attributeConstraintPattern)){				
				String[] e = constraint.split("attribute\\[_idatt"+index+"\\] ([!><]=|[=><])  ");
				Integer val = getIdAsInteger(e[1]);
				a.insertRangeInterval(val.intValue());
			}
		}
		a.rangeCompleted();
	}
	
	public ArrayList<String> getConstraintsContainingId(int id){
		ArrayList<Integer> constraintNumbers = idsInConstraints.get(id);
		ArrayList<String> constraintList = new ArrayList<String>();
		for(int n : constraintNumbers){
			constraintList.add(constraints.get(n));
		}
		return constraintList;
	}
	
	public ArrayList<String> getConstraints(){
		return constraints;
	}
	
	public HashSet<Integer> getSelectedFeatures(){
		return selectedFeatures;
	}
	
	private void locateSelectedFeatures(){
		HashSet<Integer> queue = new HashSet<Integer>();
		queue.addAll(selectedFeatures);
		while (!queue.isEmpty()){
			for (Integer f : queue.toArray(new Integer[queue.size()])){
				selectedFeatures.add(f);
				//System.out.println("Added to selected features: feature[_id"+f+"]");		//
				HashSet<Integer> dependentFeatures = findDependentFeatures(f);
				for (Integer g : dependentFeatures){
					if (!selectedFeatures.contains(g)) queue.add(g);
				}
				queue.remove(f);
			}
		}
		//System.out.println("Number of features always selected "+selectedFeatures.size());		//
	}
	
	private void removeRulesAffectedBySelectedFeatures(){
		String selectedIdsPattern = generateOrPatternFromSetOfIntegers(selectedFeatures);
		//System.out.println(selectedIdsPattern);
		String obsoleteRulePattern = "\\(*.* impl \\(*(feature\\[_id\\d+\\] = [01] or )*feature\\[_id"+selectedIdsPattern+"\\] = 1( or feature\\[_id\\d+\\] = [01])*\\)*";
		for(ListIterator<String> constrIter = constraints.listIterator(); constrIter.hasNext();){
			String rule = constrIter.next();
			if(rule.matches(obsoleteRulePattern)){
				constrIter.remove();
			}else{
			}
		}
		
	}
	
	private String generateOrPatternFromSetOfIntegers(HashSet<Integer> set){
		Iterator<Integer> selectedIds = set.iterator();
		String selectedIdsPattern = "("+selectedIds.next();
		while (selectedIds.hasNext()){
			selectedIdsPattern += "|"+selectedIds.next();
		}
		selectedIdsPattern += ")";
		return selectedIdsPattern;
	}
	
	private void populateIndexOfIdsBelongingToConstraints(){
		for(int i = 0; i < constraints.size(); i++){
			ArrayList<Integer> idsInThisConstraint = ExpressionEvaluator.getAllFeatAndAttrIds(constraints.get(i));
			for (int j : idsInThisConstraint){
				ArrayList<Integer> constraintNumbers = idsInConstraints.get(j);
				constraintNumbers.add(i);
			}
		}
	}
	private HashSet<Integer> findDependentFeatures(int trueFeat){
		HashSet<Integer> dependentFeatures = new HashSet<Integer>();
		ArrayList<String> constraintsToBeRemoved = new ArrayList<String>();
		String requiresPattern = "\\(*(feature\\[_id\\d+\\] = 1 or )*feature\\[_id"+trueFeat+"\\] = 1( or feature\\[_id\\d+\\] = 1)*\\)* impl \\(?feature\\[_id\\d+\\] = 1( and feature\\[_id\\d+\\] = 1)*\\)*";
		
		for (String constraint : constraints){			
			if (constraint.matches(requiresPattern)){
				boolean constraintCanBeRemoved = true;
				String[] r = constraint.split(" impl ");
				Matcher matcher = assignedFeatPattern.matcher(r[1]);
				while (matcher.find()){
				    String[] assignedFeat = matcher.group().split(" = ");
				    int assignedTruthValue = Integer.parseInt(assignedFeat[1]); 
				    if (assignedTruthValue == 1){
				    	dependentFeatures.add(getIdAsInteger(assignedFeat[0]));
				    }else{
				    	constraintCanBeRemoved = false;
				    }
				}
				if (constraintCanBeRemoved) constraintsToBeRemoved.add(constraint);
			}
		}
		for (String c : constraintsToBeRemoved){
			constraints.remove(c);
			//System.out.println("Removed from R: "+c);	//
		}
		
		return dependentFeatures;
	}
	
	private Integer getIdAsInteger(String id){
		Pattern intPattern = Pattern.compile("\\d+");
		Matcher m = intPattern.matcher(id);
		if (m.find()) {
		  return (Integer) Integer.parseInt(m.group());
		}
		return -1;
	}
	
	public int numberOfFeatures(){
		return numberOfFeatures;
	}
	
	public void setNumberOfFeatures(int numberOfFeatures){
		this.numberOfFeatures = numberOfFeatures;
	}
	
	public int numberOfAttributes(){
		return numberOfAttributes;
	}
	
	public void setNumberOfAttributes(int numberOfAttributes){
		this.numberOfAttributes = numberOfAttributes;
	}

	public int size() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int contextSize() {
		return contextSize;
	}

	public void setContextSize(int contextSize) {
		this.contextSize = contextSize;
	}
}