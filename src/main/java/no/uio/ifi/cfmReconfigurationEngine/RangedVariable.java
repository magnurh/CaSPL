/**
 * @author Magnus
 *
 */
package no.uio.ifi.cfmReconfigurationEngine;

public class RangedVariable{
	
	public String id;
	private int minRange;
	private int maxRange;
	private int value;
	private int[] multiRange;
	private boolean rangeIsComplete = false;
	
	public RangedVariable(String id, int minRange, int maxRange){
		this.id = id;
		this.minRange = minRange;
		this.maxRange = maxRange;
		this.value = minRange;
		this.multiRange = new int[]{minRange, maxRange};
	}
	
	public RangedVariable(String id, int minRange, int maxRange, int value){
		this.id = id;
		this.minRange = minRange;
		this.maxRange = maxRange;
		this.value = value;
	}
	
	public void setValue(int value){
		this.value = value;
	}
	
	public int getMinRange(){
		return minRange;
	}
	
	public int getMaxRange(){
		return maxRange;
	}
	
	public int[] getRange(){
		return multiRange;
	}
	
	public boolean insertRangeInterval(int v){
		if(v <= multiRange[0]) return false;
		if(v >= multiRange[multiRange.length-1]) return false;
		int[] newRange = new int[multiRange.length+1];
		int i = 1;
		int j = 1;
		newRange[0] = multiRange[0];
		while (j < newRange.length){
			if(v == multiRange[i]){
				return false;
			}else if (v > newRange[j-1] && v < multiRange[i]){
				newRange[j++] = v;
			}
			newRange[j] = multiRange[i];
			i++;
			j++;
		}
		multiRange = newRange;
		return true;
	}
	
	public boolean isRangeComplete(){
		return rangeIsComplete;
	}
	
	public void rangeCompleted(){
		rangeIsComplete = true;
	}
	
	public int getValue(){
		return value;
	}
}