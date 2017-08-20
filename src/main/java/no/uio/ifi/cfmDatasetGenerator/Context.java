/**
 * @author Magnus
 *
 */
package no.uio.ifi.cfmDatasetGenerator;

import java.util.concurrent.ThreadLocalRandom;

public class Context{
	
	private int sizeOfContext;
	private int maxValue;
	private int minValue;
	private int[][] contextInstances;
	private int[] contextValues;
	
	public Context(){
		int minContextSize = 1;
		int maxContextSize = 10;
		sizeOfContext = ThreadLocalRandom.current().nextInt(minContextSize, maxContextSize + 1);
		maxValue = 10;
		minValue = 0;
		initializeContext();
	}
	
	public Context(int minContextSize, int maxContextSize){
		sizeOfContext = ThreadLocalRandom.current().nextInt(minContextSize, maxContextSize + 1);
		maxValue = 10;
		minValue = 0;
		initializeContext();
	}
	
	public Context(int minContextSize, int maxContextSize, int maxValue){
		sizeOfContext = ThreadLocalRandom.current().nextInt(minContextSize, maxContextSize + 1);
		this.maxValue = maxValue;
		minValue = 0;
		initializeContext();
	}
	
	private void initializeContext(){
		contextInstances = new int[sizeOfContext][2];
		contextValues = new int[sizeOfContext];
		
		for (int i = 0; i < sizeOfContext; i++){
			contextInstances[i][0] = minValue;
			contextInstances[i][1] = ThreadLocalRandom.current().nextInt(minValue+1, maxValue+1);
			contextValues[i] = ThreadLocalRandom.current().nextInt(contextInstances[i][0], contextInstances[i][1]+1);
		}
	}
	
	public int size(){
		return sizeOfContext;
	}
	
	public int[] getRange(int index){
		return contextInstances[index];
	}
	
	public int getRangeMin(int index){
		return contextInstances[index][0];
	}
	
	public int getRangeMax(int index){
		return contextInstances[index][1];
	}
	
	public int getValue(int index){
		return contextValues[index];
	}
}