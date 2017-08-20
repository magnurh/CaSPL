/**
 * @author Magnus
 *
 */
package no.uio.ifi.cfmReconfigurationEngine;

import java.util.ArrayList;
import java.util.HashMap;
//import java.util.List;
import java.util.regex.*;

public class ExpressionEvaluator{
	
	private HashMap<String, HashMap<String, Boolean>> evaluatedExpressions = new HashMap<String, HashMap<String, Boolean>>();
	private HashMap<String, Boolean> contextEvaluated = new HashMap<String, Boolean>();
	ContextDepFeatureModel fm;
	
//	private static Pattern andPattern = Pattern.compile(" and ");
//	private static Pattern orPattern = Pattern.compile(" or ");
//	private static Pattern plusPattern = Pattern.compile(" \\+ ");
//	private static Pattern singleEqSignPattern = Pattern.compile(" = ");
	
	private static Pattern eqNotation = Pattern.compile("([!><]=|[=><])");
	private static Pattern expectedValuePattern = Pattern.compile("([!><]=|[=><]) \\d+");
	private static Pattern implPattern = Pattern.compile(" impl ");
	private static Pattern intPattern = Pattern.compile("\\d+");
	private static Pattern idPattern = Pattern.compile("_id(att)?\\d+");
	private static Pattern idcPattern = Pattern.compile("_idc\\d+");
	
//	private static Pattern featExpPattern = Pattern.compile("^(\\(*feature\\[_id\\d+\\] = [01]\\)*)$");
//	private static Pattern contextExpPattern = Pattern.compile("^\\(*context\\[_idc\\d+\\] ([!><]=|[=><])  \\d+\\s\\)*$");
//	private static Pattern attributeExpPattern = Pattern.compile("^\\(*attribute\\[_idatt\\d+\\] ([!><]=|[=><])  \\d+\\s\\)*$");
//	private static Pattern conExpPattern = Pattern.compile("^(\\(*feature\\[_id\\d+\\] = [01]( and feature\\[_id\\d+\\] = [01])+\\)*)$");
//	private static Pattern disExpPattern = Pattern.compile("^(\\(*feature\\[_id\\d+\\] = [01]( or feature\\[_id\\d+\\] = [01])+\\)*)$");
//	private static Pattern sumExpPattern = Pattern.compile("^\\(?feature\\[_id\\d+\\]( \\+ feature\\[_id\\d+\\])+ = [01]\\)?$");
	
	private static Pattern contextPattern = Pattern.compile("\\(context\\[_idc\\d+\\] ([!><]=|[=><])  \\d+\\s\\)");
	private static Pattern featPattern = Pattern.compile("^feature\\[_id\\d+\\] = [01]$");
	private static Pattern attrPattern = Pattern.compile("^attribute\\[_idatt\\d+\\] ([!><]=|[=><])  \\d+ $");
	private static Pattern conjuncPattern = Pattern.compile("^(feature\\[_id\\d+\\] = [01]( and feature\\[_id\\d+\\] = [01])+)$");
	private static Pattern disjuncPattern = Pattern.compile("^(feature\\[_id\\d+\\] = [01]( or feature\\[_id\\d+\\] = [01])+)$");
	private static Pattern sumationPattern = Pattern.compile("^feature\\[_id\\d+\\]( \\+ feature\\[_id\\d+\\])+ = [01]$");
	
	ExpressionEvaluator(ContextDepFeatureModel fm){
		this.fm = fm;
	}

	
	public boolean evaluate(String exp, int[] candidate){
		if (fm == null){
			System.err.println("The Feature Model is empty!");
		}
		
		boolean res1 = innerEvaluation(exp, candidate);
		return res1;

	}
	
	public void printConstraintWithAssignment(String exp, int[] vect){
		ArrayList<Integer> ids = getAllFeatAndAttrIds(exp);
		System.out.println(exp);
		for (int id : ids){
			System.out.print("id"+id+" = "+vect[id]+" . ");
		}
		System.out.println();
		
	}
	
	private boolean innerEvaluation(String exp, int[] assignment){
		assignment = getAssignment(exp, assignment);
		exp = preprocess(exp, assignment.length);
		String assignmentKey = getVectorAsString(assignment);
		if(evaluatedExpressions.containsKey(exp)){
			HashMap<String, Boolean> evaluation = evaluatedExpressions.get(exp);
			if(evaluation.containsKey(assignmentKey)){
				boolean result = evaluation.get(assignmentKey);
				return result;
			}else{
				boolean result = recursiveEvaluation(exp, assignment);
				evaluation.put(assignmentKey, result);
				return result;
			}
		}else{
			HashMap<String, Boolean> evaluation = new HashMap<String, Boolean>();
			boolean result = recursiveEvaluation(exp, assignment);
			evaluation.put(assignmentKey, result);
			evaluatedExpressions.put(exp, evaluation);
			return result;
		}
	}
	
	private int[] getAssignment(String exp, int[] candidate){
		ArrayList<Integer> ids = getAllFeatAndAttrIds(exp);
		int[] assignment = new int[ids.size()];
		for(int i = 0; i < assignment.length; i++){
			assignment[i] = candidate[ids.get(i)];
		}
		return assignment;
	}
	
	public String getVectorAsString(int[] v){
		String str = "";
		if(v.length > 0){
			str = ""+v[0];
		}
		for(int i = 1; i < v.length; i++){
			str += ","+v[i];
		}
		return str;
	}

	
	private boolean recursiveEvaluation(String exp, int[] assignment){
		Matcher implMatcher = implPattern.matcher(exp);
		if(implMatcher.find()){
			String[] clauses = splitExpression(exp);
			return (!innerEvaluation(clauses[0], assignment)) || innerEvaluation(clauses[1], assignment);
		}
		//else, it's one of several basecases
		if(assignment.length == 0){
			if(exp.compareTo("TRUE") == 0) return true;
			else if(exp.compareTo("FALSE") == 0) return false;
			else System.err.println("WARNING: Unable to evaluate expression: "+exp+", "+assignment.length);
		}else if(assignment.length == 1){
			//if feat
			// else if attr
			// else err
			Matcher featMatcher = featPattern.matcher(exp);
			if(featMatcher.find()){
				return expectedValue(exp) == assignment[0];
			}
			Matcher attrMatcher = attrPattern.matcher(exp);
			if(attrMatcher.find()){
				return evaluateEqOperation(assignment[0], getEqualityOperator(exp), expectedValue(exp));
			}
			System.err.println("WARNING: Unable to evaluate expression: "+exp+", "+assignment.length);			
		}else if(assignment.length > 1){
			// if and
			// else if or
			// else if sum
			// else 
			Matcher orMatcher = disjuncPattern.matcher(exp);
			if(orMatcher.find()){
				return evalDisjunction2(exp, assignment);
			}
			Matcher andMatcher = conjuncPattern.matcher(exp);
			if(andMatcher.find()){
				return evalConjunction2(exp, assignment);
			}
			Matcher sumMatcher = sumationPattern.matcher(exp);
			if(sumMatcher.find()){
				return expectedValue(exp) == sumVector(assignment);
			}
			System.err.println("WARNING: Unable to evaluate expression: "+exp+", "+assignment.length);		
		}
		return false;
	}
	
	private int sumVector(int[] vect){
		int res = 0;
		for(int i = 0; i < vect.length; i++){
			res += vect[i];
		}
		return res;
	}
	
	private int expectedValue(String exp){
		String[] expVal = eqNotation.split(exp);
		if(expVal.length < 2) System.err.println(exp);
		return getInt(expVal[1]);
	}
	
	private boolean evalDisjunction2(String exp, int[] assignment){
		int i = 0;
		Matcher m = expectedValuePattern.matcher(exp);
		while(m.find()){
			if(i >= assignment.length){
				System.err.println("Assignment is too short, "+exp+", "+assignment.length);
				return false;
			}
			int expVal = getInt(m.group());
			if(expVal == assignment[i]) return true;
			i++;
		}
		return false;
	}
	
	private boolean evalConjunction2(String exp, int[] assignment){
		int i = 0;
		Matcher m = expectedValuePattern.matcher(exp);
		while(m.find()){
			if(i >= assignment.length){
				System.err.println("Assignment is too short, "+exp+", "+assignment.length);
				return false;
			}
			int expVal = getInt(m.group());
			if(expVal != assignment[i]) {
				return false;
			}
			i++;
		}
		return true;
	}
	
	private String[] splitExpression(String exp){
		int clausePos = exp.indexOf('(');
		if(clausePos == -1){
			return implPattern.split(exp);
		}else if(clausePos == 0){
			String[] result = new String[2];
			int clauseEnd = exp.lastIndexOf(')');
			result[0] = exp.substring(0, clauseEnd+1);
			result[1] = exp.substring(clauseEnd+7, exp.length());
			return result;
		}else{
			String[] result = new String[2];
			result[0] = exp.substring(0, clausePos-6);
			result[1] = exp.substring(clausePos, exp.length());
			return result;
		}
	}
	
	private String preprocess(String exp, int numberOfIds){
		exp = replaceContext(exp);
		exp = trimForParentheses(exp);
		return generaliseExpression(exp, numberOfIds);
	}
	
	
	private String generaliseExpression(String exp, int numberOfIds){
		StringBuilder result = new StringBuilder(exp);
		int newId = 0;
		Matcher m = idPattern.matcher(result);
		while (newId < numberOfIds && m.find()){
			String id = m.group();
			String replacingId = "";
			if (id.contains("att")) replacingId = "_idatt"+newId;
			else replacingId = "_id"+newId;
			result.replace(m.start(), m.end(), replacingId);
			newId++;
		}		
		return result.toString();
	}

	
	private String replaceContext(String exp){
		Matcher m = contextPattern.matcher(exp);
		String result = "";
		if (m.find()){
			String cont = m.group();
			boolean contextEval = evalContext(cont);
			if(contextEval){
				result = result + exp.substring(0, m.start()) + "TRUE" + exp.substring(m.end(), exp.length());
			}else{
				result = result + exp.substring(0, m.start()) + "FALSE" + exp.substring(m.end(), exp.length());
			}
			return result;
		}
		return exp;
	}
	
	private boolean evalContext(String contextExp){
		if(contextEvaluated.containsKey(contextExp)){
			return contextEvaluated.get(contextExp);
		}else{
			ArrayList<Integer> idExpectedValue = getAllInts(contextExp);
			int idc = idExpectedValue.get(0);
			int expectedValue = idExpectedValue.get(1);
			int value = fm.getContextValue("context[_idc"+idc+"]");
			String eqOp = getEqualityOperator(contextExp);
			boolean res = evaluateEqOperation(value, eqOp, expectedValue);
			contextEvaluated.put(contextExp, res);
			return res;
		}
	}
	
	
	public ArrayList<Integer> getAllInts(String exp){
		ArrayList<Integer> result = new ArrayList<Integer>();
		Matcher m = intPattern.matcher(exp);
		while (m.find()) {
		  result.add(Integer.parseInt(m.group()));
		}
		return result;
	}
	
	public static int getInt(String s){
		Matcher m = intPattern.matcher(s);
		if (m.find()) {
		  return Integer.parseInt(m.group());
		}
		return -1;
	}

	
	private String getEqualityOperator(String exp){
		Matcher m = eqNotation.matcher(exp);
		if (m.find()){
			return m.group(1);
		}else{
			System.err.println(exp+" does not contain a valid equality sign");
			return "";
		}
	}
	
	private boolean evaluateEqOperation(int a, String op, int b){
		if(op.equals("=")){
			return a == b;
		}else if(op.equals("!=")){
			return a != b;
		}else if(op.equals("<")){
			return a < b;
		}else if(op.equals(">")){
			return a > b;
		}else if (op.equals("<=")){
			return a <= b;
		}else if(op.equals(">=")){
			return a >= b;
		}else{
			System.err.println(op+" is not a valid equality sign");
			return false;
		}
	}

	public int getContextId(String exp){
		Matcher m = idcPattern.matcher(exp);
		if (m.find()) {
			return getInt(m.group());
		}
		return -1;
	}
	
	public static ArrayList<Integer> getAllFeatAndAttrIds(String exp){
		ArrayList<Integer> result = new ArrayList<Integer>();
		Matcher m = idPattern.matcher(exp);
		while (m.find()) {
			result.add(getInt(m.group()));
			}
		return result;
	}
	
	private String trimForParentheses(String exp) {
		while (exp.charAt(0) == '(' && exp.charAt(exp.length()-1) == ')'){
			exp = exp.substring(1, exp.length()-1);
		}
		return exp;
	}
	
	
	//Using String instead of StringBuilder
//	private String generaliseExpressionVersion2(String exp, int numberOfIds){
//		int newId = 0;
//		int lastBreakIndex = 0;
//		String result = "";
//		Matcher m = idPattern.matcher(exp);
//		while (newId < numberOfIds && m.find()){
//			String id = m.group();
//			String replacingId = "";
//			//System.out.println(m.group());
//			if (id.contains("att")) replacingId = "_idatt"+newId;
//			else replacingId = "_id"+newId;
//			//System.out.println(exp+" with substring("+lastBreakIndex+","+m.start()+"): "+exp.substring(lastBreakIndex, m2.start()));
//			result = result + exp.substring(lastBreakIndex, m.start()) + replacingId;
//			lastBreakIndex = m.end();
//			//result.replace(m.start(), m.end(), replacingId);
//			newId++;
//		}
//		result = result + exp.substring(lastBreakIndex, exp.length());
//		return result;
//	}
	
//	private boolean evaluateInner(String exp, int[] assignment){
//	exp = trimForParentheses(exp);
//		boolean res = false;
//		HashMap<String, Boolean> evaluations;
//		if (expressionsEvaluated.containsKey(exp)){
//			evaluations = expressionsEvaluated.get(exp);
//		}else{
//			evaluations = new HashMap<String, Boolean>();
//			expressionsEvaluated.put(exp, evaluations);
//		}
//		String assignmentKey = getVectorAsString(assignment);
//		if(evaluations.containsKey(assignmentKey)){
//			res = evaluations.get(assignmentKey);
//		}else{
//			res = evaluateRecursive(exp, assignment);
//			evaluations.put(assignmentKey, res);
//			//System.out.println(exp+" with assignment: "+assignmentKey+" evaluated: "+res);
//		}
//		return res;
//	}
	

//	private boolean evaluateRecursive(String exp, int[] val){
//		if (isImplication(exp)){
//			//System.out.println(exp+" -is implication");
//			return evalImplication(exp, val);
//		}else if(isAtomic(exp)){
//			//System.out.println(exp+" -is atomic");
//			return evalAtomic(exp, val);
//			
//		}else if(isConjunction(exp)){
//			//System.out.println(exp+" -is conjunction");
//			return evalConjunction(exp, val);
//
//		}else if(isDisjunction(exp)){
//			//System.out.println(exp+" -is disjunction");			
//			return evalDisjunction(exp, val);
//
///*		}else if(isCompoundOrGrouping(exp)){
//			System.out.println(exp+" -is compound");
//			return evalCompoundOrGrouping(exp, val);*/
//
//		}else if(isSummation(exp)){
//			//System.out.println(exp+" -is summation");
//			return evalSummation(exp,val);
//
//		}else{
//			System.err.println("Can't recognize expression: "+exp);
//		}
//		return false;
//	}
	
//	private boolean evalSummation(String exp, int[] val){
//		String[] leftRightSide = singleEqSignPattern.split(exp);
//		int expectedValue = Integer.parseInt(""+leftRightSide[1].charAt(0));
//		int sum = 0;
//		ArrayList<Integer> lhsIds = getAllInts(leftRightSide[0]);
//		for (Integer i: lhsIds){
//			sum += val[i];
//		}
//		return sum == expectedValue;
//	}
	
//	private boolean isSummation(String exp){
//		Matcher m = sumExpPattern.matcher(exp);
//		return m.find();
//		//(feature[_id36] + feature[_id37] + feature[_id38] = 1)",
//	}
	
//	private boolean evalCompoundOrGrouping(String exp, int[] val){
//		String[] leftRightSide = singleEqSignPattern.split(exp);
//		int lhsValue = 0;
//		int rhsValue = 0;
//		// Assumes true if (lhs = rhs = 0) OR (lhs > 0 and rhs > 0)
//		ArrayList<Integer> lhsIds = getAllInts(leftRightSide[0]);
//		for (Integer i : lhsIds){
//			lhsValue += val[i];
//		}
//		ArrayList<Integer> rhsIds = getAllInts(leftRightSide[1]);
//		for (Integer i : rhsIds){
//			rhsValue += val[i];
//		}		
//		return (lhsValue > 0 && rhsValue >0) || (lhsValue == 0 && rhsValue == 0);
//	}
	
//	private boolean isCompoundOrGrouping(String exp){
//		return exp.matches("\\(?feature\\[_id\\d+\\] = \\(feature\\[_id\\d+\\]( \\+ feature\\[_id\\d+\\])+\\){1,2}");
//	}
	
//	private boolean evalConjunction(String exp, int[] val){
//		Matcher m = andPattern.matcher(exp);
//		boolean c1 = false;
//		boolean c2 = false;
//		if (m.find()) {
//           c1 = evaluateInner(exp.substring(0, m.start()), val);
//           c2 = evaluateInner(exp.substring(m.end(), exp.length()), val);
//		}
//		return c1 && c2;
//	}
	
//	private boolean isConjunction(String exp){
//		Matcher m = conExpPattern.matcher(exp);
//		return m.find();
//	}
	
//	private boolean evalDisjunction(String exp, int[] val){
//		Matcher m = orPattern.matcher(exp);
//		boolean c1 = false;
//		boolean c2 = false;
//		if (m.find()) {
//            c1 = evaluateInner(exp.substring(0, m.start()), val);
//            c2 = evaluateInner(exp.substring(m.end(), exp.length()), val);
//		}
//		return c1 || c2;
//	}
	
//	private boolean isDisjunction(String exp){
//		Matcher m = disExpPattern.matcher(exp);
//		return m.find();
//	}
	
//	private boolean evalAtomic(String exp, int[] val){
//		int expectedValue;
//		ArrayList<Integer> idExpectedValue = getAllInts(exp);
//		int id = idExpectedValue.get(0).intValue();
//		expectedValue = idExpectedValue.get(1).intValue();
//		
//		if(isFeature(exp)){
//			//System.out.println(exp+" -is Feature");
//			return val[id] == expectedValue;
//		}else if (isContext(exp)){
//			if(contextEvaluated.containsKey(exp)){
//				return contextEvaluated.get(exp);
//			}else{
//				//System.out.println(exp+" -is Context");
//				int value = fm.getContextValue("context[_idc"+id+"]");
//				//System.out.println("context[_idc"+id+"] = "+value);
//				boolean res = evaluateEqOperation(value, getEqualityOperator(exp), expectedValue);
//				//System.out.println("context[_idc"+id+"] (= "+value+") "+getEqualityOperator(exp)+" "+expectedValue+" : "+res);
//				contextEvaluated.put(exp, res);
//				return res;
//			}
//		}else if(isAttribute(exp)){
//			//System.out.println(exp+" -is Attribute");
//			int value = val[id];
//			boolean res = evaluateEqOperation(value, getEqualityOperator(exp), expectedValue);
//			//System.out.println("attribute[_idatt"+id+"] (= "+value+") "+getEqualityOperator(exp)+" "+expectedValue+" : "+res);
//			return res;
//		}else{
//			System.err.println(exp+" -is not a valid atomic");
//			return false;
//		}
//	}
	
//	
//	public boolean isAtomic(String exp){
//		//System.out.println("Check atomicity: "+exp);
//		//System.out.println("F: "+isFeature(exp)+", C: "+isContext(exp)+", A: "+isAttribute(exp));
//		return isFeature(exp) || isContext(exp) || isAttribute(exp);
//	}
	
//	private boolean isFeature(String exp){
//		Matcher m = featExpPattern.matcher(exp);
//		return m.find();
//	}
//	
//	private boolean isContext(String exp){
//		Matcher m = contextExpPattern.matcher(exp);
//		return m.find();
//	}
//	
//	private boolean isAttribute(String exp){
//		Matcher m = attributeExpPattern.matcher(exp);
//		return m.find();
//	}
	
//	private boolean evalImplication(String exp, int[] val){
//		String[] clauses = implPattern.split(exp);
//		boolean c1 = true;
//		boolean c2 = false;
//		if (clauses.length > 3) System.err.println("Implications with more than 3 clauses is not supported: "+exp);
//		else if (clauses.length < 2) System.err.println("Implications must have at least 2 clauses: "+exp);
//		else if(clauses.length == 3){
//			boolean leftFirst = isLeftImplInnerMost(clauses);
//			/*for (String cl: clauses){
//				System.out.println(cl);
//			}*/
//			if (leftFirst){
//				c1 = evaluateInner(clauses[0]+" impl "+clauses[1], val);
//				c2 = evaluateInner(clauses[2], val);
//			}else{
//				c1 = evaluateInner(clauses[0], val);
//				c2 = evaluateInner(clauses[1]+" impl "+clauses[2], val);
//			}
//		}else {
//			c1 = evaluateInner(clauses[0], val);
//			c2 = evaluateInner(clauses[1], val);
//		}
//		return !(c1 && !c2);
//	}
	
	
//	private boolean isLeftImplInnerMost(String[] clauses){
//		int[][] numberOfParentheses = new int[clauses.length][2];
//		for (int i = 0; i < clauses.length; i++){
//			Pattern left = Pattern.compile("\\(");
//			Pattern right = Pattern.compile("\\)");
//	        Matcher m = left.matcher(clauses[i]);
//			while(m.find()) numberOfParentheses[i][0] += 1;
//			m = right.matcher(clauses[i]);
//			while(m.find()) numberOfParentheses[i][1] += 1;
//			if (numberOfParentheses[i][0] > numberOfParentheses[i][1]) {
//				numberOfParentheses[i][0] -= numberOfParentheses[i][1];
//				numberOfParentheses[i][1] -= numberOfParentheses[i][1];
//			}else{
//				numberOfParentheses[i][1] -= numberOfParentheses[i][0];
//				numberOfParentheses[i][0] -= numberOfParentheses[i][0];
//			}
//			//System.out.println("#parentheses ("+i+"): "+numberOfParentheses[i][0]+", "+numberOfParentheses[i][1]);
//		}
//		
//		if (numberOfParentheses[0][1] == 0 && numberOfParentheses[1][0] == 0) return true;
//		else return false;
//	}
	

	
//	public void printStoredEvaluations(){
//	List<String> expressions = new ArrayList<String>(expressionsEvaluated.keySet());
//	java.util.Collections.sort(expressions);
//	for(String exp : expressions){
//		HashMap<String, Boolean> evaluations = expressionsEvaluated.get(exp);
//		System.out.println(exp);
//		for (String eval : evaluations.keySet()){
//			System.out.println(eval);
//		}
//	}
//}
	
}