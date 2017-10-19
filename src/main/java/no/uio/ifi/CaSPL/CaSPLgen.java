package no.uio.ifi.CaSPL;

import es.us.isa.utils.BettyException;
import no.uio.ifi.cfmDatasetGenerator.DatasetGenerator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CaSPLgen {
	
	static String dataSetName = "CaSPL";
	static Integer sizeDataSet = 10;
	
	// CFM Parameters
	static int numberOfFeatures = 50;
	static int percentageCTC = 35;
	
	static int probMand = 25;
	static int probOpt = 25;
	static int probAlt = 25;
	static int probOr = 25;
	
	static int maxBranchingFactor = 10;
	static int maxSetChildren = 5;
	
	static int maxPercentageVFs = 20;
	static int minAttrValue = 0;
	static int maxAttrValue = 100;
	static int contextMaxSize = 10;
	static int contextMaxValue = 10;
	
	static int maxTriesValidModel = 10;
	static int requiredNumberOfPathsFromRoot = 5;		// Advanced
	static int pathSearchDepth = 5;					// Advanced
	static int maxTriesPathRequirement = 0;			// Advanced
	
	static boolean simpleMode = false;
	static boolean hyvarrecInputScript = false;		// Advanced
	static int hyvarrecPort = 4000;					// Advanced

	public static void main(String[] args) {
		
		String settingsFile = userPrompt();
		JSONObject settings = readSettings(settingsFile);
		
		if(settings != null){
			if(settings.containsKey("dataset_name")) dataSetName = (String) settings.get("dataset_name");
			if(settings.containsKey("sizeDataSet")) sizeDataSet = getIntFromJSON(settings, "sizeDataSet");
			if(settings.containsKey("numberOfFeatures")) numberOfFeatures = getIntFromJSON(settings, "numberOfFeatures");
			if(settings.containsKey("percentageCTC")) percentageCTC = getIntFromJSON(settings, "percentageCTC");
			if(settings.containsKey("maxPercentageVFs")) maxPercentageVFs = getIntFromJSON(settings, "maxPercentageVFs");
		
			if(settings.containsKey("probMand")) probMand = getIntFromJSON(settings, "probMand");
			if(settings.containsKey("probOpt")) probOpt = getIntFromJSON(settings, "probOpt");
			if(settings.containsKey("probAlt")) probAlt = getIntFromJSON(settings, "probAlt");
			if(settings.containsKey("probOr")) probOr = getIntFromJSON(settings, "probOr");
			
			if(settings.containsKey("maxBranchingFactor")) maxBranchingFactor = getIntFromJSON(settings, "maxBranchingFactor");
			if(settings.containsKey("maxSetChildren")) maxSetChildren = getIntFromJSON(settings, "maxSetChildren");
			
			if(settings.containsKey("minAttrValue")) minAttrValue = getIntFromJSON(settings, "minAttrValue");
			if(settings.containsKey("maxAttrValue")) maxAttrValue = getIntFromJSON(settings, "maxAttrValue");
			
			if(settings.containsKey("contextMaxSize")) contextMaxSize = getIntFromJSON(settings, "contextMaxSize");
			if(settings.containsKey("contextMaxValue")) contextMaxValue = getIntFromJSON(settings, "contextMaxValue");
			
			if(settings.containsKey("advanced")){
				JSONObject advanced = (JSONObject) settings.get("advanced");
				if(advanced.containsKey("simpleMode")) simpleMode = (boolean) advanced.get("simpleMode");
				if(advanced.containsKey("maxTriesValidModel")) maxTriesValidModel = getIntFromJSON(advanced, "maxTriesValidModel");
				
				if(advanced.containsKey("maxTriesPathRequirement")) maxTriesPathRequirement = getIntFromJSON(advanced, "maxTriesPathRequirement");
				if(advanced.containsKey("requiredNumberOfPathsFromRoot")) requiredNumberOfPathsFromRoot = getIntFromJSON(advanced, "requiredNumberOfPathsFromRoot");
				if(advanced.containsKey("pathSearchDepth")) pathSearchDepth = getIntFromJSON(advanced, "pathSearchDepth");
				
				if(advanced.containsKey("hyvarrecInputScript")) hyvarrecInputScript = (boolean) advanced.get("hyvarrecInputScript");
				if(advanced.containsKey("hyvarrecPort")) hyvarrecPort = getIntFromJSON(advanced, "hyvarrecPort");
			}
			
		}

		DatasetGenerator generator = new DatasetGenerator(dataSetName, sizeDataSet, numberOfFeatures, percentageCTC, maxPercentageVFs);
		generator.setRelationshipParameters(probMand, probOpt, probAlt, probOr);
		generator.setTreeStructurePreferences(maxBranchingFactor, maxSetChildren);
		generator.setMaxAttributeRange(minAttrValue, maxAttrValue);
		generator.setRelativeContextSizeAndRange(contextMaxSize, contextMaxValue);
		generator.setMaxTriesValidModelReasoner(maxTriesValidModel);
		generator.setPathRequirements(requiredNumberOfPathsFromRoot, pathSearchDepth, maxTriesPathRequirement);
		generator.setHyVarRecScriptSettings(hyvarrecInputScript, hyvarrecPort);
		
		try {
			if(simpleMode){
				generator.generateCFMDataSetWithoutRestrictions();
			}else{
				generator.generateCFMDataSet();
			}
		} catch (BettyException b) {
			System.err.println(b.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}
	
	private static int getIntFromJSON(JSONObject obj, String key){
		Long tmp = (Long) obj.get(key);
		return tmp.intValue();
	}
	
	private static JSONObject readSettings(String settingsFile){
		JSONParser parser = new JSONParser();
		JSONObject settings = null;
		
        try {     
            settings =  (JSONObject) parser.parse(new FileReader(settingsFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("File could not be found. The system default settings will be used");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Something went wrong. The system default settings will be used");
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("Something went wrong while parsing "+settingsFile+". The system default settings will be used");
        }
		
		return settings;
	}
	
	private static String userPrompt(){
		String ret = "user-settings/default.json";
		Scanner sc = new Scanner(System.in);
		System.out.println("================= Type in the path to the settings file (i.e.: user-settings/default.json). =================\n"
				+ "         ======== Type default or press return to load the default settings                 =========");
		String input = sc.nextLine();
		sc.close();
		input = input.trim();
		if(!input.equals("") && !input.equals("default")) ret = input;
		return ret;
	}

}
