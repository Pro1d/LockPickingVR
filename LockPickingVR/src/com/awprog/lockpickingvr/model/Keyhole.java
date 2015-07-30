package com.awprog.lockpickingvr.model;

import java.util.ArrayList;

public class Keyhole {
	/** **/
	public ArrayList<Notch> mNotches;
	
	public int getDepth() {
		return mNotches.size();
	}
}
