package no.uio.ifi.CaSPL;
import es.us.isa.utils.BettyException;
import no.uio.ifi.cfmDatasetGenerator.DatasetGenerator;

public class RunnableDockerApp {

	public static void main(String[] args) {
		
		String dataSetName = "_CaSPL";
		
		int sizeDataSet = 10;
		
		// CFM Parameters
		int numberOfFeatures = 50;
		int percentageCTC = 35;
		
		int probMand = 25;
		int probOpt = 25;
		int probAlt = 25;
		int probOr = 25;
		
		int maxBranchingFactor = 10;			//TODO: implement
		int maxSetChildren = 5;					//TODO: implement
		
		int maxPercentageVFs = 20;
		int minAttrValue = 0;
		int maxAttrValue = 100;
		int contextMaxSize = 10;
		int contextMaxValue = 10;
		
		int maxTriesValidModel = 10;
		int requiredNumberOfPathsFromRoot = 5;		// Advanced
		int pathSearchDepth = 5;					// Advanced
		int maxTriesPathRequirement = 0;			// Advanced
		
		boolean simpleMode = false;
		boolean hyvarrecInputScript = false;		// Advanced
		int hyvarrecPort = 4000;					// Advanced
		
		
		DatasetGenerator generator = new DatasetGenerator(dataSetName, sizeDataSet, numberOfFeatures, percentageCTC, maxPercentageVFs);
		generator.setRelationshipParameters(probMand, probOpt, probAlt, probOr);
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

}
