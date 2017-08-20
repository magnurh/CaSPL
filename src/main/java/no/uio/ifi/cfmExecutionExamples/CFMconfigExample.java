package no.uio.ifi.cfmExecutionExamples;

import no.uio.ifi.cfmReconfigurationEngine.CFMReconfigurer;

public class CFMconfigExample {

	public static void main(String[] args) {

		String input = "./data/DatasetExample/dataset.txt";
		System.out.println("DataSetExample: "+input);
		CFMReconfigurer engine = new CFMReconfigurer(input);
		
		engine.applyHillClimbing();				
		engine.applySimulatedAnnealing();
		engine.applyGeneticAlgorithm();
		
		engine.setHillClimbMaxPlateauIterations(10);
		engine.setHillClimbNumberOfExecutions(5);
		
		engine.setSimAnnealInitialTemperature(400.0);
		engine.setSimAnnealMaxIterations(1000);
		engine.setSimAnnealNumberOfExecutions(8);
		
		engine.setGeneticAlgCrossoverBreakPoints(1);
		engine.setGeneticAlgInitialPopulationSize(64);
		engine.setGeneticAlgMutationProbability(0.02);
		engine.setGeneticAlgRandomSelectionProbability(0.05);
		
		engine.executeReconfig(40);

	}

}
