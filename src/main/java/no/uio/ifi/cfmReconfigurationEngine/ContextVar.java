/**
 * @author Magnus
 *
 */
package no.uio.ifi.cfmReconfigurationEngine;

public class ContextVar extends RangedVariable{

	public ContextVar(String id, int minRange, int maxRange) {
		super(id, minRange, maxRange);
	}
	
	public ContextVar(String id, int minRange, int maxRange, int value) {
		super(id, minRange, maxRange, value);
	}
	
}