/**
 * @author Magnus
 *
 */
package no.uio.ifi.cfmReconfigurationEngine;

public class Attribute extends RangedVariable{

	private String featureId;

	public Attribute(String id, int minRange, int maxRange, String featureId) {
		super(id, minRange, maxRange);
		this.featureId = featureId;
	}
	
	public Attribute(String id, int minRange, int maxRange, int value, String featureId) {
		super(id, minRange, maxRange, value);
		this.featureId = featureId;
	}
	
	public String getFeatureId(){
		return featureId;
	}
	
	
	
}