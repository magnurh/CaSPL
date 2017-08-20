package no.uio.ifi.cfmExecutionExamples;

import no.uio.ifi.cfmReconfigurationEngine.CFMReconfigurer;

public class MainTest {

	public static void main(String[] args) {
		
				
//		String[] inputs = {
//				"./out/data/0503_MainTests/60C_170503-105201/dataset.txt",
//				"./out/data/0503_MainTests/60D_170503-105207/dataset.txt"
//		};
		
//		String[] inputs = {
//				"./out/data/0503_MainTests/90C_170503-105215/dataset.txt",
//				"./out/data/0503_MainTests/90D_170503-105220/dataset.txt"
//		};
		
		
//		String[] inputs = {
//				"./out/data/0503_MainTests/120A_170430-165928/dataset.txt",
//				"./out/data/0503_MainTests/120B_170430-170047/dataset.txt"
//		};
		
//		String[] inputs = {
//				"./out/data/0503_MainTests/120E_170430-170425/dataset.txt",
//				"./out/data/0503_MainTests/120F_170430-170432/dataset.txt"
//		};		
		
		String[]inputs150 = {

		};
		
		String[]inputs = {
//			"./out/data/0503_MainTests/120F_170430-170432/dataset.txt",
			"./out/data/0503_MainTests/120G_170430-170927/dataset.txt",
			"./out/data/0503_MainTests/120H_170430-171842/dataset.txt",
//			"./out/data/0503_MainTests/120A_170430-165928/dataset.txt",
//			"./out/data/0503_MainTests/120B_170430-170047/dataset.txt",
//			"./out/data/0503_MainTests/120C_170504-161534/dataset.txt",
//			"./out/data/0503_MainTests/120D_170504-161556/dataset.txt",
//			"./out/data/0503_MainTests/120E_170430-170425/dataset.txt",
//			"./out/data/0503_MainTests/120F_170430-170432/dataset.txt",
//			"./out/data/0503_MainTests/120G_170430-170927/dataset.txt",
//			"./out/data/0503_MainTests/120H_170430-171842/dataset.txt",
//			"./out/data/0503_MainTests/120I_170430-172023/dataset.txt",
//			"./out/data/0503_MainTests/120J_170430-172338/dataset.txt",
//			"./out/data/0503_MainTests/120K_170430-172613/dataset.txt",
//			"./out/data/0503_MainTests/120L_170430-173012/dataset.txt",
//			"./out/data/0503_MainTests/90A_170420-092421/dataset.txt",
//			"./out/data/0503_MainTests/90B_170420-092427/dataset.txt",
//			"./out/data/0503_MainTests/90C_170503-105215/dataset.txt",
//			"./out/data/0503_MainTests/90D_170503-105220/dataset.txt",
//			"./out/data/0503_MainTests/90E_170420-092553/dataset.txt",
//			"./out/data/0503_MainTests/90F_170420-092601/dataset.txt",
//			"./out/data/0503_MainTests/90G_170420-094155/dataset.txt",
//			"./out/data/0503_MainTests/90H_170420-094231/dataset.txt",
//			"./out/data/0503_MainTests/90I_170420-094238/dataset.txt",
//			"./out/data/0503_MainTests/90J_170420-094252/dataset.txt",
//			"./out/data/0503_MainTests/90K_170420-094302/dataset.txt",
//			"./out/data/0503_MainTests/90L_170420-094325/dataset.txt",
//			
//			"./out/data/0503_MainTests/60A_170419-155930/dataset.txt",
//			"./out/data/0503_MainTests/60B_170505-082510/dataset.txt",
//			"./out/data/0503_MainTests/60C_170503-105201/dataset.txt",
//			"./out/data/0503_MainTests/60D_170503-105207/dataset.txt",
//			"./out/data/0503_MainTests/60E_170419-160336/dataset.txt",
//			"./out/data/0503_MainTests/60F_170419-160402/dataset.txt",
//			"./out/data/0503_MainTests/60G_170419-192334/dataset.txt",
//			"./out/data/0503_MainTests/60H_170419-192558/dataset.txt",
//			"./out/data/0503_MainTests/60I_170419-192628/dataset.txt",
//			"./out/data/0503_MainTests/60J_170419-192652/dataset.txt",
//			"./out/data/0503_MainTests/60K_170419-192729/dataset.txt",
//			"./out/data/0503_MainTests/60L_170419-192748/dataset.txt"
		};
		
//		String[]inputs = {
//
//		};
		
//		String[]inputs = {
//				"./out/data/0505_TestLarge/170505-125253/dataset.txt"	
//		};
		
		for (int i = 0; i < inputs150.length; i++){
			System.out.println("--150--- "+i);
			CFMReconfigurer engine = new CFMReconfigurer(inputs150[i]);
			engine.applyHillClimbing();
			engine.applySimulatedAnnealing();
//			engine.applyGeneticAlgorithm();
			
			engine.setHillClimbMaxPlateauIterations(10);
			engine.setHillClimbNumberOfExecutions(15);
			
			engine.setSimAnnealMaxIterations(1000);
			engine.setSimAnnealInitialTemperature(500);
			engine.setSimAnnealNumberOfExecutions(20);
			
			engine.setGeneticAlgInitialPopulationSize(128);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.setGeneticAlgMutationProbability(0.015);
			engine.setGeneticAlgRandomSelectionProbability(0.08);
			
			engine.executeReconfig(400);
			
			engine.setHillClimbNumberOfExecutions(25);
			engine.setSimAnnealNumberOfExecutions(30);
			
			engine.executeReconfig(400);
			
			engine.setHillClimbNumberOfExecutions(35);
			engine.setSimAnnealNumberOfExecutions(40);
			
			engine.executeReconfig(400);
			
		}
	
		for (int i = 0; i < inputs.length; i++){
			System.out.println("----- "+i);
			CFMReconfigurer engine = new CFMReconfigurer(inputs[i]);
			engine.applyHillClimbing();
			engine.applySimulatedAnnealing();
//			engine.applyGeneticAlgorithm();
			
			engine.setHillClimbMaxPlateauIterations(10);
			engine.setHillClimbNumberOfExecutions(15);
			
			engine.setSimAnnealMaxIterations(1000);
			engine.setSimAnnealInitialTemperature(500);
			engine.setSimAnnealNumberOfExecutions(20);
			
			engine.setGeneticAlgInitialPopulationSize(128);
			engine.setGeneticAlgCrossoverBreakPoints(1);
			engine.setGeneticAlgMutationProbability(0.03);
			engine.setGeneticAlgRandomSelectionProbability(0.08);
			
			engine.executeReconfig(400);
			
			engine.setHillClimbNumberOfExecutions(25);
			engine.setSimAnnealNumberOfExecutions(30);
			
			engine.executeReconfig(400);
			
			engine.setHillClimbNumberOfExecutions(35);
			engine.setSimAnnealNumberOfExecutions(40);
			
			engine.executeReconfig(400);
			
		}

	}

}
