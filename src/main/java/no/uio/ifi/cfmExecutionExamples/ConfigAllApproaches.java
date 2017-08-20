/**
 * @author Magnus
 *
 */
package no.uio.ifi.cfmExecutionExamples;

import no.uio.ifi.cfmReconfigurationEngine.CFMReconfigurer;

public class ConfigAllApproaches {
	
	public static void main(String[] args){
		
		//String input = "./out/data/0308_TestingRulestructure/170308-125122/dataset.txt";
/*		String[] inputs = {
				"./out/data/0419_TestSet01/170419-155930/dataset.txt",
				"./out/data/0419_TestSet01/170419-160153/dataset.txt",
				"./out/data/0419_TestSet01/170419-160228/dataset.txt",
				"./out/data/0419_TestSet01/170419-160336/dataset.txt",
				"./out/data/0419_TestSet01/170419-160402/dataset.txt",
				"./out/data/0419_TestSet01/170419-160434/dataset.txt"
				};*/
		
		
/*		String[] inputs = {
				"./out/data/0419_TestSet02/170419-192334/dataset.txt",
				"./out/data/0419_TestSet02/170419-192558/dataset.txt",
				"./out/data/0419_TestSet02/170419-192628/dataset.txt",
				"./out/data/0419_TestSet02/170419-192652/dataset.txt",
				"./out/data/0419_TestSet02/170419-192729/dataset.txt",
				"./out/data/0419_TestSet02/170419-192748/dataset.txt"
				};*/

		
/*				
		String[] inputs = {
				"./out/data/0420_TestSet03/170420-092421/dataset.txt",
				"./out/data/0420_TestSet03/170420-092427/dataset.txt",
				"./out/data/0420_TestSet03/170420-092432/dataset.txt",
				"./out/data/0420_TestSet03/170420-092553/dataset.txt",
				"./out/data/0420_TestSet03/170420-092601/dataset.txt",
				"./out/data/0420_TestSet03/170420-092606/dataset.txt"
				};*/

				
//		String[] inputs = {
//				"./out/data/0420_TestSet04/170420-094155/dataset.txt",
//				"./out/data/0420_TestSet04/170420-094231/dataset.txt",
//				"./out/data/0420_TestSet04/170420-094238/dataset.txt",
//				"./out/data/0420_TestSet04/170420-094252/dataset.txt",
//				"./out/data/0420_TestSet04/170420-094302/dataset.txt",
//				"./out/data/0420_TestSet04/170420-094325/dataset.txt"
//				};
		
		String[] inputs = {
				"./out/data/000_Dataset/170512-093813/dataset.txt"
		};

	
		for (int i = 0; i < inputs.length; i++){
			System.out.println("----- "+i);
			CFMReconfigurer engine = new CFMReconfigurer(inputs[i]);
			engine.applyHillClimbing();
			engine.applySimulatedAnnealing();
			engine.applyGeneticAlgorithm();
			
	
			engine.setHillClimbMaxPlateauIterations(20);
			engine.setHillClimbNumberOfExecutions(40);
			
			engine.setSimAnnealMaxIterations(10000);
			engine.setSimAnnealInitialTemperature(4.0);
			engine.setSimAnnealNumberOfExecutions(10);
			
			engine.setGeneticAlgInitialPopulationSize(512);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.setGeneticAlgMutationProbability(0.04);
			
			for(int j = 0; j < 1; j++){
				engine.executeReconfig(20);
			}
/*			
			engine.executeReconfig(40);
			
			engine.setSimAnnealInitialTemperature(4.0);
			engine.executeReconfig(40);
			
			engine.setSimAnnealInitialTemperature(3.0);
			engine.executeReconfig(40);
			
			engine.setSimAnnealInitialTemperature(2.0);
			engine.executeReconfig(40);
			
			engine.setSimAnnealInitialTemperature(1.0);
			engine.executeReconfig(40);*/
/*			
			engine.setGeneticAlgInitialPopulationSize(128);
			
			engine.setGeneticAlgMutationProbability(0.01);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(4);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(8);
			engine.executeReconfig(40);
			
			engine.setGeneticAlgMutationProbability(0.04);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(4);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(8);
			engine.executeReconfig(40);
			
			engine.setGeneticAlgMutationProbability(0.08);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(4);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(8);
			engine.executeReconfig(40);
			
			engine.setGeneticAlgInitialPopulationSize(256);
			
			engine.setGeneticAlgMutationProbability(0.01);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(4);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(8);
			engine.executeReconfig(40);
			
			engine.setGeneticAlgMutationProbability(0.04);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(4);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(8);
			engine.executeReconfig(40);
			
			engine.setGeneticAlgMutationProbability(0.08);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(4);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(8);
			engine.executeReconfig(40);
			
			engine.setGeneticAlgInitialPopulationSize(512);
			
			engine.setGeneticAlgMutationProbability(0.01);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(4);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(8);
			engine.executeReconfig(40);
			
			engine.setGeneticAlgMutationProbability(0.04);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(4);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(8);
			engine.executeReconfig(40);
			
			engine.setGeneticAlgMutationProbability(0.08);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(4);
			engine.executeReconfig(40);
			engine.setGeneticAlgCrossoverBreakPoints(8);
			engine.executeReconfig(40);*/
			
		}
		
		
		//engine.setHillClimbMaxPlateauIterations(20);
		//engine.setHillClimbNumberOfExecutions(24);
		//engine.setSimAnnealMaxIterations(9999*10);
		
/*		engine.setSimAnnealInitialTemperature(10.0);
		engine.executeReconfig(input);
		
		engine.setSimAnnealInitialTemperature(4.0);
		engine.executeReconfig(input);
		
		engine.setSimAnnealInitialTemperature(2.0);
		engine.executeReconfig(input);
		*/

/*		System.out.println("Running first set: plateau: 10");
		engine.setHillClimbMaxPlateauIterations(10);
		engine.executeReconfig(input);
		
		engine.setHillClimbMaxPlateauIterations(0);
		engine.setHillClimbNumberOfExecutions(5);
		System.out.println("Running first set: re-tries: 5");
		engine.executeReconfig(input);
		
		engine.setHillClimbMaxPlateauIterations(10);
		System.out.println("Running first set: plateau: 10, retries: 5");
		engine.executeReconfig(input);*/
		
	}
}