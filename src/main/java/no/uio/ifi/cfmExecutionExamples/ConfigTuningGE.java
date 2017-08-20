package no.uio.ifi.cfmExecutionExamples;

import no.uio.ifi.cfmReconfigurationEngine.CFMReconfigurer;

public class ConfigTuningGE {

	public static void main(String[] args) {

//		String[] inputs = {
//				"./out/data/0426_NEWtunigMetaheuristics/170426-175629/dataset.txt",
//				"./out/data/0426_NEWtunigMetaheuristics/170426-175636/dataset.txt",
//				"./out/data/0426_NEWtunigMetaheuristics/170426-175640/dataset.txt"
//				};
		
//		String[] inputs = {
//				"./out/data/0426_NEWtunigMetaheuristics/170426-175808/dataset.txt",
//				"./out/data/0426_NEWtunigMetaheuristics/170426-175820/dataset.txt",
//				"./out/data/0426_NEWtunigMetaheuristics/170426-175836/dataset.txt"
//				};
		
		String[] inputs = {
				"./out/data/0426_NEWtunigMetaheuristics/170426-175911/dataset.txt",
				"./out/data/0426_NEWtunigMetaheuristics/170426-175924/dataset.txt",
				"./out/data/0426_NEWtunigMetaheuristics/170426-175942/dataset.txt"
				};
	
		for (int i = 0; i < inputs.length; i++){
			CFMReconfigurer engine = new CFMReconfigurer(inputs[i]);
			engine.applyGeneticAlgorithm();
			
			engine.setGeneticAlgInitialPopulationSize(128);
			engine.setGeneticAlgMutationProbability(0.03);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			System.out.println("----- "+i+": 128");
			System.out.println("----- "+i+": 0.00");
			engine.setGeneticAlgRandomSelectionProbability(0.0);
			engine.executeReconfig(100);
			System.out.println("----- "+i+": 128");
			System.out.println("----- "+i+": 0.08");
			engine.setGeneticAlgRandomSelectionProbability(0.08);
			engine.executeReconfig(100);
			System.out.println("----- "+i+": 128");
			System.out.println("----- "+i+": 0.2");
			engine.setGeneticAlgRandomSelectionProbability(0.2);
			engine.executeReconfig(100);
			System.out.println("----- "+i+": 128");
			System.out.println("----- "+i+": 0.4");
			engine.setGeneticAlgRandomSelectionProbability(0.4);
			engine.executeReconfig(100);
			
			engine.setGeneticAlgInitialPopulationSize(256);
			System.out.println("----- "+i+": 256");
			System.out.println("----- "+i+": 0.00");
			engine.setGeneticAlgRandomSelectionProbability(0.0);
			engine.executeReconfig(100);
			System.out.println("----- "+i+": 256");
			System.out.println("----- "+i+": 0.08");
			engine.setGeneticAlgRandomSelectionProbability(0.08);
			engine.executeReconfig(100);
			System.out.println("----- "+i+": 256");
			System.out.println("----- "+i+": 0.2");
			engine.setGeneticAlgRandomSelectionProbability(0.2);
			engine.executeReconfig(100);
			System.out.println("----- "+i+": 256");
			System.out.println("----- "+i+": 0.4");
			engine.setGeneticAlgRandomSelectionProbability(0.4);
			engine.executeReconfig(100);
			
			engine.setGeneticAlgInitialPopulationSize(512);
			System.out.println("----- "+i+": 512");
			System.out.println("----- "+i+": 0.00");
			engine.setGeneticAlgRandomSelectionProbability(0.0);
			engine.executeReconfig(100);
			System.out.println("----- "+i+": 512");
			System.out.println("----- "+i+": 0.08");
			engine.setGeneticAlgRandomSelectionProbability(0.08);
			engine.executeReconfig(100);
			System.out.println("----- "+i+": 512");
			System.out.println("----- "+i+": 0.2");
			engine.setGeneticAlgRandomSelectionProbability(0.2);
			engine.executeReconfig(100);
			System.out.println("----- "+i+": 512");
			System.out.println("----- "+i+": 0.4");
			engine.setGeneticAlgRandomSelectionProbability(0.4);
			engine.executeReconfig(100);
			
//			System.out.println("----- "+i+": 0.01");
//			engine.setGeneticAlgCrossoverBreakPoints(5);
//			
//			engine.setGeneticAlgRandomSelectionProbability(0.01);
//			engine.executeReconfig(100);
//			System.out.println("----- "+i+": 0.04");
//			engine.setGeneticAlgRandomSelectionProbability(0.04);
//			engine.executeReconfig(100);
//			System.out.println("----- "+i+": 0.12");
//			engine.setGeneticAlgRandomSelectionProbability(0.12);
//			engine.executeReconfig(100);
			
			
/*			engine.setGeneticAlgRandomSelectionProbability(0.08);
			
			engine.setGeneticAlgInitialPopulationSize(128);
			engine.setGeneticAlgMutationProbability(0.01);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(5);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(12);
			engine.executeReconfig(100);
			engine.setGeneticAlgMutationProbability(0.03);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(5);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(12);
			engine.executeReconfig(100);
			engine.setGeneticAlgMutationProbability(0.06);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(5);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(12);
			engine.executeReconfig(100);
			System.out.println("----- "+i+": 256");
			engine.setGeneticAlgInitialPopulationSize(256);
			engine.setGeneticAlgMutationProbability(0.01);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(5);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(12);
			engine.executeReconfig(100);
			engine.setGeneticAlgMutationProbability(0.03);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(5);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(12);
			engine.executeReconfig(100);
			engine.setGeneticAlgMutationProbability(0.06);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(5);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(12);
			engine.executeReconfig(100);
			System.out.println("----- "+i+": 512");
			engine.setGeneticAlgInitialPopulationSize(512);
			engine.setGeneticAlgMutationProbability(0.01);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(5);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(12);
			engine.executeReconfig(100);
			engine.setGeneticAlgMutationProbability(0.03);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(5);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(12);
			engine.executeReconfig(100);
			engine.setGeneticAlgMutationProbability(0.06);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(5);
			engine.executeReconfig(100);
			engine.setGeneticAlgCrossoverBreakPoints(12);
			engine.executeReconfig(100);*/
		}

	}

}
