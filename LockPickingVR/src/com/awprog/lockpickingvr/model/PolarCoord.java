package com.awprog.lockpickingvr.model;

public class PolarCoord {
	public int mAngle; // from 0 to AngleStepCount -> 0 to 2*PI by AngleStepRadius  
	public int mDistance; // 1 or 2

	public PolarCoord(int a, int d) {
		mAngle = a;
		mDistance = d;
	}
	
	/** Possible distance values **/
	public static final int MaxDistance = 3, HighDistance = 2, MediumDistance = 1, LowDistance = 0;
	public static final int AngleStepCount = 18;
	public static final float AngleStepRad = (float) (2*Math.PI / AngleStepCount);
	public static final float AngleStepDeg = (float)Math.toDegrees(AngleStepRad);
	private static float[] cos, sin;

	public static float cos(int discretAngle) {
		if(cos == null) {
			cos = new float[AngleStepCount+1];
			for(int i = 0; i <= AngleStepCount; i++)
				cos[i] = (float) Math.cos(i*AngleStepRad);
		}
		return cos[discretAngle];
	}
	public static float sin(int discretAngle) {
		if(sin == null) {
			sin = new float[AngleStepCount+1];
			for(int i = 0; i <= AngleStepCount; i++)
				sin[i] = (float) Math.sin(i*AngleStepRad);
		}
		return sin[discretAngle];
	}
}
