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
public class KeyNotch extends Object {
    private final int cylOffset = 0, cylCount = PolarCoord.AngleStepCount * 2 * COORDS_PER_VERTEX;
    private final int extCylOffset = cylOffset+cylCount, extCylCount = PolarCoord.AngleStepCount * 4 * COORDS_PER_VERTEX;
    private final int plansOffset = extCylOffset+extCylCount, plansCount = 2 * (PolarCoord.AngleStepCount * 2 + 1) * COORDS_PER_VERTEX;
    private final int latOffset = plansOffset+plansCount, latCount = PolarCoord.AngleStepCount * 4 * COORDS_PER_VERTEX;

    private final int vertexCount = (latOffset+latCount)/COORDS_PER_VERTEX;
    
    static protected final float dist[] = {0.0f, 0.50f, 0.85f}, radius = 0.13f;
    private final float thickness, length;

    static final float color[] = { 0.5f, 0.5f, 0.5f, 1.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public KeyNotch(float thickness, float length) {
    	this.thickness = thickness;
    	this.length = length;
        indicesBuffer = ByteBuffer.allocateDirect((6+6+6+6) * PolarCoord.AngleStepCount * 4 /*sizeof(int)*/)
        		.order(ByteOrder.nativeOrder()).asIntBuffer();        
        vertexBuffer = ByteBuffer.allocateDirect(vertexCount * COORDS_PER_VERTEX * 4/*sizeof(float*/)
        		.order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalBuffer = ByteBuffer.allocateDirect(vertexCount * COORDS_PER_VERTEX * 4/*sizeof(float*/)
        		.order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer = ByteBuffer.allocateDirect(vertexCount * 4 * 4/*sizeof(float*/)
        		.order(ByteOrder.nativeOrder()).asFloatBuffer();
       for(int i = 0; i < vertexCount; i++)
    	   colorBuffer.put(color);

        // cylinder
        int triangleIndice[] = {0,0,0}, triangleCursor = 0;
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = radius * (float) Math.cos(a*PolarCoord.AngleStepRad);
        	float y = radius * (float) Math.sin(a*PolarCoord.AngleStepRad);
	        for (int i = 0; i <= 1; i++) {
				vertexBuffer.put(x).put(y).put(i * length);
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
    		
        // external cylinder
        float lastx = (float) Math.cos(0*PolarCoord.AngleStepRad);
        float lasty = (float) Math.sin(0*PolarCoord.AngleStepRad);
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = (float) Math.cos((a+1)*PolarCoord.AngleStepRad);
        	float y = (float) Math.sin((a+1)*PolarCoord.AngleStepRad);
        	int offset = vertexBuffer.position()/COORDS_PER_VERTEX;
	        for (int i = -1; i <= 1; i+=2) {
				vertexBuffer.put(lastx).put(lasty).put(length * 0.5f + i * thickness);
				vertexBuffer.put(x).put(y).put(length * 0.5f + i * thickness);
				normalBuffer.put(lastx).put(lasty).put(0.0f);
				normalBuffer.put(x).put(y).put(0.0f);
			}
	        lastx = x;
	        lasty = y;
	        indicesBuffer.put(offset+0).put(offset+3).put(offset+2);
	        indicesBuffer.put(offset+0).put(offset+1).put(offset+3);
    	}

        // front and back plan
        vertexBuffer.put(0).put(0).put(length * 0.5f - thickness);
        vertexBuffer.put(0).put(0).put(length * 0.5f + thickness);
		normalBuffer.put(0).put(0).put(-1.0f);
		normalBuffer.put(0).put(0).put(+1.0f);
		
        lastx = (float) Math.cos(0*PolarCoord.AngleStepRad);
        lasty = (float) Math.sin(0*PolarCoord.AngleStepRad);
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = (float) Math.cos((a+1)*PolarCoord.AngleStepRad);
        	float y = (float) Math.sin((a+1)*PolarCoord.AngleStepRad);
        	int offset = vertexBuffer.position()/COORDS_PER_VERTEX;
	        for (int i = -1; i <= 1; i+=2) {
				vertexBuffer.put(lastx).put(lasty).put(length * 0.5f + i * thickness);
				vertexBuffer.put(x).put(y).put(length * 0.5f + i * thickness);
				normalBuffer.put(0).put(0).put(i * 1.0f);
				normalBuffer.put(0).put(0).put(i * 1.0f);
			}
	        lastx = x;
	        lasty = y;

			// front
	        indicesBuffer.put(plansOffset/COORDS_PER_VERTEX + 1)
						.put(offset + 0 + 2)
						.put(offset + 1 + 2);
	        // back
	        indicesBuffer.put(plansOffset/COORDS_PER_VERTEX + 0)
						.put(offset + 1 + 0)
						.put(offset + 0 + 0);
    	}
        // lateral
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = (float) Math.cos(a*PolarCoord.AngleStepRad);
        	float y = (float) Math.sin(a*PolarCoord.AngleStepRad);
        	int offset = vertexBuffer.position()/COORDS_PER_VERTEX;
        	for (int i = -1; i <= 1; i+=2) {
				vertexBuffer.put(x*0.4f).put(y*0.4f).put(length * 0.5f + i * thickness);
				vertexBuffer.put(x*0.6f).put(y*0.6f).put(length * 0.5f + i * thickness);
				normalBuffer.put(-y).put(x).put(0.0f);
				normalBuffer.put(-y).put(x).put(0.0f);
			}
        	indicesBuffer.put(offset + 1).put(offset + 0).put(offset + 2);
        	indicesBuffer.put(offset + 1).put(offset + 2).put(offset + 3);
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

    public void update(FilledPolarPlan plan) {
    	// ext
    	vertexBuffer.position(extCylOffset);
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = dist[plan.fillDistance(a)] * (float) Math.cos((a+1)*PolarCoord.AngleStepRad);
        	float y = dist[plan.fillDistance(a)] * (float) Math.sin((a+1)*PolarCoord.AngleStepRad);
	        float lastx = dist[plan.fillDistance(a)] * (float) Math.cos(a*PolarCoord.AngleStepRad);
	        float lasty = dist[plan.fillDistance(a)] * (float) Math.sin(a*PolarCoord.AngleStepRad);
	        for (int i = -1; i <= 1; i+=2)
				vertexBuffer.put(lastx).put(lasty).put(length * 0.5f + i * thickness)
							.put(x).put(y).put(length * 0.5f + i * thickness);
    	}
        // front and back
        vertexBuffer.position(plansOffset + COORDS_PER_VERTEX*2);
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = dist[plan.fillDistance(a)] * (float) Math.cos((a+1)*PolarCoord.AngleStepRad);
        	float y = dist[plan.fillDistance(a)] * (float) Math.sin((a+1)*PolarCoord.AngleStepRad);
        	float lastx = dist[plan.fillDistance(a)] * (float) Math.cos(a*PolarCoord.AngleStepRad);
        	float lasty = dist[plan.fillDistance(a)] * (float) Math.sin(a*PolarCoord.AngleStepRad);
            for (int i = -1; i <= 1; i+=2)
				vertexBuffer.put(lastx).put(lasty).put(length * 0.5f + i * thickness)
							.put(x)	   .put(y)	  .put(length * 0.5f + i * thickness);
    	}
        // lateral
        vertexBuffer.position(latOffset);
        normalBuffer.position(latOffset);
        for(int a = 0; a < PolarCoord.AngleStepCount; a++) {
        	float x = (float) Math.cos(a*PolarCoord.AngleStepRad);
        	float y = (float) Math.sin(a*PolarCoord.AngleStepRad);
        	int aa = plan.fillDistance(a), bb = plan.fillDistance(a-1);
        	int d = aa < bb ? +1 : -1;
        	for (int i = -1; i <= 1; i+=2) {
				vertexBuffer.put(x*dist[aa]).put(y*dist[aa]).put(length * 0.5f + i * thickness);
				vertexBuffer.put(x*dist[bb]).put(y*dist[bb]).put(length * 0.5f + i * thickness);
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
    public void draw(float[] mvpMatrix, float[] mvMatrix, com.awprog.lockpickingvr.model.FilledPolarPlan plan) {
    	update(plan);
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
