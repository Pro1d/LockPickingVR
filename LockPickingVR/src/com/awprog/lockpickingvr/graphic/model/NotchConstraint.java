/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.awprog.lockpickingvr.graphic.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.opengl.GLES20;

import com.awprog.lockpickingvr.graphic.GLSurfaceView;
import com.awprog.lockpickingvr.graphic.Shader;
import com.awprog.lockpickingvr.model.FilledPolarPlan;
import com.awprog.lockpickingvr.model.PolarCoord;

/**
 * A two-dimensional triangle for use as a drawn object in OpenGL ES 2.0.
 */
public class NotchConstraint extends Object {
    private final int extCylOffset = 0, extCylCount = PolarCoord.AngleStepCount * 2 * COORDS_PER_VERTEX;
    private final int intCylOffset = extCylOffset+extCylCount, intCylCount = PolarCoord.AngleStepCount * 4 * COORDS_PER_VERTEX;
    private final int plansOffset = intCylOffset+intCylCount, plansCount = 2 * PolarCoord.AngleStepCount * 3 * COORDS_PER_VERTEX;
    private final int latOffset = plansOffset+plansCount, latCount = PolarCoord.AngleStepCount * 4 * COORDS_PER_VERTEX;

    private final int vertexCount = (latOffset+latCount)/COORDS_PER_VERTEX;
    
    static private final float dist[] = NotchHole.dist;
    private final float thickness;

    static final float color[] = { 0.9f, 0.8f, 0.7f, 1.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public NotchConstraint(float thickness, float tz) {
    	this.thickness = thickness;
    	
        indicesBuffer = ByteBuffer.allocateDirect(3 * 2 * 5 * PolarCoord.AngleStepCount * 4 /*sizeof(int)*/)
        		.order(ByteOrder.nativeOrder()).asIntBuffer();        
        vertexBuffer = ByteBuffer.allocateDirect(vertexCount * COORDS_PER_VERTEX * 4/*sizeof(float*/)
        		.order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalBuffer = ByteBuffer.allocateDirect(vertexCount * COORDS_PER_VERTEX * 4/*sizeof(float*/)
        		.order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer = ByteBuffer.allocateDirect(vertexCount * 4 * 4/*sizeof(float*/)
        		.order(ByteOrder.nativeOrder()).asFloatBuffer();
       for(int i = 0; i < vertexCount; i++)
    	   colorBuffer.put(color);

        // External cylinder
        int triangleIndice[] = {0,0,0}, triangleCursor = 0;
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = (float) Math.cos(a*PolarCoord.AngleStepRad);
        	float y = (float) Math.sin(a*PolarCoord.AngleStepRad);
	        for (int i = -1; i <= 1; i+=2) {
				vertexBuffer.put(x*dist[2]).put(y*dist[2]).put(i * thickness + tz);
				normalBuffer.put(x).put(y).put(0.0f);
				
	        	triangleIndice[triangleCursor%3] = triangleCursor;
				if(++triangleCursor >= 3) {
					if((triangleCursor & 1) == 0)
						indicesBuffer.put(triangleIndice);
					else
						indicesBuffer.put(triangleIndice[0]).put(triangleIndice[2]).put(triangleIndice[1]);
				}
			}
    	}
        triangleIndice[triangleCursor++%3] = 0;
		indicesBuffer.put(triangleIndice[0]).put(triangleIndice[2]).put(triangleIndice[1]);
    	triangleIndice[triangleCursor++%3] = 1;
		indicesBuffer.put(triangleIndice);

    		
        // internal cylinder
        float lastx = (float) Math.cos(0*PolarCoord.AngleStepRad);
        float lasty = (float) Math.sin(0*PolarCoord.AngleStepRad);
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = (float) Math.cos((a+1)*PolarCoord.AngleStepRad);
        	float y = (float) Math.sin((a+1)*PolarCoord.AngleStepRad);
        	int offset = vertexBuffer.position()/COORDS_PER_VERTEX;
	        for (int i = -1; i <= 1; i+=2) {
				vertexBuffer.put(lastx).put(lasty).put(i * thickness + tz);
				vertexBuffer.put(x).put(y).put(i * thickness + tz);
				normalBuffer.put(-lastx).put(-lasty).put(0.0f);
				normalBuffer.put(-x).put(-y).put(0.0f);
			}
	        lastx = x;
	        lasty = y;
	        indicesBuffer.put(offset+0).put(offset+2).put(offset+3);
	        indicesBuffer.put(offset+0).put(offset+3).put(offset+1);
    	}

        // front and back plan
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = (float) Math.cos(a*PolarCoord.AngleStepRad);
        	float y = (float) Math.sin(a*PolarCoord.AngleStepRad);
	        for (int i = -1; i <= 1; i+=2) {
				vertexBuffer.put(x*dist[2]).put(y*dist[2]).put(i * thickness + tz);
				normalBuffer.put(0).put(0).put(i * 1.0f);
	        }
        }
        lastx = (float) Math.cos(0*PolarCoord.AngleStepRad);
        lasty = (float) Math.sin(0*PolarCoord.AngleStepRad);
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = (float) Math.cos((a+1)*PolarCoord.AngleStepRad);
        	float y = (float) Math.sin((a+1)*PolarCoord.AngleStepRad);
        	int offset = vertexBuffer.position()/COORDS_PER_VERTEX;
	        for (int i = -1; i <= 1; i+=2) {
				vertexBuffer.put(lastx).put(lasty).put(i * thickness + tz);
				vertexBuffer.put(x).put(y).put(i * thickness + tz);
				normalBuffer.put(0).put(0).put(i * 1.0f);
				normalBuffer.put(0).put(0).put(i * 1.0f);
			}
	        lastx = x;
	        lasty = y;

			int b = ((a+1) == PolarCoord.AngleStepCount) ? 0 : (a+1);
			// front
	        indicesBuffer.put(plansOffset/COORDS_PER_VERTEX + 2*a+1)
						.put(plansOffset/COORDS_PER_VERTEX + 2*b+1)
						.put(offset+3);
	        indicesBuffer.put(plansOffset/COORDS_PER_VERTEX + 2*a+1)
						.put(offset+3)
						.put(offset+2);
	        // back
	        indicesBuffer.put(plansOffset/COORDS_PER_VERTEX + 2*b+0)
						.put(plansOffset/COORDS_PER_VERTEX + 2*a+0)
						.put(offset+1);
	        indicesBuffer.put(plansOffset/COORDS_PER_VERTEX + 2*a+0)
						.put(offset+0)
						.put(offset+1);
    	}
        // lateral
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = (float) Math.cos(a*PolarCoord.AngleStepRad);
        	float y = (float) Math.sin(a*PolarCoord.AngleStepRad);
        	int offset = vertexBuffer.position()/COORDS_PER_VERTEX;
        	for (int i = -1; i <= 1; i+=2) {
				vertexBuffer.put(x*0.4f).put(y*0.4f).put(i * thickness + tz);
				vertexBuffer.put(x*0.6f).put(y*0.6f).put(i * thickness + tz);
				normalBuffer.put(-y).put(x).put(0.0f);
				normalBuffer.put(-y).put(x).put(0.0f);
			}
        	indicesBuffer.put(offset + 0).put(offset + 1).put(offset + 2);
        	indicesBuffer.put(offset + 2).put(offset + 1).put(offset + 3);
        }

        vertexBuffer.position(0);
        normalBuffer.position(0);
        colorBuffer.position(0);
        indicesBuffer.position(0);
        
        shader = new Shader();
        shader.addLight(0, 0, -4.5f, true, true);
        shader.addLight(-1, 1, -3.5f, false, true);
        shader.createAndlinkProgram();
    }

    public void update(FilledPolarPlan constraints, float tz) {
    	// external cylinder
    	indicesBuffer.position(0);
        int triangleIndice[] = {0,0,0}, triangleCursor = 0;
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	for (int i = -1; i <= 1; i+=2) {
				triangleIndice[triangleCursor%3] = triangleCursor;
				if(++triangleCursor >= 3) {
					if(constraints.fillDistance(a-1) == PolarCoord.HighDistance)
						indicesBuffer.put(triangleIndice[0]).put(triangleIndice[0]).put(triangleIndice[0]);
					else if((triangleCursor & 1) == 0)
						indicesBuffer.put(triangleIndice);
					else
						indicesBuffer.put(triangleIndice[0]).put(triangleIndice[2]).put(triangleIndice[1]);
				}
			}
    	}
        triangleIndice[triangleCursor++%3] = 0;
    	if(constraints.fillDistance(-1) == PolarCoord.HighDistance)
    		indicesBuffer.put(triangleIndice[0]).put(triangleIndice[0]).put(triangleIndice[0])
    					.put(triangleIndice[0]).put(triangleIndice[0]).put(triangleIndice[0]);
    	else {
	 		indicesBuffer.put(triangleIndice[0]).put(triangleIndice[2]).put(triangleIndice[1]);
	    	triangleIndice[triangleCursor++%3] = 1;
			indicesBuffer.put(triangleIndice);
    	}
        // Internal cylinder
        vertexBuffer.position(intCylOffset);
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	int aa = constraints.fillDistance(a);
        	float x = dist[aa] * (float) Math.cos((a+1)*PolarCoord.AngleStepRad);
        	float y = dist[aa] * (float) Math.sin((a+1)*PolarCoord.AngleStepRad);
        	float lastx = dist[aa] * (float) Math.cos(a*PolarCoord.AngleStepRad);
            float lasty = dist[aa] * (float) Math.sin(a*PolarCoord.AngleStepRad);
	        for (int i = -1; i <= 1; i+=2)
	        	if(aa == PolarCoord.HighDistance)
	        		vertexBuffer.put(0).put(0).put(i*thickness + tz).put(0).put(0).put(i*thickness + tz);
	        	else
					vertexBuffer.put(lastx).put(lasty).put(i * thickness + tz)
								.put(x)	   .put(y)    .put(i * thickness + tz);
	        lastx = x;
	        lasty = y;
    	}
        // front and back
        vertexBuffer.position(plansOffset + COORDS_PER_VERTEX*PolarCoord.AngleStepCount*2);
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = dist[constraints.fillDistance(a)] * (float) Math.cos((a+1)*PolarCoord.AngleStepRad);
        	float y = dist[constraints.fillDistance(a)] * (float) Math.sin((a+1)*PolarCoord.AngleStepRad);
        	float lastx = dist[constraints.fillDistance(a)] * (float) Math.cos(a*PolarCoord.AngleStepRad);
            float lasty = dist[constraints.fillDistance(a)] * (float) Math.sin(a*PolarCoord.AngleStepRad);
            for (int i = -1; i <= 1; i+=2)
				vertexBuffer.put(lastx).put(lasty).put(i * thickness + tz)
							.put(x)	   .put(y)	  .put(i * thickness + tz);
	        lastx = x;
	        lasty = y;
    	}
        // lateral
        vertexBuffer.position(latOffset);
        normalBuffer.position(latOffset);
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = (float) Math.cos(a*PolarCoord.AngleStepRad);
        	float y = (float) Math.sin(a*PolarCoord.AngleStepRad);
        	int aa = constraints.fillDistance(a), bb = constraints.fillDistance(a-1);
        	int d = aa < bb ? +1 : -1;
        	for (int i = -1; i <= 1; i+=2) {
				vertexBuffer.put(x*dist[aa]).put(y*dist[aa]).put(i * thickness + tz);
				vertexBuffer.put(x*dist[bb]).put(y*dist[bb]).put(i * thickness + tz);
				normalBuffer.put(-y*d).put(x*d).put(0.0f);
				normalBuffer.put(-y*d).put(x*d).put(0.0f);
			}
        }
        vertexBuffer.position(0);
        normalBuffer.position(0);
    }
    
    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix, float[] mvMatrix, com.awprog.lockpickingvr.model.Notch notch) {
    	update(notch.mPositionRestriction, 0.0f);
        indicesBuffer.position(0);
    	
        // Add program to OpenGL environment
        shader.useProgram();
        GLSurfaceView.checkGlError("glUseProgram");

        shader.setPositionAttribute(vertexBuffer);
        shader.setNormalAttribute(normalBuffer);
        shader.setColorAttribute(colorBuffer);
        shader.setMVMatrixUniform(mvMatrix);
        shader.setMVPMatrixUniform(mvpMatrix);
        
        GLSurfaceView.checkGlError("glGet[...]Location");

        // Draw 
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesBuffer.capacity(), GLES20.GL_UNSIGNED_INT, indicesBuffer);

        // Disable vertex array
//        GLES20.glDisableVertexAttribArray(mPositionHandle);
//        GLES20.glDisableVertexAttribArray(mNormalHandle);
    }

}
