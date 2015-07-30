package com.awprog.lockpickingvr;

public interface KeyController {

	void rotateAbs(float absoluteFogglePosition);
	void rotateRel(float relativeFogglePosition);
	void moveRel(int relativeDepthPosition);
	void moveAbs(int absDepthPosition);
	// TODO complete as you wish with alternative methods :)
}
