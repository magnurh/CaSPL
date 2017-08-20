/**
 * @author Magnus
 *
 */
package no.uio.ifi.cfmExecutionExamples;

import es.us.isa.utils.BettyException;
import no.uio.ifi.cfmDatasetGenerator.DatasetGenerator;

public class DataSetGeneration{
	public static void main(String[] args){
		
		String dataSetName = "Dataset01";
		
		int sizeOfDataSet = 50;
		
		// CFM Parameters
		int numberOfFeatures = 40;
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
		
		
		DatasetGenerator generator = new DatasetGenerator(dataSetName, sizeOfDataSet, numberOfFeatures, percentageOfCrossTreeConstraints, maxPercentageOfVFs);
		generator.setRelationshipParameters(mandatoryProbability, optionalProbability, alternativeProbability, orProbability);
		generator.setMaxAttributeRange(attributeRangeFrom, attributeRangeTo);
		generator.setRelativeContextSizeAndRange(contextMaxSize, contextMaxValue);
		
		try {
			generator.generateCFMDataSet();
		} catch (BettyException b) {
			System.err.println(b.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}