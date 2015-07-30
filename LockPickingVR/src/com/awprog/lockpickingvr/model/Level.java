package com.awprog.lockpickingvr.model;

import java.util.ArrayList;
import java.util.Random;

public class Level {
	Key mKey;
	Keyhole mKeyhole;

	static Level getRandomLevel() {
		Level l = new Level();
		int length = 2;
		Random r = new Random();
		l.mKey = new Key();
		l.mKey.mNotches = new ArrayList<FilledPolarPlan>(length);
		for(int i = 0; i < length; i++) {
			l.mKey.mNotches.add(new FilledPolarPlan());
			for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
				int d = Math.max(0, r.nextInt(6*PolarCoord.MaxDistance)-5*PolarCoord.MaxDistance);
				l.mKey.mNotches.get(i).setFillDistance(a, d);
			}
		}
		
		l.mKeyhole = new Keyhole();
		l.mKeyhole.mNotches = new ArrayList<Notch>(length);
		for(int i = 0; i < length; i++) {
			l.mKeyhole.mNotches.add(new Notch());
			l.mKeyhole.mNotches.get(i).mHole = new FilledPolarPlan();
			l.mKeyhole.mNotches.get(i).mPositionRestriction = new FilledPolarPlan();
			for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
				int d1 = Math.max(0, r.nextInt(4*PolarCoord.MaxDistance)-3*PolarCoord.MaxDistance);
				l.mKeyhole.mNotches.get(i).mHole.setFillDistance(a, d1);
				int d2 = Math.max(d1, (2-Math.max(0, r.nextInt(5*PolarCoord.MaxDistance)-4*PolarCoord.MaxDistance)));
				l.mKeyhole.mNotches.get(i).mPositionRestriction.setFillDistance(a, d2);
			}
		}
		
		return l;
	}
	static Level getCustomLevel1() {
		Level l = new Level();
		int length = 3;

		String k[] = {
				"000000000100000000",
				"002000000000000000",
				"200000000000000000",
		};
		String khh[] = {
				"000020000020010000",
				"001000202001000100",
				"000020000000002000",
		};
		String khc[] = {
				"222222222222222122",
				"222222222222222220",
				"222222222222222222",
		};
		
		l.mKey = new Key();
		l.mKey.mNotches = new ArrayList<FilledPolarPlan>(length);
		for(int i = 0; i < length; i++) {
			l.mKey.mNotches.add(new FilledPolarPlan());
			for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
				int d = k[i].charAt(a)-'0';/*TODO*/;
				l.mKey.mNotches.get(i).setFillDistance(a, d);
			}
		}
		
		l.mKeyhole = new Keyhole();
		l.mKeyhole.mNotches = new ArrayList<Notch>(length);
		for(int i = 0; i < length; i++) {
			l.mKeyhole.mNotches.add(new Notch());
			l.mKeyhole.mNotches.get(i).mHole = new FilledPolarPlan();
			l.mKeyhole.mNotches.get(i).mPositionRestriction = new FilledPolarPlan();
			for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
				int d1 = khh[i].charAt(a)-'0'/*TODO*/;
				l.mKeyhole.mNotches.get(i).mHole.setFillDistance(a, d1);
				int d2 = khc[i].charAt(a)-'0'/*TODO*/;
				l.mKeyhole.mNotches.get(i).mPositionRestriction.setFillDistance(a, d2);
			}
		}
		
		return l;
	}
}
