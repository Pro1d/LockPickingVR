package com.awprog.lockpickingvr.model;

import java.util.ArrayList;

public class Game {
	private Keyhole mKeyhole;
	private Key mKey; // mKey length must greater tha mKeyhole depth
	
	private int mKeyAngle;
	private int mKeyDepth;

	private float mDisplayedKeyAngle;
	private float mDisplayedKeyDepth;
	
	private static final int keyDepthStepCount = 3;
	private int mKeyDepthStep;
	
	public Game() {
		Level l = Level.getCustomLevel1();
		mKey = l.mKey;
		mKeyhole = l.mKeyhole;
		mKeyAngle = 0;
		mKeyDepth = 0;
		mKeyDepthStep = -1;
		float smallStepLength = 1.0f / keyDepthStepCount;
		mDisplayedKeyDepth = (mKeyDepth + smallStepLength * mKeyDepthStep);
	}

	public void setDisplayedKeyAngle(float a) {
		mDisplayedKeyAngle = a;
	}
	public float getDisplayedKeyAngle() {
		return mDisplayedKeyAngle;
	}
	public void moveTowardAngle(float angle) {
		float keyAngle = mKeyAngle * PolarCoord.AngleStepRad;
		float delta = (angle - keyAngle);
		while(delta > Math.PI) delta -= 2*Math.PI;
		while(delta < -Math.PI) delta += 2*Math.PI;
		int deltaDiscret = (int) (delta / PolarCoord.AngleStepRad);
		
		if(Math.abs(deltaDiscret) > 0)
			rotate(deltaDiscret);
	}
	public void updateDisplayedAngle() {
		float persistence = 0.8f;
		mDisplayedKeyAngle = mDisplayedKeyAngle * persistence + mKeyAngle * PolarCoord.AngleStepRad * (1-persistence);
	}
	
	public void setDisplayedKeyDepth(float d) {
		mDisplayedKeyDepth = d;
	}
	public float getDisplayedKeyDepth() {
		return mDisplayedKeyDepth;
	}
	/** dir = -1 or +1 **/
	public void moveDepth(int dir) {
		if(mKeyDepthStep == -1 && dir == -1) {
			if(translate(dir) == null) {
				mKeyDepthStep = +1;
			}
		}
		else if(mKeyDepthStep == +1 && dir == +1) {
			if(translate(dir) == null) {
				mKeyDepthStep = -1;
			}
		} else {
			mKeyDepthStep += dir;
		}
	}
	public void updateDisplayedDepth() {
		float persistence = 0.7f;
		float smallStepLength = 1.0f / keyDepthStepCount;
		mDisplayedKeyDepth = mDisplayedKeyDepth * persistence + (mKeyDepth + smallStepLength * mKeyDepthStep) * (1-persistence);
	}

	/** Apply a rotation if possible.
	 * Returns the list of 3D points where the first collisions occur,
	 * may return null if there is no collision  */
	private ArrayList<PolarCoord3D> rotate(int rotationAngle) {
		int angleMax = Math.abs(rotationAngle);
		int dir = rotationAngle > 0 ? 1 : -1;
		for(int angle = 1; angle <= angleMax; angle++) {
			ArrayList<PolarCoord3D> collisions = getCollisionsRotate(mKeyAngle, dir);
			if(collisions.size() > 0) {
				return collisions;
			} else {
				mKeyAngle += dir;
			}
		}
		return null;
	}
	
	/** Apply a translation if possible.
	 * Returns the list of 3D points where the first collisions occur,
	 * may return null if there is no collision  */
	private ArrayList<PolarCoord3D> translate(int translationDistance) {
		int distMax = Math.abs(translationDistance);
		int dir = translationDistance > 0 ? 1 : -1;
		
		for(int dist = 0; dist < distMax; dist++) {
			if(mKeyDepth+dir*(dist+1) < 0 || mKeyDepth+dir*(dist+1) > Math.min(mKeyhole.getDepth(), mKey.getLength())) {
				return new ArrayList<PolarCoord3D>();
			}
			else {
				ArrayList<PolarCoord3D> collisions = getCollisionsTranslate(mKeyDepth, dir); 
				if(collisions.size() > 0) {
					return collisions;
				} else {
					mKeyDepth += dir;
				}
			}
		}
		
		return null;
	}

	/** Returns the list of 3D points where collisions occur if we apply the newAngle **/
	private ArrayList<PolarCoord3D> getCollisionsRotate(int angle, int stepAngle) {
		ArrayList<PolarCoord3D> collisions = new ArrayList<PolarCoord3D>();
		
		// range : common key and keyhole depth
		int keyDepthOffset = mKey.getLength() - mKeyDepth;
		int newAngle = angle + stepAngle;
		for(int d = 0; d < Math.min(mKeyDepth, mKeyhole.getDepth()); d++) {
			for(int a = 0; a < 24; a++) {
				int khDist = mKeyhole.mNotches.get(d).mPositionRestriction.fillDistance(a); 
				int kDist = mKey.mNotches.get(keyDepthOffset+d).fillDistance(a - newAngle); 
				if(khDist < kDist) {
					collisions.add(new PolarCoord3D(a, kDist, d));
				}	
			}
		}
		
		return collisions;
	}
	
	/** Returns the list of 3D points where collisions occur if we translate by stepDirection (-1 or 1) **/
	private ArrayList<PolarCoord3D> getCollisionsTranslate(int depth, int stepDirection) {
		ArrayList<PolarCoord3D> collisions = new ArrayList<PolarCoord3D>();
		
		// range : common key and keyhole depth
		int translationOffset = stepDirection < 0 ? 0 : 1; 
		int keyDepthOffset = mKey.getLength() - (depth + translationOffset);
		for(int d = 0; d < Math.min(depth+translationOffset, mKeyhole.getDepth()); d++) {
			for(int a = 0; a < 24; a++) {
				int khDist = mKeyhole.mNotches.get(d).mHole.fillDistance(a); 
				int kDist = mKey.mNotches.get(keyDepthOffset+d).fillDistance(a - mKeyAngle); 
				if(khDist < kDist) {
					collisions.add(new PolarCoord3D(a, kDist, d+translationOffset));
					//Log.i("###", "coll tr d:"+d+" a:"+a);
				}
			}
		}
		
		return collisions;
	}

	public Keyhole getKeyhole() {
		return mKeyhole;
	}
	public Key getKey() {
		return mKey;
	}
}
