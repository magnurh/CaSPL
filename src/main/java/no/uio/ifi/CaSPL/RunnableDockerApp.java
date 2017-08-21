package no.uio.ifi.CaSPL;
import es.us.isa.utils.BettyException;
import no.uio.ifi.cfmDatasetGenerator.DatasetGenerator;

public class RunnableDockerApp {

	public static void main(String[] args) {
		
		String dataSetName = "_CaSPL";
		
		int sizeOfDataSet = 10;
		
		// CFM Parameters
		int numberOfFeatures = 50;
		int percentageOfCrossTreeConstraints = 35;
		int maxPercentageOfVFs = 17;
		int contextMaxSize = 13;
		
		int mandatoryProbability = 25;
		int optionalProbability = 25;
		int alternativeProbability = 25;
		int orProbability = 25;
		
		int attributeRangeFrom = 0;
		int attributeRangeTo = 100;
		int contextMaxValue = 10;
		
		int maxTriesValidModel = 1;
		int requiredNumberOfPathsFromRoot = 6;
		int pathSearchDepth = 5;
		int maxTriesPathRequirement = 20;
		
		//boolean hyvarrecInputScript = false;		// Implement as a setting to toggle returning hvr-script
		
		System.out.println("Hello World!");
		System.out.println(System.getProperty("java.class.path"));
		
		DatasetGenerator generator = new DatasetGenerator(dataSetName, sizeOfDataSet, numberOfFeatures, percentageOfCrossTreeConstraints, maxPercentageOfVFs);
		generator.setRelationshipParameters(mandatoryProbability, optionalProbability, alternativeProbability, orProbability);
		generator.setMaxAttributeRange(attributeRangeFrom, attributeRangeTo);
		generator.setRelativeContextSizeAndRange(contextMaxSize, contextMaxValue);
		generator.setMaxTriesValidModelReasoner(maxTriesValidModel);
		generator.setPathRequirements(requiredNumberOfPathsFromRoot, pathSearchDepth, maxTriesPathRequirement);
		
		try {
			generator.generateCFMDataSet();
			//generator.generateCFMDataSetWithoutRestrictions();
		} catch (BettyException b) {
			System.err.println(b.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

}
