/**
 * @author Magnus
 *
 */
package no.uio.ifi.cfmDatasetGenerator;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class AFMWithContext{
	
	JSONObject afmc;
	JSONArray contexts;
	JSONArray constraints;
	JSONArray attributes;
	JSONArray preferences;
	
	JSONObject configuration;
	JSONArray selectedFeatures;
	JSONArray attributeValues;
	JSONArray contextValues;
	
	public AFMWithContext(){
		
		 afmc = new JSONObject();
		 contexts = new JSONArray();
		 constraints = new JSONArray();
		 attributes = new JSONArray();
		 preferences = new JSONArray();
		 
		 configuration = new JSONObject();
		 selectedFeatures = new JSONArray();
		 attributeValues = new JSONArray();
		 contextValues = new JSONArray();
	}
	
	
	@SuppressWarnings("unchecked")
	public void addContext(int id, int rangeMin, int rangeMax, int value){
		JSONObject context = new JSONObject();
		String idc = "context[_idc"+id+"]";
		context.put("id", idc);
		context.put("min", new Integer(rangeMin));
		context.put("max", new Integer(rangeMax));
		contexts.add(context);
		
		setContextValue(idc, value);
	}
	
	@SuppressWarnings("unchecked")
	public void setContextValue(String idc, int value){
		JSONObject contextValue = new JSONObject();
		contextValue.put("id", idc);
		contextValue.put("value", new Integer(value));
		contextValues.add(contextValue);
	}
	
	@SuppressWarnings("unchecked")
	public void addAttribute(int id, int rangeMin, int rangeMax, int value, int featureId){
		JSONObject attribute = new JSONObject();
		String idatt = "attribute[_idatt"+id+"]";
		String idf = "feature[_id"+featureId+"]";
		attribute.put("id", idatt);
		attribute.put("min", new Integer(rangeMin));
		attribute.put("max", new Integer(rangeMax));
		attribute.put("featureId", idf);
		attributes.add(attribute);
		
		setAttributeValue(idatt, value);
	}
	
	@SuppressWarnings("unchecked")
	public void setAttributeValue(String idatt, int value){
		JSONObject attributeValue = new JSONObject();
		attributeValue.put("id", idatt);
		attributeValue.put("value", new Integer(value));
		attributeValues.add(attributeValue);
	}
	
	@SuppressWarnings("unchecked")
	public void setFeatureSelected(int idf){
		selectedFeatures.add("feature[_id"+idf+"]");
	}
	
	@SuppressWarnings("unchecked")
	public void setRoot(int rootId){
		constraints.add("feature[_id"+rootId+"] = 1");
		setFeatureSelected(rootId);
	}
	
	public void setMandatoryConstraints(int parent, int[] children){
		//setMandatoryConstraintsComplexRule(parent, children);
		setMandatoryConstraintsSimpleRules(parent, children);
	}
	
	public void setOptionalConstraints(int parent, int[] children){
		setOptionalConstraintsComplexRule(parent, children);
		//setOptionalConstraintsSimpleRules(parent, children);
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private void setMandatoryConstraintsComplexRule(int parent, int[] children){
		if (children.length > 0){
			String newConstraint = "feature[_id"+parent+"] = 1 impl (feature[_id"+children[0]+"] = 1";
			for (int i = 1; i < children.length; i++){
				newConstraint = newConstraint.concat(" and feature[_id"+children[i]+"] = 1");
			}
			newConstraint = newConstraint.concat(")");
			constraints.add(newConstraint);
			
			setOptionalConstraints(parent, children);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setMandatoryConstraintsSimpleRules(int parent, int[] children){
		if (children.length > 0){
			for (int i = 0; i < children.length; i++){
				String newConstraint = "feature[_id"+parent+"] = 1 impl feature[_id"+children[i]+"] = 1";
				constraints.add(newConstraint);
			}
			setOptionalConstraintsComplexRule(parent, children);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setOptionalConstraintsComplexRule(int parent, int[] children){
		if (children.length > 0){
			String newConstraint = "(feature[_id"+children[0]+"] = 1";
			for (int i = 1; i < children.length; i++){
				newConstraint = newConstraint.concat(" or feature[_id"+children[i]+"] = 1");
			}
			newConstraint = newConstraint.concat(") impl feature[_id"+parent+"] = 1");
			constraints.add(newConstraint);
		}
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private void setOptionalConstraintsSimpleRules(int parent, int[] children){
		for (int i = 0; i < children.length; i++){
			constraints.add("(feature[_id"+children[i]+"] = 1 impl feature[_id"+parent+"] = 1)");
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setOrConstraint(int parent, int[] children) throws Exception{
		if (children.length < 2){
			System.err.println("Or group must have at least two children ("+parent+" has "+children.length+" children in or group)");
			throw new Exception();
		}
		String orGroup = "(feature[_id"+children[0]+"] = 1 or feature[_id"+children[1]+"] = 1";
		for (int i = 2; i < children.length; i++){
			orGroup = orGroup.concat(" or feature[_id"+children[i]+"] = 1");
		}
		orGroup = orGroup.concat(")");
		
		String newConstraint1 = "feature[_id"+parent+"] = 1 impl ".concat(orGroup);
		constraints.add(newConstraint1);
		setOptionalConstraintsComplexRule(parent, children);
	}
	
	@SuppressWarnings("unchecked")
	public void setAlternativeConstraint(int parent, int[] children) throws Exception{
		if (children.length < 2){
			System.err.println("Alternative group must have at least two children ("+parent+" has "+children.length+" children in alternative group)");
			throw new Exception();
		}
		String newConstraint2 = "feature[_id"+parent+"] = 1 impl (feature[_id"+children[0]
				+"] + feature[_id"+children[1]+"]";
		for (int i = 2; i < children.length; i++){
			newConstraint2 = newConstraint2.concat(" + feature[_id"+children[i]+"]");
		}
		newConstraint2 = newConstraint2.concat(" = 1)");
		constraints.add(newConstraint2);
		setOptionalConstraintsComplexRule(parent, children);
	}
	
	@SuppressWarnings("unchecked")
	public void setCrossTreeConstraint(int lhsId, String type, int rhsId){
		String newConstraint = "(feature[_id"+lhsId+"] = 1 impl feature[_id"+rhsId+"] = ";
		if(type.compareTo("EXCLUDES") == 0) newConstraint = newConstraint.concat("0)");
		else newConstraint = newConstraint.concat("1)");
		constraints.add(newConstraint);
	}
	
	@SuppressWarnings("unchecked")
	public void addVF(int idf, int idc, String equality, int cValue){
		String newConstraint = "(feature[_id"+idf+"] = 1 impl (context[_idc"+idc+"] "
				+equality+"  "+cValue+" ))";
		constraints.add(newConstraint);
	}
	
	@SuppressWarnings("unchecked")
	public void addVF(int idf, int idc, String contextEq, int cValue, int ida, String attrEq, int aValue){
		String newConstraint = "(feature[_id"+idf+"] = 1 impl (((context[_idc"+idc+"] "
				+contextEq+"  "+cValue+" ) impl (attribute[_idatt"+ida+"] "+attrEq+"  "+aValue+" ))))";
		constraints.add(newConstraint);
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject retrieveModel(){
		configuration.put("selectedFeatures", selectedFeatures);
		configuration.put("attribute_values", attributeValues);
		configuration.put("context_values", contextValues);
		
		afmc.put("contexts", contexts);
		afmc.put("configuration", configuration);
		afmc.put("constraints", constraints);
		afmc.put("attributes", attributes);
		afmc.put("preferences", preferences);
		return afmc;
	}
	
	
}