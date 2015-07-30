package com.awprog.lockpickingvr.model;

public class PolarCoord3D extends PolarCoord {
	public int mDepth;

	public PolarCoord3D(int a, int d, int z) {
		super(a, d);
		mDepth = z;
	}

}
