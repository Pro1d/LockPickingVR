package com.awprog.lockpickingvr.model;

import java.util.ArrayList;

import android.util.Range;

public class Key {
	/** **/
	public ArrayList<FilledPolarPlan> mNotches;
	
	public int getLength() {
		return mNotches.size();
	}
}
