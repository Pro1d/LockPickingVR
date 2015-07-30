package com.awprog.lockpickingvr.model;

public class FilledPolarPlan {
	/**  **/
	private final int[] fillDistance = new int[PolarCoord.AngleStepCount];
	
	public int fillDistance(int angle) {
		if(angle < 0)
			angle = (angle%PolarCoord.AngleStepCount + PolarCoord.AngleStepCount)%PolarCoord.AngleStepCount;
		else if(angle >= PolarCoord.AngleStepCount)
			angle %= PolarCoord.AngleStepCount;
		
		return fillDistance[angle];
	}
	
	public void setFillDistance(int angle, int dist) {
		if(angle < 0)
			angle = angle%PolarCoord.AngleStepCount + PolarCoord.AngleStepCount;
		else if(angle >= PolarCoord.AngleStepCount)
			angle %= PolarCoord.AngleStepCount;
		
		fillDistance[angle] = dist;
	}
}
